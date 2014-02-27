package edu.dartmouth.cs.myruns5;

import android.content.Context;
import android.hardware.usb.UsbDevice;

/**
 * Base class for all USB-based sensor hardware.
 * 
 * @author daniel
 *
 */
public abstract class UsbSensor extends Sensor
{
	protected final Context mContext;
	
	protected final UsbSensorManager mUsbSensorManager;
	protected final UsbDevice mUsbDevice;
	protected final SerialUsbDriver mSerialDriver;
	
	protected Callback mCallback;
	protected Callback mBaseCallback;
	protected CallbackRunnable mCallback_runnable;
	protected Thread mCallback_thread;
	
	protected int mPulseId;
	protected boolean mIsInitialized;
	
	private CallbackEvent mPendingEvent;
	
	public interface Callback
	{
		public abstract void onNewData(Pulse32 pkt);
		public abstract void onDeviceEjected();
	}
	
	private class CallbackEvent
	{
		private int what;
		private Pulse32 data;
		
		CallbackEvent(int what)
		{
			this.what = what;
			this.data = null;
		}
		
		CallbackEvent(int what, Pulse32 data)
		{
			this.what = what;
			this.data = data;
		}
	}
	
	private class CallbackRunnable implements Runnable
	{
		private static final int EVENT_SENSOR_UPDATE = 1;
		private static final int EVENT_SENSOR_EJECT = 2;
		private static final int EVENT_EXIT = 3;
		
		private CallbackEvent mCurrentEvent;
		
		private volatile Callback mRunnableCallback;
		private volatile boolean mIsActive;
		
		private volatile boolean mResult;
		
		CallbackRunnable(Callback runnableCallback)
		{
			mRunnableCallback = runnableCallback;
			
			mCurrentEvent = null;
			mPendingEvent = null;
			
			mIsActive = false;
			mResult = false;
		}
		
		private void handleMessage()
		{
			Pulse32 pkt = mCurrentEvent.data;
			
			switch(mCurrentEvent.what)
			{
				case EVENT_SENSOR_UPDATE:
				{
					if(pkt == null)
					{
						break;
					}
					
					if(mRunnableCallback != null)
					{
						mRunnableCallback.onNewData(pkt);
					}
					
					break;
				}
				case EVENT_SENSOR_EJECT:
				{
					if(mRunnableCallback != null)
					{
						mResult = false;
						mRunnableCallback.onDeviceEjected();
						mResult = true;
					}
					
					break;
				}
				case EVENT_EXIT:
				{
					mIsActive = false;
					break;
				}
			}
		}
		
		@Override
		public void run()
		{
			mIsActive = true;
			
			synchronized(this)
			{
				while(mIsActive == true)
				{
					if(mCurrentEvent == null)
					{
						try
						{
							wait();
						}
						catch(InterruptedException e)
						{
							// No need to catch the exception here since we are handling 
							// the message anyhow
						}
					}
					
					handleMessage();
					mCurrentEvent = null;
					
					if(mPendingEvent != null)
					{
						synchronized(mPendingEvent)
						{
							mCurrentEvent = mPendingEvent;
							mPendingEvent = null;
						}
					}
				}
			}
		}
		
		public void postEvent(CallbackEvent event)
		{
			if(mCallback_thread != null)
			{
				if(mCurrentEvent == null && mCallback_thread.getState() == Thread.State.WAITING)
				{
					mCurrentEvent = event;
					
					synchronized(this)
					{
						notify();
					}
				}
				else
				{
					mPendingEvent = event;
				}
			}
		}
	}
	
	public UsbSensor(Context context, UsbSensorManager usbSensorManager, UsbDevice usbDevice, int protocol)
	{
		mContext = context;
		
		mUsbSensorManager = usbSensorManager;
		mUsbDevice = usbDevice;
		
		mPulseId = 0;
		mIsInitialized = false;
		
		switch(protocol)
		{
			case Sensor.PROTOCOL_FTDI:
			{
				// Load the FTDI serial USB driver
				
				mSerialDriver = new FTDISerialUsbDriver(usbSensorManager.getUsbManager(), usbDevice);
				break;
			}
			case Sensor.PROTOCOL_ACM:
			default:
			{
				// Load the ACM serial USB driver (default)
				
				mSerialDriver = new ACMSerialUsbDriver(usbSensorManager.getUsbManager(), usbDevice);
			}
		}
	}
	
