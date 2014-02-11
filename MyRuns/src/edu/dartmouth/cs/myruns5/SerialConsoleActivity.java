package edu.dartmouth.cs.myruns5;

import java.io.UnsupportedEncodingException;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class SerialConsoleActivity extends Activity implements OnClickListener
{
	private class LightSensorCallback implements ILightSensor.Callback
	{
		private final Activity mActivity;
		
		LightSensorCallback(Activity activity)
		{
			mActivity = activity;
		}
		
		@Override
		public void onSensorUpdate(final byte data[], int length)
		{
			try
			{
				final String msg = new String(data, 0, length, "UTF-8");
				
				mActivity.runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						mConsole_text.setText(msg);
					}
				});
			}
			catch(UnsupportedEncodingException e)
			{
				e.printStackTrace();
			}
		}
		
		@Override
		public void onSensorUpdate(int updateLux)
		{
			Toast.makeText(mContext, "Light sensor update!", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onSensorEjected()
		{
			Toast.makeText(mContext, "Light sensor ejected!", Toast.LENGTH_SHORT).show();
			
			mIsStreaming = false;
			toggleStreaming_btn.setText(mResources.getString(R.string.startStreaming));
		}
	}
	
	private class UVSensorCallback implements IUVSensor.Callback
	{
		private final Activity mActivity;
		
		UVSensorCallback(Activity activity)
		{
			mActivity = activity;
		}
		
		@Override
		public void onSensorUpdate(final byte data[], int length)
		{
			try
			{
				final String msg = new String(data, 0, length, "UTF-8");
				
				mActivity.runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						mClear_btn.setText(msg);
					}
				});
			}
			catch(UnsupportedEncodingException e)
			{
				e.printStackTrace();
			}
		}
		
		@Override
		public void onSensorUpdate(int updateLux)
		{
			Toast.makeText(mContext, "UV sensor update!", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onSensorEjected()
		{
			Toast.makeText(mContext, "UV sensor ejected!", Toast.LENGTH_SHORT).show();
		}
	}
	
	private Context mContext;
	private Resources mResources;
	private UsbSensorManager mUsbSensorManager;
	
	private volatile TextView mConsole_text;
	private Button mDeviceStatus_btn;
	private Button toggleStreaming_btn;
	private Button mClear_btn;
	
	private ILightSensor mLightSensor;
	private LightSensorCallback mLightSensorCallback;
	
	private IUVSensor mUVSensor;
	private UVSensorCallback mUVSensorCallback;
	
	private boolean mIsStreaming;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_serial_console);
		
		mContext = this;
		mResources = getResources();
		mUsbSensorManager = MyRunsApplication.getUsbSensorManager();
		
		mConsole_text = (TextView) findViewById(R.id.console_text);
		mDeviceStatus_btn = (Button) findViewById(R.id.deviceStatus_btn);
		toggleStreaming_btn = (Button) findViewById(R.id.toggleStreaming_btn);
		mClear_btn = (Button) findViewById(R.id.clear_btn);
		
		mDeviceStatus_btn.setOnClickListener(this);
		toggleStreaming_btn.setOnClickListener(this);
		mClear_btn.setOnClickListener(this);
		
		mLightSensorCallback = new LightSensorCallback(this);
		mUVSensorCallback = new UVSensorCallback(this);
		
		mIsStreaming = false;
		toggleStreaming_btn.setText(mResources.getString(R.string.startStreaming));
	}
	
	@Override
	public void onClick(View view)
	{
		switch(view.getId())
		{
			case R.id.deviceStatus_btn:
			{
				List<IUVSensor> uvSensorList = mUsbSensorManager.getUVSensorList();
				List<ILightSensor> lightSensorList = mUsbSensorManager.getLightSensorList();
				
				if((uvSensorList.size() > 0) && (lightSensorList.size() > 0))
				{
					Toast.makeText(this, "Found Light + UV sensor!", Toast.LENGTH_SHORT).show();
				}
				else if(lightSensorList.size() > 0)
				{
					Toast.makeText(this, "Found Light sensor!", Toast.LENGTH_LONG).show();
				}
				else if(uvSensorList.size() > 0)
				{
					Toast.makeText(this, "Found UV sensor!", Toast.LENGTH_LONG).show();
				}
				else
				{
					Toast.makeText(this, "Could not find valid sensor!", Toast.LENGTH_SHORT).show();
				}
				
				break;
			}
			case R.id.toggleStreaming_btn:
			{
				if(!mIsStreaming)
				{
					List<ILightSensor> lightSensor_list = mUsbSensorManager.getLightSensorList();
					List<IUVSensor> uvSensor_list = mUsbSensorManager.getUVSensorList();
					
					if(lightSensor_list.isEmpty() || uvSensor_list.isEmpty())
					{
						return;
					}
					
					mLightSensor = lightSensor_list.get(0);
					mLightSensor.register(mLightSensorCallback);
					
					mUVSensor = uvSensor_list.get(0);
					mUVSensor.register(mUVSensorCallback);
					
					mIsStreaming = true;
					toggleStreaming_btn.setText(mResources.getString(R.string.stopStreaming));
				}
				else
				{
					if(mLightSensor != null)
					{
						mLightSensor.unregister();
						mUVSensor.unregister();
					}
					
					mIsStreaming = false;
					toggleStreaming_btn.setText(mResources.getString(R.string.startStreaming));
				}
				
				break;
			}
			case R.id.clear_btn:
			{
				mConsole_text.setText("");
				break;
			}
			default:
			{
				
			}
		}
	}
}
