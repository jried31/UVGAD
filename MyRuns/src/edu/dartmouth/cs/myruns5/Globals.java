package edu.dartmouth.cs.myruns5;


//All the global constants are put here
public abstract class Globals {

	// Debugging tag for the whole project
	public static final String TAG = "MyRuns";
	
	// Const for distance/time conversion
	public static final double KM2MILE_RATIO = 1.609344;
	public static final double KILO = 1000;
	public static final int SECONDS_PER_HOUR = 3600;
	
	// Motion sensor buffering related consts 
	public static final int ACCELEROMETER_BUFFER_CAPACITY = 2048;
	public static final int ACCELEROMETER_BLOCK_CAPACITY = 64;
	
	//data collection parameters
	public static final long DATA_COLLECTOR_START_DELAY = 2 * Globals.ONE_SECOND;
	public static final long DATA_COLLECTOR_INTERVAL = 2 * Globals.ONE_SECOND;
	
	
	// Table schema, column names
	public static final String KEY_ROWID = "_id";
	public static final String KEY_INPUT_TYPE = "input_type";
	public static final String KEY_ACTIVITY_TYPE = "activity_type";
	public static final String KEY_DATE_TIME = "date_time";
	public static final String KEY_DURATION = "duration";
	public static final String KEY_DISTANCE = "distance";
	public static final String KEY_SWEATRATE = "sweatrate";
	public static final String KEY_UV_EXPOSURE = "cumulative_uv_exposure";
	public static final String KEY_VITAMIN_D = "cumulative_vitamin_d";
	public static final String KEY_AVG_PACE = "avg_pace";
	public static final String KEY_AVG_SPEED = "avg_speed";
	public static final String KEY_CALORIES = "calories";
	public static final String KEY_CLIMB = "climb";
	public static final String KEY_HEARTRATE = "heartrate";
	public static final String KEY_COMMENT = "comment";
	public static final String KEY_PRIVACY = "privacy";
	public static final String KEY_GPS_DATA = "gps_data";
	public static final String KEY_TRACK = "track";
	
	//Time Related Constants
	  // One second in milliseconds
	 public static final long ONE_SECOND = 1000;
	  // One minute in milliseconds
	  public static final long ONE_MINUTE = 60 * ONE_SECOND;
	  public static final long ONE_HOUR = 60 * ONE_MINUTE;
	  public static final long ONE_MILLISECOND=1;
	  public static final long ONE_NANOSECOND=ONE_MILLISECOND*1000000;
	  
	//--------------GPS----------------
	//Accuracy Settings
	// Values for recording_gps_accuracy
	public static final int RECORDING_GPS_ACCURACY_DEFAULT = 50;
	public static final int RECORDING_GPS_ACCURACY_EXCELLENT = 10;
	public static final int RECORDING_GPS_ACCURACY_POOR = 2000;
	
	//Recording Interval
	public static final float RECORDING_GPS_DISTANCE_DEFAULT = 1;
	public static final long RECORDING_GPS_INTERVAL_DEFAULT = 2*ONE_SECOND;
	//--------------
	
	//--------------Network Provider _----------
	public static final float RECORDING_NETWORK_PROVIDER_DISTANCE_DEFAULT = 5;
	public static final long RECORDING_NETWORK_PROVIDER_INTERVAL_DEFAULT = 2*ONE_SECOND;
	//-------------------
	
	//Weka Attributes
	//JERRID: ******************************
	public static final int FEAT_NUMBER_FEATURES=6;
	public static final int LIGHT_BUFFER_CAPACITY = 2048;
	public static final int LIGHT_BLOCK_CAPACITY = 256;
	

	public static final String ACTION_LIGHT_SENSOR_UPDATED = "MYRUNS_LIGHT_SENSOR_UPDATED";
	public static final String CLASS_LABEL_IN_SUN = "in_sun";
	public static final String CLASS_LABEL_IN_SHADE = "in_shade";
	public static final String CLASS_LABEL_IN_CLOUD = "in_cloud";

	
	//public static final String FEAT_INTENSITY_LABEL = "lumen_";
	public static final String FEAT_MEAN_ABSOLUTE_DEVIATION_LABEL = "mean_abs_dev";
	public static final String FEAT_MIN_LABEL = "min";
	public static final String FEAT_MEAN_LABEL = "mean";
	public static final String FEAT_MAX_LABEL = "max";
	public static final String FEAT_STD_LABEL = "std";

