package edu.repo.ucla.serialusbdriver;


import java.io.IOException;
import java.nio.ByteBuffer;

import com.hoho.android.usbserial.driver.FtdiSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialDriver;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbManager;

public class FTDISerialUsbDriver extends SerialUsbDriver
{
	protected static final int SIO_RESET = 0x00;
	protected static final int SIO_MODEM_CTRL = 0x01;
	protected static final int SIO_SET_FLOW_CTRL = 0x02;
	protected static final int SIO_SET_BAUD_RATE = 0x03;
	protected static final int SIO_SET_DATA = 0x04;
	
	protected static final int B_REQUEST_SIO_RESET = SIO_RESET;
	protected static final int B_REQUEST_SIO_MODEM_CTRL = SIO_MODEM_CTRL;
	protected static final int B_REQUEST_SIO_SET_FLOW_CTRL = SIO_SET_FLOW_CTRL;
	protected static final int B_REQUEST_SIO_SET_BAUD_RATE = SIO_SET_BAUD_RATE;
	protected static final int B_REQUEST_SIO_SET_DATA = SIO_SET_DATA;
	
	protected static final int SIO_RESET_SIO = 0;
	protected static final int SIO_RESET_RX = 1;
	protected static final int SIO_RESET_TX = 2;
	
	protected static final int BITS_TYPE_8 = 8;
	
	protected static final int SIO_DISABLE_FLOW_CONTROL = 0;
	
	protected static final int DFLT_BUFFER_SIZE = 66;
	
	protected FtdiSerialDriver mFtdiDriver;
	
	FTDISerialUsbDriver(UsbManager usbManager, UsbDevice device)
	{
		super(usbManager, device);
	}
	
	@Override
	public int open(int baud)
	{
		int bmRequestType = BM_REQ_TYPE_HOST_TO_DEVICE | 
							BM_REQ_TYPE_VENDOR_TYPE | BM_REQ_TYPE_RECP_DEVICE;
		
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
				if(mDevice.getInterface(ifaceCursor).getInterfaceClass() == UsbConstants.USB_CLASS_VENDOR_SPEC)
				{
					mInterface = mDevice.getInterface(ifaceCursor);
					
					// Acquire exclusive access to the USB interface
					mDeviceConnection.claimInterface(mInterface, true);
					
					mDeviceConnection.controlTransfer(bmRequestType, B_REQUEST_SIO_RESET, SIO_RESET_SIO, 0, null, 0, 0);// reset
																	// mConnection.controlTransfer(0×40,
                    												// 0, 1, 0, null, 0,
                    												// 0);//clear Rx
					mDeviceConnection.controlTransfer(bmRequestType, B_REQUEST_SIO_RESET, SIO_RESET_RX, 0, null, 0, 0);// clear Rx
					mDeviceConnection.controlTransfer(bmRequestType, B_REQUEST_SIO_RESET, SIO_RESET_TX, 0, null, 0, 0);// clear Tx
					mDeviceConnection.controlTransfer(bmRequestType, B_REQUEST_SIO_SET_FLOW_CTRL, SIO_DISABLE_FLOW_CONTROL, 0, null, 0, 0);// flow
					                            // control
					                            // none
					//mDeviceConnection.controlTransfer(0x40, 0x03, 0x0034, 0, null, 0, 0);// baudrate
					                            // 57600
					//mUsbDeviceConnection.controlTransfer(0x40, 0x03, 0x4138, 0, null, 0, 0);// baudrate
					mDeviceConnection.controlTransfer(bmRequestType, B_REQUEST_SIO_SET_DATA, BITS_TYPE_8, 0, null, 0, 0);
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
		
		mFtdiDriver = new FtdiSerialDriver(mDevice, mDeviceConnection);
		
		if(mFtdiDriver == null)
		{
			close();
			return(ErrorCode.ERR_FAILED);
		}
		
		try
		{
			mFtdiDriver.open();
			mFtdiDriver.setParameters(baud, 8, UsbSerialDriver.STOPBITS_1, UsbSerialDriver.PARITY_NONE);
		}
		catch(IOException e)
		{
			close();
			return(ErrorCode.ERR_FAILED);
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
		
		if(mFtdiDriver != null)
		{
			mFtdiDriver.close();
			mFtdiDriver = null;
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
		else if(mDeviceConnection == null || mReadEndpoint == null || mFtdiDriver == null)
		{
			return(ErrorCode.ERR_STATE);
		}
		
		int bytesRead;
		int totalBytesRead = 0;
		
		byte tmpBuffer[] = new byte[DFLT_BUFFER_SIZE];
		
		len += 2;
		
		while(totalBytesRead < len)
		{
			bytesRead = mDeviceConnection.bulkTransfer(mReadEndpoint, tmpBuffer, 
													   Math.min(len - totalBytesRead, DFLT_BUFFER_SIZE), 
													   timeout);
			
			if(bytesRead < 0)
			{
				return(totalBytesRead - 2);
			}
			else if(totalBytesRead < 2)
			{
				if(bytesRead > 2)
				{
					System.arraycopy(tmpBuffer, 2 - totalBytesRead, buffer, totalBytesRead, bytesRead - totalBytesRead - 2);
				}
				
				totalBytesRead += bytesRead;
			}
			else
			{
				System.arraycopy(tmpBuffer, 0, buffer, totalBytesRead - 2, bytesRead);
				totalBytesRead += bytesRead;
			}
		}
		
		return(totalBytesRead - 2);
	}

	@Override
	public int write(byte[] buffer, int len, int timeout)
	{
		if(buffer == null || buffer.length < len || len < 0 || timeout < 0)
		{
			return(ErrorCode.ERR_PARAMS);
		}
		else if(mDeviceConnection == null || mWriteEndpoint == null || mFtdiDriver == null)
		{
			return(ErrorCode.ERR_STATE);
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
				return(totalBytesWritten);
			}
			
			totalBytesWritten += bytesWritten;
			buf = null;
		}
		
		return(totalBytesWritten);
	}
}
