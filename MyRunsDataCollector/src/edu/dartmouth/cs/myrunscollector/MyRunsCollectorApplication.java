package edu.dartmouth.cs.myrunscollector;

import edu.repo.ucla.serialusbdriver.UsbSensorManager;
import android.app.Application;

public class MyRunsCollectorApplication extends Application
{
	@Override
	public void onCreate()
	{
		UsbSensorManager.init(getApplicationContext());
	}
}
