package edu.repo.ucla.serialusbdriver;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

public class UsbSensorManager
{
	private static final String TAG = UsbSensorManager.class.getName();
	
	private static UsbSensorManager mUsbSensorManager;
	
	private final Context mContext;
	private final UsbManager mUsbManager;
	private final UsbSensorAssistant mUsbSensorAssistant;
	private final UsbDeviceBroadcastReceiver mBroadcastReceiver;
	
	private final ConcurrentHashMap<UsbDevice, UsbSensorTask> mUsbSensorTask_map;
	
	private class UsbSensorTask
	{
		private final UsbDevice mUsbDevice;
		private final ConcurrentHashMap<UsbSensor, UsbSensor.Callback> mCallback_map;
		
		private volatile UsbSensorConnection mUsbSensorConnection;
		private SerialUsbDriver mSerialDriver;
		
		private UsbSensorTask(UsbDevice usbDevice)
		{
			mUsbDevice = usbDevice;
			
			mCallback_map = new ConcurrentHashMap<UsbSensor, UsbSensor.Callback>();
		}
		
		private int register(UsbSensor sensor, UsbSensor.Callback callback)
		{
			if(sensor == null || callback == null || sensor.getDevice() != mUsbDevice)
			{
				return(ErrorCode.ERR_PARAMS);
			}
			else if(!sensor.isInitialized() || mUsbSensorConnection == null || mCallback_map == null)
			{
				return(ErrorCode.ERR_STATE);
			}
			
			synchronized(mCallback_map)
			{
				synchronized(sensor)
				{
					if(mCallback_map.containsKey(sensor) && 
					   mCallback_map.get(sensor).equals(callback))
					{
						return(ErrorCode.ERR_STATE);
					}
					
					mCallback_map.put(sensor, callback);
					
					if(!sensor.isCallbackThreadRunning())
					{
						sensor.startCallbackThread(callback);
					}
					else
					{
						sensor.setRunnableCallback(callback);
					}
				}
			}
			
			return(ErrorCode.NO_ERROR);
		}
		
		private int unregister(UsbSensor sensor)
		{
			if(sensor == null || sensor.getDevice() != mUsbDevice)
			{
				return(ErrorCode.ERR_PARAMS);
			}
			else if(!sensor.isInitialized() || mUsbSensorConnection == null || mCallback_map == null)
			{
				return(ErrorCode.ERR_STATE);
			}
			
			synchronized(mCallback_map)
			{
				synchronized(sensor)
				{
					if(!mCallback_map.containsKey(sensor))
					{
						return(ErrorCode.ERR_STATE);
					}
					
					mCallback_map.put(sensor, sensor.getBaseCallback());
					sensor.setRunnableCallback(sensor.getBaseCallback());
				}
			}
			
			return(ErrorCode.NO_ERROR);
		}
		
		private synchronized void onDeviceEjected()
		{
			if(stopTask(false) != ErrorCode.NO_ERROR)
			{
				return;
			}
			
			UsbSensor sensor;
			Iterator<Entry<UsbSensor, UsbSensor.Callback>> sensorCallback_iter = 
					mCallback_map.entrySet().iterator();
			
			while(sensorCallback_iter.hasNext())
			{
				sensor = sensorCallback_iter.next().getKey();
				
				sensor.notifySensorEjected();
				sensor.uninit();
			}
			
			mCallback_map.clear();
		}
		
		private synchronized int startTask(SerialUsbDriver serialDriver)
		{
			if(serialDriver == null)
			{
				return(ErrorCode.ERR_PARAMS);
			}
			else if(mUsbSensorConnection != null)
			{
				return(ErrorCode.ERR_STATE);
			}
			
			mSerialDriver = serialDriver;
			
			mUsbSensorConnection = new UsbSensorConnection(UsbSensorManager.this, mSerialDriver, mCallback_map);
			mUsbSensorConnection.start();
			
			return(ErrorCode.NO_ERROR);
		}
		
		private int stopTask(boolean clearCallbackMap)
		{
			if(mUsbSensorConnection == null)
			{
				return(ErrorCode.ERR_STATE);
			}
			
			mUsbSensorConnection.stop();
			
			mUsbSensorConnection = null;
			mSerialDriver = null;
			
			if(clearCallbackMap)
			{
				mCallback_map.clear();
			}
			
			return(ErrorCode.NO_ERROR);
		}
		
