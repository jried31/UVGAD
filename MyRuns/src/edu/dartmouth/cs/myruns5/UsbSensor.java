package edu.dartmouth.cs.myruns5;

import java.util.concurrent.locks.Lock;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

/**
 * Base class for all USB-based sensor hardware.
 * 
 * @author daniel
 *
 */
public abstract class UsbSensor extends Sensor
{
	private static final String TAG = UsbSensor.Callback.class.getName();
	
	protected final Context mContext;
	
	protected final UsbSensorManager mUsbSensorManager;
	protected final UsbDevice mUsbDevice;
	
	protected Callback mCallback;
	protected CallbackRunnable mCallback_runnable;
	protected Thread mCallback_thread;
	
	private CallbackEvent mPendingEvent;
	
	public interface Callback
	{
		public abstract void onNewData(final byte data[], int length);
		public abstract void onDeviceEjected();
	}
	
	private class CallbackEvent
	{
		private int what;
		private Bundle data;
		
		CallbackEvent(int what)
		{
			this.what = what;
			this.data = null;
		}
		
		CallbackEvent(int what, Bundle data)
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
		
		private static final String KEY_DATA_BUF = "dataBuffer";
		private static final String KEY_DATA_LEN = "dataLen";
		
		private CallbackEvent mCurrentEvent;
		
		private volatile boolean mIsActive;
		
		CallbackRunnable()
		{
			mIsActive = true;
			
			mCurrentEvent = null;
			mPendingEvent = null;
		}
		
		private void handleMessage()
		{
			Bundle data = mCurrentEvent.data;
			
			switch(mCurrentEvent.what)
			{
				case EVENT_SENSOR_UPDATE:
				{
					if(data == null)
					{
						break;
					}
					
					byte buffer[] = data.getByteArray(KEY_DATA_BUF);
					int length = data.getInt(KEY_DATA_LEN);
					
					if(mCallback != null)
					{
						mCallback.onNewData(buffer, length);
					}
					
					break;
				}
				case EVENT_SENSOR_EJECT:
				{
					if(mCallback != null)
					{
						mCallback.onDeviceEjected();
					}

					mIsActive = false;
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
						this.notify();
					}
				}
				else
				{
					mPendingEvent = event;
				}
			}
		}
	}
	
	public UsbSensor(Context context, UsbSensorManager usbSensorManager, UsbDevice usbDevice)
	{
		mContext = context;
		
		mUsbSensorManager = usbSensorManager;
		mUsbDevice = usbDevice;
	}
	
	public UsbDevice getDevice()
	{
		return(mUsbDevice);
	}
	
	public synchronized void startCallbackThread(Callback callback)
	{
		if(mCallback_thread != null)
		{
			return;
		}
		
		mCallback = callback;
		
		mCallback_runnable = new CallbackRunnable();
		mCallback_thread = new Thread(mCallback_runnable);
		mCallback_thread.start();
		
		while(mCallback_thread.getState() != Thread.State.WAITING)
		{
			// Busy wait until the thread is in a waiting state
		}
	}
	
	public synchronized void stopCallbackThread()
	{
		if(mCallback_thread == null)
		{
			return;
		}
		
		mCallback_runnable.postEvent(new CallbackEvent(CallbackRunnable.EVENT_EXIT));
		
		try
		{
			mCallback_thread.join();
			
			mCallback_thread = null;
			mCallback = null;
		}
		catch(InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	public void notifySensorUpdate(final byte buffer[], int length)
	{	
		if(mCallback_thread == null)
		{
			// Notify threads should not block on error
			
			return;
		}

		Bundle data = new Bundle();
		
		data.putByteArray(CallbackRunnable.KEY_DATA_BUF, buffer);
		data.putInt(CallbackRunnable.KEY_DATA_LEN, length);
		
		mCallback_runnable.postEvent(new CallbackEvent(CallbackRunnable.EVENT_SENSOR_UPDATE, data));
	}
	
	public void notifySensorEjected()
	{
		if(mCallback_thread == null)
		{
			// Notify threads should not block on error
			
			return;
		}
		
		mCallback_runnable.postEvent(new CallbackEvent(CallbackRunnable.EVENT_SENSOR_EJECT));
	}
	
	@Override
	public void finalize()
	{
		stopCallbackThread();
	}
}
