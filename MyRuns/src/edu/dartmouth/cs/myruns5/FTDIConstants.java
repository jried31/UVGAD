package edu.dartmouth.cs.myruns5;

public final class FTDIConstants
{
	static final int BM_REQ_TYPE_HOST_TO_DEVICE = 0x00;
	static final int BM_REQ_TYPE_DEVICE_TO_HOST = 0x80;
	
	static final int BM_REQ_TYPE_STANDARD_TYPE = 0x00;
	static final int BM_REQ_TYPE_CLASS_TYPE = 0x20;
	static final int BM_REQ_TYPE_VENDOR_TYPE = 0x40;
	
	static final int BM_REQ_TYPE_RECP_DEVICE = 0x00;
	static final int BM_REQ_TYPE_RECP_INTERFACE = 0x01;
	static final int BM_REQ_TYPE_RECP_ENDPOINT = 0x02;
	static final int BM_REQ_TYPE_RECP_OTHER = 0x03;
	
	static final int SIO_RESET = 0x00;
	static final int SIO_MODEM_CTRL = 0x01;
	static final int SIO_SET_FLOW_CTRL = 0x02;
	static final int SIO_SET_BAUD_RATE = 0x03;
	static final int SIO_SET_DATA = 0x04;
	
	static final int B_REQUEST_SIO_RESET = SIO_RESET;
	static final int B_REQUEST_SIO_MODEM_CTRL = SIO_MODEM_CTRL;
	static final int B_REQUEST_SIO_SET_FLOW_CTRL = SIO_SET_FLOW_CTRL;
	static final int B_REQUEST_SIO_SET_BAUD_RATE = SIO_SET_BAUD_RATE;
	static final int B_REQUEST_SIO_SET_DATA = SIO_SET_DATA;
	
}
