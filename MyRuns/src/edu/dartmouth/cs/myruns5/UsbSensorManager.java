package edu.dartmouth.cs.myruns5;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import android.content.Context;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.widget.Toast;

public class UsbSensorManager
{
	private static final String TAG = UsbSensorManager.class.getName();
	
	private final Context mContext;
	private final UsbManager mUsbManager;
	private final UsbSensorDatabaseHelper mUsbSensorDatabaseHelper;
	private final UsbDeviceBroadcastReceiver mBroadcastReceiver;
	
	private final ConcurrentHashMap<UsbSensor, UsbSensorTask> mUsbSensor_map;
	
	private class UsbSensorTask
	{
		private ExecutorService mExecutor;
		private SerialInputOutputManager mIOMgr;
		
		boolean mIsRunning;
		
		public UsbSensorTask(UsbSerialDriver driver)
		{
			mIOMgr = new SerialInputOutputManager(driver);
			mIsRunning = false;
		}
		
		public UsbSensorTask(UsbSerialDriver driver, SerialInputOutputManager.Listener listener)
		{
			mIOMgr = new SerialInputOutputManager(driver, listener);
			mIsRunning = false;
		}
		
		public void start()
		{
			if(!mIsRunning)
			{
				mExecutor = Executors.newSingleThreadExecutor();
				mExecutor.submit(mIOMgr);
				
				mIsRunning = true;
			}
		}
		
		public void stop()
		{
			if(mIsRunning)
			{
				mIOMgr.stop();
				mIsRunning = false;
			}
		}
		
		@Override
		public void finalize()
		{
			stop();
		}
	}
	
	public UsbSensorManager(Context context)
	{
		mContext = context;
	    mBroadcastReceiver = new UsbDeviceBroadcastReceiver();
	    mUsbSensor_map = new ConcurrentHashMap<UsbSensor, UsbSensorTask>();
	    
		mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
		
		mUsbSensorDatabaseHelper = new UsbSensorDatabaseHelper(mContext, this);
		
		IntentFilter filter = new IntentFilter();
	    filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
	    filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
	    
	    mContext.registerReceiver(mBroadcastReceiver, filter);
	}
	
	public UsbManager getUsbManager()
	{
		return(mUsbManager);
	}
	
	public List<ILightSensor> getLightSensorList()
	{
		ArrayList<ILightSensor> sensorList = new ArrayList<ILightSensor>();
		Iterator<UsbDevice> deviceIterator = mUsbManager.getDeviceList().values().iterator();
		
		UsbDevice device;
		UsbLightSensor sensor;
		
		while(deviceIterator.hasNext())
		{
			device = deviceIterator.next();
			sensor = (UsbLightSensor) mUsbSensorDatabaseHelper.getLightSensor(device);
			
			if(sensor != null)
			{
				sensorList.add(sensor);
			}
		}
		
		return(sensorList);
	}
	
	public List<IUVSensor> getUVSensorList()
	{
		ArrayList<IUVSensor> sensorList = new ArrayList<IUVSensor>();
		Iterator<UsbDevice> deviceIterator = mUsbManager.getDeviceList().values().iterator();
		
		UsbDevice device;
		UsbUVSensor sensor;
		UsbSensorDatabaseHelper usbSensorDatabaseHelper = 
				new UsbSensorDatabaseHelper(mContext, this);
		
		while(deviceIterator.hasNext())
		{
			device = deviceIterator.next();
			
			sensor = (UsbUVSensor) usbSensorDatabaseHelper.getUVSensor(device);
			
			if(sensor != null)
			{
				sensorList.add(sensor);
			}
		}
		
		return(sensorList);
	}
	
