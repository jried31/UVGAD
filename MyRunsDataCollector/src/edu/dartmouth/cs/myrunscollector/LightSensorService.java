package edu.dartmouth.cs.myrunscollector;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import edu.repo.ucla.serialusbdriver.Constants;
import edu.repo.ucla.serialusbdriver.ILightSensor;
import edu.repo.ucla.serialusbdriver.UsbSensorManager;
/**
 * LocationService.java
 * 
 * Created by Xiaochao Yang on Sep 11, 2011 4:50:19 PM
 * 
 */

public class LightSensorService extends Service implements SensorEventListener {

	//Classes for connecting to Arduino light sensor
	private ILightSensor mLightSensor0;
	private ILightSensor mLightSensor1;
	private LightSensor0Callback mLightSensor0Callback;
	private LightSensor1Callback mLightSensor1Callback;
	private UsbSensorManager mUsbSensorManager;	
	public static double  light0=0,light1=0,lightPhone=0;
	
	// This is the callback object for the first light sensor
	public class LightSensor0Callback implements ILightSensor.Callback
	{
		@Override
		public void onSensorUpdate(final int updateLux)
		{
			light0 = updateLux;
		}

		@Override
		public void onSensorEjected()
		{
			mLightSensor0.unregister();
		}
	}
	
	// This is the callback object for the second light sensor
	private class LightSensor1Callback implements ILightSensor.Callback
	{
		@Override
		public void onSensorUpdate(final int updateLux)
		{
			light1 = updateLux;
		}

		@Override
		public void onSensorEjected()
		{
			mLightSensor1.unregister();
		}
	}

		
	private static final int mFeatLen = Globals.FEAT_NUMBER_FEATURES;
	
	private File mWekaFeaturesFilePhone,mWekaFeaturesFileArduino;
	private SensorManager mSensorManager;
	private Sensor mLightSensor;
	private int mServiceTaskType;
	private String mLabel;
	private Instances mDatasetArdurino,mDatasetPhone;
	private Attribute mClassNameForData,
	maxAttribute;
	
	private SensorDataProcessorPhoneAsyncTask mSensorDataProcessingPhoneTask;
	private SensorDataProcessorArdurinoAsyncTask mSensorDataProcessingArdurinoTask;

	public static final DecimalFormat mdecimalformat = new DecimalFormat("#.##");

	@Override
	public void onCreate() {
		super.onCreate();
		mUsbSensorManager = UsbSensorManager.getManager();	
		
		// Create the sensor callback objects
		mLightSensor0Callback = new LightSensor0Callback();
		mLightSensor1Callback = new LightSensor1Callback();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		//Once Service starts, register the sensors
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
		
		mSensorManager.registerListener(this, mLightSensor,SensorManager.SENSOR_DELAY_FASTEST);
    	
    	//Initialize the custom UV Sensors
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

			mLightSensor0.register(mLightSensor0Callback);
			mLightSensor1.register(mLightSensor1Callback);
		}
    		
		Bundle extras = intent.getExtras();
		mLabel = extras.getString(Globals.CLASS_LABEL_KEY);

		//Create Weka features.arff file reference
		mWekaFeaturesFileArduino = new File(getExternalFilesDir(null), Globals.FEATURE_LIGHT_FILE_NAME_ARDUINO);
		mWekaFeaturesFilePhone = new File(getExternalFilesDir(null), Globals.FEATURE_LIGHT_FILE_NAME);
		Log.d(Globals.TAG, mWekaFeaturesFilePhone.getAbsolutePath());
		Log.d(Globals.TAG, mWekaFeaturesFileArduino.getAbsolutePath());

		mServiceTaskType = Globals.SERVICE_TASK_TYPE_COLLECT;

		//JERRID: SETUP THE ATTRIBUTES LIST FOR THE WEKA FILE
		// Create the container for attributes
		ArrayList<Attribute> attributeList = new ArrayList<Attribute>();

