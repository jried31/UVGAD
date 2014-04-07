package edu.dartmouth.cs.myruns5;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;

import weka.core.Attribute;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import edu.dartmouth.cs.myruns5.util.LocationUtils;
import edu.repo.ucla.serialusbdriver.Constants;
import edu.repo.ucla.serialusbdriver.ILightSensor;
import edu.repo.ucla.serialusbdriver.UsbSensorManager;


public class TrackingService extends Service implements LocationListener, SensorEventListener
{
	
	private UVIBroadcastReciever mUVReceiver = new UVIBroadcastReciever();
	private double  cumulativeSweatRate = 0.0, cumulativeUVExposure = 0.0;
	public static int MAX_ACTIVITY_INFERENCE_WINDOW = 5;//Allow 5 readings to dictate the Voted activity

	//Classes for connecting to Arduino light sensor
	private ILightSensor mLightSensor0;
	private ILightSensor mLightSensor1;
	private LightSensor0Callback mLightSensor0Callback;
	private LightSensor1Callback mLightSensor1Callback;
	
	
	private UsbSensorManager mUsbSensorManager;
	
	// This is the callback object for the first light sensor
		public class LightSensor0Callback implements ILightSensor.Callback
		{
			LightSensor0Callback()
			{
			}
			@Override
			public void onSensorUpdate(final int updateLux)
			{
				light0 = updateLux;
			}

			@Override
			public void onSensorEjected()
			{
				mLightSensor0.unregister();
				mLightSensor0 = null;
			}
		}
		
		// This is the callback object for the second light sensor
		private class LightSensor1Callback implements ILightSensor.Callback
		{
			LightSensor1Callback()
			{
			}
			
			@Override
			public void onSensorUpdate(final int updateLux)
			{
				light1 = updateLux;
			}

			@Override
			public void onSensorEjected()
			{
				mLightSensor1.unregister();
				mLightSensor1 = null;
			}
		}

	//End of Arduino light sensor required block
	
	private Attribute mClassNameForData,
		//meanAttribute,
		//stdAttribute,
		maxAttribute;
		//minAttribute,
		//meanAbsDeviationAttribute;
	
	public ArrayList<Location> mLocationList;

	// Location manager
	private LocationManager mLocationManager;
	
	// Intents for broadcasting location/motion updates
	private Intent mLocationUpdateBroadcast;
	private Intent mMotionUpdateBroadcast;
	
	private int mInputType;
	public int mCurrentActivityIndex=Globals.ACTIVITY_ID_STANDING,mInferredActivityType=Globals.INFERENCE_MAPPING[mCurrentActivityIndex];
	
	private SensorManager mSensorManager;
	private Sensor mAccelerometer,mLightSensor,mMagnetSensor,mGravitySensor;
	
	private float[] mGeomagnetic;
	private static ArrayBlockingQueue<Double> mAccBuffer;
	
	private AccelerometerActivityClassificationTask mAccelerometerActivityClassificationTask;
	
	private final IBinder mBinder = new TrackingBinder();

	private float[] mGravity;
	private double []pitchReading={0,0,0};
	private long bufferFillStartTime;
	
	public static final String LOCATION_UPDATE = "location update";
	public static final int NEW_LOCATION_AVAILABLE = 400;
	
	// broadcast 
	public static final String ACTION_MOTION_UPDATE = "motion update";
	public static final String CURRENT_MOTION_TYPE = "new motion type";
	public static final String VOTED_MOTION_TYPE = "voted motion type";
	public static final String ACTION_TRACKING = "tracking action";
	public static final String CURRENT_SWEAT_RATE_INTERVAL = "sweat rate Interval";
	public static final String FINAL_SWEAT_RATE_AVERAGE = "average sweat rate";
	
	private static String environmentClassification=Globals.CLASS_LABEL_IN_SHADE;
	
	private static final String TAG = "TrackingService";
	
	//Pulled from light sensor asynch task!
	int blockSize = 0;
	public static double maxLightMagnitude = Double.MIN_VALUE,
			//minLightMagnitude = Double.MAX_VALUE,
			//meanLightIntensity = 0,
			//varianceIntensity = 0,
			//stdLightMagnitude = 0,
			//meanAbsoluteDeveationLightIntensity = 0,
			lightIntensityReading = 0,
			light0=0,light1=0,
			maxIntensityThisBuffer = -1;
	
	
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
		mLocationUpdateBroadcast.setAction(Globals.ACTION_TRACKING);
		mMotionUpdateBroadcast = new Intent();
		mMotionUpdateBroadcast.setAction(Globals.ACTION_MOTION_UPDATE);
		
