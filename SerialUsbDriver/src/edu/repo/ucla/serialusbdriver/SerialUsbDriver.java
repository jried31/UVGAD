package edu.repo.ucla.serialusbdriver;


import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

public abstract class SerialUsbDriver
{
	protected static final int BM_REQ_TYPE_HOST_TO_DEVICE = 0x00;
	protected static final int BM_REQ_TYPE_DEVICE_TO_HOST = 0x80;
	
	protected static final int BM_REQ_TYPE_STANDARD_TYPE = 0x00;
	protected static final int BM_REQ_TYPE_CLASS_TYPE = 0x20;
	protected static final int BM_REQ_TYPE_VENDOR_TYPE = 0x40;
	
	protected static final int BM_REQ_TYPE_RECP_DEVICE = 0x00;
	protected static final int BM_REQ_TYPE_RECP_INTERFACE = 0x01;
	protected static final int BM_REQ_TYPE_RECP_ENDPOINT = 0x02;
	protected static final int BM_REQ_TYPE_RECP_OTHER = 0x03;
	
	protected static final int B_REQUEST_SET_LINE_CODING = 0x20;
	protected static final int B_REQUEST_SET_CONTROL_LINE_STATE = 0x22;
	
	protected final UsbManager mUsbManager;
	protected final UsbDevice mDevice;
	
	protected UsbDeviceConnection mDeviceConnection;
	protected UsbInterface mInterface;
	
	protected UsbEndpoint mReadEndpoint;
	protected UsbEndpoint mWriteEndpoint;
	
	abstract int open(int baud);
	abstract int close();
	
	abstract int read(final byte buffer[], int len, int timeout);
	abstract int write(final byte buffer[], int len, int timeout);
	
	SerialUsbDriver(UsbManager usbManager, UsbDevice device)
	{
		mUsbManager = usbManager;
		mDevice = device;
	}
	
	UsbDeviceConnection getDeviceConnection()
	{
		return(mDeviceConnection);
	}
	
	UsbDevice getDevice()
	{
		return(mDevice);
	}
	
	UsbInterface getInterface()
	{
		return(mInterface);
	}
	
	UsbEndpoint getReadEndpoint()
	{
		return(mReadEndpoint);
	}
	
	UsbEndpoint getWriteEndpoint()
	{
		return(mWriteEndpoint);
	}
}