		// Adding the min,max,mean,std,mean_absolute_deviation feature column in the file (since it's the last column
		maxAttribute = new Attribute(Globals.FEAT_MAX_LABEL);
		attributeList.add(maxAttribute);

		
		// Declare a nominal attribute along with its candidate values
		ArrayList<String> labelItems = new ArrayList<String>(6);
		labelItems.add(Globals.CLASS_LABEL_IN_DOORS);
		labelItems.add(Globals.CLASS_LABEL_IN_SHADE);
		labelItems.add(Globals.CLASS_LABEL_IN_SUN);
		labelItems.add(Globals.CLASS_LABEL_IN_PARTIAL_CLOUD);
		labelItems.add(Globals.CLASS_LABEL_IN_CLOUD);
		labelItems.add(Globals.CLASS_LABEL_OTHER);
		mClassNameForData = new Attribute(Globals.CLASS_LABEL_KEY, labelItems);
		attributeList.add(mClassNameForData);
		//***********END ATTRIBUTE LIST***********
		
		// Construct the dataset with the attributes specified as allAttr and
		// capacity 10000
		mDatasetArdurino = new Instances(Globals.FEAT_LIGHT_SET_NAME, attributeList, Globals.FEATURE_SET_CAPACITY);
		mDatasetPhone = new Instances(Globals.FEAT_LIGHT_SET_NAME, attributeList, Globals.FEATURE_SET_CAPACITY);
		
		// Set the last column/attribute (standing/walking/running) as the class
		// index for classification
		mDatasetArdurino.setClassIndex(mDatasetArdurino.numAttributes() - 1);
		mDatasetPhone.setClassIndex(mDatasetPhone.numAttributes() - 1);
		
