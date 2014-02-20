package edu.dartmouth.cs.myruns5;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import weka.core.Attribute;
import weka.core.Instance;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import edu.dartmouth.cs.myruns5.util.LocationUtils;


import org.apache.http.*;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class TrackingService extends Service
	implements LocationListener, SensorEventListener
	{
	private File mWekaClassificationFile;
	private Attribute mClassNameForData,meanAttribute,stdAttribute,maxAttribute,minAttribute,meanAbsDeviationAttribute;
	
	public ArrayList<Location> mLocationList;
	
	private int[] mInferenceCount = {0, 0, 0};

	// Location manager
	private LocationManager mLocationManager;
	
	// Intents for broadcasting location/motion updates
	private Intent mLocationUpdateBroadcast;
	private Intent mMotionUpdateBroadcast;
	
	private int mInputType;
	public int mInferredActivityType;
	
	private FileOutputStream trackFile;
	
	
	private SensorManager mSensorManager;
	private Sensor mAccelerometer,mLightSensor,mMagnetSensor,mGravitySensor;
	
	private float[] mGeomagnetic;
	private static ArrayBlockingQueue<Double> mAccBuffer;
	private static ArrayBlockingQueue<LumenDataPoint> mLightIntensityReadingBuffer;
	
	private AccelerometerActivityClassificationTask mAccelerometerActivityClassificationTask;
	private LightSensorActivityClassificationTask mLightSensorActivityClassificationTask;
	
	private final IBinder mBinder = new TrackingBinder();

	private float[] mGravity;
	private double []pitchReading={0,0,0};

	
	public static final String LOCATION_UPDATE = "location update";
	public static final int NEW_LOCATION_AVAILABLE = 400;
	
	// broadcast 
	public static final String ACTION_MOTION_UPDATE = "motion update";
	public static final String CURRENT_MOTION_TYPE = "new motion type";
	public static final String VOTED_MOTION_TYPE = "voted motion type";
	public static final String ACTION_TRACKING = "tracking action";
	public static final String CURRENT_SWEAT_RATE_INTERVAL = "sweat rate Interval";
	public static final String FINAL_SWEAT_RATE_AVERAGE = "average sweat rate";

	private static final String TAG = "TrackingService";
	
	
	private static Timer dataCollector;
  	private TimerTask dataCollectorTask = new TimerTask() {
  		@Override
  		public void run() 
  		{
  			dataCollectionEnabled=true;
  		}
  	};
	private static boolean dataCollectionEnabled = true;
	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}
	
	@Override
	public void onCreate() {
		mLocationList = new ArrayList<Location>();
		mLocationUpdateBroadcast = new Intent();
		mLocationUpdateBroadcast.setAction(ACTION_TRACKING);
		mMotionUpdateBroadcast = new Intent();
		mMotionUpdateBroadcast.setAction(ACTION_MOTION_UPDATE);
		mLightIntensityReadingBuffer = new ArrayBlockingQueue<LumenDataPoint>(Globals.LIGHT_BUFFER_CAPACITY);
		mAccBuffer = new ArrayBlockingQueue<Double>(Globals.ACCELEROMETER_BUFFER_CAPACITY);
		mAccelerometerActivityClassificationTask = new AccelerometerActivityClassificationTask();
		mLightSensorActivityClassificationTask = new LightSensorActivityClassificationTask();
		mInferredActivityType = Globals.ACTIVITY_TYPE_STANDING;
		
		//Start the timer for data collection
		dataCollector = new Timer();
		dataCollector.scheduleAtFixedRate(dataCollectorTask, Globals.DATA_COLLECTOR_START_DELAY, Globals.DATA_COLLECTOR_INTERVAL);
		
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		File sdCard = Environment.getExternalStorageDirectory();  
		String tempfilename = sdCard.getAbsolutePath()  + "/temp";
		File file = new File(tempfilename);
		try {
			file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Create Weka features.arff file reference
		mWekaClassificationFile = new File(getExternalFilesDir(null), Globals.FEATURE_LIGHT_FILE_NAME);
		Log.d(Globals.TAG, mWekaClassificationFile.getAbsolutePath());
		
		// Read inputType, can be GPS or Automatic.
		mInputType = intent.getIntExtra(MapDisplayActivity.INPUT_TYPE, -1);

		//JERRID I uncommented this
		Toast.makeText(getApplicationContext(), String.valueOf(mInputType), Toast.LENGTH_SHORT).show();
				
		// Get LocationManager and set related provider.
	    mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
	    boolean gpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	    
	    if (gpsEnabled)
	    	mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Globals.RECORDING_GPS_INTERVAL_DEFAULT, Globals.RECORDING_GPS_DISTANCE_DEFAULT, this);
	    else
	    	mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Globals.RECORDING_NETWORK_PROVIDER_INTERVAL_DEFAULT,Globals.RECORDING_NETWORK_PROVIDER_DISTANCE_DEFAULT, this);
	 
