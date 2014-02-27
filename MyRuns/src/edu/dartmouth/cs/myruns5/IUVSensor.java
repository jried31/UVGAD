package edu.dartmouth.cs.myruns5;

/**
 * Basic interface for all general UV sensors.
 * 
 * @author daniel
 *
 */
public interface IUVSensor
{
	public interface Callback
	{
		public void onSensorUpdate(final int updateUV);
		public void onSensorEjected();
	}
	
	public int init(int pulseId);
	public int uninit();
	
	public int getUV();

	public int register(Callback callback);
	public int unregister();
}
