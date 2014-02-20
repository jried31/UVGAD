package edu.dartmouth.cs.myruns5;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;

public class UsbSensorConnection
{
	private static final int BM_REQ_TYPE_HOST_TO_DEVICE = 0x00;
	private static final int BM_REQ_TYPE_DEVICE_TO_HOST = 0x80;
	
	private static final int BM_REQ_TYPE_STANDARD_TYPE = 0x00;
	private static final int BM_REQ_TYPE_CLASS_TYPE = 0x20;
	private static final int BM_REQ_TYPE_VENDOR_TYPE = 0x40;
	
	private static final int BM_REQ_TYPE_RECP_DEVICE = 0x00;
	private static final int BM_REQ_TYPE_RECP_INTERFACE = 0x01;
	private static final int BM_REQ_TYPE_RECP_ENDPOINT = 0x02;
	private static final int BM_REQ_TYPE_RECP_OTHER = 0x03;
	
	private static final int B_REQUEST_SET_LINE_CODING = 0x20;
	private static final int B_REQUEST_SET_CONTROL_LINE_STATE = 0x22;
	
	private final UsbManager mUsbManager;
	private final UsbDevice mUsbDevice;
	
	private UsbDeviceConnection mUsbDeviceConnection;
	private UsbInterface mUsbInterface;
	private UsbEndpoint mUsbReadEndpoint;
	private UsbEndpoint mUsbWriteEndpoint;
	
	private Map<UsbSensor, UsbSensor.Callback> mUsbSensorCallback_map;
	
	private UsbSensorRunnable mUsbSensorRunnable;
	private Thread mUsbSensorThread;
	
	UsbSensorConnection(UsbManager usbManager, UsbDevice usbDevice, 
			Map<UsbSensor, UsbSensor.Callback> usbSensorCallback_map)
	{
		mUsbManager = usbManager;
		mUsbDevice = usbDevice;
		mUsbSensorCallback_map = usbSensorCallback_map;
		
		mUsbReadEndpoint = null;
	}
	
	public UsbDevice getDevice()
	{
		return(mUsbDevice);
	}
	
	public int start()
	{
		int bmRequestType = BM_REQ_TYPE_HOST_TO_DEVICE | 
				BM_REQ_TYPE_CLASS_TYPE | BM_REQ_TYPE_RECP_INTERFACE;
				
		if(mUsbReadEndpoint != null)
		{
			return(ErrorCode.ERR_STATE);
		}
		
		mUsbDeviceConnection = mUsbManager.openDevice(mUsbDevice);
		
		synchronized(mUsbDevice)
		{
			int ifaceCount = mUsbDevice.getInterfaceCount();
			
			for(int ifaceCursor = 0; ifaceCursor < ifaceCount; ifaceCursor++)
			{
				if(mUsbDevice.getInterface(ifaceCursor).getInterfaceClass() == UsbConstants.USB_CLASS_CDC_DATA)
				{
					mUsbInterface = mUsbDevice.getInterface(ifaceCursor);
					int epCount = mUsbInterface.getEndpointCount();
					
					// Acquire exclusive access to the USB interface
					mUsbDeviceConnection.claimInterface(mUsbInterface, true);
					
			        // Arduino USB serial converter setup
					// TODO: Convert this to a class
					mUsbDeviceConnection.controlTransfer(bmRequestType, B_REQUEST_SET_CONTROL_LINE_STATE, 0, 0, null, 0, 0);
					mUsbDeviceConnection.controlTransfer(bmRequestType, B_REQUEST_SET_LINE_CODING, 0, 0, new byte[] { (byte) 0x80,
			                0x25, 0x00, 0x00, 0x00, 0x00, 0x08 }, 7, 0);
					
					for(int epCursor = 0; epCursor < epCount; epCursor++)
					{
						UsbEndpoint endpoint = mUsbInterface.getEndpoint(epCursor);
						int endpointDir = endpoint.getDirection();
						
						if(endpointDir == UsbConstants.USB_DIR_IN)
						{
							mUsbReadEndpoint = endpoint;
						}
						else if(endpointDir == UsbConstants.USB_DIR_OUT)
						{
							mUsbWriteEndpoint = endpoint;
						}
					}
				}
			}
			
			if(mUsbReadEndpoint == null)
			{
				return(ErrorCode.ERR_FAILED);
			}
			
			mUsbSensorRunnable = new UsbSensorRunnable(mUsbDeviceConnection, mUsbReadEndpoint, 
					mUsbWriteEndpoint, mUsbSensorCallback_map);
			
			mUsbSensorThread = new Thread(mUsbSensorRunnable);
			mUsbSensorThread.start();
			
			while(!mUsbSensorRunnable.isConnectionActive())
			{
				// Poll until the thread is running
			}
		}
		
		return(ErrorCode.NO_ERROR);
	}
	
	public int stop()
	{
		if(mUsbReadEndpoint == null)
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
			
			// Release exclusive access to the USB interface
			mUsbDeviceConnection.releaseInterface(mUsbInterface);
		}
		
		mUsbReadEndpoint = null;
		mUsbWriteEndpoint = null;
		
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
