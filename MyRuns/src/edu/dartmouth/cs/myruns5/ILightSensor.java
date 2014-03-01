package edu.dartmouth.cs.myruns5;

/**
 * Basic interface for all general light sensors.
 * 
 * @author daniel
 *
 */
public interface ILightSensor
{
	/**
	 * Callback object that is registered with this ILightSensor object
	 * 
	 * @author daniel
	 *
	 */
	public interface Callback
	{
		/**
		 * Callback function that is invoked when the sensor gets a new update value
		 * 
		 * @param updateLux The light sensor luminosity value
		 */
		public void onSensorUpdate(final int updateLux);
		
		/**
		 * Callback function that is invoked when the sensor is forcibly ejected
		 */
		public void onSensorEjected();
	}
	
	/**
	 * Initialize the ILightSensor object and register it with a particular Pulse ID
	 * 
	 * @param pulseId The pulse ID that matches the pulse identifier in the Arduino sensor
	 * 
	 * @return ErrorCode.NO_ERROR on success.
	 */
	public int init(int pulseId);
	
	/**
	 * Uninitialize the ILightSensor object.  This should not be manually invoked as it is used 
	 * internally by the UsbSensorManager to uninitialize the sensor.
	 * 
	 * @return ErrorCode.NO_ERROR on success.
	 */
	public int uninit();
	
	/**
	 * Get the instantaneous light sensor value.  The light sensor must be initialized before 
	 * invoking this function.
	 * 
	 * @return The light sensor luminosity value.
	 */
	public int getLuminosity();
	
	/**
	 * Register a light sensor callback object that contains the code to run when the sensor 
	 * receives a particular event.  The light sensor must be initialized before invoking this 
	 * function.
	 * 
	 * @param callback The light sensor callback object to register.
	 * @return ErrorCode.NO_ERROR on success.
	 */
	public int register(Callback callback);
	
	/**
	 * Unregister the light sensor callback object assigned to the ILightSensor object.
	 * 
	 * @return ErrorCode.NO_ERROR on success.
	 */
	public int unregister();
}
