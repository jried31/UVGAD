package edu.dartmouth.cs.myruns5;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
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
		public void onSensorUpdate(final int updateLux)
		{
			mActivity.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					lightSensor0_text.setText("LUX: " + updateLux);
				}
			});
		}

		@Override
		public void onSensorEjected()
		{
			mActivity.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					Toast.makeText(mContext, "Light sensor ejected!", Toast.LENGTH_SHORT).show();
				}
			});
			
			mLightSensor = null;
			mIsStreaming = false;
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
		public void onSensorUpdate(final int updateUV)
		{
			
			mActivity.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					uvSensor0_text.setText("UV: " + updateUV);
				}
			});
		}

		@Override
		public void onSensorEjected()
		{
			mActivity.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					Toast.makeText(mContext, "UV sensor ejected!", Toast.LENGTH_SHORT).show();
				}
			});
			
			mUVSensor = null;
		}
	}
	
	private Context mContext;
	private Resources mResources;
	private UsbSensorManager mUsbSensorManager;
	
	private TextView uvSensor0_text;
	private TextView lightSensor0_text;
	
	private Button loadSensor_btn;
	private Button toggleStream_btn;
	private Button instantSample_btn;
	
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
		
		uvSensor0_text = (TextView) findViewById(R.id.uvSensor0_text);
		lightSensor0_text = (TextView) findViewById(R.id.lightSensor0_text);
		
		loadSensor_btn = (Button) findViewById(R.id.loadSensor_btn);
		toggleStream_btn = (Button) findViewById(R.id.toggleStream_btn);
		instantSample_btn = (Button) findViewById(R.id.instantSample_btn);
		
		loadSensor_btn.setOnClickListener(this);
		toggleStream_btn.setOnClickListener(this);
		instantSample_btn.setOnClickListener(this);
		
		mLightSensorCallback = new LightSensorCallback(this);
		mUVSensorCallback = new UVSensorCallback(this);
		
		mIsStreaming = false;
		toggleStream_btn.setText(mResources.getString(R.string.startStreaming));
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		
		if(mLightSensor != null)
		{
			mLightSensor.unregister();
		}
		
		if(mUVSensor != null)
		{
			mUVSensor.unregister();
		}
	}
	
	@Override
	public void onClick(View view)
	{
		switch(view.getId())
		{
			case R.id.loadSensor_btn:
			{
				List<IUVSensor> uvSensor_list = mUsbSensorManager.getUVSensorList();
				List<ILightSensor> lightSensor_list = mUsbSensorManager.getLightSensorList();
				
				if(uvSensor_list.isEmpty() || lightSensor_list.isEmpty())
				{
					Toast.makeText(this, "ERROR: Sensor hardware not detected", Toast.LENGTH_LONG).show();
					return;
				}
				
				mLightSensor = lightSensor_list.get(0);
				mUVSensor = uvSensor_list.get(0);
				
				mLightSensor.init(Constants.PULSE_ID_LIGHT_0);
				mUVSensor.init(Constants.PULSE_ID_UV_0);
				
				Toast.makeText(this, "Initialized sensor hardware!", Toast.LENGTH_LONG).show();
				break;
			}
			case R.id.toggleStream_btn:
			{
				if(mLightSensor == null || mUVSensor == null)
				{
					Toast.makeText(this, "ERROR: Sensor hardware not initialized", Toast.LENGTH_LONG).show();
					return;
				}
				
				if(!mIsStreaming)
				{
					mLightSensor.register(mLightSensorCallback);
					mUVSensor.register(mUVSensorCallback);
					
					mIsStreaming = true;
					toggleStream_btn.setText(mResources.getString(R.string.stopStreaming));
				}
				else
				{
					mLightSensor.unregister();
					mUVSensor.unregister();
					
					mIsStreaming = false;
					toggleStream_btn.setText(mResources.getString(R.string.startStreaming));
				}
				
				break;
			}
			case R.id.instantSample_btn:
			{
				if(mLightSensor == null || mUVSensor == null)
				{
					Toast.makeText(this, "ERROR: Sensor hardware not initialized", Toast.LENGTH_LONG).show();
					return;
				}
				
				lightSensor0_text.setText("iLUX: " + mLightSensor.getLuminosity());
				uvSensor0_text.setText("iUV: " + mUVSensor.getUV());
				
				break;
			}
			default:
			{
				
			}
		}
	}
}
