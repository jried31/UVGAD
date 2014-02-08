package edu.dartmouth.cs.myruns5;

import android.app.Application;

public class MyRunsApplication extends Application
{
	protected static UsbSensorManager mUsbSensorManager;
	
	@Override
	public void onCreate()
	{
		mUsbSensorManager = new UsbSensorManager(this);
	}
	
	public static UsbSensorManager getUsbSensorManager()
	{
		return(mUsbSensorManager);
	}
}
