package edu.dartmouth.cs.myruns5;

import android.content.Context;
import android.hardware.usb.UsbDevice;

public class UsbUVSensor extends UsbSensor implements IUVSensor
{
	private static final String ACTION_SENSOR_UPDATE = "edu.dartmouth.cs.myruns5.UsbUVSensor.action.SENSOR_UPDATE";
	
	private class UsbUVSensorCallback implements UsbSensor.Callback
	{
		private IUVSensor.Callback mCallback;
		
		UsbUVSensorCallback(IUVSensor.Callback callback)
		{
			mCallback = callback;
		}
		
		@Override
		public void onNewData(Pulse32 pkt)
		{
			// Parse the data from the Arduino and convert to integer
			
			if(pkt != null && pkt.getLux() != null)
			{
				Integer uv = pkt.getUV();
				
				if(uv != null)
				{
					mUV = uv;
					
					if(mCallback != null)
					{
						mCallback.onSensorUpdate(mUV);
					}
				}
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
	
	private int mUV;
	
	public UsbUVSensor(Context context, UsbSensorManager usbSensorManager, UsbDevice usbDevice)
	{
		super(context, usbSensorManager, usbDevice);
	}

	@Override
	public int getUV()
	{
		return(mUV);
	}
	
	@Override
	public int register(IUVSensor.Callback callback)
	{
		return(mUsbSensorManager.registerSensor(this, new UsbUVSensorCallback(callback)));
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
