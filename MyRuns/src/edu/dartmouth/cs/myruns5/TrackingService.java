package edu.dartmouth.cs.myruns5;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;

import weka.core.Attribute;
import weka.core.stemmers.SnowballStemmer;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.widget.Toast;
import edu.dartmouth.cs.myruns5.util.LocationUtils;
import edu.repo.ucla.serialusbdriver.Constants;
import edu.repo.ucla.serialusbdriver.ILightSensor;
import edu.repo.ucla.serialusbdriver.UsbSensorManager;


public class TrackingService extends Service implements OnInitListener,LocationListener, SensorEventListener,RecognitionListener
{
	// Use in case you want to collect data for training
	FileOutputStream trainingDataFileStream = null;
	OutputStreamWriter myOutWriter = null;
	
	private SunAngleReciever sunAngleReciever = new SunAngleReciever();

	private static boolean LOGGING_ENABLED=false;
	private static boolean IS_PATH_TRACKING=false;
	public static boolean isTracking(){return IS_PATH_TRACKING;}
	
	public static double  cumulativeSweatRate = 0.0;
	public static int sweatRateIndex=1;
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
	
	public static int currentActivity=Globals.ACTIVITY_ID_STANDING,mCurrentActivityIndex=Globals.ACTIVITY_ID_STANDING,mInferredActivityType=Globals.INFERENCE_MAPPING[mCurrentActivityIndex];
	
	private SensorManager mSensorManager;
	private Sensor mAccelerometer,mLightSensor,mMagnetSensor,mGravitySensor;
	
	private float[] mGeomagnetic;
	private static ArrayBlockingQueue<Double> mAccBuffer,mLightBuffer;
	
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
	
	public static String environmentClassification=Globals.CLASS_LABEL_IN_DOORS;
	
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
	
	private TextToSpeech tts; 
	@Override
	public void onCreate() {
		tts = new TextToSpeech(this,  this);
		
		speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());		
		mLocationList = new ArrayList<Location>();
		
		//Instantiate the motion broadcast classes
		mLocationUpdateBroadcast = new Intent();
		mLocationUpdateBroadcast.setAction(Globals.ACTION_TRACKING);
		mMotionUpdateBroadcast = new Intent();
		mMotionUpdateBroadcast.setAction(Globals.ACTION_MOTION_UPDATE);
		
		//Initialize the activity and enviornment buffers
		mAccBuffer = new ArrayBlockingQueue<Double>(Globals.ACCELEROMETER_BUFFER_CAPACITY);
		mLightBuffer = new ArrayBlockingQueue<Double>(Globals.LIGHT_BLOCK_CAPACITY);
		mAccelerometerActivityClassificationTask = new AccelerometerActivityClassificationTask();
		
		//Initialize the light sensor information
		mUsbSensorManager = UsbSensorManager.getManager();	
		mLightSensor0Callback = new LightSensor0Callback();
		mLightSensor1Callback = new LightSensor1Callback();
		
		
		//Start timer toggle for data collection
		dataCollector = new Timer();
		dataCollector.scheduleAtFixedRate(dataCollectorTask, Globals.DATA_COLLECTOR_START_DELAY, Globals.DATA_COLLECTOR_INTERVAL);
		
		//Register sun angle data
		IntentFilter sunAngleFilter = new IntentFilter();
		sunAngleFilter.addAction(UltravioletIndexService.CURRENT_SUN_ANGLE);
    	registerReceiver(sunAngleReciever, sunAngleFilter);
		sunAngleHandler = new Handler();
		sunAngleHandler.postDelayed(this.sunAngleRunnable, 5000);
		
