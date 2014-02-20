package edu.dartmouth.cs.myruns5;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;

public class PulsePacket
{
	private static final int MAX_PAYLOAD_SIZE = 256;
	private static final int HEADER_SIZE = 4;
	
	private UsbDeviceConnection mUsbDeviceConnection;
	private UsbEndpoint mUsbReadEndpoint;
	private UsbEndpoint mUsbWriteEndpoint;
	
	private byte mRequest;
	private byte mType;
	private byte mFlags;
	private byte mSize;
	private byte mPayload[];
	
	public PulsePacket(UsbDeviceConnection usbDeviceConnection, UsbEndpoint usbReadEndpoint, 
		               UsbEndpoint usbWriteEndpoint)
	{
		mUsbDeviceConnection = usbDeviceConnection;
		mUsbReadEndpoint = usbReadEndpoint;
		mUsbWriteEndpoint = usbWriteEndpoint;
	}
	
	public int dispatch()
	{
		byte data[] = new byte[HEADER_SIZE + mSize];
		
		data[0] = mRequest;
		data[1] = mType;
		data[2] = mFlags;
		data[3] = mSize;
		
		mPayload = new byte[mSize];
		
		System.arraycopy(mPayload, 0, data, 4, mSize);
		return(mUsbDeviceConnection.bulkTransfer(mUsbWriteEndpoint, data, HEADER_SIZE + mSize, 3000));
	}
	
	public int fetch()
	{
		byte header[] = new byte[HEADER_SIZE];
		byte buffer[] = new byte[512];
		
		int bytesRead = 0;
		int totalBytesRead = 0;
		
		while(totalBytesRead < HEADER_SIZE)
		{
			bytesRead = mUsbDeviceConnection.bulkTransfer(mUsbReadEndpoint, buffer, HEADER_SIZE - totalBytesRead, 3000);
			
			if(bytesRead < 0)
			{
				return(ErrorCode.ERR_READ);
			}
			
			System.arraycopy(buffer, 0, header, totalBytesRead, bytesRead);
			totalBytesRead += bytesRead;
		}
		
		mRequest = header[0];
		mType = header[1];
		mFlags = header[2];
		mSize = header[3];
		
		totalBytesRead = 0;
		mPayload = new byte[mSize];
		
		while(totalBytesRead < mSize)
		{
			bytesRead = mUsbDeviceConnection.bulkTransfer(mUsbReadEndpoint, buffer, mSize - totalBytesRead, 3000);
			
			if(bytesRead < 0)
			{
				return(ErrorCode.ERR_READ);
			}
			
			System.arraycopy(buffer, 0, mPayload, totalBytesRead, bytesRead);
			totalBytesRead += bytesRead;
		}
		
		return(totalBytesRead + HEADER_SIZE);
	}
	
	public void setRequest(byte request)
	{
		mRequest = request;
	}
	
	public void setType(byte type)
	{
		mType = type;
	}
	
	public void setFlags(byte flags)
	{
		mFlags = flags;
	}
	
	public int setPayload(byte[] payload, int payloadSize)
	{
		if(payloadSize < 0 || payloadSize > payload.length || 
		   payloadSize > MAX_PAYLOAD_SIZE)
		{
			return(ErrorCode.ERR_PARAMS);
		}
		
		System.arraycopy(payload, 0, mPayload, 0, payloadSize);
		mSize = (byte) payloadSize;
		
		return(ErrorCode.NO_ERROR);
	}
	
	public byte getRequest()
	{
		return(mRequest);
	}
	
	public byte getType()
	{
		return(mType);
	}
	
	public byte getFlags()
	{
		return(mFlags);
	}
	
	public byte[] getPayload()
	{
		return(mPayload);
	}
	
	public int getPayloadSize()
	{
		return(mSize);
	}
}