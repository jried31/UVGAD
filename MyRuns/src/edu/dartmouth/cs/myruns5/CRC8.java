package edu.dartmouth.cs.myruns5;

public class CRC8
{
	private short mChksum;
	
	public int update(byte bin)
	{
		byte arr[] = {bin};
		
		return(update(arr, 1));
	}
	
	public int update(byte arr[], int len)
	{
		if(arr.length < len)
		{
			return(ErrorCode.ERR_PARAMS);
		}
		
		for(int idx = 0; idx < len; idx++)
		{
			mChksum ^= arr[idx] << 8;
			
			for(int bitPos = 8; bitPos > 0; bitPos--)
			{
				if((mChksum & 0x8000) != 0)
				{
					mChksum ^= (0x1070 << 3);
				}
				
				mChksum <<= 1;
			}
		}
		  
		return(ErrorCode.NO_ERROR);
	}
	
	public byte getChecksum()
	{
		return((byte) (mChksum >> 8));
	}
}
