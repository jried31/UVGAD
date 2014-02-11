package edu.dartmouth.cs.myruns5;

import android.content.Context;
import android.hardware.usb.UsbDevice;

/**
 * Base class for all USB-based sensor hardware.
 * 
 * @author daniel
 *
 */
public abstract class UsbSensor extends Sensor
{
	private static final String TAG = UsbSensor.Callback.class.getName();
	
	protected final Context mContext;
	
	protected final UsbSensorManager mUsbSensorManager;
	protected final UsbDevice mUsbDevice;
	
	protected Callback mCallback;
	
	public interface Callback
	{
		public void onNewData(final byte data[], int length);
		public void onDeviceEjected();
	}
	
	public UsbSensor(Context context, UsbSensorManager usbSensorManager, UsbDevice usbDevice)
	{
		this(context, usbSensorManager, usbDevice, null);
	}
	
	public UsbSensor(Context context, UsbSensorManager usbSensorManager, UsbDevice usbDevice, 
			Callback callback)
	{
		mContext = context;
		
		mUsbSensorManager = usbSensorManager;
		mUsbDevice = usbDevice;
		mCallback = callback;
	}
	
	public UsbDevice getDevice()
	{
		return(mUsbDevice);
	}
	
	public Callback getCallback()
	{
		return(mCallback);
	}
}