		private boolean isTaskRunning()
		{
			return(mUsbSensorConnection != null ? true : false);
		}
		
		@Override
		public void finalize()
		{
			onDeviceEjected();
		}
	}
	
	protected UsbSensorManager(Context context)
	{
		mContext = context;
	    mBroadcastReceiver = new UsbDeviceBroadcastReceiver();
	    mUsbSensorTask_map = new ConcurrentHashMap<UsbDevice, UsbSensorTask>();
	    
		mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
		mUsbSensorAssistant = new UsbSensorAssistant(mContext, this);
		
		UsbDevice device;
		Iterator<UsbDevice> deviceIterator = mUsbManager.getDeviceList().values().iterator();
		
		while(deviceIterator.hasNext())
		{
			device = deviceIterator.next();
			mUsbSensorTask_map.put(device, new UsbSensorTask(device));
		}
		
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
		Iterator<Entry<UsbDevice, UsbSensorTask>> deviceIterator = 
				mUsbSensorTask_map.entrySet().iterator();
		
		UsbDevice device;
		UsbLightSensor sensor;
		
		while(deviceIterator.hasNext())
		{
			device = deviceIterator.next().getKey();
			sensor = (UsbLightSensor) mUsbSensorAssistant.getLightSensor(device);
			
			if(sensor != null)
			{
				sensorList.add(sensor);
			}
		}
		
		return(sensorList);
	}
	
	public List<ILightSensor> getLightSensorList(int vendorId, int productId)
	{
		ArrayList<ILightSensor> sensorList = new ArrayList<ILightSensor>();
		Iterator<Entry<UsbDevice, UsbSensorTask>> deviceIterator = 
				mUsbSensorTask_map.entrySet().iterator();
		
		UsbDevice device;
		UsbLightSensor sensor;
		
		while(deviceIterator.hasNext())
		{
			device = deviceIterator.next().getKey();
			
			if(device.getVendorId() == vendorId && device.getProductId() == productId)
			{
				sensor = (UsbLightSensor) mUsbSensorAssistant.getLightSensor(device);
				
				if(sensor != null)
				{
					sensorList.add(sensor);
				}
			}
		}
		
		return(sensorList);
	}
	
	public List<IUVSensor> getUVSensorList()
	{
		ArrayList<IUVSensor> sensorList = new ArrayList<IUVSensor>();
		Iterator<Entry<UsbDevice, UsbSensorTask>> deviceIterator = 
				mUsbSensorTask_map.entrySet().iterator();
		
		UsbDevice device;
		UsbUVSensor sensor;
		
		while(deviceIterator.hasNext())
		{
			device = deviceIterator.next().getKey();
			sensor = (UsbUVSensor) mUsbSensorAssistant.getUVSensor(device);
			
			if(sensor != null)
			{
				sensorList.add(sensor);
			}
		}
		
		return(sensorList);
	}
	
	public List<IUVSensor> getUVSensorList(int vendorId, int productId)
	{
		ArrayList<IUVSensor> sensorList = new ArrayList<IUVSensor>();
		Iterator<Entry<UsbDevice, UsbSensorTask>> deviceIterator = 
				mUsbSensorTask_map.entrySet().iterator();
		
		UsbDevice device;
		UsbUVSensor sensor;
		
		while(deviceIterator.hasNext())
		{
			device = deviceIterator.next().getKey();
			
			if(device.getVendorId() == vendorId && device.getProductId() == productId)
			{
				sensor = (UsbUVSensor) mUsbSensorAssistant.getUVSensor(device);
				
				if(sensor != null)
				{
					sensorList.add(sensor);
				}
			}
		}
		
		return(sensorList);
	}
	
	public synchronized int initSensor(UsbSensor sensor)
	{
		if(sensor == null)
		{
			return(ErrorCode.ERR_PARAMS);
		}
		else if(!sensor.isInitialized())
		{
			return(ErrorCode.ERR_STATE);
		}
		
		UsbSensorTask sensorTask = mUsbSensorTask_map.get(sensor.getDevice());
		
		if(!sensorTask.isTaskRunning())
		{
			if(sensorTask.startTask(sensor.getSerialDriver()) != ErrorCode.NO_ERROR)
			{
				return(ErrorCode.ERR_FAILED);
			}
		}
		
		return(mUsbSensorTask_map.get(sensor.getDevice()).register(sensor, sensor.getBaseCallback()));
	}
	
