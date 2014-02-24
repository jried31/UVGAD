package edu.dartmouth.cs.myruns5;

import android.content.Context;
import edu.dartmouth.cs.myruns5.IUVSensor.Callback;

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
	
	public int getLuminosity();
	
	public int register(Callback callback);
	public int unregister();
}
