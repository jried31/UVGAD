package edu.repo.ucla.serialusbdriver;


import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import android.hardware.usb.UsbDeviceConnection;

public class UsbSensorRunnable implements Runnable
{
	private final SerialUsbDriver mSerialDriver;
	
	private final Map<UsbSensor, UsbSensor.Callback> mCallback_map;
	
	private boolean mConnectionActive;
	
	UsbSensorRunnable(UsbDeviceConnection usbConnection, SerialUsbDriver serialDriver, 
					  Map<UsbSensor, UsbSensor.Callback> callback_map)
	{
		mSerialDriver = serialDriver;
		
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
		
		// Request all data from the sensor
		requestPkt.setFlags((short) 0xFFFF);
		
		requestPktSerial = requestPkt.serialize();
		
		while(mConnectionActive)
		{
			if(mSerialDriver.write(requestPktSerial, Pulse32.PKT_SIZE, 1000) == Pulse32.PKT_SIZE)
			{
				// Adding a short sleep here to allow the device some time to create the response
				try
				{
					Thread.sleep(20);
				}
				catch(InterruptedException e)
				{
					// Clear the response before terminating
				}
				
				if(mSerialDriver.read(responsePktSerial, Pulse32.PKT_SIZE, 1000) == Pulse32.PKT_SIZE)
				{
					responsePkt.parse(responsePktSerial);
					
					if(mCallback_map != null)
					{
						UsbSensor sensor;
						Iterator<Entry<UsbSensor, UsbSensor.Callback>> sensor_iter = mCallback_map.entrySet().iterator();
						
						while(sensor_iter.hasNext())
						{
							sensor = sensor_iter.next().getKey();
							sensor.notifySensorUpdate(responsePkt);
						}
					}
				}
			}
		}
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