		mAccBuffer = new ArrayBlockingQueue<Double>(Globals.ACCELEROMETER_BUFFER_CAPACITY);
		mAccelerometerActivityClassificationTask = new AccelerometerActivityClassificationTask();
		mUsbSensorManager = UsbSensorManager.getManager();	
		
		// Create the sensor callback objects
		mLightSensor0Callback = new LightSensor0Callback();
		mLightSensor1Callback = new LightSensor1Callback();
		
		//Start the timer for data collection
		dataCollector = new Timer();
		dataCollector.scheduleAtFixedRate(dataCollectorTask, Globals.DATA_COLLECTOR_START_DELAY, Globals.DATA_COLLECTOR_INTERVAL);
		
		IntentFilter filter = new IntentFilter();
    	filter.addAction(UltravioletIndexService.CURRENT_UV_INDEX_ALL);
    	registerReceiver(mUVReceiver, filter);
		
		uviHandler = new Handler();
		uviHandler.postDelayed(uviRunnable, 1000);
    	Toast.makeText(getApplicationContext(), "service onCreate", Toast.LENGTH_SHORT).show();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

    	Toast.makeText(getApplicationContext(), "service onStartCommand", Toast.LENGTH_SHORT).show();
		
		//In here, create an instance of Daniel's sensor callback. Put that clas down in the bottom of this file and use it here
		Globals.FOUND_ARDUINO = false;
		
		// Get the UV and light sensors that the UsbSensorManager recognizes
		List<ILightSensor> lightSensor_list = mUsbSensorManager.getLightSensorList();
		
		// Make sure that the lists aren't empty
		if(lightSensor_list.isEmpty())
		{
			Toast.makeText(this, "ERROR 1: Sensor hardware not detected", Toast.LENGTH_LONG).show();
		}
		else
		{
			// Grab the first pair of sensor objects.  On an Android phone there really shouldn't 
			// be more than one
			mLightSensor0 = lightSensor_list.get(0);
			
			// @NOTE: We need to grab a new list of sensors since Java must create a new sensor 
			//    	  object.  Otherwise, they'll refer to the same sensor object and invoking 
			// 		  the register() method will overwrite the original callback object.
			// 		  Another way to think about it is the getLightSensorList() method is like a 
			// 		  factory that returns light sensor objects so we need it to create a new 
			// 		  object for us.
			lightSensor_list = mUsbSensorManager.getLightSensorList();
			
			// Make sure that the lists aren't empty
			if(lightSensor_list.isEmpty())
			{
				Toast.makeText(this, "ERROR 2: Sensor hardware not detected", Toast.LENGTH_LONG).show();
			}
			else
			{	
				// Grab the handle to the UV sensor and second light sensor objects
				mLightSensor1 = lightSensor_list.get(0);
				
				// Initialize the UV sensor and light sensor objects
				mLightSensor0.init(Constants.PULSE_ID_LIGHT_0);
				mLightSensor1.init(Constants.PULSE_ID_LIGHT_1);
				
				Globals.FOUND_ARDUINO = true;
				Toast.makeText(this, "Initialized sensor hardware!", Toast.LENGTH_LONG).show();
			}
		}
		
		
		// Read inputType, can be GPS or Automatic.
		mInputType = intent.getIntExtra(MapDisplayActivity.INPUT_TYPE, -1);
		//Toast.makeText(getApplicationContext(), String.valueOf(mInputType), Toast.LENGTH_SHORT).show();
				
