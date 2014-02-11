package edu.dartmouth.cs.myruns5; 

import android.content.Context;
import android.hardware.usb.UsbDevice;

public class UsbLightSensor extends UsbSensor implements ILightSensor
{
	private static final String ACTION_SENSOR_UPDATE = "edu.dartmouth.cs.myruns5.UsbLightSensor.action.SENSOR_UPDATE";
	
	private class UsbLightSensorCallback implements UsbSensor.Callback
	{
		private ILightSensor.Callback mCallback;
		
		UsbLightSensorCallback(ILightSensor.Callback callback)
		{
			mCallback = callback;
		}
		
		@Override
		public void onNewData(final byte data[], int length)
		{
			// TODO: Parse the data from the Arduino and convert to integer
			
			if(mCallback != null)
			{
				mCallback.onSensorUpdate(data, length);
				// mCallback.onSensorUpdate(0);
			}
		}

		@Override
		public void onDeviceEjected()
		{
			if(mCallback != null)
			{
				mCallback.onSensorEjected();
			}
		}
	}
	
	private int mLight;
	
	public UsbLightSensor(Context context, UsbSensorManager usbSensorManager, UsbDevice usbDevice)
	{
		super(context, usbSensorManager, usbDevice);
	}

	@Override
	public int getLuminosity()
	{
		return(mLight);
	}
	
	@Override
	public int register(ILightSensor.Callback callback)
	{
		return(mUsbSensorManager.registerSensor(this, new UsbLightSensorCallback(callback)));
	}
	
	@Override
	public int unregister()
	{
		return(mUsbSensorManager.unregisterSensor(this));
	}
	
	@Override
	public void finalize()
	{
		unregister();
	}
}
