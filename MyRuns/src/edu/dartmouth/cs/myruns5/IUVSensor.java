package edu.dartmouth.cs.myruns5;

/**
 * Basic interface for all general UV sensors.
 * 
 * @author daniel
 *
 */
public interface IUVSensor
{
	/**
	 * Callback object that is registered with this IUVSensor object
	 * 
	 * @author daniel
	 *
	 */
	public interface Callback
	{
		/**
		 * Callback function that is invoked when the sensor gets a new update value
		 * 
		 * @param updateUV The new raw UV sensor value.
		 */
		public void onSensorUpdate(final int updateUV);
		
		/**
		 * Callback function that is invoked when the sensor is forcibly ejected
		 */
		public void onSensorEjected();
	}
	
	/**
	 * Initialize the IUVSensor object and register it with a particular Pulse ID
	 * 
	 * @param pulseId The pulse ID that matches the pulse identifier in the Arduino sensor
	 * 
	 * @return ErrorCode.NO_ERROR on success.
	 */
	public int init(int pulseId);
	
	/**
	 * Uninitialize the IUVSensor object.  This should not be manually invoked as it is used 
	 * internally by the UsbSensorManager to uninitialize the sensor.
	 * 
	 * @return ErrorCode.NO_ERROR on success.
	 */
	public int uninit();
	
	/**
	 * Get the instantaneous UV sensor value.  The UV sensor must be initialized before invoking 
	 * this function.
	 * 
	 * @return The UV sensor's raw UV value.
	 */
	public int getUV();
	
	/**
	 * Register a UV sensor callback object that contains the code to run when the sensor 
	 * receives a particular event.  The UV sensor must be initialized before invoking this 
	 * function.
	 * 
	 * @param callback The UV sensor callback object to register.
	 * @return ErrorCode.NO_ERROR on success.
	 */
	public int register(Callback callback);
	
	/**
	 * Unregister the UV sensor callback object assigned to the IUVSensor object.
	 * 
	 * @return ErrorCode.NO_ERROR on success.
	 */
	public int unregister();
}
