package edu.dartmouth.cs.myruns5;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

public class UsbDeviceBroadcastReceiver extends BroadcastReceiver
{
	private static final String TAG = UsbDeviceBroadcastReceiver.class.getName();
	
	@Override
	public void onReceive(Context context, Intent intent)
	{
		UsbManager usbManager = MyRunsApplication.getUsbSensorManager().getUsbManager();
		UsbSensorManager usbSensorManager = MyRunsApplication.getUsbSensorManager();
		
        String action = intent.getAction();
        UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
        
    	synchronized(this)
    	{
            if(UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action))
            {
            	if(device != null)
            	{
            		if(usbSensorManager.isValidSensor(device))
		        	{
		        		PendingIntent pendingIntent = 
		        				PendingIntent.getBroadcast(context, 0, new Intent(Constants.PERMISSION_ACTION_USB_SENSOR), 0);
		        		
		        		IntentFilter intentFilter = new IntentFilter(Constants.PERMISSION_ACTION_USB_SENSOR);
		        		context.registerReceiver(this, intentFilter);
		        		
		        		usbManager.requestPermission(device, pendingIntent);
		        	}
            	}
            }
            else if(UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action))
            {
            	if(device != null)
            	{
            		usbSensorManager.onDeviceEjected(context, device);
            	}
            }
            else if(action.equals(Constants.PERMISSION_ACTION_USB_SENSOR))
    		{
            	// Check if user approved access to USB hardware
            	
            	synchronized(this)
            	{
                    if(intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false))
                    {
	            		usbSensorManager.onDeviceInserted(context, device);
                    } 
                    else
                    {
                        Log.d(TAG, "User denied permission to access USB device");
                    }
                }
    		}
        }
    }
}
