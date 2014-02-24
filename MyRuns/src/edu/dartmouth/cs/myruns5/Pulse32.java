package edu.dartmouth.cs.myruns5;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Pulse32
{
	public static final int PKT_SIZE = 32;
	public static final int PKT_MAX_PAYLOAD_ENTRIES = 7;
	
	protected static final short ID_LIGHT = 0;
	protected static final short ID_UV = 1;
	
	protected static final short FLAG_LIGHT = (short) (0x8000 >>> ID_LIGHT);
	protected static final short FLAG_UV = (short) (0x8000 >>> ID_UV);
	
	protected class Pulse32Pkt
	{
		protected short flags;
		protected final short padding;
		protected int payload[] = new int[PKT_MAX_PAYLOAD_ENTRIES];
		
		Pulse32Pkt()
		{
			flags = 0;
			padding = 0;
			
			for(int cursor = 0; cursor < payload.length; cursor++)
			{
				payload[cursor] = 0;
			}
		}
	}
	
	protected final Pulse32Pkt mPkt;
	
	public Pulse32()
	{
		mPkt = new Pulse32Pkt();
	}
	
	byte[] serialize()
	{
		ByteBuffer buffer = ByteBuffer.allocate(PKT_SIZE);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		
		buffer.putShort(mPkt.flags);
		buffer.putShort(mPkt.padding);
		
		for(int cursor = 0; cursor < PKT_MAX_PAYLOAD_ENTRIES; cursor++)
		{
			buffer.putInt(mPkt.payload[cursor]);
		}
		
		return(buffer.array());
	}
	
	int parse(final byte data[])
	{
		if(data == null)
		{
			return(ErrorCode.ERR_PARAMS);
		}
		
		ByteBuffer buffer = ByteBuffer.wrap(data);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		
		mPkt.flags = buffer.getShort();
		buffer.getShort();			// Increments the buffer pointer to ignore padding
		
		for(int cursor = 0; cursor < PKT_MAX_PAYLOAD_ENTRIES; cursor++)
		{
			mPkt.payload[cursor] = buffer.getInt();
		}
		
		return(PKT_SIZE);
	}
	
	void setLux(int value)
	{
		mPkt.flags |= FLAG_LIGHT;
		mPkt.payload[ID_LIGHT] = value;
	}
	
	void setUV(int value)
	{
		mPkt.flags |= FLAG_UV;
		mPkt.payload[ID_UV] = value;
	}
	
	Integer getLux()
	{
		if((mPkt.flags & FLAG_LIGHT) == 0)
		{
			return(null);
		}
		
		return(mPkt.payload[ID_LIGHT]);
	}
	
	Integer getUV()
	{
		if((mPkt.flags & FLAG_UV) == 0)
		{
			return(null);
		}
		
		return(mPkt.payload[ID_UV]);
	}
	
	boolean isLightFlagSet()
	{
		return(((mPkt.flags & FLAG_LIGHT) > 0) ? true : false);
	}
	
	boolean isUVFlagSet()
	{
		return(((mPkt.flags & FLAG_UV) > 0) ? true : false);
	}
	
	void clear()
	{
		mPkt.flags = 0;
		
		for(int cursor = 0; cursor < PKT_MAX_PAYLOAD_ENTRIES; cursor++)
		{
			mPkt.payload[cursor] = 0;
		}
	}
}
