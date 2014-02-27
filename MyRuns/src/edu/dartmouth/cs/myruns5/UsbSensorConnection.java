package edu.dartmouth.cs.myruns5;

import java.util.Map;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;

public class UsbSensorConnection
{
	private final UsbSensorManager mSensorManager;
	private final SerialUsbDriver mSerialDriver;
	private final UsbDevice mUsbDevice;
	
	private UsbDeviceConnection mUsbDeviceConnection;
	
	private Map<UsbSensor, UsbSensor.Callback> mUsbSensorCallback_map;
	
	private UsbSensorRunnable mUsbSensorRunnable;
	private Thread mUsbSensorThread;
	
	UsbSensorConnection(UsbSensorManager sensorManager, SerialUsbDriver serialDriver, Map<UsbSensor, UsbSensor.Callback> usbSensorCallback_map)
	{
		mSensorManager = sensorManager;
		mSerialDriver = serialDriver;
		mUsbSensorCallback_map = usbSensorCallback_map;
		
		mUsbDevice = serialDriver.getDevice();
	}
	
	public UsbDevice getDevice()
	{
		return(mUsbDevice);
	}
	
	public int start()
	{
		int baud = mSensorManager.getDeviceBaudRate(mUsbDevice);
		
		if(baud <= 0)
		{
			return(ErrorCode.ERR_FAILED);
		}
		
		if(mSerialDriver.open(baud) != ErrorCode.NO_ERROR)
		{
			return(ErrorCode.ERR_FAILED);
		}
			
		mUsbSensorRunnable = new UsbSensorRunnable(mUsbDeviceConnection, mSerialDriver, 
												   mUsbSensorCallback_map);
		
		mUsbSensorThread = new Thread(mUsbSensorRunnable);
		mUsbSensorThread.start();
		
		while(!mUsbSensorRunnable.isConnectionActive())
		{
			// Poll until the thread is running
		}
		
		return(ErrorCode.NO_ERROR);
	}
	
	public int stop()
	{
		if(mUsbSensorRunnable == null || mUsbSensorThread == null)
		{
			return(ErrorCode.ERR_STATE);
		}
		
		if(mUsbSensorRunnable.isConnectionActive())
		{
			mUsbSensorRunnable.stop();
			mUsbSensorThread.interrupt();
			
			try
			{
				mUsbSensorThread.join();
			}
			catch(InterruptedException exception)
			{
				exception.printStackTrace();
			}
			
			mSerialDriver.close();
		}
		
		mUsbSensorRunnable = null;
		mUsbSensorThread = null;
		
		return(ErrorCode.NO_ERROR);
	}
	
	@Override
	public void finalize()
	{
		stop();
	}
}