    	Toast.makeText(getApplicationContext(), "service onCreate", Toast.LENGTH_SHORT).show();
	}

	private float solarZenithAngle=Float.MAX_VALUE;
	private float azimuthAngle=Float.MAX_VALUE;
	private float elevationAngle=Float.MAX_VALUE;
	
	public class SunAngleReciever extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent){
			azimuthAngle = intent.getExtras().getFloat(UltravioletIndexService.AZIMUTH_ANGLE,Float.MAX_VALUE);
			solarZenithAngle = intent.getExtras().getFloat(UltravioletIndexService.SOLAR_ZENITH_ANGLE,Float.MAX_VALUE);
			elevationAngle = intent.getExtras().getFloat(UltravioletIndexService.ELEVATION_ANGLE, Float.MAX_VALUE);
		}
	}
	
	SpeechRecognizer speechRecognizer = null;
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		String action = intent.getAction();
		if(action != null){
			if(action.equals(Globals.VOICE_COMMAND)){
				//Listen for speech
				speechRecognizer.setRecognitionListener(this);
				speechRecognizer.startListening(RecognizerIntent.getVoiceDetailsIntent(getApplicationContext()));
				
				ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);				
				toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 500); 
				return START_STICKY;
			}
			
			if(action.equals(Globals.TRACK_COMMAND))
			{
				String extra = intent.getExtras().getString(Globals.TRACK_COMMAND);
				if(extra != null)
				{
					if(extra.equals(Globals.START_TRACKING)){
						//Stop the GPS part but just track Exposure
						
						
					}else if(extra.equals(Globals.STOP_TRACKING)){
						
					}
				}
				 intent.putExtra(Globals.VOICE_COMMAND, Globals.STOP_TRACKING);
				 startService(intent);
			 }else
			 //Start the path recordings
			 if(input.contains("record") && input.contains("path")){
				 Intent intent = new Intent(this, TrackingService.class);
				 intent.putExtra(Globals.VOICE_COMMAND, Globals.START_TRACKING);
			}
		}
		
		
    	Toast.makeText(getApplicationContext(), "service onStartCommand", Toast.LENGTH_SHORT).show();
		
    	//Configure the Ardurino Sensors
    	setupArdurinoTracking();
		

		setupLocationTracking();

		if(LOGGING_ENABLED)
			setupDataLogging();
		timestamp = new GregorianCalendar();
	   
		
    	
    	// init sensor manager
		// Get LocationManager and set related provider.
	    mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
	    boolean gpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	    
	    if (gpsEnabled)
	    	mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Globals.RECORDING_GPS_INTERVAL_DEFAULT,
	    			Globals.RECORDING_GPS_DISTANCE_DEFAULT, this);
	    else
	    	mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Globals.RECORDING_NETWORK_PROVIDER_INTERVAL_DEFAULT,
	    			Globals.RECORDING_NETWORK_PROVIDER_DISTANCE_DEFAULT, this);

    	mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    
    	mGravitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
    	mSensorManager.registerListener(this, mGravitySensor, SensorManager.SENSOR_DELAY_FASTEST);
    	
    	mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
    	mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
    	
		mMagnetSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		mSensorManager.registerListener(this, mMagnetSensor,SensorManager.SENSOR_DELAY_FASTEST);
		
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),SensorManager.SENSOR_DELAY_GAME);
        
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

		//setup tracking
		IS_PATH_TRACKING = true;
		
		return START_STICKY;
	}

	private void setupDataLogging() {    	
		File root = Environment.getExternalStorageDirectory();
		File file = new File(root.getAbsolutePath()+"/dataTrace"+System.currentTimeMillis()+".txt");
		try {
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
				trainingDataFileStream = new FileOutputStream(file);
				myOutWriter = new OutputStreamWriter(trainingDataFileStream);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void setupLocationTracking() {
		// TODO Auto-generated method stub
		
	}

	private void setupArdurinoTracking(){
		// Get the UV and light sensors that the UsbSensorManager recognizes
		List<ILightSensor> lightSensor_list = mUsbSensorManager.getLightSensorList();
		if(lightSensor_list.isEmpty())
		{
			Globals.FOUND_ARDUINO = false;
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
			if(lightSensor_list.isEmpty())
			{
				Globals.FOUND_ARDUINO = false;
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
	}
	
	@Override
	public void onDestroy() {
        try {
            myOutWriter.close();
			trainingDataFileStream.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	Toast.makeText(getApplicationContext(), "service onDestroy", Toast.LENGTH_SHORT).show();

		// Unregistering listeners
		mLocationManager.removeUpdates(this);
		// Remove notification
	    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	    notificationManager.cancelAll();

	    // unregister listener
//	    	Toast.makeText(getApplicationContext(), "unregister linstener", Toast.LENGTH_SHORT).show();
	    mSensorManager.unregisterListener(this);
	    
	    
	    //Unregister the UVI filter/
    	unregisterReceiver(sunAngleReciever);
    	
	    // cancel task
	    mAccelerometerActivityClassificationTask.cancel(true);
		mAccBuffer.clear();
		mLightBuffer.clear();

		IS_PATH_TRACKING = false;

	     if (tts != null) {
	            tts.stop();
	            tts.shutdown();
	     }
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
		mLightBuffer.clear();
	}
	
	double phoneVsSunOrientationDifference = 0,phoneScreenAngle=0;
	/************ implement SensorEventLister interface ***********/
	public void onSensorChanged(SensorEvent event) {
	      if (event.sensor.getType() == android.hardware.Sensor.TYPE_MAGNETIC_FIELD){
	           mGeomagnetic = event.values;
	      }else if(event.sensor.getType() == android.hardware.Sensor.TYPE_ORIENTATION){
	          // get the angle around the z-axis rotated
	    	  //NOTE: I subtract 180 because the orientation assumes the back side of the phone facing sun as front.
	          phoneScreenAngle = Math.abs(Math.round(event.values[0]-180));
	          
	          if(azimuthAngle != Float.MAX_VALUE)
	        	  phoneVsSunOrientationDifference = Math.abs(azimuthAngle - phoneScreenAngle);

	         //System.out.println("Phone Orintation (Screen): " + phoneScreenAngle + " | Elevation Angle (Sun): "+elevationAngle + " | Azimuth Angle (Sun): "+ azimuthAngle +" | Difference: "+phoneVsSunOrientationDifference);
	          
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
              try {
            	  mLightBuffer.add(lightIntensityReading);
              } catch (IllegalStateException e) {
            	  ArrayBlockingQueue<Double> newBuf = new ArrayBlockingQueue<Double>(2*mLightBuffer.size());
            	  mLightBuffer.drainTo(newBuf);
            	  mLightBuffer = newBuf;
            	  mLightBuffer.add(lightIntensityReading);				
              }
	      }
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {}
	
//NO LONGER CALLED AT THE MOMENT
	public void gotLightSensorData() {

        //only want max, don't store buffer
        blockSize++;
        
        if(lightIntensityReading > maxIntensityThisBuffer)
        	maxIntensityThisBuffer = lightIntensityReading;
	     
        //JERRID: Once 16 readings are found, identify the MIN, MAX, magnitude
        if (blockSize == Globals.LIGHT_BLOCK_CAPACITY) 
        {
        	//Indoors
    		if(maxIntensityThisBuffer <= 200){
    				environmentClassification = Globals.CLASS_LABEL_IN_DOORS;
    		}else if(maxIntensityThisBuffer > 200 && maxIntensityThisBuffer <= 6000){
    			environmentClassification = Globals.CLASS_LABEL_IN_SHADE;
    		}else if(maxIntensityThisBuffer > 6000 && maxIntensityThisBuffer <= 10000){
    			environmentClassification = Globals.CLASS_LABEL_IN_CLOUD;
    		}else
    			environmentClassification = Globals.CLASS_LABEL_IN_SUN;
        	
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
        	lightIntensityReading=0;
        	maxIntensityThisBuffer = -1;
        }
		return;
	}

	Handler sunAngleHandler;
	Runnable sunAngleRunnable = new Runnable() {
        @Override
        public void run() {
        	final Intent currentSunAngleIntent = new Intent(getApplicationContext(), UltravioletIndexService.class);
        	currentSunAngleIntent.setAction(UltravioletIndexService.CURRENT_SUN_ANGLE);
        	startService(currentSunAngleIntent);
        	sunAngleHandler.postDelayed(sunAngleRunnable, Globals.SUN_ANGLE_UPDATE_RATE);
        }
    };
	
	// Timer object to periodically update final type
	Timer updateFinalTypeTimer = new Timer();
	
	int inferenceCount=0;
	Map<Integer,Integer> mInferredActivityTypeMap = new HashMap<Integer,Integer>(Globals.FEAT_NUMBER_FEATURES);
	Map<Integer,Integer> mFinalInferredActivityTypeMap = new HashMap<Integer,Integer>();
	Map<Integer,Double> mActivityVsDurationMap = new HashMap<Integer,Double>();


	private void updateRelativeExposurePercentages() {
		if(this.solarZenithAngle >= 0 && this.solarZenithAngle <= 30){
			relativeFaceAngle=.26f;
			relativeNeckAngle=.23f;
			relativeChestAngle=.23f;
			relativeBackAngle=.23f;
			relativeForearmAngle=.13f;
			relativeDorsalHandAngle=.30f;
			relativeLegAngle=.12f;
		}else if(this.solarZenithAngle >= 31 && this.solarZenithAngle <= 50){
			relativeFaceAngle=.39f;
			relativeNeckAngle=.36f;
			relativeChestAngle=.36f;
			relativeBackAngle=.36f;
			relativeForearmAngle=.17f;
			relativeDorsalHandAngle=.35f;
			relativeLegAngle=.23f;
		}else if(this.solarZenithAngle >= 51 && this.solarZenithAngle <= 80){
			relativeFaceAngle=.48f;
			relativeNeckAngle=.59f;
			relativeChestAngle=.59f;
			relativeBackAngle=.59f;
			relativeForearmAngle=.41f;
			relativeDorsalHandAngle=.42f;
			relativeLegAngle=.47f;
		}
	}

	private int leadInferredActivityIndex = Globals.ACTIVITY_ID_STANDING,leadActivityCount=0;
	Calendar timestamp;
	public static double cumulativeFaceExposure=0,
			cumulativeNeckExposure=0,
			cumulativeChestExposure=0,
			cumulativeBackExposure=0,
			cumulativeForearmExposure=0,
			cumulativeDorsalHandExposure=0,
			cumulativeLegExposure=0,
			cumulativeHorizontalExposure=0;

	private float relativeFaceAngle=.26f;
	private float relativeNeckAngle=.23f;
	private float relativeChestAngle=.23f;
	private float relativeBackAngle=.23f;
	private float relativeForearmAngle=.13f;
	private float relativeDorsalHandAngle=.30f;
	private float relativeLegAngle=.12f;
	
	@SuppressLint("DefaultLocale")
	private class AccelerometerActivityClassificationTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {

			int blockSize = 0,blockSizeLight = 0;
			FFT fft = new FFT(Globals.ACCELEROMETER_BLOCK_CAPACITY);
			double[] accBlock = new double[Globals.ACCELEROMETER_BLOCK_CAPACITY];
			double[] re = accBlock;
			double[] im = new double[Globals.ACCELEROMETER_BLOCK_CAPACITY];
			
			double max = Double.MIN_VALUE;

			while (true) 
			{
				try 
				{
		        	Calendar time = new GregorianCalendar();
		    		//Update the Sun angles
		            if(time.getTimeInMillis() > timestamp.getTimeInMillis() + 12 * Globals.ONE_MINUTE) {
		        		final Intent currentUVIIntent = new Intent();
		        		currentUVIIntent.setAction(UltravioletIndexService.CURRENT_SUN_ANGLE);
		        		startService(currentUVIIntent);
		        		timestamp = time;
		            }
		            
		            //-------Update the Light Intensity Values-----
		            
			            
			        if(Globals.FOUND_ARDUINO == false)
					{
			        	try
			        	{
			        		lightIntensityReading = mLightBuffer.take().doubleValue();
			        		blockSizeLight++;
					        if(lightIntensityReading > maxIntensityThisBuffer)
					        	maxIntensityThisBuffer = lightIntensityReading;
					        if (blockSizeLight == Globals.LIGHT_BLOCK_CAPACITY ) 
					        {
					        	if(LOGGING_ENABLED){
					        	//Indoors
					        		myOutWriter.write(environmentClassification+",");
					        		myOutWriter.write(maxIntensityThisBuffer+",");
					        	}
					        	System.out.print("Environmant Classificaton: "+environmentClassification);
					        	System.out.print(" | Intensity Default: "+ maxIntensityThisBuffer);
				        		
					        	/*
				        		 * NOTE: THe commented out sections were an attempt to scale the light intensity values when the phone
				        		 * screen is oriented opposite from the Sun. The falasy of this approach is more information is required
				        		 * to discriminate between shade vs Sun so this same scalor is applied when the phones in the shade, which
				        		 * causes the shade values to reflect Sun, and thats not what we want
				        		 */
					        	/*if(elevationAngle != Float.MAX_VALUE)
					        	{

					        		//double scalor=1;
					        		//if(phoneVsSunOrientationDifference > 180){
					        			//NOTE: I fit the linear model as a factor of 0:180 0x:16x in terms of avg intensity difference
					        			//So if angular difference is > 180 then I need to normalize back to w/in 180 range
					        		//	phoneVsSunOrientationDifference = 180 - (phoneVsSunOrientationDifference % 180);
					        		//}
					        		//scalor = phoneVsSunOrientationDifference*8/90;//slope measured of intensity differences standing still and turning in a circle
					        		//NOTE: The true function is exponential but for time it's linear for now

						        	//System.out.print(" | Scalor Default: "+ scalor);
									//myOutWriter.write(scalor+",");
					        		//maxIntensityThisBuffer = maxIntensityThisBuffer + maxIntensityThisBuffer*scalor*Math.sin(Utils.convertDegreeToRadian(phoneVsSunOrientationDifference))*Math.cos(Utils.convertDegreeToRadian(elevationAngle));

									//myOutWriter.write(maxIntensityThisBuffer+",");
									//System.out.println(" | New Intensity: "+ maxIntensityThisBuffer);
					        		//System.out.println("Elevation Angle (Sun): "+elevationAngle + " | Azimuth Angle (Sun): "+ azimuthAngle +" | Difference: "+phoneVsSunOrientationDifference);

									myOutWriter.write(elevationAngle+",");
									myOutWriter.write(azimuthAngle+",");
									myOutWriter.write(phoneVsSunOrientationDifference+",");
					        	}
								myOutWriter.write("\n");
					    		*/
					        	 
					    		if(maxIntensityThisBuffer <= 200){
					    				environmentClassification = Globals.CLASS_LABEL_IN_DOORS;
					    		}else if(maxIntensityThisBuffer > 200 && maxIntensityThisBuffer <= 4800){
					    			environmentClassification = Globals.CLASS_LABEL_IN_SHADE;
					    		}
					    		//else if(maxIntensityThisBuffer > 2000 && maxIntensityThisBuffer <= 10000){
					    		//	environmentClassification = Globals.CLASS_LABEL_IN_CLOUD;
					    		//}
					    		else
					    			environmentClassification = Globals.CLASS_LABEL_IN_SUN;
					        	
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
					        	lightIntensityReading=0;
					        	maxIntensityThisBuffer = -1;
					        }
						}catch(Exception e){
							e.printStackTrace();
						}
					}else
					{
						//PROCESS ARDURINO LIGHT SENSOR DATA
						lightIntensityReading = Math.max(light0, light1);
				        if(lightIntensityReading > maxIntensityThisBuffer)
				        	maxIntensityThisBuffer = lightIntensityReading;
	
						blockSizeLight++;
				        //JERRID: Once 16 readings are found, identify the MIN, MAX, magnitude
				        if (blockSizeLight == Globals.LIGHT_BLOCK_CAPACITY_ARDURINO) 
				        {
				        	//Indoors
				    		if(maxIntensityThisBuffer <= 200){
				    				environmentClassification = Globals.CLASS_LABEL_IN_DOORS;
				    		}else if(maxIntensityThisBuffer > 200 && maxIntensityThisBuffer <= 700){
				    			environmentClassification = Globals.CLASS_LABEL_IN_CLOUD;
				    		}else if(maxIntensityThisBuffer > 700 && maxIntensityThisBuffer <= 1500){
				    			environmentClassification = Globals.CLASS_LABEL_IN_SHADE;
				    		}else 
				    			environmentClassification = Globals.CLASS_LABEL_IN_SUN;
				        	
				        	
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
					}
			        //-------------END LIGHT SENSOR
			        
					//----------PROCESS ACCELEROMATER DATA
					ArrayList<Double> featVect = new ArrayList<Double>(Globals.ACCELEROMETER_BLOCK_CAPACITY + 1);
					// Pops the "head" element from the Blocking Queue one at a time
					double accelValue = mAccBuffer.take().doubleValue();
					accBlock[blockSize++] = accelValue;
					if(accelValue > max)
						max = accelValue;
					
					if (blockSize == Globals.ACCELEROMETER_BLOCK_CAPACITY) 
					{
						//Recieved a full block/disable data collection						
						pauseDataCollection();
						
						long bufferFillFinishTime = System.currentTimeMillis();
						
						// Compute the time elapsed since start of the buffer 
						double timeElapsed = bufferFillFinishTime - bufferFillStartTime,
								timeElapsedSeconds = timeElapsed/Globals.ONE_SECOND;
						bufferFillStartTime = bufferFillFinishTime;
						
						// Calculate running total of UV Exposure w.r.t. second
						double uviValue = 0;
						if (environmentClassification.equals(Globals.CLASS_LABEL_IN_SUN)){
							uviValue = UltravioletIndexService.uvIrradianceSun;
						}if(environmentClassification.equals(Globals.CLASS_LABEL_IN_CLOUD)){
							uviValue = UltravioletIndexService.uvIrradianceShade;
						}else if (environmentClassification.equals(Globals.CLASS_LABEL_IN_SHADE)){
							uviValue = UltravioletIndexService.uvIrradianceShade;
						}else if (environmentClassification.equals(Globals.CLASS_LABEL_IN_DOORS)){
							uviValue = 0;
						}

						updateRelativeExposurePercentages();
						cumulativeHorizontalExposure+= uviValue * timeElapsedSeconds;
						cumulativeFaceExposure+= uviValue * relativeFaceAngle * timeElapsedSeconds;
						cumulativeNeckExposure+= uviValue * relativeNeckAngle * timeElapsedSeconds;
						cumulativeChestExposure+= uviValue * relativeChestAngle * timeElapsedSeconds;
						cumulativeBackExposure+= uviValue * relativeBackAngle * timeElapsedSeconds;
						cumulativeForearmExposure+= uviValue * relativeForearmAngle * timeElapsedSeconds;
						cumulativeDorsalHandExposure+= uviValue * relativeDorsalHandAngle * timeElapsedSeconds;
						cumulativeLegExposure+= uviValue * relativeLegAngle * timeElapsedSeconds;

						mMotionUpdateBroadcast.putExtra(Globals.CUMULATIVE_UV_EXPOSURE, cumulativeHorizontalExposure);
						mMotionUpdateBroadcast.putExtra(Globals.CUMULATIVE_FACE_UV_EXPOSURE, cumulativeFaceExposure);
						mMotionUpdateBroadcast.putExtra(Globals.CUMULATIVE_NECK_UV_EXPOSURE, cumulativeNeckExposure);
						mMotionUpdateBroadcast.putExtra(Globals.CUMULATIVE_CHEST_UV_EXPOSURE, cumulativeChestExposure);
						mMotionUpdateBroadcast.putExtra(Globals.CUMULATIVE_BACK_UV_EXPOSURE, cumulativeBackExposure);
						mMotionUpdateBroadcast.putExtra(Globals.CUMULATIVE_FOREARM_UV_EXPOSURE, cumulativeForearmExposure);
						mMotionUpdateBroadcast.putExtra(Globals.CUMULATIVE_DORSAL_HAND_UV_EXPOSURE, cumulativeDorsalHandExposure);
						mMotionUpdateBroadcast.putExtra(Globals.CUMULATIVE_LEG_UV_EXPOSURE, cumulativeLegExposure);
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
		                	
							sweatRateIndex = GetSweatRateIndexForActivity(mInferredActivityType);
							mMotionUpdateBroadcast.putExtra(Globals.SWEAT_RATE_INDEX,Globals.SWEAT_RATE_INTERVALS[sweatRateIndex]);
		                }
		                
						currentActivity = Globals.INFERENCE_MAPPING[mCurrentActivityIndex];
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

						if(LOGGING_ENABLED){
							myOutWriter.write(Long.toString(bufferFillFinishTime)+",");
							myOutWriter.write(Double.toString(timeElapsedSeconds)+",");
							myOutWriter.write(Float.toString(solarZenithAngle)+",");
							myOutWriter.write(Float.toString(elevationAngle)+",");
							myOutWriter.write(Float.toString(azimuthAngle)+",");
							myOutWriter.write(Double.toString(phoneScreenAngle)+",");
							myOutWriter.write(Double.toString(phoneVsSunOrientationDifference)+",");
							myOutWriter.write(Double.toString(relativeFaceAngle)+",");
							myOutWriter.write(Double.toString(relativeNeckAngle)+",");
							myOutWriter.write(Double.toString(relativeChestAngle)+",");
							myOutWriter.write(Double.toString(relativeBackAngle)+",");
							myOutWriter.write(Double.toString(relativeForearmAngle)+",");
							myOutWriter.write(Double.toString(relativeDorsalHandAngle)+",");
							myOutWriter.write(Float.toString(relativeLegAngle)+",");
							myOutWriter.write(Globals.ACTIVITY_TYPES[currentActivity]+",");
							myOutWriter.write(Globals.ACTIVITY_TYPES[mInferredActivityType]+",");
							
							if(mLocationList.size() > 0){
								Location location = mLocationList.get(mLocationList.size()-1);
								myOutWriter.write(","+location.getLatitude()+","+location.getLongitude());
							}
							myOutWriter.write("\n");
						}
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


	
	@Override
	public void onReadyForSpeech(Bundle params) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onBeginningOfSpeech() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRmsChanged(float rmsdB) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onBufferReceived(byte[] buffer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onEndOfSpeech() {
		// TODO Auto-generated method stub

		speechRecognizer.stopListening();
	}

	@Override
	public void onError(int error) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onResults(Bundle results) {
		//Disable the speech
		ArrayList strlist = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        SnowballStemmer stemmer = new SnowballStemmer();  // initialize stopwords
        stemmer.setStemmer("english");
                   
        String input=stemmer.stem(strlist.get(0).toString().toLowerCase()) + " ";
        if(input.contains ("start") && input.contains("log")){
        	LOGGING_ENABLED=true;
        	setupDataLogging();
        }else
        if(input.contains ("stop") && input.contains("log")){
        	LOGGING_ENABLED=false;
        	try {
				myOutWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        	
        }else
		
		 //Stop the path recording
		 if(input.contains("stop") && input.contains("record")){
			 Intent intent = new Intent(this, TrackingService.class);
			 intent.putExtra(Globals.VOICE_COMMAND, Globals.STOP_TRACKING);
			 startService(intent);
		 }else
		 //Start the path recordings
		 if(input.contains("record") && input.contains("path")){
			 Intent intent = new Intent(this, TrackingService.class);
			 intent.putExtra(Globals.VOICE_COMMAND, Globals.START_TRACKING);
			 startService(intent);
		 }else
		 // Find uv index, ultraviolet index.
		 if (input.contains("ultraviolet") && input. contains ("index")){
		 	tts.speak("The ultraviolet index is 3", TextToSpeech.QUEUE_FLUSH, null);
		 }else
		 // Find vitamin d exposure
		 if (input.contains ("vitamin d") && input. contains ("exposure")){
		 	tts.speak("10 mins of direct sun exposure", TextToSpeech.QUEUE_FLUSH, null);
		 }
		 
		 // Find when I should reapply sunblock 
		 if (input.contains("reapply") && input. contains ("sunblock")){
			 tts.speak("In 20 minutes based on your recent activity levels", TextToSpeech. QUEUE_FLUSH, null);
		 }
		 
		
		 Log.e("TGAGS",input);
	}

	@Override
	public void onPartialResults(Bundle partialResults) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onEvent(int eventType, Bundle params) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onInit(int status) {
		// TODO Auto-generated method stub
		
	}
}