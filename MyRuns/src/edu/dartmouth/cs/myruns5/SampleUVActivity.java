package edu.dartmouth.cs.myruns5;

import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.parse.ParseGeoPoint;

import edu.dartmouth.cs.myruns5.util.uv.ParseUVReading;

import edu.repo.ucla.serialusbdriver.*;

public class SampleUVActivity extends Activity implements OnClickListener,GooglePlayServicesClient.ConnectionCallbacks,GooglePlayServicesClient.OnConnectionFailedListener 
{
	int uv1=-1,uv2=-1,uv=0;
	// This is the callback object for the first light sensor
	private class LightSensor0Callback implements ILightSensor.Callback
	{
		private final Activity mActivity;
		
		LightSensor0Callback(Activity activity)
		{
			mActivity = activity;
		}
		
		@Override
		public void onSensorUpdate(final int updateLux)
		{
			// This callback method is invoked when the light sensor gets a new light reading data
			
			// All UI updates MUST occur on the main thread (a.k.a. UI thread) so we update the
			// light sensor TextView object using this Runnable
			mActivity.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					lightSensor0_text.setText("LUX0: " + updateLux);
				}
			});
		}

		@Override
		public void onSensorEjected()
		{
			// This function is run when the sensor is forcibly ejected while this callback object 
			// is active and registered with the sensor
			
			mActivity.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					Toast.makeText(mContext, "Light sensor ejected!", Toast.LENGTH_SHORT).show();
				}
			});
			
			mLightSensor0 = null;
		}
	}
	
	// This is the callback object for the second light sensor
	private class LightSensor1Callback implements ILightSensor.Callback
	{
		private final Activity mActivity;
		
		LightSensor1Callback(Activity activity)
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
					lightSensor1_text.setText("LUX1: " + updateLux);
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
			
			mLightSensor1 = null;
		}
	}
	
	// This is the callback object for the first UV sensor
	private class UVSensor0Callback implements IUVSensor.Callback
	{
		private final Activity mActivity;
		
		UVSensor0Callback(Activity activity)
		{
			mActivity = activity;
		}
		
		@Override
		public void onSensorUpdate(final int updateUV)
		{
			// This callback method is invoked when the UV sensor gets a new UV reading data
			
			// All UI updates MUST occur on the main thread (a.k.a. UI thread) so we update the
			// UV sensor TextView object using this Runnable
			mActivity.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					uv1=updateUV;
					uvSensor0_text.setText("UV0: " + updateUV);
				}
			});
		}

		@Override
		public void onSensorEjected()
		{
			// This function is run when the sensor is forcibly ejected while this callback object 
			// is active and registered with the sensor
			
			mActivity.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					Toast.makeText(mContext, "UV sensor ejected!", Toast.LENGTH_SHORT).show();
				}
			});
			
			mUVSensor0 = null;
		}
	}
	
	// This is the callback object for the second UV sensor
	private class UVSensor1Callback implements IUVSensor.Callback
	{
		private final Activity mActivity;
		
		UVSensor1Callback(Activity activity)
		{
			mActivity = activity;
		}
		
		@Override
		public void onSensorUpdate(final int updateUV)
		{
			// This callback method is invoked when the UV sensor gets a new UV reading data
			
			// All UI updates MUST occur on the main thread (a.k.a. UI thread) so we update the
			// UV sensor TextView object using this Runnable
			mActivity.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					uv2=updateUV;
					uvSensor1_text.setText("UV1: " + updateUV);
				}
			});
		}

		@Override
		public void onSensorEjected()
		{
			// This function is run when the sensor is forcibly ejected while this callback object 
			// is active and registered with the sensor
			
			mActivity.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					Toast.makeText(mContext, "UV sensor ejected!", Toast.LENGTH_SHORT).show();
				}
			});
			
			mUVSensor1 = null;
			mIsStreaming = false;
		}
	}
	
	public void uploadSample(View v){
		uv=Math.max(uv1, uv2);
		if(uv > 0){
			location = this.mLocationClient.getLastLocation();
			Date timestamp = new Date();
			
			ParseUVReading reading = new ParseUVReading();
			reading.setUVI(uv);
			reading.setLocation(new ParseGeoPoint(location.getLatitude(),location.getLongitude()));
			reading.setTimestamp(timestamp);
			reading.saveInBackground();
		}
	}

	private Context mContext;
	private Resources mResources;
	private UsbSensorManager mUsbSensorManager;
	
	private TextView uvSensor0_text;
	private TextView uvSensor1_text;
	private TextView lightSensor0_text;
	private TextView lightSensor1_text;
	
	private Button loadSensor_btn;
	private Button toggleStream_btn;
	private Button instantSample_btn;
	private Button sampleUVBtn;
	private RadioGroup radioGroup;
	private final RadioButton[] radioBtns = new RadioButton[5];
	private final String[] mLabels = {Globals.CLASS_LABEL_IN_SHADE,Globals.CLASS_LABEL_IN_SUN,Globals.CLASS_LABEL_IN_CLOUD,Globals.CLASS_LABEL_OTHER};

	
	private ILightSensor mLightSensor0;
	private ILightSensor mLightSensor1;
	private LightSensor0Callback mLightSensor0Callback;
	private LightSensor1Callback mLightSensor1Callback;
	
	private IUVSensor mUVSensor0;
	private IUVSensor mUVSensor1;
	private UVSensor0Callback mUVSensor0Callback;
	private UVSensor1Callback mUVSensor1Callback;
	
	private boolean mIsStreaming;
	private Location location = null;
	private LocationClient mLocationClient; 
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_serial_console);

        mLocationClient = new LocationClient(this, this, this);

		mContext = this;
		mResources = getResources();
		mUsbSensorManager = UsbSensorManager.getManager();
		
		uvSensor0_text = (TextView) findViewById(R.id.uvSensor0_text);
		uvSensor1_text = (TextView) findViewById(R.id.uvSensor1_text);
		lightSensor0_text = (TextView) findViewById(R.id.lightSensor0_text);
		lightSensor1_text = (TextView) findViewById(R.id.lightSensor1_text);
		
		loadSensor_btn = (Button) findViewById(R.id.loadSensor_btn);
		toggleStream_btn = (Button) findViewById(R.id.toggleStream_btn);
		instantSample_btn = (Button) findViewById(R.id.instantSample_btn);
		sampleUVBtn = (Button) findViewById(R.id.sampleUVBtn);

		radioGroup = (RadioGroup) findViewById(R.id.radioGroupLabels);
		radioBtns[0] = (RadioButton) findViewById(R.id.radioShade);
		radioBtns[1] = (RadioButton) findViewById(R.id.radioSun);
		radioBtns[2] = (RadioButton) findViewById(R.id.radioCloud);
		radioBtns[3] = (RadioButton) findViewById(R.id.radioOther);
		
		loadSensor_btn.setOnClickListener(this);
		toggleStream_btn.setOnClickListener(this);
		instantSample_btn.setOnClickListener(this);
		sampleUVBtn.setOnClickListener(this);
		
		// Create the sensor callback objects
		mLightSensor0Callback = new LightSensor0Callback(this);
		mLightSensor1Callback = new LightSensor1Callback(this);
		mUVSensor0Callback = new UVSensor0Callback(this);
		mUVSensor1Callback = new UVSensor1Callback(this);
		
		mIsStreaming = false;
		toggleStream_btn.setText(mResources.getString(R.string.startStreaming));
	}
	
	
	@Override
	public void onResume() {
	    super.onResume();
        mLocationClient.connect();
	}
	@Override
	public void onPause() {
	    super.onPause();
        mLocationClient.disconnect();
	}
    /*
     * Called when the Activity becomes visible.
     */
    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        mLocationClient.connect();
    }
    /*
     * Called when the Activity is no longer visible.
     */
    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        mLocationClient.disconnect();
        super.onStop();
    }
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		
		// Unregister the sensors when the Activity is destroyed if they are active
		if(mLightSensor0 != null)
		{
			mLightSensor0.unregister();
		}
		
		if(mLightSensor1 != null)
		{
			mLightSensor1.unregister();
		}
		
		if(mUVSensor0 != null)
		{
			mUVSensor0.unregister();
		}
		
		if(mUVSensor1 != null)
		{
			mUVSensor1.unregister();
		}
        mLocationClient.disconnect();
	}
	
	@Override
	public void onClick(View view)
	{
		switch(view.getId())
		{
			case R.id.loadSensor_btn:
			{
				// Get the handle on the sensor and load their serial drivers from the 
				// UsbSensorManager.
				
				// Get the UV and light sensors that the UsbSensorManager recognizes
				List<IUVSensor> uvSensor_list = mUsbSensorManager.getUVSensorList();
				List<ILightSensor> lightSensor_list = mUsbSensorManager.getLightSensorList();
				
				// Make sure that the lists aren't empty
				if(uvSensor_list.isEmpty() || lightSensor_list.isEmpty())
				{
					Toast.makeText(this, "ERROR: Sensor hardware not detected", Toast.LENGTH_LONG).show();
					return;
				}
				
				// Grab the first pair of sensor objects.  On an Android phone there really shouldn't 
				// be more than one
				mLightSensor0 = lightSensor_list.get(0);
				mUVSensor0 = uvSensor_list.get(0);
				
				// @NOTE: We need to grab a new list of sensors since Java must create a new sensor 
				//    	  object.  Otherwise, they'll refer to the same sensor object and invoking 
				// 		  the register() method will overwrite the original callback object.
				// 		  Another way to think about it is the getLightSensorList() method is like a 
				// 		  factory that returns light sensor objects so we need it to create a new 
				// 		  object for us.  The same applies to the UV sensor list.
				uvSensor_list = mUsbSensorManager.getUVSensorList();
				lightSensor_list = mUsbSensorManager.getLightSensorList();
				
				// Make sure that the lists aren't empty
				if(uvSensor_list.isEmpty() || lightSensor_list.isEmpty())
				{
					Toast.makeText(this, "ERROR: Sensor hardware not detected", Toast.LENGTH_LONG).show();
					return;
				}
				
				// Grab the handle to the UV sensor and second light sensor objects
				mLightSensor1 = lightSensor_list.get(0);
				mUVSensor1 = uvSensor_list.get(0);
				
				// Initialize the UV sensor and light sensor objects
				mLightSensor0.init(Constants.PULSE_ID_LIGHT_0);
				mLightSensor1.init(Constants.PULSE_ID_LIGHT_1);
				mUVSensor0.init(Constants.PULSE_ID_UV_0);
				mUVSensor1.init(Constants.PULSE_ID_UV_1);
				
				Toast.makeText(this, "Initialized sensor hardware!", Toast.LENGTH_LONG).show();
				break;
			}
			case R.id.toggleStream_btn:
			{
				if(mLightSensor0 == null || mLightSensor1 == null || mUVSensor0 == null || mUVSensor1 == null)
				{
					Toast.makeText(this, "ERROR: Sensor hardware not initialized", Toast.LENGTH_LONG).show();
					return;
				}
				
				if(!mIsStreaming)
				{
					// Register the respective callback objects with the sensor objects.
					// Ideally, you should do some error checking here...
					mLightSensor0.register(mLightSensor0Callback);
					mLightSensor1.register(mLightSensor1Callback);
					mUVSensor0.register(mUVSensor0Callback);
					mUVSensor1.register(mUVSensor1Callback);
					
					mIsStreaming = true;
					toggleStream_btn.setText(mResources.getString(R.string.stopStreaming));
				}
				else
				{
					// Unregister the callback objects to stop the streaming functionality
					mLightSensor0.unregister();
					mLightSensor1.unregister();
					mUVSensor0.unregister();
					mUVSensor1.unregister();
					
					mIsStreaming = false;
					toggleStream_btn.setText(mResources.getString(R.string.startStreaming));
				}
				
				break;
			}
			case R.id.instantSample_btn:
			{
				if(mLightSensor0 == null || mLightSensor1 == null || mUVSensor0 == null || mUVSensor1 == null)
				{
					Toast.makeText(this, "ERROR: Sensor hardware not initialized", Toast.LENGTH_LONG).show();
					return;
				}
				
				// Update the screen to show the instantaneous sensor values
				uvSensor0_text.setText("iUV0: " + mUVSensor0.getUV());
				uvSensor1_text.setText("iUV1: " + mUVSensor1.getUV());
				lightSensor0_text.setText("iLUX0: " + mLightSensor0.getLuminosity());
				lightSensor1_text.setText("iLUX1: " + mLightSensor1.getLuminosity());
				
				break;
			}
			case R.id.sampleUVBtn:
			{
				if(mUVSensor0 == null || mUVSensor1 == null)
				{
					Toast.makeText(this, "ERROR: Sensor hardware not initialized", Toast.LENGTH_LONG).show();
					return;
				}
				
				int uv1 = mUVSensor0.getUV(),uv2 = mUVSensor1.getUV(),uv = Math.max(uv1,uv2);
				uvSensor0_text.setText("iUV0: " +uv1);
				uvSensor1_text.setText("iUV1: " +uv2);

				int acvitivtyId = radioGroup.indexOfChild(findViewById(radioGroup.getCheckedRadioButtonId()));
				String environment = mLabels[acvitivtyId];
				
				location = this.mLocationClient.getLastLocation();
				Date timestamp = new Date();
				
				ParseUVReading reading = new ParseUVReading();
				reading.setUVI(uv);
				reading.setLocation(new ParseGeoPoint(location.getLatitude(),location.getLongitude()));
				reading.setTimestamp(timestamp);
				reading.setEnvironment(environment);
				reading.saveInBackground();
			}
			default:
			{
			}
		}
	}
	/*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle dataBundle) {
        // Display the connection status
        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
    }
    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    @Override
    public void onDisconnected() {
        // Display the connection status
        Toast.makeText(this, "Disconnected. Please re-connect.",Toast.LENGTH_SHORT).show();
    }
    /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this,CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            Toast.makeText(this, connectionResult.getErrorCode(),Toast.LENGTH_SHORT).show();
        }
    }

}
