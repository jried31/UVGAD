package edu.dartmouth.cs.myruns5;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
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
	
	private GoogleMap mMap;

	public Context mContext;
	
	public TextView typeStats;
	public TextView lightingType;
	public TextView avgspeedStats;
	public TextView curspeedStats;
	public TextView climbStats;
	public TextView caloriesStats;
	public TextView distanceStats;
	public TextView uviStats;
	PolylineOptions rectOptions;
	Polyline polyline;

	public ArrayList<Location> mLocationList;
	private ArrayList<LatLng> mLatLngList;
	
	public ExerciseEntryHelper mEntryHelper;
	public ExerciseEntry mEntry;
	
	private int mTaskType;
	private int mInputType;
	
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

    	Toast.makeText(getApplicationContext(), "MapDisplay onCreate", Toast.LENGTH_SHORT).show();
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
		avgspeedStats = (TextView) findViewById(R.id.avg_speed_stats);
		curspeedStats = (TextView) findViewById(R.id.cur_speed_stats);
		climbStats = (TextView) findViewById(R.id.climb_stats_stats);
		caloriesStats = (TextView) findViewById(R.id.calories_stats);
		distanceStats = (TextView) findViewById(R.id.distance_stats);
		uviStats = (TextView) findViewById(R.id.uviType);
		
		mEntryHelper = new ExerciseEntryHelper();
		// Get extras from intent and set the mTaskType, InputType, Row Id and ActivityType
		Intent intent = getIntent();
		mTaskType = intent.getIntExtra(MainActivity.TASK_TYPE, -1);
		mInputType = intent.getIntExtra(MainActivity.INPUT_TYPE, -1);
		mInferredActivityType = intent.getIntExtra(Globals.VOTED_ACTIVITY_TYPE, Globals.ACTIVITY_TYPE_STANDING);
		
		mDistance = 0;
		mCalories = 0;
		mAvgSpeed = 0;
		mStartTime = 0;
		mClimb = 0;
		mDuration = 0;
		mCumulativeUVExposure = 0.0;
		this.mCumulativeSweatTotal = 0.0;
		
		// init mEntry
		mEntry = new ExerciseEntry();
		mEntry.setActivityType(mInferredActivityType);
		mEntry.setInputType(mInputType);
		
		FragmentManager myFragmentManager = getFragmentManager();
		MapFragment mMapFragment = (MapFragment)myFragmentManager.findFragmentById(R.id.map);
		mMap = mMapFragment.getMap();
		
		switch (mTaskType) {
		case Globals.TASK_TYPE_NEW:	//Start new Track
			intent = new Intent(this, TrackingService.class);
			intent.putExtra(INPUT_TYPE, mInputType);
			bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
			startService(intent);
			
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(Globals.ACTION_MOTION_UPDATE);
			registerReceiver(mMotionUpdateReceiver, intentFilter);
			break;

		case Globals.TASK_TYPE_HISTORY:
			// remove buttons
			Button saveButton = (Button) findViewById(R.id.button_map_save);
			saveButton.setVisibility(View.GONE);
			Button cancelButton = (Button) findViewById(R.id.button_map_cancel);
			cancelButton.setVisibility(View.GONE);
			
			// Add send data button
			sendButton.setVisibility(View.VISIBLE);
			sendButton.setClickable(true);

			// Read track from database
			mLocationList = intent.getParcelableArrayListExtra(HistoryFragment.TRACK);
				
			// sanity check
			if (mLocationList == null){
				Log.d(null, "this should not happen");
				return;
			}

			// convert to LatLng class
			for (int i = 0; i < mLocationList.size(); i++)
				mLatLngList.add(Utils.fromLocationToLatLng(mLocationList.get(i)));
			
			//Draw location history if it exists
			boolean draw = mLocationList.size() > 0;
			
			// draw markers
			if (draw){
				firstLatLng = Utils.fromLocationToLatLng(mLocationList.get(0));
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
			String sweatTotal =  Globals.SWEAT_STATS + intent.getStringExtra(HistoryFragment.SWEAT_TOTAL);
			
			String avgSpeed = Globals.AVG_SPEED_STATS + String.format("%1$.2f", 
					Double.parseDouble(intent.getStringExtra(HistoryFragment.AVG_SPEED))) + " meters / sec";
			String curSpeed = Globals.CUR_SPEED_STATS + "0" + " meters / sec";
			String climb = Globals.CLIMB_STATS + String.format("%1$.2f", 
					Double.parseDouble(intent.getStringExtra(HistoryFragment.CLIMB))) + " meters";
			String calories = Globals.CALORIES_STATS + intent.getStringExtra(HistoryFragment.CALORIE);
			String distance = Globals.DISTANCE_STATS + String.format("%1$.2f", 
					Double.parseDouble(intent.getStringExtra(HistoryFragment.DISTANCE))) + " meters";
			
			String cumulativeUVExposure = intent.getStringExtra(HistoryFragment.UV_EXPOSURE);
			String cumulativeUVExposureChest = intent.getStringExtra(HistoryFragment.UV_EXPOSURE_CHEST);
			String cumulativeUVExposureHand = intent.getStringExtra(HistoryFragment.UV_EXPOSURE_DORSAL_HAND);
			String cumulativeUVExposureFace = intent.getStringExtra(HistoryFragment.UV_EXPOSURE_FACE);
			String cumulativeUVExposureForearm = intent.getStringExtra(HistoryFragment.UV_EXPOSURE_FOREARM);
			String cumulativeUVExposureLeg = intent.getStringExtra(HistoryFragment.UV_EXPOSURE_LEG);
			String cumulativeUVExposureNeck = intent.getStringExtra(HistoryFragment.UV_EXPOSURE_NECK);	

			mEntry.setSweatCumulative(mCumulativeSweatTotal);
			mEntry.setCumulativeBackExposure(cumulativeBackExposure);
			mEntry.setCumulativeChestExposure(cumulativeChestExposure);
			mEntry.setCumulativeHandExposure(cumulativeDorsalHandExposure);
			mEntry.setCumulativeFaceExposure(cumulativeFaceExposure);
			mEntry.setCumulativeForearmExposure(cumulativeForearmExposure);
			mEntry.setCumulativeLegExposure(cumulativeLegExposure);
			mEntry.setCumulativeNeckExposure(cumulativeNeckExposure);
			mEntry.setCumulativeHorizontalExposure(mCumulativeUVExposure);
			
			uviStats.setText(String.format("Total UV Exposure: \nFace: %s J/m^2 | Neck: %s J/m^2\nChest: %s J/m^2 | Forearm: %s J/m^2\nHand: %s J/m^2 | Leg: %s J/m^2\nBody: %s J/m^2", 
					cumulativeUVExposureFace,cumulativeUVExposureNeck,cumulativeUVExposureChest,cumulativeUVExposureForearm,cumulativeUVExposureHand,cumulativeUVExposureLeg,cumulativeUVExposure));

			typeStats.setText(String.format("%s\nSweat Total: %s liters/hr",type,sweatTotal));
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

    	Toast.makeText(getApplicationContext(), "MapDisplay onDestroy", Toast.LENGTH_SHORT).show();
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
			}
			
		}
		super.onPause();
	
	}
	
	@Override
	public void onResume(){
		super.onResume();
		if (mTaskType == Globals.TASK_TYPE_NEW){
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(Globals.ACTION_TRACKING);
			registerReceiver(receiver, intentFilter);
			
			if(mInputType == Globals.INPUT_TYPE_AUTOMATIC){
				intentFilter = new IntentFilter();
				intentFilter.addAction(Globals.ACTION_MOTION_UPDATE);
				registerReceiver(mMotionUpdateReceiver, intentFilter);
			}
		}
	}
	
	public void sunblockReapp(Context context){
		String notificationTitle = "MyRuns";
		String notificationText = "Time to reapply sunblock!";
		
		Notification notification = new Notification.Builder(context)
			.setContentTitle(notificationTitle)
			.setContentText(notificationText)
			.setSmallIcon(R.drawable.runner)
			.setAutoCancel(true)
			.build();
		
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notification.flags = notification.flags | Notification.FLAG_AUTO_CANCEL;
		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.defaults |= Notification.DEFAULT_VIBRATE;
		notificationManager.notify(0, notification);
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
		mEntry.setSweatCumulative(mCumulativeSweatTotal);
		mEntry.setCumulativeBackExposure(cumulativeBackExposure);
		mEntry.setCumulativeChestExposure(cumulativeChestExposure);
		mEntry.setCumulativeHandExposure(cumulativeDorsalHandExposure);
		mEntry.setCumulativeFaceExposure(cumulativeFaceExposure);
		mEntry.setCumulativeForearmExposure(cumulativeForearmExposure);
		mEntry.setCumulativeLegExposure(cumulativeLegExposure);
		mEntry.setCumulativeNeckExposure(cumulativeNeckExposure);
		mEntry.setCumulativeHorizontalExposure(mCumulativeUVExposure);
			
		SharedPreferences sharedPref = mContext.getSharedPreferences(Globals.TAG, Context.MODE_PRIVATE);
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

    	Toast.makeText(getApplicationContext(), "MapDisplay onCancel", Toast.LENGTH_SHORT).show();
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
			menuitem = menu.add(Menu.NONE, MENU_ID_DELETE, MENU_ID_DELETE,"Delete");
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

	private double mCumulativeUVExposure=0,mCumulativeSweatTotal,cumulativeDorsalHandExposure=0,cumulativeChestExposure=0,
	cumulativeBackExposure=0,cumulativeFaceExposure=0,cumulativeForearmExposure=0,cumulativeNeckExposure=0,cumulativeLegExposure=0;
	private String environmentClassification;
	private int mInferredActivityType;
	int interval = 0;
	public class MotionUpdateReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent){
		
			cumulativeBackExposure = intent.getDoubleExtra(Globals.CUMULATIVE_BACK_UV_EXPOSURE,0.0);
			cumulativeChestExposure = intent.getDoubleExtra(Globals.CUMULATIVE_CHEST_UV_EXPOSURE, 0);
			cumulativeDorsalHandExposure = intent.getDoubleExtra(Globals.CUMULATIVE_DORSAL_HAND_UV_EXPOSURE, 0);
			cumulativeFaceExposure = intent.getDoubleExtra(Globals.CUMULATIVE_FACE_UV_EXPOSURE, 0);
			cumulativeForearmExposure = intent.getDoubleExtra(Globals.CUMULATIVE_FOREARM_UV_EXPOSURE, 0);
			cumulativeLegExposure = intent.getDoubleExtra(Globals.CUMULATIVE_LEG_UV_EXPOSURE, 0);
			cumulativeNeckExposure = intent.getDoubleExtra(Globals.CUMULATIVE_NECK_UV_EXPOSURE, 0);
			mCumulativeUVExposure = intent.getDoubleExtra(Globals.CUMULATIVE_UV_EXPOSURE, 0);
			environmentClassification = intent.getStringExtra(Globals.ENVIRONMENT_CLASSIFICATION);
			double lightIntensity = intent.getDoubleExtra(Globals.LIGHT_INTENSITY_READING,0);
			mInferredActivityType = intent.getIntExtra(Globals.VOTED_ACTIVITY_TYPE, 0);
			int currentActivity = intent.getIntExtra(Globals.CURRENT_ACTIVITY_TYPE, 0);
			String type = Globals.TYPE_STATS + "Instant: " + Globals.ACTIVITY_TYPES[currentActivity] + " Voted: " + Globals.ACTIVITY_TYPES[mInferredActivityType];
			
			mCumulativeSweatTotal = intent.getDoubleExtra(Globals.SWEAT_TOTAL, 0);
			String sweatRate = intent.getStringExtra(Globals.SWEAT_RATE_INDEX);
			
			//if(interval++ % 100==0){
				sweatRate =  Globals.SWEAT_STATS + sweatRate;
				typeStats.setText(String.format("%s\n%s\nSweat Total: %.2f liters/hr",type,sweatRate,mCumulativeSweatTotal));//type + "\n" + sweatRate + "\n" + "Total amount sweat:  %.2f liters/hr",mCumulativeSweatTotal);
				
				uviStats.setText(String.format("Total UV Exposure: \nFace: %.2f J/m^2 | Neck: %.2f J/m^2\nChest: %.2f J/m^2 | Forearm: %.2f J/m^2\nHand: %.2f J/m^2 | Leg: %.2f J/m^2\nBody: %.2f J/m^2", 
						cumulativeFaceExposure,cumulativeNeckExposure,cumulativeChestExposure,cumulativeForearmExposure,cumulativeDorsalHandExposure,cumulativeLegExposure,mCumulativeUVExposure));
				
				
				/*if (sweatRate > Globals.SWEAT_REAPPLY ) {
					sunblockReapp(context);
				}*/
				
				lightingType.setText((Globals.FOUND_ARDUINO == true ? "Arduino":"") + " Environment: " + environmentClassification);// + "\nLast Max: " +lightIntensity + " lux");
			//}
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
