package edu.dartmouth.cs.myruns5;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbRequest;
import android.util.Log;

public class UsbSensorRunnable implements Runnable
{
	private final UsbDeviceConnection mUsbDeviceConnection;
	private final UsbEndpoint mUsbReadEndpoint;
	private final UsbEndpoint mUsbWriteEndpoint;
	
	private final Map<UsbSensor, UsbSensor.Callback> mCallback_map;
	
	private static final int MAX_MSG_BUFFER_SIZE = 4096;
	
	private enum MsgState
	{
		MSG_READING, 		// Currently parsing a message
		MSG_QUEUED, 		// The message buffer has staged a new message
		MSG_EMPTY			// The message buffer is empty and waiting for a new message to process
	}
	
	private boolean mConnectionActive;
	private MsgState mMsgState;
	
	private final byte mMsgBuffer[];
	private int mMsgBufferSize;
	
	UsbSensorRunnable(UsbDeviceConnection usbConnection, UsbEndpoint usbReadEndpoint, 
			UsbEndpoint usbWriteEndpoint, Map<UsbSensor, UsbSensor.Callback> callback_map)
	{
		mUsbDeviceConnection = usbConnection;
		mUsbReadEndpoint = usbReadEndpoint;
		mUsbWriteEndpoint = usbWriteEndpoint;
		
		mCallback_map = callback_map;
		
		mMsgBuffer = new byte[MAX_MSG_BUFFER_SIZE];
		mMsgBufferSize = 0;
		
		mConnectionActive = false;
	}
	
	@Override
	public void run()
	{
		byte outBuffer[] = new byte[Constants.USB_READ_BUFFER_SIZE];
		int outBufferLen;
		
		byte inBuffer[] = new byte[Constants.USB_READ_BUFFER_SIZE];
		int inBufferLen;
		
		mConnectionActive = true;
		mMsgState = MsgState.MSG_EMPTY;
		
		while(mConnectionActive)
		{
			PulsePacket packet = new PulsePacket(mUsbDeviceConnection, mUsbReadEndpoint, mUsbWriteEndpoint);
			
			if(packet.dispatch() > 0 && packet.fetch() > 0)
			{
				Log.e("FOO", "OK!");
				Log.e("FOO", "FLAG: " + Integer.toHexString(packet.getFlags()));
			}
		}
		
		/*
		int outBufferMaxLen = mUsbWriteEndpoint.getMaxPacketSize();
		ByteBuffer outBuffer = ByteBuffer.allocate(outBufferMaxLen);
		
		int inBufferMaxLen = mUsbReadEndpoint.getMaxPacketSize();
		ByteBuffer inBuffer = ByteBuffer.allocate(inBufferMaxLen);
		
		Log.e("FOO", "OUT: " + outBufferMaxLen);
		Log.e("FOO", "IN: " + inBufferMaxLen);
		*/
		
		
		
		/*
		int bufCursor;
		byte buffer[] = new byte[Constants.USB_READ_BUFFER_SIZE];
		
		mConnectionActive = true;
		mMsgState = MsgState.MSG_EMPTY;
		
		while(mConnectionActive)
		{
			int length = mUsbDeviceConnection.bulkTransfer(mUsbReadEndpoint, buffer, buffer.length, 3000);
			
			if(length > 0)
			{
				bufCursor = 0;
				
				switch(mMsgState)
				{
					case MSG_EMPTY:
					{
						while(bufCursor < length && buffer[bufCursor] != '\n')
						{
							bufCursor++;
						}
						
						if(bufCursor >= length)
						{
							Log.e("FOO", "DROPPED");
							continue;
						}
						
						bufCursor++;
						mMsgState = MsgState.MSG_READING;
					}
					case MSG_READING: 
					{
						while(bufCursor < length && buffer[bufCursor] != '\n')
						{
							bufCursor++;
						}

						System.arraycopy(buffer, 0, mMsgBuffer, mMsgBufferSize, bufCursor);
						mMsgBufferSize += bufCursor;
						
						if(buffer[bufCursor] == '\n')
						{
							mMsgState = MsgState.MSG_QUEUED;
						}
						else
						{
							continue;
						}
					}
				}
				
				mMsgState = MsgState.MSG_QUEUED;
				
				String msg = new String(mMsgBuffer, 0, mMsgBufferSize);
				
				Log.i("FOO2", "MSG: *" + msg + "*");
				
				
				String msg = new String();
				
				for(int i = 0; i < mMsgBufferSize; i++)
				{
					msg += Integer.toHexString(mMsgBuffer[i]) + " ";
				}
				
				Log.i("FOO", "DATA: " + msg);
				
				
				if(mCallback_map != null)
				{
					UsbSensor sensor;
					Iterator<Entry<UsbSensor, UsbSensor.Callback>> sensor_iter = 
							mCallback_map.entrySet().iterator();
					
					while(sensor_iter.hasNext())
					{
						sensor = sensor_iter.next().getKey();
						//sensor.notifySensorUpdate(mMsgBuffer, mMsgBufferSize);
					}
				}
				
				mMsgState = MsgState.MSG_EMPTY;
				mMsgBufferSize = 0;
			}
		}
		*/
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
