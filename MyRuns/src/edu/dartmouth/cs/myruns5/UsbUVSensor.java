package edu.dartmouth.cs.myruns5;

import java.nio.ByteBuffer;
import java.util.List;

import com.hoho.android.usbserial.driver.UsbSerialDriver;

import android.content.Context;
import android.hardware.usb.UsbDevice;

public class UsbUVSensor extends UsbSensor implements IUVSensor
{
	private static final String ACTION_SENSOR_UPDATE = "edu.dartmouth.cs.myruns5.UsbUVSensor.action.SENSOR_UPDATE";
	
	private class UsbUVSensorCallback extends UsbSensor.Callback
	{
		private IUVSensor.Callback mCallback;
		
		UsbUVSensorCallback(IUVSensor.Callback callback)
		{
			mCallback = callback;
		}
		
		@Override
		public void onNewData(final byte data[])
		{
			// TODO: Parse the data from the Arduino and convert to integer
			
			if(mCallback != null)
			{
				mCallback.onSensorUpdate(0);
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
	
	public UsbUVSensor(Context context, UsbSensorManager usbSensorManager, UsbDevice usbDevice, int baud)
	{
		super(context, usbSensorManager, usbDevice, baud);
	}

	@Override
	public int getUV()
	{
		return(mUV);
	}
	
	@Override
	public int register(IUVSensor.Callback callback)
	{
		setCallback(new UsbUVSensorCallback(callback));
		
		return(mUsbSensorManager.registerSensor(this));
	}
	
	@Override
	public int unregister()
	{
		int ret = mUsbSensorManager.unregisterSensor(this);
		
		if(ret != ErrorCode.NO_ERROR)
		{
			return(ret);
		}
		
		setCallback(null);
		return(ErrorCode.NO_ERROR);
	}
	
	@Override
	public void finalize()
	{
		unregister();
	}
}
