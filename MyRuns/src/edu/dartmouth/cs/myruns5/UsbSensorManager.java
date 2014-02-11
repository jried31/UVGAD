package edu.dartmouth.cs.myruns5;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import android.content.Context;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.widget.Toast;

public class UsbSensorManager
{
	private static final String TAG = UsbSensorManager.class.getName();
	
	private final Context mContext;
	private final UsbManager mUsbManager;
	private final UsbSensorAssistant mUsbSensorAssistant;
	private final UsbDeviceBroadcastReceiver mBroadcastReceiver;
	
	private final ConcurrentHashMap<UsbDevice, UsbSensorTask> mUsbSensorTask_map;
	
	private class UsbSensorTask
	{
		private final UsbDevice mUsbDevice;
		private final ConcurrentHashMap<UsbSensor, UsbSensor.Callback> mCallback_map;
		
		private UsbSensorConnection mUsbSensorConnection;
		
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
			
			if(mCallback_map.size() <= 0)
			{
				// No sensors have been registered to this USB device.
				// Add the sensor's callback to the task's callback map and start the sensor.
				
				mCallback_map.put(sensor, callback);
				
				if(startTask() != ErrorCode.NO_ERROR)
				{
					return(ErrorCode.ERR_FAILED);
				}
			}
			else
			{	
				if(mCallback_map.containsKey(sensor))
				{
					// @ERROR: The same sensor has already been registered.
					
					return(ErrorCode.ERR_STATE);
				}
				
				// A sensor has already been registered to this USB device.
				// Add the sensor's callback to the task's callback map and return success.
				
				mCallback_map.put(sensor, callback);
			}
			
			return(ErrorCode.NO_ERROR);
		}
		
		private int unregister(UsbSensor sensor)
		{
			if(sensor == null || sensor.getDevice() != mUsbDevice)
			{
				return(ErrorCode.ERR_PARAMS);
			}
			else if(mCallback_map.size() <= 0 || !mCallback_map.containsKey(sensor))
			{
				// @ERROR: No callbacks are associated with the sensor task or the sensor 
				//         has not been registered with this task.
				
				return(ErrorCode.ERR_STATE);
			}
			
			mCallback_map.remove(sensor);
			
			if(mCallback_map.size() == 0)
			{
				// The last sensor was unregistered with the sensor task so terminate the 
				// task.
				
				if(stopTask() != ErrorCode.NO_ERROR)
				{
					return(ErrorCode.ERR_FAILED);
				}
			}
			
			return(ErrorCode.NO_ERROR);
		}
		
		private void onDeviceEjected()
		{
			if(stopTask() != ErrorCode.NO_ERROR)
			{
				return;
			}
			
			Iterator<Entry<UsbSensor, UsbSensor.Callback>> callback_iter = 
					mCallback_map.entrySet().iterator();
			
			while(callback_iter.hasNext())
			{
				callback_iter.next().getValue().onDeviceEjected();
			}
			
			mCallback_map.clear();
		}
		
		private void unregisterAll()
		{
			stopTask();
			mCallback_map.clear();
		}
		
		private int startTask()
		{
			if(mUsbSensorConnection != null)
			{
				return(ErrorCode.ERR_STATE);
			}
			
			mUsbSensorConnection = new UsbSensorConnection(mUsbManager, mUsbDevice, 
					mCallback_map);
			mUsbSensorConnection.start();
			
			return(ErrorCode.NO_ERROR);
		}
		
		private int stopTask()
		{
			if(mUsbSensorConnection == null)
			{
				return(ErrorCode.ERR_STATE);
			}
			
			mUsbSensorConnection.stop();
			mUsbSensorConnection = null;
			
			return(ErrorCode.NO_ERROR);
		}
		
		@Override
		public void finalize()
		{
			unregisterAll();
		}
	}
	
	public UsbSensorManager(Context context)
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
	
	public synchronized int registerSensor(UsbSensor sensor, UsbSensor.Callback callback)
	{
		if(sensor == null)
		{
			return(ErrorCode.ERR_PARAMS);
		}
		
		UsbDevice device = sensor.getDevice();
		
		if(device == null)
		{
			return(ErrorCode.ERR_RESOURCE);
		}
		
		UsbSensorTask sensorTask = mUsbSensorTask_map.get(device);
		
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
		
		UsbDevice device = sensor.getDevice();
		
		if(device == null)
		{
			return(ErrorCode.ERR_RESOURCE);
		}
		
		UsbSensorTask sensorTask = mUsbSensorTask_map.get(device);
		
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
	
	public boolean isValidSensor(int vendorId, int productId)
	{
		return(mUsbSensorAssistant.isValidSensor(vendorId, productId));
	}
	
	public boolean isValidSensor(UsbDevice device)
	{
		return(mUsbSensorAssistant.isValidSensor(device));
	}
	
	@Override
	public void finalize()
	{
		mContext.unregisterReceiver(mBroadcastReceiver);
	}
}