		// Get LocationManager and set related provider.
	    mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
	    boolean gpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	    
	    if (gpsEnabled)
	    	mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Globals.RECORDING_GPS_INTERVAL_DEFAULT, Globals.RECORDING_GPS_DISTANCE_DEFAULT, this);
	    else
	    	mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Globals.RECORDING_NETWORK_PROVIDER_INTERVAL_DEFAULT,Globals.RECORDING_NETWORK_PROVIDER_DISTANCE_DEFAULT, this);

	    if (mInputType == Globals.INPUT_TYPE_AUTOMATIC){
	    	// init sensor manager
	    	mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
	    
	    	mGravitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
	    	mSensorManager.registerListener(this, mGravitySensor, SensorManager.SENSOR_DELAY_FASTEST);
	    	
	    	mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
	    	mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
	    	
			mMagnetSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
			mSensorManager.registerListener(this, mMagnetSensor,SensorManager.SENSOR_DELAY_FASTEST);
	    	
			//register the phones light sensor
			mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
			mSensorManager.registerListener(this, mLightSensor,SensorManager.SENSOR_DELAY_FASTEST);
		 
			if(Globals.FOUND_ARDUINO)
			{
				mLightSensor0.register(mLightSensor0Callback);
				mLightSensor1.register(mLightSensor1Callback);
			}
				
			bufferFillStartTime = System.currentTimeMillis();
	    	mAccelerometerActivityClassificationTask.execute();
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
		
		//---
    
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
    	Toast.makeText(getApplicationContext(), "service onDestroy", Toast.LENGTH_SHORT).show();

		// Unregistering listeners
		mLocationManager.removeUpdates(this);
		// Remove notification
	    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	    notificationManager.cancelAll();

	    // unregister listener
	    if (mInputType == Globals.INPUT_TYPE_AUTOMATIC){
//	    	Toast.makeText(getApplicationContext(), "unregister linstener", Toast.LENGTH_SHORT).show();
	    	mSensorManager.unregisterListener(this);
	    }
	    
	    //Unregister the UVI filter/
    	unregisterReceiver(mUVReceiver);
    	
	    // cancel task
	    mAccelerometerActivityClassificationTask.cancel(true);
		mAccBuffer.clear();
	}
	
	public class TrackingBinder extends Binder{
		public TrackingService getService(){
			return TrackingService.this;
		}
	}

	
	/************ implement LocationLister interface ***********/
	public void onLocationChanged(Location location) {

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
	}
	
	/************ implement SensorEventLister interface ***********/
	public void onSensorChanged(SensorEvent event) {
	      if (event.sensor.getType() == android.hardware.Sensor.TYPE_MAGNETIC_FIELD){
	           mGeomagnetic = event.values;
	      }else if(event.sensor.getType() == android.hardware.Sensor.TYPE_GRAVITY){
	    	  mGravity = event.values;
	      }else if(event.sensor.getType() == android.hardware.Sensor.TYPE_LINEAR_ACCELERATION )
	      {
	    	  if(dataCollectionEnabled)
	    	  {
	    		  double x = event.values[0];
	              double y = event.values[1];
	              double z = event.values[2];				
	              double m = Math.sqrt(x*x + y*y + z*z);
	              //Compute the devices orientation -- but do nothing with it right now
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
	                  }
	              }
	          	
	          	
	              // Add m to the mAccBuffer one by one.
	              try {
	            	  mAccBuffer.add(m);
	              } catch (IllegalStateException e) {
	            	  ArrayBlockingQueue<Double> newBuf = new ArrayBlockingQueue<Double>(2*mAccBuffer.size());
	            	  mAccBuffer.drainTo(newBuf);
	            	  mAccBuffer = newBuf;
	            	  mAccBuffer.add(m);				
	              }
		      }
	      }else if (event.sensor.getType() == android.hardware.Sensor.TYPE_LIGHT) {
				lightIntensityReading = event.values[0];
				gotLightSensorData();
//			Toast.makeText(getApplicationContext(), String.valueOf(mAccBuffer.size()), Toast.LENGTH_SHORT).show();
	      }
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {}
	
