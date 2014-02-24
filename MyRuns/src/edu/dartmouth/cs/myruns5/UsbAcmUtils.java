package edu.dartmouth.cs.myruns5;

import java.nio.ByteBuffer;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.util.Log;

public class UsbAcmUtils
{
	public static final int DFLT_TIMEOUT = 3000;
	protected  static final int DFLT_BUFFER_SIZE = 64;
	
	public static int serialRead(final UsbDeviceConnection connection, final UsbEndpoint readEndpoint, 
                                 final byte buffer[])
	{
		return(serialRead(connection, readEndpoint, buffer, buffer.length, DFLT_TIMEOUT));
	}
	
	public static int serialRead(final UsbDeviceConnection connection, final UsbEndpoint readEndpoint, 
			                     final byte buffer[], int len)
	{
		return(serialRead(connection, readEndpoint, buffer, len, DFLT_TIMEOUT));
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
		
		while(totalBytesRead < len)
		{
			bytesRead = connection.bulkTransfer(readEndpoint, tmpBuffer, 
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
				return(ErrorCode.ERR_WRITE);
			}
			
			totalBytesWritten += bytesWritten;
			buf = null;
		}
		
		return(totalBytesWritten);
	}
}
