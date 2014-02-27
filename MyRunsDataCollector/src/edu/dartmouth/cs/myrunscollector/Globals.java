/**
 * Globals.java
 * 
 * Created by Xiaochao Yang on Dec 9, 2011 1:43:35 PM
 * 
 */

package edu.dartmouth.cs.myrunscollector;


// More on class on constants:
// http://www.javapractices.com/topic/TopicAction.do?Id=2

public abstract class Globals {

	// Debugging tag
	public static final String TAG = "MyRuns";
	
	//JERRID: ******************************
	public static final int FEAT_NUMBER_FEATURES=3;
	public static final int LIGHT_BUFFER_CAPACITY = 2048;
	public static final int LIGHT_BLOCK_CAPACITY = 256;
	public static final String LIGHT_INTENSITY_FILE_NAME = "lightClassification.txt";
	

	public static final String ACTION_LIGHT_SENSOR_UPDATED = "MYRUNS_LIGHT_SENSOR_UPDATED";
	public static final String CLASS_LABEL_IN_SUN = "in_sun";
	public static final String CLASS_LABEL_IN_SHADE = "in_shade";
	public static final String CLASS_LABEL_IN_CLOUD = "in_cloud";

	
	//public static final String FEAT_INTENSITY_LABEL = "lumen_";
	public static final String FEAT_MEAN_ABSOLUTE_DEVIATION_LABEL = "mean_abs_dev";
	public static final String FEAT_MIN_LABEL = "min";
	public static final String FEAT_MEAN_LABEL = "mean";
	public static final String FEAT_STD_LABEL = "std";

	public static final String FEAT_LIGHT_SET_NAME = "light_intensity_features";
	public static final String RAW_DATA_LIGHT_NAME = "raw_data_light.txt";
	public static final String FEATURE_LIGHT_FILE_NAME = "lightFeatures.arff";
	
	//*************************************
	
	public static final int ACCELEROMETER_BUFFER_CAPACITY = 2048;
	public static final int ACCELEROMETER_BLOCK_CAPACITY = 64;
	
	public static final int ACTIVITY_ID_STANDING = 0;
	public static final int ACTIVITY_ID_WALKING = 1;
	public static final int ACTIVITY_ID_RUNNING = 2;
	public static final int ACTIVITY_ID_OTHER = 2;
	
	public static final int SERVICE_TASK_TYPE_COLLECT = 0;
	public static final int SERVICE_TASK_TYPE_CLASSIFY = 1;
	
	public static final String ACTION_MOTION_UPDATED = "MYRUNS_MOTION_UPDATED";
	
	public static final String CLASS_LABEL_KEY = "label";	
	public static final String CLASS_LABEL_STANDING = "standing";
	public static final String CLASS_LABEL_WALKING = "walking";
	public static final String CLASS_LABEL_JOGGING = "jogging";
	public static final String CLASS_LABEL_SPRINTING = "sprinting";
	public static final String CLASS_LABEL_OTHER = "others";
	
	public static final String FEAT_FFT_COEF_LABEL = "fft_coef_";
	public static final String FEAT_MAX_LABEL = "max";
	public static final String FEAT_ACCELEROMETER_SET_NAME = "accelerometer_features";

	public static final String FEATURE_MOTION_FILE_NAME = "motionFeatures.arff";
	public static final String RAW_DATA_ACCELEROMETER_NAME = "raw_data_accelerometer.txt";
	public static final int FEATURE_SET_CAPACITY = 10000;
	
	public static final int NOTIFICATION_ID = 1;
	
	
  
}