//SEND IN timestamp as long and then luxvalue as float
	public void gotLightSensorData() {

        //only want max, don't store buffer
        blockSize++;
        
        if(lightIntensityReading > maxIntensityThisBuffer)
        	maxIntensityThisBuffer = lightIntensityReading;
	     
        //JERRID: Once 16 readings are found, identify the MIN, MAX, magnitude
        if (blockSize == Globals.LIGHT_BLOCK_CAPACITY) 
        {

    		if(maxIntensityThisBuffer > 1500){
    				environmentClassification = Globals.CLASS_LABEL_IN_SUN;
    		}else if(maxIntensityThisBuffer < 200){
    			environmentClassification = Globals.CLASS_LABEL_IN_DOORS;
    		}else{
    			environmentClassification = Globals.CLASS_LABEL_IN_SHADE;
    		}
        	
        	
			mMotionUpdateBroadcast.putExtra(Globals.ENVIRONMENT_CLASSIFICATION,environmentClassification);
			mMotionUpdateBroadcast.putExtra(Globals.PITCH_BODY,pitchReading);
			mMotionUpdateBroadcast.putExtra(Globals.LIGHT_INTENSITY_READING,maxIntensityThisBuffer);
			
        	//Reset the Values
        	blockSize = 0;
        	// time = System.currentTimeMillis();
        	maxLightMagnitude = Double.MIN_VALUE;
        	//minLightMagnitude = Double.MAX_VALUE;
        	//stdLightMagnitude = 0;
        	//varianceIntensity = 0;
        	//meanAbsoluteDeveationLightIntensity = 0;
        	//meanLightIntensity = 0;
        	maxIntensityThisBuffer = -1;
        }
		return;
	}

	Handler uviHandler;
	Runnable uviRunnable = new Runnable() {
        @Override
        public void run() {
        	final Intent currentUVIIntent = new Intent(getApplicationContext(), UltravioletIndexService.class);
        	currentUVIIntent.setAction(UltravioletIndexService.CURRENT_UV_INDEX_ALL);
        	startService(currentUVIIntent);

    		uviHandler.postDelayed(uviRunnable, Globals.UVI_UPDATE_RATE);
        }
    };
			
    private double mCurrentUVISun = 0.0;
    private double mCurrentUVIShade = 0.0;
	class UVIBroadcastReciever extends BroadcastReceiver {

		
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			 // Convert from mW/cm^2 to (J/s)/m^2
			mCurrentUVISun = arg1.getExtras().getDouble(UltravioletIndexService.CURRENT_UV_INDEX_SUN) * (100*100 / 1000);
			mCurrentUVIShade = arg1.getExtras().getDouble(UltravioletIndexService.CURRENT_UV_INDEX_SHADE) * (100*100 / 1000);
		}
	}
	
	// Timer object to periodically update final type
	Timer updateFinalTypeTimer = new Timer();
	
	int inferenceCount=0;
	Map<Integer,Integer> mInferredActivityTypeMap = new HashMap<Integer,Integer>(Globals.FEAT_NUMBER_FEATURES);
	Map<Integer,Integer> mFinalInferredActivityTypeMap = new HashMap<Integer,Integer>();
	Map<Integer,Double> mActivityVsDurationMap = new HashMap<Integer,Double>();

	private int leadInferredActivityIndex = Globals.ACTIVITY_ID_STANDING,leadActivityCount=0;
	
	private class AccelerometerActivityClassificationTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... arg0) {

			int blockSize = 0,blockSizeLight = 0;
			FFT fft = new FFT(Globals.ACCELEROMETER_BLOCK_CAPACITY);
			double[] accBlock = new double[Globals.ACCELEROMETER_BLOCK_CAPACITY];
			double[] re = accBlock;
			double[] im = new double[Globals.ACCELEROMETER_BLOCK_CAPACITY];
			
			double max = Double.MIN_VALUE;
			// Use in case you want to collect data for training
			FileOutputStream trainingDataFileStream = null;

			while (true) {
				
				try {
					//PROCESS LIGHT SENSOR DATA
					lightIntensityReading = Math.max(light0, light1);
			        if(lightIntensityReading > maxIntensityThisBuffer)
			        	maxIntensityThisBuffer = lightIntensityReading;

					blockSizeLight++;
			        //JERRID: Once 16 readings are found, identify the MIN, MAX, magnitude
			        if (blockSizeLight == Globals.LIGHT_BLOCK_CAPACITY) 
			        {

			    		if(maxIntensityThisBuffer > 1500){
			    				environmentClassification = Globals.CLASS_LABEL_IN_SUN;
			    		}else if(maxIntensityThisBuffer < 200){
			    			environmentClassification = Globals.CLASS_LABEL_IN_DOORS;
			    		}else{
			    			environmentClassification = Globals.CLASS_LABEL_IN_SHADE;
			    		}
			        	
			        	
						mMotionUpdateBroadcast.putExtra(Globals.ENVIRONMENT_CLASSIFICATION,environmentClassification);
						mMotionUpdateBroadcast.putExtra(Globals.PITCH_BODY,pitchReading);
						mMotionUpdateBroadcast.putExtra(Globals.LIGHT_INTENSITY_READING,maxIntensityThisBuffer);
						
			        	//Reset the Values
						blockSizeLight = 0;
			        	// time = System.currentTimeMillis();
			        	maxLightMagnitude = Double.MIN_VALUE;
			        	//minLightMagnitude = Double.MAX_VALUE;
			        	//stdLightMagnitude = 0;
			        	//varianceIntensity = 0;
			        	//meanAbsoluteDeveationLightIntensity = 0;
			        	//meanLightIntensity = 0;
			        	maxIntensityThisBuffer = -1;
			        }
			        //-------------END LIGHT SENSOR
			        
					//----------PROCESS ACCELEROMATER DATA
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
						
						long bufferFillFinishTime = System.currentTimeMillis();
						
						// Compute the time elapsed since start of the buffer 
						double timeElapsed = bufferFillFinishTime - bufferFillStartTime,
								timeElapsedSeconds = timeElapsed/Globals.ONE_SECOND;
						bufferFillStartTime = bufferFillFinishTime;
						
						// Calculate running total of UV Exposure w.r.t. second
						if (environmentClassification.equals(Globals.CLASS_LABEL_IN_SUN))
							cumulativeUVExposure += mCurrentUVISun * timeElapsedSeconds;
						else if (environmentClassification.equals(Globals.CLASS_LABEL_IN_SHADE))
							cumulativeUVExposure += mCurrentUVIShade * timeElapsedSeconds;
					
						mMotionUpdateBroadcast.putExtra(Globals.CUMULATIVE_UV_EXPOSURE, cumulativeUVExposure);
						//----------
						
						blockSize = 0;
		
						fft.fft(re, im);
						for (int i = 0; i < re.length; i++) {
							double mag = Math.sqrt(re[i] * re[i] + im[i] * im[i]);
							featVect.add(mag);
							im[i] = .0; // Clear the field
						}
							
						// Append max after frequency component
						featVect.add(max);						
						mCurrentActivityIndex = (int) WekaClassifier.classify(featVect.toArray());
						
						int currentActivityCount = mInferredActivityTypeMap.containsKey(mCurrentActivityIndex) ? mInferredActivityTypeMap.get(mCurrentActivityIndex) : 0;
						mInferredActivityTypeMap.put(mCurrentActivityIndex, currentActivityCount++);
						
						//Track running total for the lead activity
						if(currentActivityCount > leadActivityCount){
							leadActivityCount = currentActivityCount;
							leadInferredActivityIndex = mCurrentActivityIndex;
						}
						
						// Increment inference count to handle sliding window mechanism.
						inferenceCount++;
						
						// new code, used to track the activity duration.
						//Just note the activity and the time that has elapsed;
						Double currentDuration = mActivityVsDurationMap.containsKey(mCurrentActivityIndex) ? mActivityVsDurationMap.get(mCurrentActivityIndex):0;
						mActivityVsDurationMap.put(mCurrentActivityIndex,currentDuration+timeElapsed);
						
						
	                	// Finished collection the 5 samples
	                	// Now increase the weight of an activity based on the current dominant activity.
		                if(inferenceCount == MAX_ACTIVITY_INFERENCE_WINDOW)  {
		                	inferenceCount = 0;
		                	leadActivityCount = 0;
		                	mInferredActivityTypeMap.clear();
		                	mInferredActivityType = Globals.INFERENCE_MAPPING[leadInferredActivityIndex];
		                	mMotionUpdateBroadcast.putExtra(Globals.VOTED_ACTIVITY_TYPE, mInferredActivityType);
		                	
							int sweatRateIndex = GetSweatRateIndexForActivity(mInferredActivityType);
							mMotionUpdateBroadcast.putExtra(Globals.SWEAT_RATE_INDEX,Globals.SWEAT_RATE_INTERVALS[sweatRateIndex]);
		                }
		                
						int currentActivity = Globals.INFERENCE_MAPPING[mCurrentActivityIndex];
						mMotionUpdateBroadcast.putExtra(Globals.CURRENT_ACTIVITY_TYPE, currentActivity);
						
						
						//Calculate Sweat rate
						if(leadInferredActivityIndex == Globals.ACTIVITY_TYPE_STANDING) {
							cumulativeSweatRate += Globals.SWEAT_RATE_HOURLY_STANDING * (timeElapsedSeconds/Globals.ONE_HOUR);
						} else if(leadInferredActivityIndex == Globals.ACTIVITY_TYPE_WALKING) {
							cumulativeSweatRate += Globals.SWEAT_RATE_HOURLY_WALKING * (timeElapsedSeconds/Globals.ONE_HOUR);
						}else if(leadInferredActivityIndex == Globals.ACTIVITY_TYPE_JOGGING) {
							cumulativeSweatRate += Globals.SWEAT_RATE_HOURLY_JOGGING * (timeElapsedSeconds/Globals.ONE_HOUR);
						} else if(leadInferredActivityIndex == Globals.ACTIVITY_TYPE_RUNNING) {
							cumulativeSweatRate += Globals.SWEAT_RATE_HOURLY_RUNNING * (timeElapsedSeconds/Globals.ONE_HOUR);
						}
	
						mMotionUpdateBroadcast.putExtra(Globals.SWEAT_TOTAL, cumulativeSweatRate);

						
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
				break;
			case Globals.ACTIVITY_TYPE_WALKING:
				sweatRateIndex = 1;
				break;
			case Globals.ACTIVITY_TYPE_JOGGING:
				sweatRateIndex = 2;
				break;
			case Globals.ACTIVITY_TYPE_RUNNING:
				sweatRateIndex = 3;
				break;
			}
		
		return sweatRateIndex;
		}
	}
}