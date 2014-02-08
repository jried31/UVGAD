package edu.dartmouth.cs.myruns5;

import java.util.List;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.util.Log;

/**
 * Base class for all USB-based sensor hardware.
 * 
 * @author daniel
 *
 */
public abstract class UsbSensor extends Sensor
{
	private static final String TAG = UsbSensor.Callback.class.getName();
	private static final String TAG_CALLBACK = UsbSensor.Callback.class.getName();
	
	protected final Context mContext;
	protected final UsbSensorManager mUsbSensorManager;
	
	protected final UsbDevice mUsbDevice;
	protected final List<UsbSerialDriver> mUsbDriver_list;
	
	protected final int mBaud;
	protected Callback mCallback;
	
	public abstract class Callback implements SerialInputOutputManager.Listener
	{
		public abstract void onDeviceEjected();
		
		@Override
		public void onRunError(Exception exception)
		{
		}
	}
	
	public UsbSensor(Context context, UsbSensorManager usbSensorManager, UsbDevice usbDevice, int baud)
	{
		this(context, usbSensorManager, usbDevice, baud, null);
	}
	
	public UsbSensor(Context context, UsbSensorManager usbSensorManager, UsbDevice usbDevice, int baud, Callback callback)
	{
		mContext = context;
		mUsbSensorManager = usbSensorManager;
		mCallback = callback;
		mBaud = baud;
		
		mUsbDevice = usbDevice;
		mUsbDriver_list = UsbSerialProber.probeSingleDevice(mUsbSensorManager.getUsbManager(), mUsbDevice);
	}
	
	public UsbDevice getDevice()
	{
		return(mUsbDevice);
	}
	
	public List<UsbSerialDriver> getUsbDriverList()
	{
		return(mUsbDriver_list);
	}
	
	public void setCallback(Callback callback)
	{
		mCallback = callback;
	}
	
	public Callback getCallback()
	{
		return(mCallback);
	}
	
	public void onEject()
	{
		Runnable deviceEjected_runnable = new Runnable()
		{
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				mCallback.onDeviceEjected();
			}
		};
		
		Thread deviceEjected_thread = new Thread(deviceEjected_runnable);
		deviceEjected_thread.run();
	}
}
