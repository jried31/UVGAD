package edu.repo.ucla.serialusbdriver;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Pulse32
{
	public static final int PKT_SIZE = 32;
	public static final int PKT_MAX_PAYLOAD_ENTRIES = 7;
	
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

	void setField(int id, int value)
	{
		if(id < 0 || id > PKT_MAX_PAYLOAD_ENTRIES)
		{
			id = 0;
		}
		
		mPkt.flags |= (short) (0x8000 >>> id);
		mPkt.payload[id] = value;
	}
	
	int getField(int id)
	{
		if(id < 0 || id > PKT_MAX_PAYLOAD_ENTRIES)
		{
			id = 0;
		}
		
		return(mPkt.payload[id]);
	}
	
	void setFlags(short flags)
	{
		mPkt.flags = flags;
	}
	
	short getFlags()
	{
		return(mPkt.flags);
	}
	
	boolean isFieldSet(int id)
	{
		if(id < 0 || id > PKT_MAX_PAYLOAD_ENTRIES)
		{
			id = 0;
		}
		
		return((mPkt.flags & (short) (0x8000 >>> id)) != 0 ? true : false);
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
