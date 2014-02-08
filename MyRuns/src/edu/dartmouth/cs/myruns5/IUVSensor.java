package edu.dartmouth.cs.myruns5;

import android.content.Context;

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
		public void onSensorUpdate(int updateUV);
		public void onSensorEjected();
	}
	
	public int getUV();

	public int register(Callback callback);
	public int unregister();
}
