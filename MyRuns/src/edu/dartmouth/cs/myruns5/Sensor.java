package edu.dartmouth.cs.myruns5;

/**
 * Base class for all sensor hardware.
 * 
 * @author daniel
 *
 */
public abstract class Sensor
{
	protected static final int SENSOR_LIGHT = 1;
	protected static final int SENSOR_UV = 1 << 1;
	
	public static final boolean hasLightSensor(int sensors)
	{
		return((sensors & SENSOR_LIGHT) != 0 ? true : false);
	}
	
	public static final boolean hasUVSensor(int sensors)
	{
		return((sensors & SENSOR_UV) != 0 ? true : false);
	}
}
