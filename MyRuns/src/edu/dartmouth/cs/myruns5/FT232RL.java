package edu.dartmouth.cs.myruns5;

import android.hardware.usb.UsbDeviceConnection;

public class FT232RL
{
	
	public int setBaudRate(int baud)
	{
		if(baud < 0)
		{
			return(ErrorCode.ERR_PARAMS);
		}
		
		int clk = 48000000;
		int clk_div = 16;
		
		return(ErrorCode.NO_ERROR);
	}
	
	public int reset(UsbDeviceConnection connection)
	{
		int bmRequestType = FTDIConstants.BM_REQ_TYPE_HOST_TO_DEVICE | 
							FTDIConstants.BM_REQ_TYPE_VENDOR_TYPE | 
							FTDIConstants.BM_REQ_TYPE_RECP_DEVICE;
		int bRequest = FTDIConstants.B_REQUEST_SIO_RESET;
		int value = FTDIConstants.SIO_RESET;
		
		connection.controlTransfer(bmRequestType, bRequest, value, 0, null, 0, 0);
		return(ErrorCode.NO_ERROR);
	}
	
	private int convertBaudRate(int baudrate, int clk, int clk_div, 
								Integer encoded_divisor, Integer best_baud)
	{
		int divisor;
		int best_divisor;
		
		if(baudrate < 0 || clk < 0 || clk_div < 0 || encoded_divisor == null || 
		   best_baud == null)
		{
			return(ErrorCode.ERR_PARAMS);
		}
		else if(baudrate >= clk / clk_div)
		{
			encoded_divisor = 0;
			best_baud = clk / clk_div;
		}
		else if(baudrate >= clk / (clk_div + clk_div / 2))
		{
			encoded_divisor = 1;
			best_baud = clk / (clk_div + clk_div / 2);
		}
		else
		{
			divisor = clk * 16 / clk_div / baudrate;
			
			if((divisor & 1) != 0)
			{
				best_divisor = divisor / 2 + 1;
			}
			else
			{
				best_divisor = divisor / 2;
			}
			
			if(best_divisor > 0x20000)
			{
				best_divisor = 0x1FFFF;
			}
			
			best_baud = clk * 16 / clk_div / best_divisor;
			
			if((best_baud & 1) != 0)
			{
				best_baud = best_baud / 2 + 1;
			}
			else
			{
				best_baud = best_baud / 2;
			}
			
			//encoded_divisor = (best_divisor >> 3) | (frac_code[best_divisor & 0x7] << 14);
		}
		
		return(0);
	}
}
