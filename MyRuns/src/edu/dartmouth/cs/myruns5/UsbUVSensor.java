package edu.dartmouth.cs.myruns5;

import android.content.Context;
import android.hardware.usb.UsbDevice;

public class UsbUVSensor extends UsbSensor implements IUVSensor
{
	protected int mUV;
	
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
			
			if(pkt != null)
			{
				if(mPulseId >= 0 && mPulseId <= Pulse32.PKT_MAX_PAYLOAD_ENTRIES)
				{
					mUV = pkt.getField(mPulseId) - 309;
				}
				
				if(mCallback != null)
				{
					mCallback.onSensorUpdate(mUV);
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
	
	public UsbUVSensor(Context context, UsbSensorManager usbSensorManager, UsbDevice usbDevice, int protocol)
	{
		super(context, usbSensorManager, usbDevice, protocol);
		
		mPulseId = -1;
	}
	
	@Override
	public UsbSensor.Callback baseCallback()
	{
		return(new UsbUVSensorCallback(null));
	}

	@Override
	public int getUV()
	{
		if(!mIsInitialized)
		{
			return(0);
		}
		
		return(mUV);
	}
	
	@Override
	public int register(IUVSensor.Callback callback)
	{
		if(!mIsInitialized)
		{
			return(ErrorCode.ERR_STATE);
		}
		
		return(mUsbSensorManager.registerSensor(this, new UsbUVSensorCallback(callback)));
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
