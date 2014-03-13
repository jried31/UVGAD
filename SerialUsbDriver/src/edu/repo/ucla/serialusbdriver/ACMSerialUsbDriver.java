package edu.repo.ucla.serialusbdriver;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbManager;
import android.util.Log;

public class ACMSerialUsbDriver extends SerialUsbDriver
{
	protected static final int DATA_BITS_8 = 8;
	
	protected static final int DFLT_BUFFER_SIZE = 64;
	
	ACMSerialUsbDriver(UsbManager usbManager, UsbDevice device)
	{
		super(usbManager, device);
	}
	
	@Override
	public int open(int baud)
	{
		ByteBuffer baudBuffer = ByteBuffer.allocate(4);
		baudBuffer.order(ByteOrder.LITTLE_ENDIAN);
		baudBuffer.putInt(baud);

		byte[] baudBytes = baudBuffer.array();
		
		int bmRequestType = BM_REQ_TYPE_HOST_TO_DEVICE | 
				            BM_REQ_TYPE_CLASS_TYPE | BM_REQ_TYPE_RECP_INTERFACE;
				
		if(mReadEndpoint != null || mWriteEndpoint != null || mInterface != null || mDeviceConnection != null)
		{
			return(ErrorCode.ERR_STATE);
		}
		
		mDeviceConnection = mUsbManager.openDevice(mDevice);
		
		if(mDeviceConnection == null)
		{
			return(ErrorCode.ERR_FAILED);
		}
		
		synchronized(mDevice)
		{
			int ifaceCount = mDevice.getInterfaceCount();
			
			for(int ifaceCursor = 0; (ifaceCursor < ifaceCount) && (mInterface == null); ifaceCursor++)
			{
				if(mDevice.getInterface(ifaceCursor).getInterfaceClass() == UsbConstants.USB_CLASS_CDC_DATA)
				{
					mInterface = mDevice.getInterface(ifaceCursor);
					
					// Acquire exclusive access to the USB interface
					mDeviceConnection.claimInterface(mInterface, true);
					
			        // Arduino USB serial converter setup
					mDeviceConnection.controlTransfer(bmRequestType, B_REQUEST_SET_CONTROL_LINE_STATE, 0, 0, null, 0, 0);
					mDeviceConnection.controlTransfer(bmRequestType, B_REQUEST_SET_LINE_CODING, 0, 0, 
							new byte[] { baudBytes[0], baudBytes[1], baudBytes[2], baudBytes[3], 0x00, 0x00, DATA_BITS_8 }, 
							7, 0);
				}
			}
			
			if(mInterface == null)
			{
				close();
				return(ErrorCode.ERR_FAILED);
			}
			
			int epCount = mInterface.getEndpointCount();
			
			for(int epCursor = 0; epCursor < epCount; epCursor++)
			{
				UsbEndpoint endpoint = mInterface.getEndpoint(epCursor);
				int endpointDir = endpoint.getDirection();
				
				if(endpointDir == UsbConstants.USB_DIR_IN)
				{
					mReadEndpoint = endpoint;
				}
				else if(endpointDir == UsbConstants.USB_DIR_OUT)
				{
					mWriteEndpoint = endpoint;
				}
			}
			
			if(mReadEndpoint == null || mWriteEndpoint == null)
			{
				close();
				return(ErrorCode.ERR_FAILED);
			}
		}
		
		return(ErrorCode.NO_ERROR);
	}

	@Override
	public int close()
	{
		mReadEndpoint = null;
		mWriteEndpoint = null;
		
		if(mInterface != null)
		{
			mDeviceConnection.releaseInterface(mInterface);
			mInterface = null;
		}
		
		if(mDeviceConnection != null)
		{
			mDeviceConnection.close();
			mDeviceConnection = null;
		}
		
		return(ErrorCode.NO_ERROR);
	}

	@Override
	public int read(byte[] buffer, int len, int timeout)
	{
		if(buffer == null || buffer.length < len || len < 0 || timeout < 0)
		{
			return(ErrorCode.ERR_PARAMS);
		}
		else if(mDeviceConnection == null || mReadEndpoint == null)
		{
			return(ErrorCode.ERR_STATE);
		}
		
		int bytesRead;
		int totalBytesRead = 0;
		
		byte tmpBuffer[] = new byte[DFLT_BUFFER_SIZE];
		
		while(totalBytesRead < len)
		{
			bytesRead = mDeviceConnection.bulkTransfer(mReadEndpoint, tmpBuffer, 
													   Math.min(len - totalBytesRead, DFLT_BUFFER_SIZE), 
												       timeout);
			
			if(bytesRead < 0)
			{
				return(ErrorCode.ERR_READ);
			}

			System.arraycopy(tmpBuffer, 0, buffer, totalBytesRead, bytesRead);
			totalBytesRead += bytesRead;
		}
		
		return(totalBytesRead);
	}

	@Override
	public int write(byte[] buffer, int len, int timeout)
	{
		if(mDeviceConnection == null || mWriteEndpoint == null || buffer == null || buffer.length < len || 
		   len < 0 || timeout < 0)
		{
			return(ErrorCode.ERR_PARAMS);
		}
		
		int bytesWritten;
		int totalBytesWritten = 0;
		
		byte buf[] = buffer;
		ByteBuffer bufWrapper = ByteBuffer.wrap(buf);
		
		while(totalBytesWritten < len)
		{
			if(buf == null)
			{
				bufWrapper.position(totalBytesWritten);
				buf = new byte[bufWrapper.remaining()];
				
				bufWrapper.get(buf, 0, bufWrapper.remaining());
			}
			
			bytesWritten = mDeviceConnection.bulkTransfer(mWriteEndpoint, buf, 
														  len - totalBytesWritten, timeout);
			
			if(bytesWritten < 0)
			{
				return(ErrorCode.ERR_WRITE);
			}
			
			totalBytesWritten += bytesWritten;
			buf = null;
		}
		
		return(totalBytesWritten);
	}
}

