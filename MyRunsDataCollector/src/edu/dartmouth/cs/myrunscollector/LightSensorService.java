package edu.dartmouth.cs.myrunscollector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils.DataSource;
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
/**
 * LocationService.java
 * 
 * Created by Xiaochao Yang on Sep 11, 2011 4:50:19 PM
 * 
 */

public class LightSensorService extends Service implements SensorEventListener {

	private static final int mFeatLen = Globals.FEAT_NUMBER_FEATURES ;
	
	private File mWekaFeaturesFile;
	private SensorManager mSensorManager;
	private Sensor mLightSensor;
	private Sensor mGravitySensor;
	private Sensor mMagneticSensor;
	private int mServiceTaskType;
	private String mLabel;
	private Instances mDataset;
	private Attribute mClassNameForData,meanAttribute,stdAttribute,maxAttribute,minAttribute,meanAbsDeviationAttribute;
	private SensorDataProcessorAsyncTask mSensorDataProcessingTask;

	private static ArrayBlockingQueue<LumenDataPoint> mLightIntensityReadingBuffer;
	public static final DecimalFormat mdecimalformat = new DecimalFormat("#.##");

	@Override
	public void onCreate() {
		super.onCreate();
		mLightIntensityReadingBuffer = new ArrayBlockingQueue<LumenDataPoint>( Globals.LIGHT_BUFFER_CAPACITY);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		//Once Service starts, register the sensors
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
		mGravitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
		mMagneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		mSensorManager.registerListener(this, mLightSensor,SensorManager.SENSOR_DELAY_FASTEST);
    	mSensorManager.registerListener(this, mGravitySensor, SensorManager.SENSOR_DELAY_FASTEST);
    	mSensorManager.registerListener(this, mMagneticSensor, SensorManager.SENSOR_DELAY_FASTEST);

		Bundle extras = intent.getExtras();
		mLabel = extras.getString(Globals.CLASS_LABEL_KEY);

		//Create Weka features.arff file reference
		mWekaFeaturesFile = new File(getExternalFilesDir(null), Globals.FEATURE_LIGHT_FILE_NAME);
		Log.d(Globals.TAG, mWekaFeaturesFile.getAbsolutePath());

		mServiceTaskType = Globals.SERVICE_TASK_TYPE_COLLECT;

		//JERRID: SETUP THE ATTRIBUTES LIST FOR THE WEKA FILE
		// Create the container for attributes
		ArrayList<Attribute> attributeList = new ArrayList<Attribute>();

		// Adding FFT coefficient attributes
		/*DecimalFormat lightIntensityColNumFormat = new DecimalFormat("0000");

		//Add the attribute column in the weka file, which sets up the light intensity value columns and data types
		for (int i = 0; i < Globals.LIGHT_BLOCK_CAPACITY; i++) {
			attributeList.add(new Attribute(Globals.FEAT_INTENSITY_LABEL + lightIntensityColNumFormat.format(i)));
		}*/
		
		// Adding the min,max,mean,std,mean_absolute_deviation feature column in the file (since it's the last column
		minAttribute = new Attribute(Globals.FEAT_MIN_LABEL);
		maxAttribute = new Attribute(Globals.FEAT_MAX_LABEL);
		meanAttribute = new Attribute(Globals.FEAT_MEAN_LABEL);
		stdAttribute = new Attribute(Globals.FEAT_STD_LABEL);
		meanAbsDeviationAttribute = new Attribute(Globals.FEAT_MEAN_ABSOLUTE_DEVIATION_LABEL);
		
		attributeList.add(minAttribute);
		attributeList.add(maxAttribute);
		attributeList.add(meanAttribute);
		attributeList.add(stdAttribute);
		attributeList.add(meanAbsDeviationAttribute);

		
		// Declare a nominal attribute along with its candidate values
		ArrayList<String> labelItems = new ArrayList<String>(2);
		labelItems.add(Globals.CLASS_LABEL_IN_SHADE);
		labelItems.add(Globals.CLASS_LABEL_IN_SUN);
		labelItems.add(Globals.CLASS_LABEL_IN_CLOUD);
		labelItems.add(Globals.CLASS_LABEL_OTHER);
		mClassNameForData = new Attribute(Globals.CLASS_LABEL_KEY, labelItems);
		attributeList.add(mClassNameForData);
		//***********END ATTRIBUTE LIST***********
		
		// Construct the dataset with the attributes specified as allAttr and
		// capacity 10000
		mDataset = new Instances(Globals.FEAT_LIGHT_SET_NAME, attributeList, Globals.FEATURE_SET_CAPACITY);

		// Set the last column/attribute (standing/walking/running) as the class
		// index for classification
		mDataset.setClassIndex(mDataset.numAttributes() - 1);

		
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
		mSensorDataProcessingTask = new SensorDataProcessorAsyncTask();
		mSensorDataProcessingTask.execute();

		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		mSensorManager.unregisterListener(this);
		mSensorDataProcessingTask.cancel(false);
		Log.i("Collector App","onDestroy()");
		super.onDestroy();

	}

	private class SensorDataProcessorAsyncTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... arg0) {

			/*
			 * The training phase stores the min, max, mean, std, and mean absolute deviation of an intensity window
			 * for each of the 16 light intensity readings (m0..m14), and the label the user provided to the 
			 *  collector (see collector UI later). Collectively, we call these features the 
			 *  feature vector comprises: magnitudes (f0..f14), MAX magnitude, label....
			 */
			Instance featureInstance = new DenseInstance(mFeatLen);
			featureInstance.setDataset(mDataset);
			int blockSize = 0;
			
			double[] lightIntensityDataBlock = new double[Globals.LIGHT_BLOCK_CAPACITY];
			double maxLightMagnitude = Double.MIN_VALUE,
					minLightMagnitude = Double.MAX_VALUE,
					meanLightIntensity = 0,
					varianceIntensity = 0,
					stdLightMagnitude = 0,
					meanAbsoluteDeveationLightIntensity = 0;

			while (true) {
				try 
				{
					// need to check if the AsyncTask is cancelled or not in the while loop
					if (isCancelled () == true)
				    {
				        return null;
				    }
					
					// JERRID: Pops the "head" element from the Blocking Queue one at a time
					LumenDataPoint reading = mLightIntensityReadingBuffer.take();
					
					double intensityReading = reading.getIntensity();
						
					
					lightIntensityDataBlock[blockSize++] = intensityReading;
					
					//Calculate Mean Intensity Value
					if(blockSize <= 1)
						meanLightIntensity = intensityReading;
					else
						meanLightIntensity = (intensityReading + meanLightIntensity*(blockSize-1))/blockSize;
				
					
					//JERRID: Once 16 readings are found, identify the MIN, MAX, magnitude
					if (blockSize == Globals.LIGHT_BLOCK_CAPACITY) 
					{
						//Compute the Mean Absolute Deviation since we have a full buffer=
						for (double val : lightIntensityDataBlock) {
							//find mean absolute deviation
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
						featureInstance.setValue(mClassNameForData, mLabel);
						mDataset.add(featureInstance);
						
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

		@Override
		protected void onCancelled() {

			if (mServiceTaskType == Globals.SERVICE_TASK_TYPE_CLASSIFY) {
				super.onCancelled();
				return;
			}
			Log.i("in the loop","still in the loop cancelled");
			String toastDisp;

			if (mWekaFeaturesFile.exists()) {

				// merge existing and delete the old dataset
				DataSource source;
				try {
					// Create a datasource from mFeatureFile where
					// mFeatureFile = new File(getExternalFilesDir(null),
					// "features.arff");
					source = new DataSource(new FileInputStream(mWekaFeaturesFile));
					// Read the dataset set out of this datasource
					Instances oldDataset = source.getDataSet();
					oldDataset.setClassIndex(mDataset.numAttributes() - 1);
					// Sanity checking if the dataset format matches.
					if (!oldDataset.equalHeaders(mDataset)) {
						// Log.d(Globals.TAG,
						// oldDataset.equalHeadersMsg(mDataset));
						throw new Exception("The two datasets have different headers:\n");
					}

					// Move all items over manually
					for (int i = 0; i < mDataset.size(); i++) {
						oldDataset.add(mDataset.get(i));
					}

					mDataset = oldDataset;
					// Delete the existing old file.
					mWekaFeaturesFile.delete();
					Log.i("delete","delete the file");
				} catch (Exception e) {
					e.printStackTrace();
				}
				toastDisp = getString(R.string.ui_sensor_service_toast_success_file_updated);

			} else {
				toastDisp = getString(R.string.ui_sensor_service_toast_success_file_created);
			}
			Log.i("save","create saver here");
			// create new Arff file
			ArffSaver saver = new ArffSaver();
			// Set the data source of the file content
			saver.setInstances(mDataset);
			try {
				// Set the destination of the file.
				// mFeatureFile = new File(getExternalFilesDir(null),
				// "features.arff");
				saver.setFile(mWekaFeaturesFile);
				// Write into the file
				saver.writeBatch();
				Log.i("batch","write batch here");
				Toast.makeText(getApplicationContext(), toastDisp,
						Toast.LENGTH_SHORT).show();
			} catch (IOException e) {
				toastDisp = getString(R.string.ui_sensor_service_toast_error_file_saving_failed);
				e.printStackTrace();
			}
			
			Log.i("toast","toast here");
			super.onCancelled();
		}

	}

	float mGravity[]=null;
	float[] mGeomagnetic=null;
	float []pitchReading={0,0,0};;
	public void onSensorChanged(SensorEvent event) {

		if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
			//JERRID: Light Sensor Reading
			double lightReading = event.values[0];
			if (mGravity != null && mGeomagnetic != null && lightReading > 0)
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
		             

					// Inserts the specified element into this queue if it is possible
					// to do so immediately without violating capacity restrictions,
					// returning true upon success and throwing an IllegalStateException
					// if no space is currently available. When using a
					// capacity-restricted queue, it is generally preferable to use
					// offer.
					try {
						//JERRID: Add the magnitude reading to the buffer
						mLightIntensityReadingBuffer.add(new LumenDataPoint(System.currentTimeMillis(),pitchReading,lightReading));
						
						File dir = new File (android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/accelerometer");
						dir.mkdirs();
						FileWriter sunClassificationFile = new FileWriter(dir.getAbsolutePath()+"/"+Globals.LIGHT_INTENSITY_FILE_NAME, true);                       
						try {                  
							String out = (System.currentTimeMillis() + "\t" + event.values[0] + "\t" + pitchReading[0] +  "\t" + pitchReading[1] + "\t" + pitchReading[2] +"\n");         
							sunClassificationFile.append(out);           
						} catch (IOException ex){
							ex.printStackTrace();
						}
						finally{
							sunClassificationFile.flush();
							sunClassificationFile.close();
						}
					} catch (IllegalStateException e) {
						
						// Exception happens when reach the capacity.
						// Doubling the buffer. ListBlockingQueue has no such issue,
						// But generally has worse performance
						ArrayBlockingQueue<LumenDataPoint> newBuf = new ArrayBlockingQueue<LumenDataPoint>(mLightIntensityReadingBuffer.size() * 2);
		
						mLightIntensityReadingBuffer.drainTo(newBuf);
						mLightIntensityReadingBuffer = newBuf;
						mLightIntensityReadingBuffer.add(new LumenDataPoint(System.currentTimeMillis(),pitchReading,lightReading));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	              }
	          }
		}else if(event.sensor.getType() == android.hardware.Sensor.TYPE_GRAVITY){
			mGravity = event.values;
	    }else if (event.sensor.getType() == android.hardware.Sensor.TYPE_MAGNETIC_FIELD){
           mGeomagnetic = event.values;
	    }
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}