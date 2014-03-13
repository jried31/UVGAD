package edu.dartmouth.cs.myruns5;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.graphics.Rect;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.VisibleRegion;

import edu.dartmouth.cs.myruns5.TrackingService.TrackingBinder;


public class MapDisplayActivity extends Activity {

		public static final String INPUT_TYPE = "input type";
	/****************** member vars *******************/
	private TrackingBinder mTrackingBinder;
	
	private boolean mBound;
	
	private TrackingServiceReceiver receiver = new TrackingServiceReceiver();
	private MotionUpdateReceiver mMotionUpdateReceiver = new MotionUpdateReceiver();
	//private LightingClassificationReceiver mLightingClassReceiver = new LightingClassificationReceiver();
	
	private GoogleMap mMap;

	public Context mContext;
	
	public TextView typeStats;
	public TextView lightingType;
	public TextView lightingType_Arduino;
	public TextView avgspeedStats;
	public TextView curspeedStats;
	public TextView climbStats;
	public TextView caloriesStats;
	public TextView distanceStats;
	PolylineOptions rectOptions;
	Polyline polyline;

	 
	public ArrayList<Location> mLocationList;
	private ArrayList<LatLng> mLatLngList;
	
	public ExerciseEntryHelper mEntryHelper;
	public ExerciseEntry mEntry;
	
	private int mTaskType;
	private int mInputType;
	private int mInferredActivityType;
	private String mSweatRate;
	
	public static final int MENU_ID_DELETE = 0;
	
    public boolean mFirstLoc;
    public Marker curMarker;
    
    private double mDistance;
    private double mAvgSpeed;
    private int mCalories;
    private double mStartTime;
    private double mClimb;
    private double mCurSpeed;
    private double mDuration;
    