		Intent collectorActivity = new Intent(this, LightCollectorActivity.class);
		// Read:
		// http://developer.android.com/guide/topics/manifest/activity-element.html#lmode
		// IMPORTANT!. no re-create activity
		collectorActivity.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, collectorActivity, 0);

		Notification notification = new Notification.Builder(this)
				.setContentTitle(getApplicationContext().getString( R.string.ui_sensor_service_notification_title))
				.setContentText( getResources().getString(R.string.ui_sensor_service_notification_content))
				.setSmallIcon(R.drawable.greend).setContentIntent(pendingIntent).build();
		
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notification.flags = notification.flags | Notification.FLAG_ONGOING_EVENT;
		notificationManager.notify(0, notification);

		//JERRID: Executor tasks execute on a single thread. However, one can change it to execute on multiple threads
		mSensorDataProcessingPhoneTask = new SensorDataProcessorPhoneAsyncTask();
		mSensorDataProcessingPhoneTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		 
		if(Globals.FOUND_ARDUINO)
		{		
			mSensorDataProcessingArdurinoTask = new SensorDataProcessorArdurinoAsyncTask();
			mSensorDataProcessingArdurinoTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		mSensorManager.unregisterListener(this);
		mSensorDataProcessingPhoneTask.cancel(false);
		
		if(Globals.FOUND_ARDUINO){
			mSensorDataProcessingArdurinoTask.cancel(false);
			mLightSensor0.unregister();
			mLightSensor1.unregister();
		}
		Log.i("Collector App","onDestroy()");
		super.onDestroy();

	}

	
	private class SensorDataProcessorArdurinoAsyncTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... arg0) {

			/*
			 * The training phase stores the min, max, mean, std, and mean absolute deviation of an intensity window
			 * for each of the 16 light intensity readings (m0..m14), and the label the user provided to the 
			 *  collector (see collector UI later). Collectively, we call these features the 
			 *  feature vector comprises: magnitudes (f0..f14), MAX magnitude, label....
			 */
			Instance featureInstance = new DenseInstance(mFeatLen);
			int blockSize = 0;
			
			double maxLightIntensity = Double.MIN_VALUE,
					lightIntensityReading=0;

			//Create file if doesnt exist
			if(!mWekaFeaturesFileArduino.exists()){
				ArffSaver saver = new ArffSaver();
				// Set the data source of the file content
				saver.setInstances(mDatasetArdurino);
				try {
					saver.setFile(mWekaFeaturesFileArduino);
					// Write into the file
					saver.writeBatch();
					Log.i("batch","write batch here");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			while (true) {
				try 
				{
					// need to check if the AsyncTask is cancelled or not in the while loop
					if (isCancelled () == true)
				    {
				        return null;
				    }

					// JERRID: Pops the "head" element from the Blocking Queue one at a time
					lightIntensityReading = Math.max(light0, light1);
			        blockSize++;
			        
			        if(lightIntensityReading > maxLightIntensity)
			        	maxLightIntensity = lightIntensityReading;
				     
			        //JERRID: Once 16 readings are found, identify the MIN, MAX, magnitude
			        if (blockSize == Globals.LIGHT_BLOCK_CAPACITY_ARDURINO) 
			        {
						featureInstance.setValue(maxAttribute,maxLightIntensity);
						featureInstance.setValue(mClassNameForData, mLabel);
						featureInstance.setDataset(mDatasetArdurino);
						// Set the data source of the file content
						
						BufferedWriter writer = new BufferedWriter(new FileWriter(mWekaFeaturesFileArduino,true));
		        		writer.write(featureInstance.toString()+"\n");
		        		writer.flush();
		        		writer.close();
		        		
			        	//Reset the Values
			        	blockSize = 0;
			        	maxLightIntensity = Double.MIN_VALUE;
			        	lightIntensityReading = -1;
			        }
			     
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		protected void onCancelled() {
			if (mServiceTaskType == Globals.SERVICE_TASK_TYPE_CLASSIFY) {
				super.onCancelled();
				return;
			}
			super.onCancelled();
		}
	}
	
	private class SensorDataProcessorPhoneAsyncTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... arg0) {

			/*
			 * The training phase stores the min, max, mean, std, and mean absolute deviation of an intensity window
			 * for each of the 16 light intensity readings (m0..m14), and the label the user provided to the 
			 *  collector (see collector UI later). Collectively, we call these features the 
			 *  feature vector comprises: magnitudes (f0..f14), MAX magnitude, label....
			 */
			Instance featureInstance = new DenseInstance(mFeatLen);
			int blockSize = 0;
			
			double maxLightIntensity = Double.MIN_VALUE,
					lightIntensityReading=0;
			
			//Create file if doesnt exist
			if(!mWekaFeaturesFilePhone.exists()){
				ArffSaver saver = new ArffSaver();
				// Set the data source of the file content
				saver.setInstances(mDatasetPhone);
				try {
					// Set the destination of the file.
					saver.setFile(mWekaFeaturesFilePhone);
					// Write into the file
					saver.writeBatch();
					Log.i("batch","write batch here");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			while (true) {
				try 
				{
					// need to check if the AsyncTask is cancelled or not in the while loop
					if (isCancelled () == true)
				    {
				        return null;
				    }
					
					// JERRID: Pops the "head" element from the Blocking Queue one at a time
					lightIntensityReading = lightPhone;
			        //only want max, don't store buffer
			        blockSize++;
			        
			        if(lightIntensityReading > maxLightIntensity)
			        	maxLightIntensity = lightIntensityReading;
				     
			        //JERRID: Once 16 readings are found, identify the MIN, MAX, magnitude
			        if (blockSize == Globals.LIGHT_BLOCK_CAPACITY) 
			        {
						featureInstance.setValue(maxAttribute,maxLightIntensity);
						featureInstance.setValue(mClassNameForData, mLabel);
						featureInstance.setDataset(mDatasetPhone);
						
						BufferedWriter writer = new BufferedWriter(new FileWriter(mWekaFeaturesFilePhone,true));
		        		writer.write(featureInstance.toString()+"\n");
		        		writer.flush();
		        		writer.close();
						
			        	//Reset the Values
			        	blockSize = 0;
			        	maxLightIntensity = Double.MIN_VALUE;
			        	lightIntensityReading = -1;		
			        }
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		protected void onCancelled() {

			if (mServiceTaskType == Globals.SERVICE_TASK_TYPE_CLASSIFY) {
				super.onCancelled();
				return;
			}
			super.onCancelled();
		}

	}

	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
			//JERRID: Light Sensor Reading
	    	lightPhone = event.values[0];
        }
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}