	public static final String FEAT_LIGHT_SET_NAME = "light_intensity_features";
	public static final String RAW_DATA_LIGHT_NAME = "raw_data_light.txt";
	public static final String FEATURE_LIGHT_FILE_NAME = "lightFeatures.arff";

	public static final String LIGHT_INTENSITY_FILE_NAME = "lightClassification.txt";
	//*************************************
	
	
	// All activity types in a string array.
	// "Standing" is not an exercise type... it's there for activity recognition result
	// There was a debate about using "Standing" or "Still". The authority decided using
	// "Standing"...
	public static final String[] ACTIVITY_TYPES = {"Running", "Cycling", "Walking", "Hiking", 
		"Downhill Skiing", "Cross-Country Skiing", "Snowboarding", "Skating", "Swimming", 
		"Mountain Biking", "Wheelchair", "Elliptical", "Other",  "Standing" , "Jogging" , "Unknown"};
	
	public static final String[] SWEAT_RATE_INTERVALS = { "Low(1-1.5 Liter/hour)","Medium(1.5-2 Liter/hour)", "High(2 - 3 Liter/hour)"};
	
	// Int encoded activity types
	public static final int ACTIVITY_TYPE_ERROR = -1;
	public static final int ACTIVITY_TYPE_RUNNING = 0;
	public static final int ACTIVITY_TYPE_CYCLING = 1;
	public static final int ACTIVITY_TYPE_WALKING = 2;
	public static final int ACTIVITY_TYPE_HIKING = 3;
	public static final int ACTIVITY_TYPE_DOWNHILL_SKIING = 4;
	public static final int ACTIVITY_TYPE_CROSS_COUNTRY_SKIING = 5;
	public static final int ACTIVITY_TYPE_SNOWBOARDING = 6;
	public static final int ACTIVITY_TYPE_SKATING = 7;
	public static final int ACTIVITY_TYPE_SWIMMING = 8;
	public static final int ACTIVITY_TYPE_MOUNTAIN_BIKING = 9;
	public static final int ACTIVITY_TYPE_WHEELCHAIR = 10;
	public static final int ACTIVITY_TYPE_ELLIPTICAL = 11;
	public static final int ACTIVITY_TYPE_OTHER = 12;
	public static final int ACTIVITY_TYPE_STANDING = 13; 
	public static final int ACTIVITY_TYPE_JOGGING = 14;
	public static final int ACTIVITY_TYPE_UNKNOWN = 15;
	
	
	// Consts for input types. 
	public static final int INPUT_TYPE_ERROR = -1;
	public static final int INPUT_TYPE_MANUAL = 0;
	public static final int INPUT_TYPE_GPS = 1;
	public static final int INPUT_TYPE_AUTOMATIC = 2;
	
	// Consts for task types
	public static final String KEY_TASK_TYPE = "TASK_TYPE";
	public static final int TASK_TYPE_ERROR = -1;
	public static final int TASK_TYPE_NEW = 1;
	public static final int TASK_TYPE_HISTORY = 2;
	
	// Stat titles 
	public static final String TYPE_STATS = "Type: ";
	public static final String AVG_SPEED_STATS = "Ave speed: ";
	public static final String CUR_SPEED_STATS = "Cur speed: ";
	public static final String CLIMB_STATS = "Climb: ";
	public static final String CALORIES_STATS = "Calories: ";
	public static final String DISTANCE_STATS = "Diatance: ";
	public static final String SWEAT_STATS = "Sweat Rate:";
	
	public static final int[] INFERENCE_MAPPING = {ACTIVITY_TYPE_STANDING, ACTIVITY_TYPE_WALKING, ACTIVITY_TYPE_JOGGING, ACTIVITY_TYPE_RUNNING};
	public static final String[] INFERENCE_LIST = {"Standing", "Walking" , "Jogging" , "Sprinting" ,"Other"};

	
	//COllector Variables------
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
	public static final String CLASS_LABEL_RUNNING = "running";
	public static final String CLASS_LABEL_OTHER = "others";
	
	public static final String FEAT_FFT_COEF_LABEL = "fft_coef_";
	public static final String FEAT_ACCELEROMETER_SET_NAME = "accelerometer_features";

	public static final String FEATURE_MOTION_FILE_NAME = "motionFeatures.arff";
	public static final String RAW_DATA_ACCELEROMETER_NAME = "raw_data_accelerometer.txt";
	public static final int FEATURE_SET_CAPACITY = 10000;
	
	public static final int NOTIFICATION_ID = 1;
	//---------------
	
}