    public LatLng firstLatLng;


	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder binder) {
			mTrackingBinder = (TrackingBinder) binder;
			// bind the list with TrackingService obj's mLocationList, using the binder's
			// public method
			mLocationList = mTrackingBinder.getService().mLocationList;
			mBound = true;
		}

		public void onServiceDisconnected(ComponentName className) {
			mBound = false;
		}
	};

	
	/******************* methods ********************/
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map_display);
		
		Button sendButton = (Button) findViewById(R.id.button_send_data);
		sendButton.setVisibility(View.GONE);
		sendButton.setClickable(false);
		
		
		// initialize member variables
		mBound = false;
		mLatLngList = new ArrayList<LatLng>();
		mContext = this;
		mFirstLoc = true;
		
		// mark: init views here
		typeStats = (TextView) findViewById(R.id.type_stats);
		lightingType = (TextView) findViewById(R.id.lightingType);
		lightingType_Arduino = (TextView) findViewById(R.id.lightingType_Arduino);
		avgspeedStats = (TextView) findViewById(R.id.avg_speed_stats);
		curspeedStats = (TextView) findViewById(R.id.cur_speed_stats);
		climbStats = (TextView) findViewById(R.id.climb_stats_stats);
		caloriesStats = (TextView) findViewById(R.id.calories_stats);
		distanceStats = (TextView) findViewById(R.id.distance_stats);
		
		mEntryHelper = new ExerciseEntryHelper();
		// Get extras from intent and set the mTaskType, InputType, Row Id and ActivityType
		Intent intent = getIntent();
		mTaskType = intent.getIntExtra(MainActivity.TASK_TYPE, -1);
		mInputType = intent.getIntExtra(MainActivity.INPUT_TYPE, -1);
		mInferredActivityType = intent.getIntExtra(MainActivity.ACTIVITY_TYPE, -1);
		//Lohith
		//mSweatRate = intent.getIntExtra(MainActivity.SWEAT_RATE,-1);
		// mark: row id ?
		
		mDistance = 0;
		mCalories = 0;
		mAvgSpeed = 0;
		mSweatRate = "";
		mStartTime = 0;
		mClimb = 0;
		mDuration = 0;
		
		// init mEntry
		mEntry = new ExerciseEntry();
		mEntry.setActivityType(mInferredActivityType);
		mEntry.setSweatRate(mSweatRate);
		mEntry.setInputType(mInputType);
		
		FragmentManager myFragmentManager = getFragmentManager();
		MapFragment mMapFragment = (MapFragment)myFragmentManager.findFragmentById(R.id.map);
		mMap = mMapFragment.getMap();
		
		switch (mTaskType) {

		case Globals.TASK_TYPE_NEW:		

			intent = new Intent(this, TrackingService.class);
			intent.putExtra(INPUT_TYPE, mInputType);
			bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
			startService(intent);
			break;

		case Globals.TASK_TYPE_HISTORY:
			Log.d(null, "history 1");

			// remove buttons
			Button saveButton = (Button) findViewById(R.id.button_map_save);
			saveButton.setVisibility(View.GONE);
			Button cancelButton = (Button) findViewById(R.id.button_map_cancel);
			cancelButton.setVisibility(View.GONE);
			
			// Add send data button
			sendButton.setVisibility(View.VISIBLE);
			sendButton.setClickable(true);

			// Read track from database
			intent = getIntent();
			mLocationList = intent.getParcelableArrayListExtra(HistoryFragment.TRACK);
				
			// sanity check
			if (mLocationList == null){
				Log.d(null, "this should not happen");
				return;
			}
			Log.d(null, "history 2");

			// convert
			for (int i = 0; i < mLocationList.size(); i++)
				mLatLngList.add(Utils.fromLocationToLatLng(mLocationList.get(i)));
			
			boolean draw = mLocationList.size()!=0;
			if (draw)
				firstLatLng = Utils.fromLocationToLatLng(mLocationList.get(0));
			Log.d(null, "history 3");

			// draw markers
			if (draw){
			mMap.addMarker(new MarkerOptions().position(firstLatLng).title("Start Point"));
			mMap.addMarker(new MarkerOptions().position(mLatLngList.get(mLatLngList.size()-1)).
					title("You Are Here").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
			
			
			// move camera
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLatLng, 17));
			mMap.animateCamera(CameraUpdateFactory.zoomTo(17), 2000, null); 
			
			// draw trace
			rectOptions = new PolylineOptions();
			rectOptions.color(Color.RED);
			rectOptions.addAll(mLatLngList);
			polyline = mMap.addPolyline(rectOptions);
					
			mLatLngList.clear();
			}
			
			// read stats
			intent = getIntent();
			String type  = Globals.TYPE_STATS + intent.getStringExtra(HistoryFragment.ACTIVITY_TYPE);
			String light = Globals.LIGHT_TYPE_HEADER + "I made a string!";
			String avgSpeed = Globals.AVG_SPEED_STATS + String.format("%1$.2f", 
					Double.parseDouble(intent.getStringExtra(HistoryFragment.AVG_SPEED))) + " meters / sec";
			String curSpeed = Globals.CUR_SPEED_STATS + "0" + " meters / sec";
			String climb = Globals.CLIMB_STATS + String.format("%1$.2f", 
					Double.parseDouble(intent.getStringExtra(HistoryFragment.CLIMB))) + " meters";
			String calories = Globals.CALORIES_STATS + intent.getStringExtra(HistoryFragment.CALORIE);
			String distance = Globals.DISTANCE_STATS + String.format("%1$.2f", 
					Double.parseDouble(intent.getStringExtra(HistoryFragment.DISTANCE))) + " meters";
			
			typeStats.setText(type);
			lightingType.setText(light);
			lightingType_Arduino.setText(light);
			avgspeedStats.setText(avgSpeed);
			curspeedStats.setText(curSpeed);
			climbStats.setText(climb);
			caloriesStats.setText(calories);
			distanceStats.setText(distance);
			break;
		default:
			Toast.makeText(getApplicationContext(), "should not happen", Toast.LENGTH_SHORT).show();
			finish(); // Should never happen.
			return;
		}
	}
	
	@Override
	public void onDestroy(){	
		if (mBound){
			unbindService(mConnection);
			stopService(new Intent(this, TrackingService.class));
		}
		super.onDestroy();
	}
	
	@Override
	public void onPause(){
		
		
		
		if (mTaskType == Globals.TASK_TYPE_NEW){
			unregisterReceiver(receiver);
			if (mInputType == Globals.INPUT_TYPE_AUTOMATIC)
			{
				unregisterReceiver(mMotionUpdateReceiver);
				//unregisterReceiver(mLightingClassReceiver);
			}
			
		}
		super.onPause();
	
	}
	
	@Override
	public void onResume(){
		super.onResume();
		
		if (mTaskType == Globals.TASK_TYPE_NEW){
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(TrackingService.ACTION_TRACKING);
			registerReceiver(receiver, intentFilter);
			
			if(mInputType == Globals.INPUT_TYPE_AUTOMATIC){
				intentFilter = new IntentFilter();
				intentFilter.addAction(TrackingService.ACTION_MOTION_UPDATE);
				registerReceiver(mMotionUpdateReceiver, intentFilter);
				//IntentFilter intentFilter2 = new IntentFilter();
				//intentFilter2.addAction(TrackingService.LIGHTING_CLASS_UPDATE);
				//registerReceiver(mLightingClassReceiver, intentFilter);
			}
		}
	}
	
	/******************* button listeners ******************/
	public void onSaveClicked(View v) {
		// disable the button
		Button button = (Button) findViewById(R.id.button_map_save);
		button.setClickable(false);

		// Insert entry into db
		long id=0;
		mEntry.setAvgSpeed(mAvgSpeed);
		mEntry.setCalorie(mCalories);
		mEntry.setClimb(mClimb);
		mEntry.setDistance(mDistance);
		mEntry.setLocationList(mLocationList);
		mEntry.setDuration((int)mDuration);
		mEntry.setActivityType(mInferredActivityType);
		mEntry.setSweatRate(mSweatRate);
		
		SharedPreferences sharedPref = mContext.getSharedPreferences(Globals.TAG, Context.MODE_PRIVATE);
		mEntry.setGender(sharedPref.getInt(mContext.getString(R.string.data_Gender), 0));
		mEntry.setSkinTone(SpriteSkinType.positionToSkinType[sharedPref.getInt(mContext.getString(R.string.data_SkinTone), 0)]);
		mEntry.setSPF(SpriteSPF.positionToSPF[sharedPref.getInt(mContext.getString(R.string.data_SPF), 0)]);
		mEntry.setClothingCover(sharedPref.getFloat(mContext.getString(R.string.data_ClothingCover), 0.0f));
		
		/*
		String clothingValue = sharedPref.getString(
				mContext.getString(R.string.data_Hat), SpriteHeadApparel.HeadApparelType.NONE.name());
		mEntry.setHeadApparel(SpriteHeadApparel.HeadApparelType.valueOf(clothingValue));
		clothingValue = sharedPref.getString(
				mContext.getString(R.string.data_ApparelTop), SpriteUpperApparel.UpperApparelType.NONE.name());
		mEntry.setUpperApparel(SpriteUpperApparel.UpperApparelType.valueOf(clothingValue));
		clothingValue = sharedPref.getString(
				mContext.getString(R.string.data_ApparelBottom), SpriteLowerApparel.LowerApparelType.NONE.name());
		mEntry.setLowerApparel(SpriteLowerApparel.LowerApparelType.valueOf(clothingValue));
		*/
		
		mEntryHelper = new ExerciseEntryHelper(mEntry);
		id = mEntryHelper.insertToDB(this);
		if (id > 0) 
			Toast.makeText(getApplicationContext(), "Entry #" + id + " saved.",
					Toast.LENGTH_SHORT).show();
		
		// stop service
		// notification has flag auto_cancel set
		Intent intent = new Intent(this, TrackingService.class);
		if(mBound){
			unbindService(mConnection);
			mBound = false;
		}
		stopService(intent);
		
		// go back to MainActivity
		finish();
	}

	public void onCancelClicked(View v) {
		Intent intent = new Intent(this, TrackingService.class);
		if(mBound){
			unbindService(mConnection);
			mBound = false;
		}
		stopService(intent);
		// notification has flag auto_cancel set
		finish();
	}
	
	public void onSendData(View v) {
		int val;
		
		Intent intent = getIntent();
		JSONObject json = new JSONObject();
		try {
			json.put("id", intent.getIntExtra(HistoryFragment.ROW_ID, 0));
			json.put("android_id", Secure.getString(getApplicationContext().getContentResolver(), Secure.ANDROID_ID));
			json.put(Globals.KEY_DATE_TIME, intent.getStringExtra(HistoryFragment.DATE_TIME));
			json.put(Globals.KEY_ACTIVITY_TYPE, intent.getStringExtra(HistoryFragment.ACTIVITY_TYPE));
			json.put(Globals.KEY_GENDER, intent.getIntExtra(HistoryFragment.GENDER, 0));
			json.put(Globals.KEY_DISTANCE, intent.getStringExtra(HistoryFragment.DISTANCE));
			json.put(Globals.KEY_DURATION, intent.getStringExtra(HistoryFragment.DURATION));
			json.put(Globals.KEY_CLOTHING_COVER, intent.getFloatExtra(HistoryFragment.CLOTHING_COVER, 0.0f));
			json.put(Globals.KEY_SKIN_TONE, intent.getIntExtra(HistoryFragment.SKIN_TONE, 1));
			json.put(Globals.KEY_SPF, intent.getIntExtra(HistoryFragment.SPF, 0));
			
			ArrayList<Location> locations = intent.getParcelableArrayListExtra(HistoryFragment.TRACK);
			// FAKE DATA
			Location tempA = new Location("");
			tempA.setLongitude(44.56);
			tempA.setLatitude(36.33);
			locations.add(tempA);
			Location tempB = new Location("");
			tempB.setLongitude(66.14);
			tempB.setLatitude(39.04);
			locations.add(tempB);
			
			ArrayList<JSONObject> locationData = new ArrayList<JSONObject>();
			for (Location loc : locations) {
				JSONObject map = new JSONObject();
				map.put("longitude", loc.getLongitude());
				map.put("latitude", loc.getLatitude());
				locationData.add(map);
			}
			json.put("tracking_data", new JSONArray(locationData));
			
			/*
			val = Integer.parseInt(intent.getStringExtra(HistoryFragment.HEAD_APPAREL));
			json.put(Globals.KEY_HEAD_APPAREL, SpriteHeadApparel.HeadApparelType.valueOf(val).name());
			
			val = Integer.parseInt(intent.getStringExtra(HistoryFragment.UPPER_APPAREL));
			json.put(Globals.KEY_UPPER_APPAREL, SpriteUpperApparel.UpperApparelType.getTypeFromValue(val).name());
			
			val = Integer.parseInt(intent.getStringExtra(HistoryFragment.LOWER_APPAREL));
			json.put(Globals.KEY_LOWER_APPAREL, SpriteLowerApparel.LowerApparelType.getTypeFromValue(val).name());
			*/
			
			new HTTPSender().execute(json);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		finish();
	}
	
	@Override
	public void onBackPressed() {
		// When back is pressed, similar to onCancelClicked, stop service and the notification.
		if (mTaskType == Globals.TASK_TYPE_NEW) {
			Intent intent = new Intent(this, TrackingService.class);
			if(mBound){
				unbindService(mConnection);
				mBound = false;
			}
			stopService(intent);
			// notification has flag auto_cancel set
			finish();		
		}

		
		super.onBackPressed();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// If task type is displaying history, also give a menu button
		// To delete the entry
		MenuItem menuitem;
		if (mTaskType == Globals.TASK_TYPE_HISTORY) {
			menuitem = menu.add(Menu.NONE, MENU_ID_DELETE, MENU_ID_DELETE,
					"Delete");
			menuitem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		}
		return true;
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ID_DELETE:
			// Delete entry in database
			ExerciseEntryHelper.deleteEntryInDB(mContext, getIntent().getIntExtra(HistoryFragment.ROW_ID, -1));
			finish();
			return true;
		default:
			finish();
			return false;
		}
	}
	
	/******************* broadcast receiver *****************/
	public class TrackingServiceReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(Globals.TAG, "Location update received; ");
        	
                Location loc = mLocationList.get(mLocationList.size()-1);
    			LatLng latlng = Utils.fromLocationToLatLng(loc);
    									
    			// save the start point
    			if (mFirstLoc) {
    				mFirstLoc= false;
    				firstLatLng=latlng;
    				mStartTime = loc.getTime();
    			}
    			else {
    	            // update stats
    				Location preLoc = mLocationList.get(mLocationList.size()-2);
    				mDistance = mDistance + loc.distanceTo(preLoc);
    				mAvgSpeed = mDistance / ((loc.getTime()-mStartTime) / 1000); 
    				mCurSpeed = loc.distanceTo(mLocationList.get(mLocationList.size()-2)) / ((loc.getTime()-preLoc.getTime()) / 1000);
    				mClimb = loc.getAltitude(); 
    				mCalories = (int) mDistance / 10;
    				mDuration = ((loc.getTime()-mStartTime) / 1000 / 60); // minutes

    			}	
    			
    		
                Log.d(Globals.TAG, "onPostExecute 0");

    			synchronized (mLocationList) {
    				
    				// Convert the mLocationList to mLatLngList 
    				for (int i = 0; i < mLocationList.size() ; i++) {
    					loc = mLocationList.get(i);
    					mLatLngList.add(Utils.fromLocationToLatLng(loc)); 
    				}

    				// draw trace using PolyLine
    				rectOptions = new PolylineOptions();
    				rectOptions.color(Color.RED);
    				rectOptions.addAll(mLatLngList);
    				polyline = mMap.addPolyline(rectOptions);

    				// draw start marker
    				mMap.addMarker(new MarkerOptions().position(firstLatLng).title("Start Point"));
    				
    				// draw current marker
    				if (curMarker!=null)
    					curMarker.remove();
    				curMarker = mMap.addMarker(new MarkerOptions().position(mLatLngList.get(mLatLngList.size()-1)).
    						title("You Are Here").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
    				
    				// Get real-time stats from the Exercise Entry
    				String type = "Not initialized";
    				if (mInputType == Globals.INPUT_TYPE_GPS)
    					type = Globals.TYPE_STATS + Globals.ACTIVITY_TYPES[mEntry.getActivityType()];

    				String avgSpeed = Globals.AVG_SPEED_STATS + String.format("%1$.2f", mAvgSpeed) + " meters / sec";
    				String curSpeed = Globals.CUR_SPEED_STATS + String.format("%1$.2f", mCurSpeed) + " meters / sec";
    				String climb = Globals.CLIMB_STATS + String.format("%1$.2f", mClimb) + " meters";
    				String calories = Globals.CALORIES_STATS + String.format("%1d", mCalories);
    				String distance = Globals.DISTANCE_STATS + String.format("%1$.2f", mDistance) + " meters";

    				// Draw the stats on the map
    				if (mInputType == Globals.INPUT_TYPE_GPS)
    					typeStats.setText(type);
    				avgspeedStats.setText(avgSpeed);
    				curspeedStats.setText(curSpeed);
    				climbStats.setText(climb);
    				caloriesStats.setText(calories);
    				distanceStats.setText(distance);
    				
    				// Clear the mLatLngList
    				mLatLngList.clear();
    			}

    			// re-center map if needed
    			latlng = Utils.fromLocationToLatLng(mLocationList.get(mLocationList.size()-1));
                Log.d(Globals.TAG, "onPostExecute 5");

    			if (isMapNeedRecenter(latlng)){
    	            Log.d(Globals.TAG, "onPostExecute 6");

    				mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 17));
    				mMap.animateCamera(CameraUpdateFactory.zoomTo(17), 2000, null); 
    			}		
        }
	}
	
	public class MotionUpdateReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent){
			mInferredActivityType = intent.getIntExtra(TrackingService.VOTED_MOTION_TYPE, -1);
			mSweatRate = intent.getStringExtra(TrackingService.FINAL_SWEAT_RATE_AVERAGE);
			int currentActivity = intent.getIntExtra(TrackingService.CURRENT_MOTION_TYPE, -1);
			int sweatRateIndex = intent.getIntExtra(TrackingService.CURRENT_SWEAT_RATE_INTERVAL, -1);
			String type = Globals.TYPE_STATS + Globals.ACTIVITY_TYPES[currentActivity];
			String sweatRate = Globals.SWEAT_STATS + Globals.SWEAT_RATE_INTERVALS[sweatRateIndex];
			typeStats.setText(type + "\n" + sweatRate);
			
			lightingType.setText( Globals.LIGHT_TYPE_HEADER + TrackingService.CUR_LIGHT_CONDITION + ", Last Max: " + TrackingService.lastMaxIntensityBuffer);
			lightingType_Arduino.setText( Globals.LIGHT_TYPE_HEADER_ARDUINO + "No Sensor Detected"  );
		}
	}
	
	
		private boolean isMapNeedRecenter(LatLng latlng) {
		
			VisibleRegion vr = mMap.getProjection().getVisibleRegion();
			
			double left = vr.latLngBounds.southwest.longitude;	
			double top = vr.latLngBounds.northeast.latitude;
			double right = vr.latLngBounds.northeast.longitude;
			double bottom = vr.latLngBounds.southwest.latitude;

		
			int rectWidth = (int) Math.abs(right - left);
			int rectHeight = (int) Math.abs(top - bottom);

			int rectCenterX = (int) mMap.getCameraPosition().target.longitude;
			int rectCenterY = (int) mMap.getCameraPosition().target.latitude;

			// Constructs the rectangle
			Rect validScreenRect = new Rect(rectCenterX - rectWidth / 2,
				rectCenterY - rectHeight / 2, rectCenterX + rectWidth / 2,
				rectCenterY + rectHeight / 2);

			return !validScreenRect.contains((int) latlng.longitude,
				(int) latlng.latitude);	
		}
}