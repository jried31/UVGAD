package edu.dartmouth.cs.myruns5;

import edu.repo.ucla.serialusbdriver.UsbSensorManager;
import android.app.Application;

public class MyRunsApplication extends Application
{
	@Override
	public void onCreate()
	{
		UsbSensorManager.init(getApplicationContext());
	}
}