	public int registerSensor(UsbSensor sensor)
	{
		if(sensor == null)
		{
			return(ErrorCode.ERR_PARAMS);
		}
		else if(mUsbSensor_map.containsKey(sensor))
		{
			return(ErrorCode.ERR_STATE);
		}
		
		List<UsbSerialDriver> driver_list = sensor.getUsbDriverList();
		
		if(driver_list == null || driver_list.isEmpty())
		{
			return(ErrorCode.ERR_RESOURCE);
		}
		
		UsbSensorTask task = new UsbSensorTask(driver_list.get(0), sensor.getCallback());
		
		task.start();
		mUsbSensor_map.put(sensor, task);
		
		return(ErrorCode.NO_ERROR);
	}
	
	public int unregisterSensor(UsbSensor sensor)
	{
		if(sensor == null)
		{
			return(ErrorCode.ERR_PARAMS);
		}
		else if(!mUsbSensor_map.containsKey(sensor))
		{
			return(ErrorCode.ERR_STATE);
		}
		
		UsbSensorTask task = mUsbSensor_map.get(sensor);
		
		task.stop();
		mUsbSensor_map.remove(sensor);
		
		return(ErrorCode.NO_ERROR);
	}
	
	public void onDeviceInserted(Context context, UsbDevice device)
	{
		Log.i(TAG, "USB sensor connected!");

		UsbSensorDatabaseHelper usbSensorDatabaseHelper = new UsbSensorDatabaseHelper(mContext, this);
		
		int baud = usbSensorDatabaseHelper.getDeviceBaudRate(device);
		
		if(baud == UsbSensorDatabaseHelper.ERROR_BAUD || baud <= 0)
		{
			// Use the default baud rate if a device-specific baud rate is not found
			baud = Constants.DFLT_BAUD_RATE;
		}
		
		UsbSerialDriver driver;
		List<UsbSerialDriver> usbDriver_list = UsbSerialProber.probeSingleDevice(mUsbManager, device);
		Iterator<UsbSerialDriver> usbDriver_listIter = usbDriver_list.iterator();
		
		while(usbDriver_listIter.hasNext())
		{
			driver = usbDriver_listIter.next();
			
			try
			{
				driver.open();
				driver.setParameters(baud, 8, UsbSerialDriver.STOPBITS_1, UsbSerialDriver.PARITY_NONE);
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	public void onDeviceEjected(Context context, UsbDevice device)
	{
		// TODO: Unregister all UsbSensor instances when its associated device is unplugged
		
		Log.i(TAG, "USB sensor disconnected!");
		
		Iterator<Entry<UsbSensor, UsbSensorTask>> usbSensor_mapIter 
				= mUsbSensor_map.entrySet().iterator();
		
		while(usbSensor_mapIter.hasNext())
		{
			Entry<UsbSensor, UsbSensorTask> usbSensor_entry = usbSensor_mapIter.next();
			UsbSensor sensor = usbSensor_entry.getKey();
			UsbSensorTask task = usbSensor_entry.getValue();
			
			if(sensor.getDevice().getDeviceId() == device.getDeviceId())
			{
				task.stop();
				sensor.onEject();
				
				usbSensor_mapIter.remove();
			}
		}
	}

	public int getDeviceBaudRate(int vendorId, int productId)
	{
		return(mUsbSensorDatabaseHelper.getDeviceBaudRate(vendorId, productId));
	}
	
	public int getDeviceBaudRate(UsbDevice device)
	{
		return(mUsbSensorDatabaseHelper.getDeviceBaudRate(device));
	}
	
	public ILightSensor getLightSensor(UsbDevice device)
	{
		return(mUsbSensorDatabaseHelper.getLightSensor(device));
	}
	
	public IUVSensor getUVSensor(UsbDevice device)
	{
		return(mUsbSensorDatabaseHelper.getUVSensor(device));
	}
	
	public boolean isValidSensor(int vendorId, int productId)
	{
		return(mUsbSensorDatabaseHelper.isValidSensor(vendorId, productId));
	}
	
	public boolean isValidSensor(UsbDevice device)
	{
		return(mUsbSensorDatabaseHelper.isValidSensor(device));
	}
	
	@Override
	public void finalize()
	{
		mContext.unregisterReceiver(mBroadcastReceiver);
	}
}
