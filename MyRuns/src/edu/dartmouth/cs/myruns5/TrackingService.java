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
import java.util.List;
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
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import edu.dartmouth.cs.myruns5.util.LocationUtils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import edu.repo.ucla.serialusbdriver.*;


import org.apache.http.*;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;


public class TrackingService extends Service
	implements LocationListener, SensorEventListener
	{
	
	private UVIBroadcastReciever mUVReceiver = new UVIBroadcastReciever();
	private double runningUVExposure = 0.0;
	
	//Classes for connecting to Arduino light sensor

	private TextView uvSensor0_text;
	private TextView lightSensor0_text;
	private boolean mIsStreaming;

	private ILightSensor mLightSensor0;
	private ILightSensor mLightSensor1;
	private LightSensor0Callback mLightSensor0Callback;
	private LightSensor1Callback mLightSensor1Callback;
	
	private IUVSensor mUVSensor0;
	private IUVSensor mUVSensor1;
	private UVSensor0Callback mUVSensor0Callback;
	private UVSensor1Callback mUVSensor1Callback;
	
	public static int light0 = -1;
	public static int light1 = -1;
	
	private UsbSensorManager mUsbSensorManager;
	
	// This is the callback object for the first light sensor
		public class LightSensor0Callback implements ILightSensor.Callback
		{
			//private final Activity mActivity;
			
			LightSensor0Callback()
			{
				//mActivity = activity;
			}
			
			public LightSensor0Callback(TrackingService trackingService) {
				// TODO Auto-generated constructor stub
			}

			@Override
			public void onSensorUpdate(final int updateLux)
			{
				light0 = updateLux;
				//Toast.makeText(getApplicationContext(), "Initialized sensor hardware! " + updateLux, Toast.LENGTH_LONG).show();
				// This callback method is invoked when the light sensor gets a new light reading data
				
				// All UI updates MUST occur on the main thread (a.k.a. UI thread) so we update the
				// light sensor TextView object using this Runnable
				/*mActivity.runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						lightSensor0_text.setText("LUX0: " + updateLux);
					}
				});*/
			}

			@Override
			public void onSensorEjected()
			{
				// This function is run when the sensor is forcibly ejected while this callback object 
				// is active and registered with the sensor
				
				/*mActivity.runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						Toast.makeText(getApplicationContext(), "Light sensor ejected!", Toast.LENGTH_SHORT).show();
					}
				});*/
				
				mLightSensor0 = null;
			}
		}
		
		// This is the callback object for the second light sensor
		private class LightSensor1Callback implements ILightSensor.Callback
		{
			//private final Activity mActivity;
			
			LightSensor1Callback()
			{
				//mActivity = activity;
			}
			
			public LightSensor1Callback(TrackingService trackingService) {
				// TODO Auto-generated constructor stub
			}

			@Override
			public void onSensorUpdate(final int updateLux)
			{

				light1 = updateLux;
				/*mActivity.runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						//lightSensor1_text.setText("LUX1: " + updateLux);
					}
				});*/
			}

			@Override
			public void onSensorEjected()
			{
				/*mActivity.runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						Toast.makeText(getApplicationContext(), "Light sensor ejected!", Toast.LENGTH_SHORT).show();
					}
				});*/
				
				mLightSensor1 = null;
			}
		}
		
		// This is the callback object for the first UV sensor
		public class UVSensor0Callback implements IUVSensor.Callback
		{
			//private final Activity mActivity;
			
			UVSensor0Callback()
			{
				//mActivity = activity;
			}
			
			public UVSensor0Callback(TrackingService trackingService) {
				// TODO Auto-generated constructor stub
			}

			@Override
			public void onSensorUpdate(final int updateUV)
			{
				// This callback method is invoked when the UV sensor gets a new UV reading data
				
				// All UI updates MUST occur on the main thread (a.k.a. UI thread) so we update the
				// UV sensor TextView object using this Runnable
				/*mActivity.runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						//uvSensor0_text.setText("UV0: " + updateUV);
					}
				});*/
			}

			@Override
			public void onSensorEjected()
			{
				// This function is run when the sensor is forcibly ejected while this callback object 
				// is active and registered with the sensor
				
				/*mActivity.runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						//Toast.makeText(mContext, "UV sensor ejected!", Toast.LENGTH_SHORT).show();
					}
				});*/
				
				mUVSensor0 = null;
			}
		}
		
		// This is the callback object for the second UV sensor
		private class UVSensor1Callback implements IUVSensor.Callback
		{
			//private final Activity mActivity;
			
			UVSensor1Callback()
			{
				//mActivity = activity;
			}
			
			public UVSensor1Callback(TrackingService trackingService) {
				// TODO Auto-generated constructor stub
			}

			@Override
			public void onSensorUpdate(final int updateUV)
			{
				// This callback method is invoked when the UV sensor gets a new UV reading data
				
				// All UI updates MUST occur on the main thread (a.k.a. UI thread) so we update the
				// UV sensor TextView object using this Runnable
				/*mActivity.runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						//uvSensor1_text.setText("UV1: " + updateUV);
					}
				});*/
			}

			@Override
			public void onSensorEjected()
			{
				// This function is run when the sensor is forcibly ejected while this callback object 
				// is active and registered with the sensor
				
				/*mActivity.runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						//Toast.makeText(mContext, "UV sensor ejected!", Toast.LENGTH_SHORT).show();
					}
				});*/
				
				mUVSensor1 = null;
				mIsStreaming = false;
			}
		}
		
	//End of Arduino light sensor required block
	

	private File mWekaClassificationFile;
	private Attribute mClassNameForData,
		meanAttribute,
		//stdAttribute,
		maxAttribute;
		//minAttribute,
		//meanAbsDeviationAttribute;
	
	public ArrayList<Location> mLocationList;
	
	private int[] mInferenceCount = {0, 0, 0};

	// Location manager
	private LocationManager mLocationManager;
	
	// Intents for broadcasting location/motion updates
	private Intent mLocationUpdateBroadcast;
	private Intent mMotionUpdateBroadcast;
	
	private Intent mLightingClassificationBroadcast;
	
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
	
	public static final String LIGHTING_CLASS_UPDATE = "lighting class update";
	public static String  CUR_LIGHT_CONDITION = "no_data";
	public static String  CUR_LIGHT_CONDITION_ARDUINO = "no_data";
	private static String environmentClassification="no_data";
	
	private static final String TAG = "TrackingService";
	
	//Pulled from light sensor asynch task!
	int blockSize = 0;
	LumenDataPoint[] lightIntensityDataBlock = new LumenDataPoint[Globals.LIGHT_BLOCK_CAPACITY];
	public static double maxLightMagnitude = Double.MIN_VALUE,
			minLightMagnitude = Double.MAX_VALUE,
			meanLightIntensity = 0,
			varianceIntensity = 0,
			stdLightMagnitude = 0,
			meanAbsoluteDeveationLightIntensity = 0,
			intensityReading = 0,
			maxIntensityThisBuffer = -1,
			lastMaxIntensityBuffer = -1;
	
	
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
		
		//mLightingClassificationBroadcast = new Intent();
		//mLightingClassificationBroadcast.setAction(ACTION_MOTION_UPDATE);
		
		mLightIntensityReadingBuffer = new ArrayBlockingQueue<LumenDataPoint>(Globals.LIGHT_BUFFER_CAPACITY);
		mAccBuffer = new ArrayBlockingQueue<Double>(Globals.ACCELEROMETER_BUFFER_CAPACITY);
		mAccelerometerActivityClassificationTask = new AccelerometerActivityClassificationTask();
		//mLightSensorActivityClassificationTask = new LightSensorActivityClassificationTask();
		mInferredActivityType = Globals.ACTIVITY_TYPE_STANDING;
		
		mUsbSensorManager = UsbSensorManager.getManager();	
		
		// Create the sensor callback objects
		mLightSensor0Callback = new LightSensor0Callback(this);
		mLightSensor1Callback = new LightSensor1Callback(this);
		mUVSensor0Callback = new UVSensor0Callback(this);
		mUVSensor1Callback = new UVSensor1Callback(this);
		
		//Start the timer for data collection
		dataCollector = new Timer();
		dataCollector.scheduleAtFixedRate(dataCollectorTask, Globals.DATA_COLLECTOR_START_DELAY, Globals.DATA_COLLECTOR_INTERVAL);
		
		IntentFilter filter = new IntentFilter();
    	filter.addAction(UltravioletIndexService.CURRENT_UV_INDEX_ALL);
    	registerReceiver(mUVReceiver, filter);
		
		uviHandler = new Handler();
		uviHandler.postDelayed(uviRunnable, Globals.UVI_UPDATE_RATE);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null) {
			return START_STICKY;		
		}
		
		String option = intent.getAction();
		//Option to retrieve Envirnment Context from Light Sensor
		if (option != null && option.equals(Globals.ENVIRONMENT_CLASSIFICATION)) {
			Intent i;
			if(Globals.FOUND_ARDUINO)
				i = new Intent(Globals.ENVIRONMENT_CLASSIFICATION).putExtra(Globals.ENVIRONMENT_CLASSIFICATION, CUR_LIGHT_CONDITION_ARDUINO);
			else
				i = new Intent(Globals.ENVIRONMENT_CLASSIFICATION).putExtra(Globals.ENVIRONMENT_CLASSIFICATION, CUR_LIGHT_CONDITION);
			sendBroadcast(i);

			return START_STICKY;
		}
		
		File sdCard = Environment.getExternalStorageDirectory();  
		String tempfilename = sdCard.getAbsolutePath()  + "/temp";
		File file = new File(tempfilename);
		try {
			file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//In here, create an instance of Daniel's sensor callback. Put that clas down in the bottom of this file and use it here
		Globals.FOUND_ARDUINO = false;
		mIsStreaming = false;
		
		// Get the UV and light sensors that the UsbSensorManager recognizes
		List<IUVSensor> uvSensor_list       = mUsbSensorManager.getUVSensorList();
		List<ILightSensor> lightSensor_list = mUsbSensorManager.getLightSensorList();
		
		// Make sure that the lists aren't empty
		if(uvSensor_list.isEmpty() || lightSensor_list.isEmpty())
		{
			Toast.makeText(this, "ERROR 1: Sensor hardware not detected", Toast.LENGTH_LONG).show();
		}
		else
		{
			// Grab the first pair of sensor objects.  On an Android phone there really shouldn't 
			// be more than one
			mLightSensor0 = lightSensor_list.get(0);
			mUVSensor0 = uvSensor_list.get(0);
			
			// @NOTE: We need to grab a new list of sensors since Java must create a new sensor 
			//    	  object.  Otherwise, they'll refer to the same sensor object and invoking 
			// 		  the register() method will overwrite the original callback object.
			// 		  Another way to think about it is the getLightSensorList() method is like a 
			// 		  factory that returns light sensor objects so we need it to create a new 
			// 		  object for us.
			lightSensor_list = mUsbSensorManager.getLightSensorList();
			uvSensor_list    = mUsbSensorManager.getUVSensorList();
			
			// Make sure that the lists aren't empty
			if(uvSensor_list.isEmpty() || lightSensor_list.isEmpty())
			{
				Toast.makeText(this, "ERROR 2: Sensor hardware not detected", Toast.LENGTH_LONG).show();
				
			}
			else
			{	
				// Grab the handle to the UV sensor and second light sensor objects
				mLightSensor1 = lightSensor_list.get(0);
				mUVSensor1 = uvSensor_list.get(0);
				
				// Initialize the UV sensor and light sensor objects
				mLightSensor0.init(Constants.PULSE_ID_LIGHT_0);
				mLightSensor1.init(Constants.PULSE_ID_LIGHT_1);
				mUVSensor0.init(Constants.PULSE_ID_UV_0);
				mUVSensor1.init(Constants.PULSE_ID_UV_1);
				
				Globals.FOUND_ARDUINO = true;
				Toast.makeText(this, "Initialized sensor hardware!", Toast.LENGTH_LONG).show();
			}
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

	    if (mInputType == Globals.INPUT_TYPE_AUTOMATIC){
	    	// init sensor manager
	    	mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
	    
	    	mGravitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
	    	mSensorManager.registerListener(this, mGravitySensor, SensorManager.SENSOR_DELAY_FASTEST);
	    	
	    	mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
	    	mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
	    	mAccelerometerActivityClassificationTask.execute();

	    	//mLightSensorActivityClassificationTask.execute();
	    	
	    	//JERRID: Register light Sensor
			mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
			mSensorManager.registerListener(this, mLightSensor,SensorManager.SENSOR_DELAY_FASTEST);
			

			if(Globals.FOUND_ARDUINO)
			{
				mLightSensor0.register(mLightSensor0Callback);
				mLightSensor1.register(mLightSensor1Callback);
			}
			
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
		
		//---
    	
		/*
    	final Intent currentUVIIntent = new Intent(getApplicationContext(), UltravioletIndexService.class);
    	currentUVIIntent.setAction(UltravioletIndexService.CURRENT_UV_INDEX_ALL);
    	startService(currentUVIIntent);
		*/	
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
//    	Toast.makeText(getApplicationContext(), "service onDestroy", Toast.LENGTH_SHORT).show();

		//this.unregisterReceiver(mUVReceiver);
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
	    
	    // cancel task
	    mAccelerometerActivityClassificationTask.cancel(true);
	    //mLightSensorActivityClassificationTask.cancel(true);
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
		mLightIntensityReadingBuffer.clear();
	}
	
	/************ implement SensorEventLister interface ***********/
	public void onSensorChanged(SensorEvent event) {
		 // Many sensors return 3 values, one for each axis.
		if(trackFile == null) {
			String resultPredictor = android.os.Environment.getExternalStorageDirectory().getAbsolutePath()  + "/keepTrack.txt";     
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
	    	  gotLightSensorData(event);
	    	  return;
//			Toast.makeText(getApplicationContext(), String.valueOf(mAccBuffer.size()), Toast.LENGTH_SHORT).show();
		}
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {}
	
//SEND IN timestamp as long and then luxvalue as float
	public void gotLightSensorData(SensorEvent event) {

		double pitchTemp[] = new double[3];
		pitchTemp[0] = 0; pitchTemp[1] = 0; pitchTemp[2] = 0;
		float uvi = 0;
        LumenDataPoint intensityReading = new LumenDataPoint(event.timestamp, pitchTemp, event.values[0], uvi);
        
        
        try {
            //JERRID: Add the magnitude reading to the buffer
            mLightIntensityReadingBuffer.add(intensityReading);
            
            
        } catch (IllegalStateException e) {
      
            // Exception happens when reach the capacity.
            // Doubling the buffer. ListBlockingQueue has no such issue,
            // But generally has worse performance
            ArrayBlockingQueue<LumenDataPoint> newBuf = new ArrayBlockingQueue<LumenDataPoint>( mLightIntensityReadingBuffer.size() * 2);
            mLightIntensityReadingBuffer.drainTo(newBuf);
            mLightIntensityReadingBuffer = newBuf;
            mLightIntensityReadingBuffer.add(intensityReading);
        } 
		
     // JERRID: Pops the "head" element from the Blocking Queue one at a time
        try
        {
	        LumenDataPoint dataPoint = mLightIntensityReadingBuffer.take();
	        double intensityPopped = dataPoint.getIntensity();
	        //lightIntensityDataBlock[blockSize++] = dataPoint;
	        //only want max, don't store buffer
	        blockSize++;
	        
	        if(intensityPopped > maxIntensityThisBuffer)
	        	maxIntensityThisBuffer = intensityPopped;
	     
	        
	        //JERRID: Once 16 readings are found, identify the MIN, MAX, magnitude
	        if (blockSize == Globals.LIGHT_BLOCK_CAPACITY) 
	        {
	
	            //Compute the Mean Absolute Deviation since we have a full buffer=
	        	/*
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
	            */
	        	
	        	//set CURR_LIGHT_CLASSIFICATION
	        	if(maxIntensityThisBuffer > 0)
	        	{
	        		if(maxIntensityThisBuffer > 1500){
	        			CUR_LIGHT_CONDITION = Globals.CLASS_LABEL_IN_SUN;
	        			if(!Globals.FOUND_ARDUINO)
	        				environmentClassification = Globals.CLASS_LABEL_IN_SUN;
	        		}else{
	        			CUR_LIGHT_CONDITION = Globals.CLASS_LABEL_IN_SHADE;
	        			if(!Globals.FOUND_ARDUINO)
	        				environmentClassification = Globals.CLASS_LABEL_IN_SHADE;
	        		}
	        		lastMaxIntensityBuffer = maxIntensityThisBuffer;
	        	}
	            
	            
	            //Reset the Values
	            blockSize = 0;
	            // time = System.currentTimeMillis();
	            maxLightMagnitude = Double.MIN_VALUE;
	            minLightMagnitude = Double.MAX_VALUE;
	            stdLightMagnitude = 0;
	            varianceIntensity = 0;
	            meanAbsoluteDeveationLightIntensity = 0;
	            meanLightIntensity = 0;
	            maxIntensityThisBuffer = -1;
	        }
	 } catch (Exception e) {
         e.printStackTrace();
     }
    
    
		/* Don't use these things anymore because we know that orientation is not useful!
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
        */
		return;
	}


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
	                  

        	    	  Toast.makeText(getApplicationContext(), "HELLO!", Toast.LENGTH_SHORT).show();
	                  
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
	                  
	                      //featureInstance.setValue(minAttribute,minLightMagnitude);
	                      featureInstance.setValue(maxAttribute,maxLightMagnitude);
	                      featureInstance.setValue(meanAttribute,meanLightIntensity);
	                      //featureInstance.setValue(stdAttribute,stdLightMagnitude);
	                      //featureInstance.setValue(meanAbsDeviationAttribute,meanAbsoluteDeveationLightIntensity);
	                      
	                      //Classifier -- Get the classified activity
	                      //WekaWrapper wrapper = new WekaWrapper();
	                      //double prediction = wrapper.classifyInstance(featureInstance);
	                      //String classification = featureInstance.classAttribute().value((int) prediction);
	                      
	                      
	                      //Reset the Values
	                      blockSize = 0;
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
	
	class UVIBroadcastReciever extends BroadcastReceiver {
		double mCurrentUVISun = 0.0;
		double mCurrentUVIShade = 0.0;
		
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			double currentUVISun = arg1.getExtras().getDouble(UltravioletIndexService.CURRENT_UV_INDEX_SUN);
			double currentUVIShade = arg1.getExtras().getDouble(UltravioletIndexService.CURRENT_UV_INDEX_SHADE);
			
			// Convert from mW/cm^2 to (J/s)/m^2
			currentUVISun = currentUVISun * 100*100 / 1000;
			currentUVIShade = currentUVIShade * 100*100 / 1000;
			
			mCurrentUVISun = currentUVISun;
			mCurrentUVIShade = currentUVIShade;
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

			// Application path.
    		String resultPredictor = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/predicted.txt";
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
						timeElapsed = TimeUnit.MILLISECONDS.toSeconds(bufferFillFinishTime) - TimeUnit.MILLISECONDS.toSeconds(bufferFillStartTime);
						
						// Calculate running total of UV Exposure
						
						if (environmentClassification.equals(Globals.CLASS_LABEL_IN_SUN))
							runningUVExposure += mUVReceiver.mCurrentUVISun * timeElapsed;
						else if (environmentClassification.equals(Globals.CLASS_LABEL_IN_SHADE))
							runningUVExposure += mUVReceiver.mCurrentUVIShade * timeElapsed;
						else
							runningUVExposure += 0.0;
						
						//System.out.println("Updating running total " + runningUVExposure);
						updateTask.setUVExposure(runningUVExposure);
						
						// Can either be positive or negative - used to correct the seconds difference.
						timeCorrectionMillis = ((bufferFillFinishTime%1000) - (bufferFillStartTime%1000));
						// Correct the time with the milli second component, so for ex: if 2 times are 2.1 and 0.9
						// Seconds difference would be 2. However the actual diff is 1.2 - which is adjusted by this component
						timeElapsed = timeElapsed + (timeCorrectionMillis/1000);						
						bufferFillStartTime = bufferFillFinishTime;
						blockSize = 0;
		
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
						
	                	// Finished collection the 5 samples
	                	// Now increase the weight of an activity based on the current dominant trend.
		                if(inferenceCount == mMaxActivityInferenceWindow)  {
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
		                	//Reset current lead count and the inference count.
		                	currentLeadCount = 0;
		                	inferenceCount = 0;
		                	
		                	// The window is done. Write to an output file a prediction indicating the current activity.
		                	
		                }
		                // Here specify the current trend.
		                // new code
						mInferredActivityType = Globals.INFERENCE_MAPPING[currentTrend == -1 ? value : currentTrend];//maxIndex];
						int currentActivity = Globals.INFERENCE_MAPPING[value];
						mMotionUpdateBroadcast.putExtra(Globals.CURRENT_MOTION_TYPE, currentActivity);
						int sweatRateIndex = GetSweatRateIndexForActivity(currentActivity);
						mMotionUpdateBroadcast.putExtra(Globals.CURRENT_SWEAT_RATE_INTERVAL,sweatRateIndex);
						updateTask.SetCurrentType(currentActivity);
						updateTask.SetSweatRateIndex(sweatRateIndex);
						// send broadcast with the CURRENT activity type
						//---------------
						
						//mMotionUpdateBroadcast.putExtra(Globals.CURR_UV_EXPOSURE, mUVReceiver.currentUVISun);
						 
						 // Updates the timer task class with the latest status
						 // This is required for the timer task implementor class to judicious determine the dominant activity as per the latest trend.
						 updateTask.SetFinalInferredActivityTypeMap(mFinalInferredActivityTypeMap);
						 updateTask.SetIntent(mMotionUpdateBroadcast);
						 updateTask.SetContext(getApplicationContext());
						 //Adding light value classification
						 int ardVal = Math.max(light0,light1);
						 mMotionUpdateBroadcast.putExtra(Globals.LIGHT_TYPE_HEADER, ardVal);
						 if(ardVal > 2000)
						 {
							 CUR_LIGHT_CONDITION_ARDUINO = Globals.CLASS_LABEL_IN_SUN;
			        			environmentClassification = Globals.CLASS_LABEL_IN_SUN;
						 }
						 else
						 {
							 CUR_LIGHT_CONDITION_ARDUINO = Globals.CLASS_LABEL_IN_SHADE;
							 environmentClassification = Globals.CLASS_LABEL_IN_SHADE;
						 }
						 mMotionUpdateBroadcast.putExtra(Globals.LIGHT_TYPE_HEADER_ARDUINO,environmentClassification);
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
				sweatRateIndex = 1;
				break;
			case Globals.ACTIVITY_TYPE_JOGGING :
				sweatRateIndex = 2;
				break;
			case Globals.ACTIVITY_TYPE_RUNNING:
				sweatRateIndex = 3;
				break;
				default:
					sweatRateIndex = 4;
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
	public static int mSweatRateIndex = 0;
	
	private double mUVExposure = 0.0;
		
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
	
	public void setUVExposure(double UVExposure) {
		mUVExposure = UVExposure;
	}
	
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
		   /*
		   if(mCallCount > mNoOfMaxCalls) {
			   System.out.println("XXXXXXXXXXXXXXXXXXXXXX");
			   return;
		   }
		   */
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
			mMotionUpdateBroadcast.putExtra(Globals.CURRENT_MOTION_TYPE, mCurrentType);
			// set the current sweat rate.
			mMotionUpdateBroadcast.putExtra(Globals.CURRENT_SWEAT_RATE_INTERVAL, mSweatRateIndex);
			// Set the final type.
			mMotionUpdateBroadcast.putExtra(Globals.VOTED_MOTION_TYPE, finalInferredType);
			Double sweatRateMeasure = 0.0;
			Double activityDuration = 0.0;
			double uvExposureMeasure = 1.0;
			if(finalInferredType == Globals.ACTIVITY_TYPE_STANDING) {
				// Get the activity duration in seconds. 				
				activityDuration = mActivityVsDurationMap.get(0);
				
				// Calculate the total amount of sweat lost.
				if(activityDuration != null ){
					sweatRateMeasure = (Globals.SWEAT_RATE_HOURLY_STANDING * activityDuration)/3600;
				}				
			} else if(finalInferredType == Globals.ACTIVITY_TYPE_WALKING) {
				// Get the activity duration in seconds. 
				activityDuration = mActivityVsDurationMap.get(1);
				if(activityDuration != null ){
					sweatRateMeasure = (Globals.SWEAT_RATE_HOURLY_WALKING * activityDuration)/3600;
				}
			}
			else if(finalInferredType == Globals.ACTIVITY_TYPE_JOGGING) {
				// Get the activity duration in seconds. 
				activityDuration = mActivityVsDurationMap.get(2);
				// Calculate the total amount of sweat lost.
				if(activityDuration != null ){
					sweatRateMeasure = (Globals.SWEAT_RATE_HOURLY_JOGGING * activityDuration)/3600;
				}
			} else if(finalInferredType == Globals.ACTIVITY_TYPE_RUNNING) {
				// Get the activity duration in seconds. 
				activityDuration = mActivityVsDurationMap.get(3);
				// Calculate the total amount of sweat lost.
				if(activityDuration != null ){
					sweatRateMeasure = (Globals.SWEAT_RATE_HOURLY_RUNNING * activityDuration)/3600;
				}
			}
			
			// set the final sweat rate.
			double sweatRateMeasureStr = Math.floor(sweatRateMeasure*100)/100;
			mMotionUpdateBroadcast.putExtra(Globals.FINAL_SWEAT_RATE_AVERAGE,sweatRateMeasureStr + " milli liters");
			mMotionUpdateBroadcast.putExtra(Globals.CURR_SWEAT_RATE_AVERAGE, sweatRateMeasure);
			
			uvExposureMeasure = mUVExposure;
			mMotionUpdateBroadcast.putExtra(Globals.CURR_UV_EXPOSURE, uvExposureMeasure);

			// Send the broad cast. It updates the UI.
			//mAppContext.sendBroadcast(mMotionUpdateBroadcast);
	   }	   
	
}


