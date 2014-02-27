package edu.dartmouth.cs.myruns5;

/**
 * Basic interface for all general light sensors.
 * 
 * @author daniel
 *
 */
public interface ILightSensor
{
	public interface Callback
	{
		public void onSensorUpdate(final int updateLux);
		public void onSensorEjected();
	}
	
	public int init(int pulseId);
	public int uninit();
	
	public int getLuminosity();
	
	public int register(Callback callback);
	public int unregister();
}
