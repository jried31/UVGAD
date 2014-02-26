/**
 * LocationService.java
 * 
 * Created by Xiaochao Yang on Sep 11, 2011 4:50:19 PM
 * 
 */

package edu.dartmouth.cs.myrunscollector;

import java.io.File;
import java.io.FileInputStream;
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

import com.meapsoft.FFT;

public class AccelerometerSensorService extends Service implements SensorEventListener {

	private static final int mFeatLen = Globals.ACCELEROMETER_BLOCK_CAPACITY + 2;
	
	private File mWekaFeaturesFile;
	private SensorManager mSensorManager;
	private Sensor mAccelerometer,mGravity;
	private int mServiceTaskType;
	private String mLabel;
	private Instances mDataset;
	private Attribute mClassNameForData;
	private SensorDataProcessorAsyncTask mSensorDataProcessingTask;

	private static ArrayBlockingQueue<Double> mAccelerometerMagnitudeBuffer;
	public static final DecimalFormat mdecimalformat = new DecimalFormat("#.##");

	@Override
	public void onCreate() {
		super.onCreate();

		mAccelerometerMagnitudeBuffer = new ArrayBlockingQueue<Double>(Globals.ACCELEROMETER_BUFFER_CAPACITY);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		//Once Service starts, register the sensors
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		mGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
		mSensorManager.registerListener(this, mAccelerometer,SensorManager.SENSOR_DELAY_FASTEST);

		Bundle extras = intent.getExtras();
		mLabel = extras.getString(Globals.CLASS_LABEL_KEY);

		//Create Weka features.arff file reference
		mWekaFeaturesFile = new File(getExternalFilesDir(null), Globals.FEATURE_MOTION_FILE_NAME);
		Log.d(Globals.TAG, mWekaFeaturesFile.getAbsolutePath());

		mServiceTaskType = Globals.SERVICE_TASK_TYPE_COLLECT;

		// Create the container for attributes
		ArrayList<Attribute> attributeList = new ArrayList<Attribute>();

		// Adding FFT coefficient attributes
		DecimalFormat fftCoefficientDecimalFormat = new DecimalFormat("0000");

		//Add the attribute column in the weka file, which sets up the columns and data types
		for (int i = 0; i < Globals.ACCELEROMETER_BLOCK_CAPACITY; i++) {
			attributeList.add(new Attribute(Globals.FEAT_FFT_COEF_LABEL + fftCoefficientDecimalFormat.format(i)));
		}
		// Adding the max feature column in the file (since it's the last column
		attributeList.add(new Attribute(Globals.FEAT_MAX_LABEL));

		// Declare a nominal attribute along with its candidate values
		ArrayList<String> labelItems = new ArrayList<String>(3);
		labelItems.add(Globals.CLASS_LABEL_STANDING);
		labelItems.add(Globals.CLASS_LABEL_WALKING);
		labelItems.add(Globals.CLASS_LABEL_JOGGING);
		labelItems.add(Globals.CLASS_LABEL_SPRINTING);
		labelItems.add(Globals.CLASS_LABEL_OTHER);
		mClassNameForData = new Attribute(Globals.CLASS_LABEL_KEY, labelItems);
		attributeList.add(mClassNameForData);

		// Construct the dataset with the attributes specified as allAttr and
		// capacity 10000
		mDataset = new Instances(Globals.FEAT_ACCELEROMETER_SET_NAME, attributeList, Globals.FEATURE_SET_CAPACITY);

		// Set the last column/attribute (standing/walking/running) as the class
		// index for classification
		mDataset.setClassIndex(mDataset.numAttributes() - 1);

		Intent collectorActivity = new Intent(this, AccelerometerCollectorActivity.class);
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
			 * The training phase stores the FFT magnitude (m0..m63) for each of the 64 X,Y,Z accelerometer readings,
			 *  the maximum (MAX) FFT magnitude seen from the 64 FFT readings, and the label the user provided to the 
			 *  collector (see collector UI later). The individual features that are computed magnitudes (f0..f63) and 
			 *  MAX magnitude in addition to the label that the user supplies. Collectively, we call these features the 
			 *  feature vector comprises: magnitudes (f0..f63), MAX magnitude, label.
			 */
			Instance featureInstance = new DenseInstance(mFeatLen);
			featureInstance.setDataset(mDataset);
			int blockSize = 0;
			FFT fft = new FFT(Globals.ACCELEROMETER_BLOCK_CAPACITY);
			double[] accelerometerMagnitudeDataBlock = new double[Globals.ACCELEROMETER_BLOCK_CAPACITY];
			double[] realComponent = accelerometerMagnitudeDataBlock;
			double[] imaginaryComponent = new double[Globals.ACCELEROMETER_BLOCK_CAPACITY];

			double maxAccelerometerMagnitude = Double.MIN_VALUE;

			while (true) {
				try {
					// need to check if the AsyncTask is cancelled or not in the while loop
					if (isCancelled () == true)
				    {
				        return null;
				    }
					
					// JERRID: Pops the "head" element from the Blocking Queue one at a time
					double value = mAccelerometerMagnitudeBuffer.take().doubleValue();
					accelerometerMagnitudeDataBlock[blockSize++] = value;

					if(value > maxAccelerometerMagnitude)
						maxAccelerometerMagnitude = value;
					
					//JERRID: Once 64 readings are found, identify the MAX magnitude
					if (blockSize == Globals.ACCELEROMETER_BLOCK_CAPACITY) {
						blockSize = 0;

						// time = System.currentTimeMillis();
						/* Old Code
						 * maxAccelerometerMagnitude = 0.0;
						 
						for (double val : accelerometerMagnitudeDataBlock) {
							if (maxAccelerometerMagnitude < val) {
								maxAccelerometerMagnitude = val;
							}
						}
						*/
						//JERRID: Perform the FFT to get the real & imaginary component
						//NOTE: We only care about the real component, not imaginary
						fft.fft(realComponent, imaginaryComponent);

						for (int i = 0; i < realComponent.length; i++) {
							double magnitudeOfFFT = Math.sqrt(realComponent[i] * realComponent[i] + imaginaryComponent[i] * imaginaryComponent[i]);
							featureInstance.setValue(i, magnitudeOfFFT);
							imaginaryComponent[i] = 0.0; // Clear the field
						}

						// Append max after frequency component (JERRID: Max magnitude is stored part of weka)
						featureInstance.setValue(Globals.ACCELEROMETER_BLOCK_CAPACITY, maxAccelerometerMagnitude);
						featureInstance.setValue(mClassNameForData, mLabel);
						mDataset.add(featureInstance);
						
						//Reset the magnitude
						maxAccelerometerMagnitude = Double.MIN_VALUE;
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
			Log.i("AccelerometerService - onCanceled","onCancelled Invoked");
			String toastDisp;

			if (mWekaFeaturesFile.exists()) 
			{
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
					// Delete old the existing old file.
					mWekaFeaturesFile.delete();
					Log.i("AccelerometerService onCancelled()","Deleting old file");
				} catch (Exception e) {
					e.printStackTrace();
				}
				toastDisp = getString(R.string.ui_sensor_service_toast_success_file_updated);

			} else {
				toastDisp = getString(R.string.ui_sensor_service_toast_success_file_created);
			}
			Log.i("AccelerometerService - onCancelled()","About to Save ARFF file");
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
				Log.i("AccelerometerService - onCancelled()"," Saved The File. Showing Toast");
				Toast.makeText(getApplicationContext(), toastDisp, Toast.LENGTH_SHORT).show();
			} catch (IOException e) {
				toastDisp = getString(R.string.ui_sensor_service_toast_error_file_saving_failed);
				e.printStackTrace();
			}
			super.onCancelled();
		}

	}

	float []gravity;
	public void onSensorChanged(SensorEvent event) {

		if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) 
		{
			//JERRID: Take the magnitude of the acceleration
			double m = Math.sqrt(event.values[0] * event.values[0] + event.values[1] * event.values[1] + event.values[2] * event.values[2]);

			// Inserts the specified element into this queue if it is possible
			// to do so immediately without violating capacity restrictions,
			// returning true upon success and throwing an IllegalStateException
			// if no space is currently available. When using a
			// capacity-restricted queue, it is generally preferable to use
			// offer.

			try {
				//JERRID: Add the magnitude reading to the buffer
				mAccelerometerMagnitudeBuffer.add(m);
			} catch (IllegalStateException e) {
				// Exception happens when reach the capacity.
				// Doubling the buffer. ListBlockingQueue has no such issue,
				// But generally has worse performance
				ArrayBlockingQueue<Double> newBuf = new ArrayBlockingQueue<Double>( mAccelerometerMagnitudeBuffer.size() * 2);

				mAccelerometerMagnitudeBuffer.drainTo(newBuf);
				mAccelerometerMagnitudeBuffer = newBuf;
				mAccelerometerMagnitudeBuffer.add(m);
			}
		}else if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
			gravity = event.values;
		}
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
