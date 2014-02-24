package edu.dartmouth.cs.myruns5;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.hoho.android.usbserial.driver.FtdiSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialDriver;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbRequest;
import android.util.Log;

public class UsbSensorRunnable implements Runnable
{
	private final UsbDeviceConnection mUsbDeviceConnection;
	private final UsbDevice mUsbDevice;
	
	private final UsbEndpoint mUsbReadEndpoint;
	private final UsbEndpoint mUsbWriteEndpoint;
	
	private final Map<UsbSensor, UsbSensor.Callback> mCallback_map;
	
	private boolean mConnectionActive;
	
	UsbSensorRunnable(UsbDeviceConnection usbConnection, UsbDevice usbDevice, UsbEndpoint usbReadEndpoint, 
			UsbEndpoint usbWriteEndpoint, Map<UsbSensor, UsbSensor.Callback> callback_map)
	{
		mUsbDeviceConnection = usbConnection;
		mUsbDevice = usbDevice;
		
		mUsbReadEndpoint = usbReadEndpoint;
		mUsbWriteEndpoint = usbWriteEndpoint;
		
		mCallback_map = callback_map;
		mConnectionActive = false;
	}
	
	@Override
	public void run()
	{
		mConnectionActive = true;
		
		byte requestPktSerial[];
		byte responsePktSerial[] = new byte[Pulse32.PKT_SIZE];
		
		Pulse32 requestPkt = new Pulse32();
		Pulse32 responsePkt = new Pulse32();
		
		requestPkt.setLux(0);
		requestPkt.setUV(0);
		
		requestPktSerial = requestPkt.serialize();
		
		long prevTime = 0;
		long curTime = 0;
		
		/*
		FtdiSerialDriver ftdi = new FtdiSerialDriver(mUsbDevice, mUsbDeviceConnection);
		
		try
		{
			ftdi.open();
			ftdi.setParameters(57600, 8, UsbSerialDriver.STOPBITS_1, UsbSerialDriver.PARITY_NONE);
		}
		catch(IOException e)
		{
			ftdi.close();
			return;
		}
		*/
		
		while(mConnectionActive)
		{
			/*
			if(UsbFtdiUtils.serialWrite(mUsbDeviceConnection, mUsbWriteEndpoint, requestPktSerial, 
	                                    Pulse32.PKT_SIZE, 1000) == Pulse32.PKT_SIZE)
			{
				// Adding a short sleep here to allow the device some time to create the message
				try
				{
					Thread.sleep(20);
				}
				catch(InterruptedException e)
				{
					// Clear the last message before terminating
				}
				
				if(UsbFtdiUtils.serialRead(mUsbDeviceConnection, mUsbReadEndpoint, responsePktSerial, 
						                   Pulse32.PKT_SIZE, 1000) == Pulse32.PKT_SIZE)
				{
					responsePkt.parse(responsePktSerial);
					
					if(mCallback_map != null)
					{
						UsbSensor sensor;
						Iterator<Entry<UsbSensor, UsbSensor.Callback>> sensor_iter = 
								mCallback_map.entrySet().iterator();
						
						while(sensor_iter.hasNext())
						{
							sensor = sensor_iter.next().getKey();
							sensor.notifySensorUpdate(responsePkt);
						}
						
						prevTime = curTime;
						curTime = System.nanoTime();
						
						Log.i("FOO", "DIFF ns: " + (curTime - prevTime));
					}
				}
			}
			*/
			// ------------------------
			
			if(UsbAcmUtils.serialWrite(mUsbDeviceConnection, mUsbWriteEndpoint, 
					                   requestPktSerial) == Pulse32.PKT_SIZE)
			{
				if(UsbAcmUtils.serialRead(mUsbDeviceConnection, mUsbReadEndpoint, 
						                  responsePktSerial) == Pulse32.PKT_SIZE)
				{
					responsePkt.parse(responsePktSerial);
					
					if(mCallback_map != null)
					{
						UsbSensor sensor;
						Iterator<Entry<UsbSensor, UsbSensor.Callback>> sensor_iter = 
								mCallback_map.entrySet().iterator();
						
						while(sensor_iter.hasNext())
						{
							sensor = sensor_iter.next().getKey();
							sensor.notifySensorUpdate(responsePkt);
						}
					}
					
					prevTime = curTime;
					curTime = System.nanoTime();
					
					Log.i("FOO", "DIFF ns: " + (curTime - prevTime));
				}
			}
		}
		
		// ftdi.close();
	}
	
	public void stop()
	{
		mConnectionActive = false;
	}
	
	public boolean isConnectionActive()
	{
		return(mConnectionActive);
	}
}