	public synchronized int uninitSensor(UsbSensor sensor)
	{
		if(sensor == null)
		{
			return(ErrorCode.ERR_PARAMS);
		}
		else if(!sensor.isInitialized())
		{
			return(ErrorCode.ERR_STATE);
		}
		
		UsbSensorTask sensorTask = mUsbSensorTask_map.get(sensor.getDevice());
		
		if(sensorTask != null)
		{
			synchronized(sensorTask.mCallback_map)
			{
				sensorTask.mCallback_map.remove(sensor);
			}
			
			if(sensorTask.mCallback_map.isEmpty())
			{
				return(mUsbSensorTask_map.get(sensor.getDevice()).stopTask(true));
			}
		}

		return(ErrorCode.NO_ERROR);
	}
	
	public synchronized int registerSensor(UsbSensor sensor, UsbSensor.Callback callback)
	{
		if(sensor == null)
		{
			return(ErrorCode.ERR_PARAMS);
		}
		else if(!sensor.isInitialized())
		{
			return(ErrorCode.ERR_STATE);
		}
		
		SerialUsbDriver serialDriver = sensor.getSerialDriver();
		
		if(serialDriver == null)
		{
			return(ErrorCode.ERR_RESOURCE);
		}
		
		UsbSensorTask sensorTask = mUsbSensorTask_map.get(serialDriver.getDevice());
		
		if(sensorTask == null)
		{
			return(ErrorCode.ERR_STATE);
		}
		
		return(sensorTask.register(sensor, callback));
	}
	
	public synchronized int unregisterSensor(UsbSensor sensor)
	{
		if(sensor == null)
		{
			return(ErrorCode.ERR_PARAMS);
		}
		else if(!sensor.isInitialized())
		{
			return(ErrorCode.ERR_STATE);
		}
		
		UsbSensorTask sensorTask = mUsbSensorTask_map.get(sensor.getDevice());
		
		if(sensorTask == null)
		{
			return(ErrorCode.ERR_STATE);
		}
		
		return(sensorTask.unregister(sensor));
	}
	
	public synchronized void onDeviceInserted(Context context, UsbDevice device)
	{
		if(device == null)
		{
			return;
		}
		
		// Register the USB device in the internal sensor task map
		
		mUsbSensorTask_map.put(device, new UsbSensorTask(device));
		Log.i(TAG, "USB sensor connected!");
	}

	public synchronized void onDeviceEjected(Context context, UsbDevice device)
	{
		if(device == null)
		{
			return;
		}
		
		// Stop the sensor task associated with the USB device and run the 'onDeviceEjected()'
		// callback functions.
		
		UsbSensorTask sensorTask = mUsbSensorTask_map.get(device);
		
		if(sensorTask != null)
		{
			sensorTask.onDeviceEjected();
			mUsbSensorTask_map.remove(device);
			
			Log.i(TAG, "USB sensor disconnected!");
		}
	}
	
	public ILightSensor getLightSensor(UsbDevice device)
	{
		return(mUsbSensorAssistant.getLightSensor(device));
	}
	
	public IUVSensor getUVSensor(UsbDevice device)
	{
		return(mUsbSensorAssistant.getUVSensor(device));
	}
	
	public int getDeviceBaudRate(UsbDevice device)
	{
		return(mUsbSensorAssistant.getDeviceBaudRate(device));
	}
	
	public int getDeviceBaudRate(int vendorId, int productId)
	{
		return(mUsbSensorAssistant.getDeviceBaudRate(vendorId, productId));
	}
	
	public boolean isValidSensor(UsbDevice device)
	{
		return(mUsbSensorAssistant.isValidSensor(device));
	}
	
	public boolean isValidSensor(int vendorId, int productId)
	{
		return(mUsbSensorAssistant.isValidSensor(vendorId, productId));
	}
	
	@Override
	public void finalize()
	{
		mContext.unregisterReceiver(mBroadcastReceiver);
	}
	
	public static int init(Context context)
	{
		if(context == null)
		{
			return(ErrorCode.ERR_PARAMS);
		}
		
		if(mUsbSensorManager == null)
		{
			mUsbSensorManager = new UsbSensorManager(context);
		}
		
		return(ErrorCode.NO_ERROR);
	}
	
	public static UsbSensorManager getManager()
	{
		return(mUsbSensorManager);
	}
}
