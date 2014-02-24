package edu.dartmouth.cs.myruns5;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.hoho.android.usbserial.driver.FtdiSerialDriver;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbRequest;
import android.util.Log;

public class UsbFtdiUtils
{
	public static final int DFLT_TIMEOUT = 1000;
	protected  static final int DFLT_BUFFER_SIZE = 34;
	
	public static int serialRead(final FtdiSerialDriver ftdi_driver, final byte buffer[])
	{
		return(serialRead(ftdi_driver, buffer, buffer.length, DFLT_TIMEOUT));
	}
	
	public static int serialRead(final FtdiSerialDriver ftdi_driver, final byte buffer[], int len)
	{
		return(serialRead(ftdi_driver, buffer, len, DFLT_TIMEOUT));
	}
	
	// TODO: Caller's buffer should be filled in with correct bytes
	public static int serialRead(final FtdiSerialDriver ftdi_driver, final byte buffer[], int len, 
			                     int timeout)
	{
		if(ftdi_driver == null || buffer == null || buffer == null || buffer.length < len || 
		   len < 0 || timeout < 0)
		{
			return(ErrorCode.ERR_PARAMS);
		}
		
		int bytesRead = -1;
		int totalBytesRead = 0;
		
		while(totalBytesRead < len)
		{
			byte tmpBuffer[] = new byte[Math.min(DFLT_BUFFER_SIZE, len - totalBytesRead)];
			
			try
			{
				bytesRead = ftdi_driver.read(tmpBuffer, timeout);
			}
			catch(IOException e)
			{
				return(totalBytesRead);
			}
			
			if(bytesRead < 0)
			{
				return(totalBytesRead);
			}
			else if(bytesRead == 0)
			{
				return(totalBytesRead);
			}
	        
	        System.arraycopy(tmpBuffer, 0, buffer, totalBytesRead, bytesRead);
	        totalBytesRead += bytesRead;
		}
		
		return(totalBytesRead);
	}
	
	// TODO: Caller's buffer should be filled in with correct bytes
	public static int serialRead(final UsbDeviceConnection connection, final UsbEndpoint readEndpoint, 
			                     final byte buffer[], int len, int timeout)
	{
		if(connection == null || readEndpoint == null || buffer == null || buffer.length < len || 
		   len < 0 || timeout < 0)
		{
			return(ErrorCode.ERR_PARAMS);
		}
		
		int bytesRead;
		int totalBytesRead = 0;
		
		byte tmpBuffer[] = new byte[DFLT_BUFFER_SIZE];
		
		len += 2;
		
		while(totalBytesRead < len)
		{
			bytesRead = connection.bulkTransfer(readEndpoint, tmpBuffer, 
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
	
	public static int serialWrite(final UsbDeviceConnection connection, final UsbEndpoint readEndpoint, 
                                  final byte buffer[])
	{
		return(serialWrite(connection, readEndpoint, buffer, buffer.length, DFLT_TIMEOUT));
	}

	public static int serialWrite(final UsbDeviceConnection connection, final UsbEndpoint readEndpoint, 
			                      final byte buffer[], int len)
	{
		return(serialWrite(connection, readEndpoint, buffer, len, DFLT_TIMEOUT));
	}
	
	public static int serialWrite(final UsbDeviceConnection connection, final UsbEndpoint writeEndpoint, 
			                      final byte buffer[], int len, int timeout)
	{
		if(connection == null || writeEndpoint == null || buffer == null || buffer.length < len || 
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
			
			bytesWritten = connection.bulkTransfer(writeEndpoint, buf, len - totalBytesWritten, timeout);
			
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