	protected abstract Callback baseCallback();
	
	public synchronized Callback getBaseCallback()
	{
		if(mBaseCallback == null)
		{
			mBaseCallback = baseCallback();
		}
		
		return(mBaseCallback);
	}
	
	public int init(int pulseId)
	{
		if(pulseId < 0 || pulseId > Pulse32.PKT_MAX_PAYLOAD_ENTRIES)
		{
			return(ErrorCode.ERR_PARAMS);
		}
		else if(mIsInitialized)
		{
			return(ErrorCode.ERR_STATE);
		}
		
		synchronized(this)
		{
			mPulseId = pulseId;
			mIsInitialized = true;
		}
		
		return(mUsbSensorManager.initSensor(this));
	}
	
	public int uninit()
	{
		int ret = mUsbSensorManager.uninitSensor(this);
		
		if(ret != ErrorCode.NO_ERROR)
		{
			return(ret);
		}
		
		synchronized(this)
		{
			mPulseId = 0;
			mIsInitialized = false;
		}
		
		return(0);
	}
	
	public UsbDevice getDevice()
	{
		return(mUsbDevice);
	}
	
	public SerialUsbDriver getSerialDriver()
	{
		return(mSerialDriver);
	}
	
	public synchronized void setRunnableCallback(Callback callback)
	{
		if(mCallback_runnable != null)
		{
			mCallback_runnable.mRunnableCallback = callback;
		}
	}
	
	public Callback getRunnableCallback()
	{
		if(mCallback_runnable != null)
		{
			return(mCallback_runnable.mRunnableCallback);
		}
		
		return(null);
	}
	
	public synchronized void startCallbackThread(Callback callback)
	{
		if(!mIsInitialized || mCallback_thread != null)
		{
			return;
		}
		
		mCallback_runnable = new CallbackRunnable(callback);
		mCallback_thread = new Thread(mCallback_runnable);
		mCallback_thread.start();
		
		while(mCallback_thread.getState() != Thread.State.WAITING)
		{
			// Busy wait until the thread is in a waiting state
		}
	}
	
	public synchronized void stopCallbackThread()
	{
		if(!mIsInitialized || mCallback_thread == null)
		{
			return;
		}
		
		mCallback_runnable.postEvent(new CallbackEvent(CallbackRunnable.EVENT_EXIT));
		
		try
		{
			mCallback_thread.join();
			
			mCallback_runnable = null;
			mCallback_thread = null;
		}
		catch(InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	public void notifySensorUpdate(Pulse32 pkt)
	{	
		if(mCallback_thread == null)
		{
			// Notify threads should not block on error
			
			return;
		}
		
		mCallback_runnable.postEvent(new CallbackEvent(CallbackRunnable.EVENT_SENSOR_UPDATE, pkt));
	}
	
	public void notifySensorEjected()
	{
		if(mCallback_thread == null)
		{
			// Notify threads should not block on error
			
			return;
		}

		mCallback_runnable.mResult = false;
		mCallback_runnable.postEvent(new CallbackEvent(CallbackRunnable.EVENT_SENSOR_EJECT));
		
		while(!mCallback_runnable.mResult)
		{
			// Busy wait until the ejection event has been taken care of
		}
	}
	
	@Override
	public void finalize()
	{
		uninit();
	}
	
	public boolean isInitialized()
	{
		return(mIsInitialized);
	}
	
	public boolean isCallbackThreadRunning()
	{
		if(mCallback_runnable != null && mCallback_runnable.mIsActive)
		{
			return(true);
		}
		
		return(false);
	}
}
