package edu.dartmouth.cs.myruns5;

import java.io.UnsupportedEncodingException;
import java.util.List;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.widget.Toast;

public class UsbLightSensor extends UsbSensor implements ILightSensor
{
	private static final String ACTION_SENSOR_UPDATE = "edu.dartmouth.cs.myruns5.UsbLightSensor.action.SENSOR_UPDATE";
	
	private class UsbLightSensorCallback extends UsbSensor.Callback
	{
		private ILightSensor.Callback mCallback;
		
		UsbLightSensorCallback(ILightSensor.Callback callback)
		{
			mCallback = callback;
		}
		
		@Override
		public void onNewData(final byte data[])
		{
			// TODO: Parse the data from the Arduino and convert to integer
			
			try
			{
				Toast.makeText(mContext, new String(data, "UTF-8"), Toast.LENGTH_SHORT).show();
			}
			catch (UnsupportedEncodingException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
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
	
	private int mLight;
	
	public UsbLightSensor(Context context, UsbSensorManager usbSensorManager, UsbDevice usbDevice, int baud)
	{
		super(context, usbSensorManager, usbDevice, baud);
	}

	@Override
	public int getLuminosity()
	{
		return(mLight);
	}
	
	@Override
	public int register(ILightSensor.Callback callback)
	{
		setCallback(new UsbLightSensorCallback(callback));
		
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
