package edu.dartmouth.cs.myruns5; 

import android.content.Context;
import android.hardware.usb.UsbDevice;

public class UsbLightSensor extends UsbSensor implements ILightSensor
{
	protected int mLight;
	
	private class UsbLightSensorCallback implements UsbSensor.Callback
	{
		private ILightSensor.Callback mCallback;
		
		UsbLightSensorCallback(ILightSensor.Callback callback)
		{
			mCallback = callback;
		}
		
		@Override
		public void onNewData(Pulse32 pkt)
		{
			// Parse the data from the Arduino and convert to integer
			
			if(pkt != null)
			{
				if(mPulseId >= 0 && mPulseId <= Pulse32.PKT_MAX_PAYLOAD_ENTRIES)
				{
					mLight = pkt.getField(mPulseId) * 100000 / 1023;
				}
				
				if(mCallback != null)
				{
					mCallback.onSensorUpdate(mLight);
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
	
	public UsbLightSensor(Context context, UsbSensorManager usbSensorManager, UsbDevice usbDevice, int protocol)
	{
		super(context, usbSensorManager, usbDevice, protocol);
		
		mPulseId = -1;
	}
	
	@Override
	public UsbSensor.Callback baseCallback()
	{
		return(new UsbLightSensorCallback(null));
	}

	@Override
	public int getLuminosity()
	{
		if(!mIsInitialized)
		{
			return(0);
		}
		
		return(mLight);
	}
	
	@Override
	public int register(ILightSensor.Callback callback)
	{
		if(!mIsInitialized)
		{
			return(ErrorCode.ERR_STATE);
		}
		
		return(mUsbSensorManager.registerSensor(this, new UsbLightSensorCallback(callback)));
	}
	
	@Override
	public int unregister()
	{
		if(!mIsInitialized)
		{
			return(ErrorCode.ERR_STATE);
		}
		
		return(mUsbSensorManager.unregisterSensor(this));
	}
	
	@Override
	public void finalize()
	{
		unregister();
	}
}