//    	Toast.makeText(getApplicationContext(), "mInputType: "+String.valueOf(mInputType), Toast.LENGTH_SHORT).show();

	    if (mInputType == Globals.INPUT_TYPE_AUTOMATIC){
	    	// init sensor manager
	    	mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
	    
	    	mGravitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
	    	mSensorManager.registerListener(this, mGravitySensor, SensorManager.SENSOR_DELAY_FASTEST);
	    	
	    	mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
	    	mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
	    	mAccelerometerActivityClassificationTask.execute();
	    	
	    	//JERRID: Register light Sensor
			mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
			mSensorManager.registerListener(this, mLightSensor,SensorManager.SENSOR_DELAY_FASTEST);
			
			mMagnetSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
			mSensorManager.registerListener(this, mMagnetSensor,SensorManager.SENSOR_DELAY_FASTEST);
			}
	    
		// Using pending intent to bring back the MapActivity from notification center.
		// Use NotificationManager to build notification(icon, content, title, flag and pIntent)
		String notificationTitle = "MyRuns";
		String notificationText = "Tracking Location";
		Intent myIntent = new Intent(this, MapDisplayActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(), 0, myIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
		
		Notification notification = new Notification.Builder(this)
	        .setContentTitle(notificationTitle)
	        .setContentText(notificationText).setSmallIcon(R.drawable.greend)
	        .setContentIntent(pendingIntent).build();
		
		NotificationManager notificationManager =  (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notification.flags = notification.flags | Notification.FLAG_ONGOING_EVENT;
		//notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notificationManager.notify(0, notification);
				
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
//    	Toast.makeText(getApplicationContext(), "service onDestroy", Toast.LENGTH_SHORT).show();

		// Unregistering listeners
		mLocationManager.removeUpdates(this);
		// Remove notification
	    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	    notificationManager.cancelAll();
	    
//    	Toast.makeText(getApplicationContext(), "mInputType: "+String.valueOf(mInputType), Toast.LENGTH_SHORT).show();

	    // unregister listener
	    if (mInputType == Globals.INPUT_TYPE_AUTOMATIC){
//	    	Toast.makeText(getApplicationContext(), "unregister linstener", Toast.LENGTH_SHORT).show();
	    	mSensorManager.unregisterListener(this);
	    }
	    
	    // cancel task
	    mAccelerometerActivityClassificationTask.cancel(true);
	}
	
	public class TrackingBinder extends Binder{
		public TrackingService getService(){
			return TrackingService.this;
		}
	}

	
	/************ implement LocationLister interface ***********/
	public void onLocationChanged(Location location) {
//    	Toast.makeText(getApplication(), "OnLocationChanged", Toast.LENGTH_SHORT).show();

		//JERRID Adds--------------
		// Check whether location is valid, drop if invalid
	      if (!LocationUtils.isValidLocation(location)) {
	        Log.w(TAG, "Ignore onLocationChangedAsync. location is invalid.");
	        return;
	      }
		
	      //Check whether location reading is accurate
	      if (!location.hasAccuracy() || location.getAccuracy() >= Globals.RECORDING_GPS_ACCURACY_DEFAULT) {
	          Log.d(TAG, "Ignore onLocationChangedAsync. Poor accuracy.");
	          return;
	        }
	      
	      // Fix for phones that do not set the time field
	      if (location.getTime() == 0L) {
	        location.setTime(System.currentTimeMillis());
	      }
	      //------------------
	      
		// update location list
		mLocationList.add(location);

		// Send broadcast saying new location is updated
		mLocationUpdateBroadcast.putExtra(TrackingService.LOCATION_UPDATE, TrackingService.NEW_LOCATION_AVAILABLE);
		sendBroadcast(mLocationUpdateBroadcast);
	}
	
	public void onProviderDisabled(String provider) {}
	public void onProviderEnabled(String provider) {}
	public void onStatusChanged(String provider, int status, Bundle extras) {}
	
	private void pauseDataCollection(){
		dataCollectionEnabled = false;
		mGravity = null;
		mGeomagnetic=null;
		//clear the buffer becasue we don't need it anymore
		mAccBuffer.clear();
		mLightIntensityReadingBuffer.clear();
	}
	
	/************ implement SensorEventLister interface ***********/
	public void onSensorChanged(SensorEvent event) {
//		Toast.makeText(getApplicationContext(), "onSensorChanged", Toast.LENGTH_SHORT).show();
		 // Many sensors return 3 values, one for each axis.
		if(trackFile == null) {
			File sdCard = Environment.getExternalStorageDirectory();  
			String resultPredictor = sdCard + "/Android/data/edu.dartmouth.cs.myruns5/files"  + "/keepTrack.txt";     
			File resultFile;
			try {
				resultFile = new File(resultPredictor);
				resultFile.createNewFile();
				trackFile = new FileOutputStream(resultFile);
				trackFile.write(("Current type is " +event.sensor.getType()).getBytes());
				trackFile.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			try {
				trackFile.write(("Current type is " +event.sensor.getType()).getBytes());
				trackFile.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
	      if (event.sensor.getType() == android.hardware.Sensor.TYPE_MAGNETIC_FIELD){
	           mGeomagnetic = event.values;
	      }else if(event.sensor.getType() == android.hardware.Sensor.TYPE_GRAVITY){
	    	  mGravity = event.values;
	      }else if(event.sensor.getType() == android.hardware.Sensor.TYPE_LINEAR_ACCELERATION ){
              double x = event.values[0];
              double y = event.values[1];
              //Jerrid: Dont care about z component (for now)
              double z = event.values[2];				
              double m = Math.sqrt(x*x + y*y + z*z);
	
              // Add m to the mAccBuffer one by one.
              try {
            	  mAccBuffer.add(m);
              } catch (IllegalStateException e) {
            	  ArrayBlockingQueue<Double> newBuf = new ArrayBlockingQueue<Double>(2*mAccBuffer.size());
            	  mAccBuffer.drainTo(newBuf);
            	  mAccBuffer = newBuf;
            	  mAccBuffer.add(m);				
              }
	      }else
	      if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
	    	  //JERRID: Light Sensor Reading
	         
	    	  if (mGravity != null && mGeomagnetic != null)
	          {
	              float R[] = new float[9];
	              float I[] = new float[9];
	              boolean success = android.hardware.SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
	              if (success) 
	              {
	                  float orientation[] = new float[3];
	                  android.hardware.SensorManager.getOrientation(R, orientation); 
		              pitchReading[0] = Math.round(Math.abs((orientation[0]*180)/Math.PI));
		              pitchReading[1] = Math.round(Math.abs((orientation[1]*180)/Math.PI));
		              pitchReading[2] = Math.round(Math.abs((orientation[2]*180)/Math.PI));
	                  float uvi = 0;
	                  LumenDataPoint intensityReading = new LumenDataPoint(event.timestamp, pitchReading, event.values[0], uvi);
	                    
	                  try {
	                      //JERRID: Add the magnitude reading to the buffer
	                      mLightIntensityReadingBuffer.add(intensityReading);
	                      File dir = new File (android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/accelerometer");
	                      dir.mkdirs();
	                      FileWriter sunClassificationFile = new FileWriter(dir.getAbsolutePath()+"/"+Globals.LIGHT_INTENSITY_FILE_NAME, true);                       
                          try {                  
                        	  String out = (System.currentTimeMillis() + "\t" + event.values[0] + "\t" + pitchReading[0] +  "\t" + pitchReading[1] + "\t" + pitchReading[2] +"\n");         
                        	  //Log.e("LIGHT DATA: ", out);
                        	  sunClassificationFile.append(out);           
                          } catch (IOException ex){
                          }
                          finally{
                        	  sunClassificationFile.flush();
                        	  sunClassificationFile.close();
                          }
	                         
	                      
	                  } catch (IllegalStateException e) {
	                
	                      // Exception happens when reach the capacity.
	                      // Doubling the buffer. ListBlockingQueue has no such issue,
	                      // But generally has worse performance
	                      ArrayBlockingQueue<LumenDataPoint> newBuf = new ArrayBlockingQueue<LumenDataPoint>( mLightIntensityReadingBuffer.size() * 2);
	                      mLightIntensityReadingBuffer.drainTo(newBuf);
	                      mLightIntensityReadingBuffer = newBuf;
	                      mLightIntensityReadingBuffer.add(intensityReading);
	                  } catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	              }
	          }
//			Toast.makeText(getApplicationContext(), String.valueOf(mAccBuffer.size()), Toast.LENGTH_SHORT).show();
		}
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {}


	/************ AsyncTask **************/
	private class LightSensorActivityClassificationTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... arg0) {

	          /*
	           * The training phase stores the min, max, mean, std, and mean absolute deviation of an intensity window
	           * for each of the 16 light intensity readings (m0..m14), and the label the user provided to the 
	           *  collector (see collector UI later). Collectively, we call these features the 
	           *  feature vector comprises: magnitudes (f0..f14), MAX magnitude, label....
	           */          
			Instance featureInstance = new Instance(Globals.FEAT_NUMBER_FEATURES - 1);
			int blockSize = 0;
			LumenDataPoint[] lightIntensityDataBlock = new LumenDataPoint[Globals.LIGHT_BLOCK_CAPACITY];
			double maxLightMagnitude = Double.MIN_VALUE,
					minLightMagnitude = Double.MAX_VALUE,
					meanLightIntensity = 0,
					varianceIntensity = 0,
					stdLightMagnitude = 0,
					meanAbsoluteDeveationLightIntensity = 0;

	          while (true) 
	          {
	        	  try 
	        	  {
	                  // need to check if the AsyncTask is cancelled or not in the while loop
	                  if (isCancelled () == true)
	                  {
	                      return null;
	                  }

	                  // JERRID: Pops the "head" element from the Blocking Queue one at a time
	                  LumenDataPoint dataPoint = mLightIntensityReadingBuffer.take();
	                  double intensityReading = dataPoint.getIntensity();
	                  lightIntensityDataBlock[blockSize++] = dataPoint;
	                  
	                  //Calculate Mean Intensity Value
	                  if(blockSize <= 1)
	                      meanLightIntensity = intensityReading;
	                  else
	                      meanLightIntensity = (intensityReading + meanLightIntensity*(blockSize-1))/blockSize;
	              
	                  
	                  //JERRID: Once 16 readings are found, identify the MIN, MAX, magnitude
	                  if (blockSize == Globals.LIGHT_BLOCK_CAPACITY) 
	                  {
	                      //Compute the Mean Absolute Deviation since we have a full buffer=
	                      for (LumenDataPoint dp : lightIntensityDataBlock) {
	                          //find mean absolute deviation
	                          double val = dp.getIntensity();
	                          double diff = val - meanLightIntensity;
	                          varianceIntensity += diff * diff;
	                          meanAbsoluteDeveationLightIntensity += Math.abs(diff);
	                          
	                          //Calculate the MIN/MAX (seen so far)
	                          if (maxLightMagnitude < intensityReading) {
	                              maxLightMagnitude = intensityReading;
	                          }
	                          
	                          //find the min intensity
	                          if (minLightMagnitude > intensityReading) {
	                              minLightMagnitude = intensityReading;
	                          }
	                      }
	                      
	                      varianceIntensity = varianceIntensity/Globals.LIGHT_BLOCK_CAPACITY;
	                      stdLightMagnitude = Math.sqrt(varianceIntensity);
	                      meanAbsoluteDeveationLightIntensity = meanAbsoluteDeveationLightIntensity / Globals.LIGHT_BLOCK_CAPACITY;
	                  
	                      featureInstance.setValue(minAttribute,minLightMagnitude);
	                      featureInstance.setValue(maxAttribute,maxLightMagnitude);
	                      featureInstance.setValue(meanAttribute,meanLightIntensity);
	                      featureInstance.setValue(stdAttribute,stdLightMagnitude);
	                      featureInstance.setValue(meanAbsDeviationAttribute,meanAbsoluteDeveationLightIntensity);
	                      
	                      //Classifier
	                      WekaWrapper wrapper = new WekaWrapper();
	                      double prediction = wrapper.classifyInstance(featureInstance);
	                      String classification = featureInstance.classAttribute().value((int) prediction);
	                      
	                      /*File dir = new File (android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/accelerometer");
	                      dir.mkdirs();
	                      FileWriter sunClassificationFile=null;
	                  
	                      try {
	                          sunClassificationFile = new FileWriter(dir.getAbsolutePath()+"/"+Globals.LIGHT_INTENSITY_FILE_NAME, true);                       
	                          try {                                  
	                        	  long uviReading=0;
	                        	  String out = (System.currentTimeMillis() +"\t" + classification + "\t" + uviReading + "\t" + pitchReading[0] +  "\t" + pitchReading[1] + "\t" + pitchReading[2] +"\n");         
	                        	  Log.e("LIGHT DATA: ", out);
	                        	  sunClassificationFile.append(out);           
	                          } catch (IOException ex){
	                          }
	                          finally{
	                        	  sunClassificationFile.flush();
	                        	  sunClassificationFile.close();
	                          }
	                          
	                      } catch (IOException e) {
	                          e.printStackTrace();
	                      }
	                      */
	                      
	                      //Reset the Values
	                      blockSize = 0;
	                      // time = System.currentTimeMillis();
	                      maxLightMagnitude = Double.MIN_VALUE;
	                      minLightMagnitude = Double.MAX_VALUE;
	                      stdLightMagnitude = 0;
	                      varianceIntensity = 0;
	                      meanAbsoluteDeveationLightIntensity = 0;
	                      meanLightIntensity = 0;
	                  }
	              } catch (Exception e) {
	                  e.printStackTrace();
	              }
			}
		}
	}
	
	
	// Timer object to periodically update final type
	Timer updateFinalTypeTimer = new Timer();
	
	private int mMaxActivityInferenceWindow = 5;//Allow 5 readings to dictate the Voted activity
	int inferenceCount=0;
	Map<Integer,Integer> mInferredActivityTypeMap = new HashMap<Integer,Integer>(Globals.FEAT_NUMBER_FEATURES);
	Map<Integer,Integer> mFinalInferredActivityTypeMap = new HashMap<Integer,Integer>();
	Map<Integer,Double> mActivityVsDurationMap = new HashMap<Integer,Double>();
	
	private class AccelerometerActivityClassificationTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... arg0) {

			// First time , the buffer gets filled. For the first entry, the current time.
			long bufferFillStartTime = System.currentTimeMillis();
			// Used to calculate time difference between activities.
			long bufferFillFinishTime = 0;
			int blockSize = 0;
			FFT fft = new FFT(Globals.ACCELEROMETER_BLOCK_CAPACITY);
			double[] accBlock = new double[Globals.ACCELEROMETER_BLOCK_CAPACITY];
			double[] re = accBlock;
			double[] im = new double[Globals.ACCELEROMETER_BLOCK_CAPACITY];
			
			double max = Double.MIN_VALUE;
			double currentLeadCount = Double.MIN_VALUE;
			int currentTrend = Integer.MIN_VALUE;
			FileOutputStream predictionFile = null;
			// Use in case you want to collect data for training
			FileOutputStream trainingDataFileStream = null;

			// Time elapsed since last buffer fill.
			float timeElapsed;
			// Correction parameter used while calculating time difference.
			float timeCorrectionMillis;
			// Delay between worker calls, the worker sets the final type and final sweat rate.
			// milli seconds
			final int delayBetweenWorkerCalls = 1000;

			// Memory card path.
			File sdCard = Environment.getExternalStorageDirectory();
			// Application path.
    		String resultPredictor = sdCard + "/Android/data/edu.dartmouth.cs.myruns5/files"  + "/predicted.txt";
    		File resultFile;
			try {
				resultFile = new File(resultPredictor);
				resultFile.createNewFile();
				predictionFile = new FileOutputStream(resultFile);
				 predictionFile.write("starting write \n\n\n\n".getBytes());
                 predictionFile.flush();      
			} catch (Exception e) {
				e.printStackTrace();
			}			
			// Create the timer task implementor class object with an intial state.
			// The various state values are required so that the timer task impelmentor can judiciously determine the dominant activity as per the latest trend.
			UpdateFinalTypeTask updateTask = new UpdateFinalTypeTask(
					mFinalInferredActivityTypeMap,
					mActivityVsDurationMap,
					mMotionUpdateBroadcast,
					getApplicationContext(),
					delayBetweenWorkerCalls
			);
			updateFinalTypeTimer.schedule(updateTask,0,delayBetweenWorkerCalls);		
           
			while (true) {
				
				try {
					
					// need to check if the AsyncTask is cancelled or not in the while loop
					if (isCancelled () == true){
						
						// Set it as the final inferred type.
						//mMotionUpdateBroadcast.putExtra(VOTED_MOTION_TYPE, finalInferredType);
						//sendBroadcast(mMotionUpdateBroadcast);
						try {
							
							predictionFile.close();
							
						} catch (Exception e) {
							e.printStackTrace();
						}
						return null;
					}
					
					ArrayList<Double> featVect = new ArrayList<Double>(Globals.ACCELEROMETER_BLOCK_CAPACITY + 1);
					
					// Dumping buffer
					double accelValue = mAccBuffer.take().doubleValue();		
					accBlock[blockSize++] = accelValue;
					
					// JERRID: Pops the "head" element from the Blocking Queue one at a time
					if(accelValue > max)
						max = accelValue;
					
					
					if (blockSize == Globals.ACCELEROMETER_BLOCK_CAPACITY) {
						//Recieved a full block/disable data collection						
						pauseDataCollection();
						bufferFillFinishTime = System.currentTimeMillis();
						
						// This gives the seconds difference
						timeElapsed = TimeUnit.MILLISECONDS.toSeconds(bufferFillFinishTime) 
								- TimeUnit.MILLISECONDS.toSeconds(bufferFillStartTime);
						// Can either be positive or negative - used to correct the seconds difference.
						timeCorrectionMillis = ((bufferFillFinishTime%1000) - (bufferFillStartTime%1000));
						// Correct the time with the milli second component, so for ex: if 2 times are 2.1 and 0.9
						// Seconds difference would be 2. However the actual diff is 1.2 - which is adjusted by this component
						timeElapsed = timeElapsed + (timeCorrectionMillis/1000);						
						bufferFillStartTime = bufferFillFinishTime;
						blockSize = 0;
						
						// time = System.currentTimeMillis();
						/* OLD CODE - jerrid
						max = .0;						 
						for (double val : accBlock) {
							if (max < val) {
								max = val;
							}
						}	
						*/
						fft.fft(re, im);
						for (int i = 0; i < re.length; i++) {
							double mag = Math.sqrt(re[i] * re[i] + im[i] * im[i]);
							featVect.add(mag);
							im[i] = .0; // Clear the field
						}
							
						// Append max after frequency component
						featVect.add(max);						
						int value = (int) WekaClassifier.classify(featVect.toArray());
						
						// new code, used to track the activity duration.
						//Just note the activity and the time that has elapsed;
						Double currentDuration = mActivityVsDurationMap.containsKey(value) ? mActivityVsDurationMap.get(value):0;
						mActivityVsDurationMap.put(value,currentDuration+timeElapsed);
						updateTask.SetActivityDurationMap(mActivityVsDurationMap);
						
						// For classification purpose
						Log.d("mag", String.valueOf(value));
						//JERRID: Infer motion type based upon majority vote------
						int count = mInferredActivityTypeMap.containsKey(value) ? mInferredActivityTypeMap.get(value) : 0;
						int currentCount = count + 1;
						mInferredActivityTypeMap.put(value, currentCount);
						// Increment inference count to handle sliding window mechanism.
						inferenceCount++;
						
						// New code.
						// The count is regenerated each cycle.
						// Check which activity has gained lead in the current trend so far.
						// Associated that activity as the current dominant type activity.
						if(currentCount >= currentLeadCount ){
							// This activity has gained lead.Store its count for future comparision.
							currentLeadCount = currentCount;
							//The associated activity is to stored to identify the dominant trend.
							currentTrend = value;
						}
						
						/* JERRID: Old code
						 * This code block performs running mean of Activity inferences. 
						 * This won't work becasue of truncation (eg: 3+3+3+2+2/5 yeilds 2.8 roughly
						 * but 3 should be the inferred
						if(inferenceCount++ == 0){//1st motion value
							
		                	  mActivityInference = value;
						}else{//1..* activity values averaged using running mean
							mActivityInference = (value + mActivityInference * (inferenceCount-1)) / inferenceCount;
						}
						*/
		                  
						//Once the max inference count is reached, do a half sliding window 
						//(ie: the current mean inference value weight counts for 2 of next 5 readings)

	                	//int maxInferenceKey=-1,maxInferenceValue=-1;
						
						// Log for testing activity durations.
	                	/*
						StringBuilder currentActivityBuilder2 = new StringBuilder();
						currentActivityBuilder2.append("\n\nThe recorded duration activities so far:\n");
                		for (Map.Entry<Integer, Double> entry : mActivityVsDurationMap.entrySet()) {
                			// List each activity along with count.
                			currentActivityBuilder2.append(Globals.INFERENCE_LIST[entry.getKey()] + " totally occured " + entry.getValue() + "  amount of duration. \n \n");
                		}
                        predictionFile.write(currentActivityBuilder2.toString().getBytes());
                        predictionFile.flush();*/
                        
	                	// Finished collection the 5 samples
	                	// Now increase the weight of an activity based on the current dominant trend.
		                if(inferenceCount == mMaxActivityInferenceWindow)  {
		                	try {		
		                		
		                		// Log for testing activity count.
		                		/*StringBuilder currentActivityBuilder = new StringBuilder();
		                		// List the current time.
		                		currentActivityBuilder.append("\n\nTime:" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()) + "\n");
		                		// List the dominant activity
		                		currentActivityBuilder.append("Current activity predicted:" + (currentTrend == -1 ? "NA" : Globals.INFERENCE_LIST[currentTrend]) + ".\n");
		                		// List all the activities.
		                		currentActivityBuilder.append("The recorded set of activities in the current cycle is as follows:\n");
		                		for (Map.Entry<Integer, Integer> entry : mInferredActivityTypeMap.entrySet()) {
		                			// List each activity along with count.
		                			currentActivityBuilder.append(Globals.INFERENCE_LIST[entry.getKey()] + " occured " + entry.getValue() + " times. \n");
		                		}
		                        predictionFile.write(currentActivityBuilder.toString().getBytes());
		                        predictionFile.flush();*/
		                    }
		                    catch (Exception e) {
		                    	//predictionFile.close();
		                        Log.e("Exception", "File write failed: " + e.toString());
		                    }
		                	
		                	// Reset the entire map
		                	mInferredActivityTypeMap.clear();
		                	// For this activity get the current count in the mapping
							// If the first time put it into a map with a count of 1.
							 int finalTypeCount = 
									 mFinalInferredActivityTypeMap.containsKey(mInferredActivityType)?mInferredActivityTypeMap.get(mInferredActivityType):0;
							 mFinalInferredActivityTypeMap.put(mInferredActivityType, finalTypeCount + 1);
		                	// New code.
		                	// After clearing the entire map increased the weight for the current dominant trend.
		                	// Unless some other activity dominates this activity in the next cycle, this would continue to be the dominant trend.
		                	mInferredActivityTypeMap.put(currentTrend, 2);
		                	//Reset current lead count and the inference count.
		                	currentLeadCount = 0;
		                	inferenceCount = 0;
		                	//Old Code --- inferenceCount = 2;
		                	//iterating over keys only
		                	//int maxIndex = -1;
		                	//for (Map.Entry<Integer, Integer> entry : mInferredActivityTypeMap.entrySet()) {
		                		// Reset the mapping.
		                		// Reset all entries to 0.
		                		//entry.setValue(0);
		                		//int val = entry.getValue();
		                		
		                		//Old code
		                		/*
		                		if(val > maxIndex){
		                			maxIndex = val;
		                			maxInferenceValue = val;
		                			maxInferenceKey = entry.getKey();
		                		}
		                		*/
		                		// Reset instance counts to 0 to avoid a 2nd for loop to clear vector
		                		// All counts have been reset to 0.
		                		// entry.setValue(0);
		                	//}
		                
		                	// Weight the Mode activity with the highest
		                	// mInferredActivityTypeMap.put(maxInferenceKey, 2);
		                	
		                	// New code.
		                	// After clearing the entire map increased the weight for the current dominant trend.
		                	// Unless some other activity dominates this activity in the next cycle, this would continue to be the dominant trend.
		                	//mInferredActivityTypeMap.put(currentTrend, 2);
		                	//Reset current lead count and the inference count.
		                	currentLeadCount = 0;
		                	inferenceCount = 0;
		                	
		                	// The window is done. Write to an output file a prediction indicating the current activity.
		                	
		                }
						
						//int maxIndex = 0;their original code
						//if (mInferenceCount[maxIndex] < mInferenceCount[1]) maxIndex = 1;
						//if (mInferenceCount[maxIndex] < mInferenceCount[2]) maxIndex = 2;their original code
		                
		                // Here specify the current trend.
		                // new code
						mInferredActivityType = Globals.INFERENCE_MAPPING[currentTrend == -1 ? value : currentTrend];//maxIndex];
						int currentActivity = Globals.INFERENCE_MAPPING[value];
						mMotionUpdateBroadcast.putExtra(CURRENT_MOTION_TYPE, currentActivity);
						int sweatRateIndex = GetSweatRateIndexForActivity(currentActivity);
						mMotionUpdateBroadcast.putExtra(CURRENT_SWEAT_RATE_INTERVAL,sweatRateIndex);
						updateTask.SetCurrentType(currentActivity);
						updateTask.SetSweatRateIndex(sweatRateIndex);

						//mMotionUpdateBroadcast.putExtra(CURRENT_MOTION_TYPE, currentActivity);
						// send broadcast with the CURRENT activity type
						//---------------
						
						
						 
						 // Updates the timer task class with the latest status
						 // This is required for the timer task implementor class to judicious determine the dominant activity as per the latest trend.
						 updateTask.SetFinalInferredActivityTypeMap(mFinalInferredActivityTypeMap);
						 updateTask.SetIntent(mMotionUpdateBroadcast);
						 updateTask.SetContext(getApplicationContext());
						 
						 // Used to update the current type.
						 sendBroadcast(mMotionUpdateBroadcast);
						
						//Reset the max value
						max = Double.MIN_VALUE;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}		
			}
		}
		
		
		// Gets the sweat rate index for the activity.
		private int GetSweatRateIndexForActivity(int currentActivityIndex) {
			int sweatRateIndex = 0;
			
			switch(currentActivityIndex) {
			case Globals.ACTIVITY_TYPE_STANDING:
			case Globals.ACTIVITY_TYPE_WALKING:
				sweatRateIndex = 0;
				break;
			case Globals.ACTIVITY_TYPE_JOGGING :
				sweatRateIndex = 1;
				break;
			case Globals.ACTIVITY_TYPE_RUNNING:
				sweatRateIndex = 2;
				break;
				default:
					sweatRateIndex = 3;
					break;
			}
		
		return sweatRateIndex;
		}
		
	}
	}



 // Utility TimerTask implementor class to periodically update the final time.
// We can configure the interval , between each call to the run method to this class
class UpdateFinalTypeTask extends TimerTask {
	   
	// Activities vs their respective count.
	Map<Integer,Integer> mFinalInferredActivityTypeMap;
	
	// Track activity vs their duration.
	Map<Integer,Double> mActivityVsDurationMap;
	
	//Intent of the calling module
	Intent mMotionUpdateBroadcast;
	
	//Context of the calling module
	Context mAppContext;
	
	// Initially set the current type to standing
	// It could be changed once we process the activity map.
	private int mCurrentType = 13;
	
	// Used to predict the current sweat rate.
	private int mSweatRateIndex = 0;
	
	// Average sweat rate while standing and walking is 1.5 liter per hour
	private int standWalkingHourlySweatRate = 1500;
	
	// Average sweat rate while standing and walking is 2 liters per hour
	private int joggingHourlySweatRate = 2000;
	
	// Average sweat rate while running is 3 liters per hour
	private int runningHourlySweatRate = 3000;
		
	// Maximum number of calls that can be done in a minute.
	private int mNoOfMaxCalls;
	
	// Constructor to be called to initialize the class object.
	public UpdateFinalTypeTask(
			Map<Integer,Integer> finalInferredActivityTypeMap, 
			Map<Integer,Double> activityVsDurationMap,
			Intent motionUpdateBroadcast, 
			Context appContext,
			int delayinMillisBetweenWorkerCalls
	) {
		mFinalInferredActivityTypeMap = finalInferredActivityTypeMap;
		mActivityVsDurationMap = activityVsDurationMap;
		mMotionUpdateBroadcast = motionUpdateBroadcast;
		mAppContext = appContext;
		// Calculate the number of calls to be called in a minute
		mNoOfMaxCalls = 60000/(delayinMillisBetweenWorkerCalls);
		if(mNoOfMaxCalls < 1) {
			mNoOfMaxCalls = 1;
		}
	}
	
	// Sets the activity vs duration map.
	// map used by the worker to choose and set the final sweat rate.
	// Set api is required as the duration of each activity would change over a course of time.
	public void SetActivityDurationMap(Map<Integer,Double> activityVsDurationMap) {
		mActivityVsDurationMap = activityVsDurationMap;
	}
	
	// Sets the activity vs count map.
	// map used by the worker to choose and set the final type.
	// Set api is required as the activity mapping would change over the course of the user action.(First stand, then run etc)
	public void SetFinalInferredActivityTypeMap(Map<Integer,Integer> finalInferredActivityTypeMap) {
		mFinalInferredActivityTypeMap = finalInferredActivityTypeMap;
	}
	
	// Sets the intent.
	// Required by the worker.
	public void SetIntent(Intent motionUpdateBroadcast) {
		mMotionUpdateBroadcast = motionUpdateBroadcast;
	}
	
	// Sets the context
	// Required by the worker.
	public void SetContext(Context appContext) {
		mAppContext = appContext;
	}
	
	// Sets the current activity type
	// Required by the worker before updating.
	public void SetCurrentType(int currentType) {
		mCurrentType = currentType;
	}
	
	
	// set the current sweat rate index.
	public void SetSweatRateIndex(int sweatRateIndex) {
		mSweatRateIndex = sweatRateIndex;
	}

	// Constant element required to update the final type.
	public static final String VOTED_MOTION_TYPE = "voted motion type";
	
	// Constant element to calculate the average sweat rate.
	public static final String FINAL_SWEAT_RATE_AVERAGE = "average sweat rate";
	
	// Constant element required to update the current type.
	public static final String CURRENT_MOTION_TYPE = "new motion type";
	
	// Constant element required to update the sweat rate interval prediction.
	public static final String CURRENT_SWEAT_RATE_INTERVAL = "sweat rate Interval";
	
	// Keeps track of the number of times this worker has been executed

	// Say the delay between calls is set to x seconds.
	// To make sure the worker run only the first minute, set the maximum 
	// no of calls to (60 seconds / delay in seconds)
	// For ex if delay is set to 2 seconds, maximum no of calls = 60/2 = 30.
	// actual count calculated in the constructor.
	private int mCallCount = 0;
	
	// worker method
	   public void run() {
		   mCallCount++;
		   // check the call count, to make sure only it is being called in the first minute.
		   if(mCallCount > mNoOfMaxCalls) {
			   return;
		   }
			// Temp element used in getting the maximum value.
			int maxElement = Integer.MIN_VALUE;
			// By default standing. Would be updated as and when we poll the activity map.
			int finalInferredType = Globals.ACTIVITY_TYPE_STANDING;
			
			// Take into consideration all the activities which has been tabulated so far.
			// check which activity has run the maximum number of times.
			// Set them as the final activity.
			for (Map.Entry<Integer, Integer> entry : mFinalInferredActivityTypeMap.entrySet()) {
				if(entry.getValue() > maxElement) {
					// Currently this activity has the maximum count.
					maxElement = entry.getValue();
					// Final type is obtained.
					finalInferredType = entry.getKey();		
				}			
			}
			// Set the current type.
			mMotionUpdateBroadcast.putExtra(CURRENT_MOTION_TYPE, mCurrentType);
			// set the current sweat rate.
			mMotionUpdateBroadcast.putExtra(CURRENT_SWEAT_RATE_INTERVAL, mSweatRateIndex);
			// Set the final type.
			mMotionUpdateBroadcast.putExtra(VOTED_MOTION_TYPE, finalInferredType);
			Double sweatRateMeasure = 0.0;
			Double activityDuration = 0.0;
			if(finalInferredType == Globals.ACTIVITY_TYPE_STANDING) {
				// Get the activity duration in seconds. 				
				activityDuration = mActivityVsDurationMap.get(0);
				
				// Calculate the total amount of sweat lost.
				if(activityDuration != null ){
					sweatRateMeasure = (standWalkingHourlySweatRate * activityDuration)/3600;
				}				
			} else if(finalInferredType == Globals.ACTIVITY_TYPE_WALKING) {
				// Get the activity duration in seconds. 
				activityDuration = mActivityVsDurationMap.get(1);
				if(activityDuration != null ){
					sweatRateMeasure = (standWalkingHourlySweatRate * activityDuration)/3600;
				}
			}
			else if(finalInferredType == Globals.ACTIVITY_TYPE_JOGGING) {
				// Get the activity duration in seconds. 
				activityDuration = mActivityVsDurationMap.get(2);
				// Calculate the total amount of sweat lost.
				if(activityDuration != null ){
					sweatRateMeasure = (joggingHourlySweatRate * activityDuration)/3600;
				}
			} else if(finalInferredType == Globals.ACTIVITY_TYPE_RUNNING) {
				// Get the activity duration in seconds. 
				activityDuration = mActivityVsDurationMap.get(3);
				// Calculate the total amount of sweat lost.
				if(activityDuration != null ){
					sweatRateMeasure = (runningHourlySweatRate * activityDuration)/3600;
				}
			}
			
			// set the final sweat rate.
			mMotionUpdateBroadcast.putExtra(FINAL_SWEAT_RATE_AVERAGE,sweatRateMeasure + " milli liters");
			
			// Send the broad cast. It updates the UI.
			mAppContext.sendBroadcast(mMotionUpdateBroadcast);
	   }	   
	
}


