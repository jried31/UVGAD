package edu.dartmouth.cs.myruns5;

import java.util.ArrayList;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class HistoryFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> 
{
	private static final int LOADER_ID = 0;
	private LoaderManager.LoaderCallbacks<Cursor> mCallbacks;

	public Context mContext; // context pointed to parent activity
	public ActivityEntriesCursorAdapter mAdapter; // customized adapter for displaying
	
	// Table column index
	public int mRowIdIndex;
	public int mActivityIndex;
	public int mTimeIndex;
	public int mDurationIndex;
	public int mDistanceIndex;
	public int mCaloriesIndex;
	public int mHeartrateIndex;
	public int mCommentIndex;
	public int mInputTypeIndex;
	public int mGenderIndex;
	public int mSkinTone;
	public int mSPF;
	public int mClothingCover;
	
	public Cursor mActivityEntryCursor;
			
	public static final String DATE_FORMAT = "H:mm:ss MMM d yyyy";
	public static final String DISTANCE_FORMAT = "#.##";
	public static final String MINUTES_FORMAT = "%d mins";
	public static final String SECONDS_FORMAT = "%d secs";
	
	public static final String ROW_ID = Globals.KEY_ROWID;
	public static final String INPUT_TYPE = Globals.KEY_INPUT_TYPE;
	public static final String COMMENT = Globals.KEY_COMMENT;
	public static final String DISTANCE = Globals.KEY_DISTANCE;
	public static final String SWEAT_TOTAL = Globals.KEY_SWEAT_TOTAL;
	public static final String UV_EXPOSURE = Globals.KEY_UV_EXPOSURE;
	public static final String UV_EXPOSURE_FACE = Globals.KEY_UV_EXPOSURE_FACE;
	public static final String UV_EXPOSURE_NECK = Globals.KEY_UV_EXPOSURE_NECK;
	public static final String UV_EXPOSURE_CHEST = Globals.KEY_UV_EXPOSURE_CHEST;
	public static final String UV_EXPOSURE_FOREARM = Globals.KEY_UV_EXPOSURE_FOREARM;
	public static final String UV_EXPOSURE_DORSAL_HAND = Globals.KEY_UV_EXPOSURE_DORSAL_HAND;
	public static final String UV_EXPOSURE_LEG = Globals.KEY_UV_EXPOSURE_LEG;
	public static final String VITAMIN_D = Globals.KEY_VITAMIN_D;
	
	public static final String DURATION = "duration";
	public static final String CALORIE = "calorie";
	public static final String DATE_TIME = "date and time";
	public static final String HEARTRATE = "heart rate";
	public static final String ACTIVITY_TYPE = "activity type";
	public static final String TASK_TYPE = "task type";
	public static final String TRACK = "track";
	public static final String AVG_SPEED = "average speed";
	public static final String CLIMB = "climb";
	public static final String GENDER = "gender";
	public static final String SKIN_TONE = "skin tone";
	public static final String SPF = "spf";
	public static final String CLOTHING_COVER = "clothing cover";
	//public static final String HEAD_APPAREL = "head apparel";
	//public static final String UPPER_APPAREL = "upper apparel";
	//public static final String LOWER_APPAREL = "lower apparel";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		mContext = getActivity();
		
		//initialize the mActivityEntryCursor
        mActivityEntryCursor = getActivity().getContentResolver().query(
                HistoryProvider.CONTENT_URI,
                null,
                null,
                null, 
                null);
		
        mAdapter = new ActivityEntriesCursorAdapter(getActivity(), mActivityEntryCursor);
				
        // set callback object
		mCallbacks = this;

		// Initialize the Loader with id "0" and callbacks "mCallbacks".
		LoaderManager lm = getLoaderManager();
		lm.initLoader(LOADER_ID, null, mCallbacks);

		// Set the mAdapter to be show the list.
		setListAdapter(mAdapter);
	}

	//	Standard layout inflation for fragment
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l,v,position,id);
		// The intent to launch the activity after click.
		mActivityEntryCursor = mAdapter.getCursor();
		int idx = mActivityEntryCursor.getColumnIndex(Globals.KEY_INPUT_TYPE);
		int inputType = Integer.parseInt(mActivityEntryCursor.getString(idx));
		
		Intent intent = new Intent();
		// The extra information needed pass through to next activity.
		Bundle extras = new Bundle();
	
		// Write row id into extras.
		idx = mActivityEntryCursor.getColumnIndex(Globals.KEY_ROWID);
		int row = Integer.parseInt(mActivityEntryCursor.getString(idx));
		intent.putExtra(ROW_ID, row);
		
		// Read the input type: Manual, GPS, or automatic
		intent.putExtra(INPUT_TYPE, inputType);
		
		// write other info
		idx = mActivityEntryCursor.getColumnIndex(Globals.KEY_CALORIES);
		String calorie = mActivityEntryCursor.getString(idx);
		intent.putExtra(CALORIE, calorie);
		
		idx = mActivityEntryCursor.getColumnIndex(Globals.KEY_COMMENT);
		String comment = mActivityEntryCursor.getString(idx);
		intent.putExtra(COMMENT, comment);
	
		idx = mActivityEntryCursor.getColumnIndex(Globals.KEY_DATE_TIME);
		String date_time = mActivityEntryCursor.getString(idx);
		intent.putExtra(DATE_TIME, date_time);
		
		idx = mActivityEntryCursor.getColumnIndex(Globals.KEY_DISTANCE);
		String distance = mActivityEntryCursor.getString(idx);
		intent.putExtra(DISTANCE, distance);
		
		idx = mActivityEntryCursor.getColumnIndex(Globals.KEY_SWEAT_TOTAL);
		String sweatrate = mActivityEntryCursor.getString(idx);
		intent.putExtra(SWEAT_TOTAL,sweatrate);
		
		idx = mActivityEntryCursor.getColumnIndex(Globals.KEY_UV_EXPOSURE);
		String uvExposure = mActivityEntryCursor.getString(idx);
		intent.putExtra(UV_EXPOSURE,uvExposure);
		
		idx = mActivityEntryCursor.getColumnIndex(Globals.KEY_UV_EXPOSURE_FACE);
		String uvExposureFace = mActivityEntryCursor.getString(idx);
		intent.putExtra(UV_EXPOSURE_FACE,uvExposureFace);

		idx = mActivityEntryCursor.getColumnIndex(Globals.KEY_UV_EXPOSURE_NECK);
		String uvExposureNeck = mActivityEntryCursor.getString(idx);
		intent.putExtra(UV_EXPOSURE_NECK,uvExposureNeck);
		
		idx = mActivityEntryCursor.getColumnIndex(Globals.KEY_UV_EXPOSURE_CHEST);
		String uvExposureChest = mActivityEntryCursor.getString(idx);
		intent.putExtra(UV_EXPOSURE_CHEST,uvExposureChest);
		
		idx = mActivityEntryCursor.getColumnIndex(Globals.KEY_UV_EXPOSURE_FOREARM);
		String uvExposureForearm = mActivityEntryCursor.getString(idx);
		intent.putExtra(UV_EXPOSURE_FOREARM,uvExposureForearm);
		
		idx = mActivityEntryCursor.getColumnIndex(Globals.KEY_UV_EXPOSURE_DORSAL_HAND);
		String uvExposureHand = mActivityEntryCursor.getString(idx);
		intent.putExtra(UV_EXPOSURE_DORSAL_HAND,uvExposureHand);

		idx = mActivityEntryCursor.getColumnIndex(Globals.KEY_UV_EXPOSURE_LEG);
		String uvExposureLeg = mActivityEntryCursor.getString(idx);
		intent.putExtra(UV_EXPOSURE_LEG,uvExposureLeg);
		
		idx = mActivityEntryCursor.getColumnIndex(Globals.KEY_DURATION);
		String duration = mActivityEntryCursor.getString(idx);
		intent.putExtra(DURATION, duration);
		
		idx = mActivityEntryCursor.getColumnIndex(Globals.KEY_HEARTRATE);
		String heartrate = mActivityEntryCursor.getString(idx);
		intent.putExtra(HEARTRATE, heartrate);
		
		idx = mActivityEntryCursor.getColumnIndex(Globals.KEY_AVG_SPEED);
		String avgSpeed = mActivityEntryCursor.getString(idx);
		intent.putExtra(AVG_SPEED, avgSpeed);

		idx = mActivityEntryCursor.getColumnIndex(Globals.KEY_CLIMB);
		String climb = mActivityEntryCursor.getString(idx);
		intent.putExtra(CLIMB, climb);
		
		idx = mActivityEntryCursor.getColumnIndex(Globals.KEY_ACTIVITY_TYPE);
		String activityType = mActivityEntryCursor.getString(idx);
		int code = Integer.parseInt(activityType);
		activityType = Globals.ACTIVITY_TYPES[code].substring(0);
		intent.putExtra(ACTIVITY_TYPE, activityType);
		
		idx = mActivityEntryCursor.getColumnIndex(Globals.KEY_TRACK);
		byte[] byteArray = mActivityEntryCursor.getBlob(idx);
		if (byteArray!=null){
			Location[] locations = Utils.fromByteArrayToLocationArray(byteArray);
			ArrayList<Location> locationList = new ArrayList<Location>();
			Log.d(null, "converting locations[] to ArrayList");
			for (int i = 0; i < locations.length; i++) {
				locationList.add(locations[i]);
				System.out.println("LLLLLLLL: " + locations[i].getLongitude());
			}
			intent.putParcelableArrayListExtra(TRACK, locationList);
		}
		
		idx = mActivityEntryCursor.getColumnIndex(Globals.KEY_GENDER);
		intent.putExtra(GENDER, mActivityEntryCursor.getInt(idx));
		
		idx = mActivityEntryCursor.getColumnIndex(Globals.KEY_SKIN_TONE);
		intent.putExtra(SKIN_TONE, mActivityEntryCursor.getInt(idx));
		
		idx = mActivityEntryCursor.getColumnIndex(Globals.KEY_SPF);
		intent.putExtra(SPF, mActivityEntryCursor.getInt(idx));
		
		idx = mActivityEntryCursor.getColumnIndex(Globals.KEY_CLOTHING_COVER);
		intent.putExtra(CLOTHING_COVER, mActivityEntryCursor.getFloat(idx));
		
		/*
		idx = mActivityEntryCursor.getColumnIndex(Globals.KEY_HEAD_APPAREL);
		String headApparel = mActivityEntryCursor.getString(idx);
		intent.putExtra(HEAD_APPAREL, headApparel);
		
		idx = mActivityEntryCursor.getColumnIndex(Globals.KEY_UPPER_APPAREL);
		String upperApparel = mActivityEntryCursor.getString(idx);
		intent.putExtra(UPPER_APPAREL, upperApparel);
		
		idx = mActivityEntryCursor.getColumnIndex(Globals.KEY_LOWER_APPAREL);
		String lowerApparel = mActivityEntryCursor.getString(idx);
		intent.putExtra(LOWER_APPAREL, lowerApparel);
		*/
		
		intent.putExtras(extras);
		// Based on different input type, launching different activities
		switch (inputType) {
		case Globals.INPUT_TYPE_MANUAL: // Manual mode
			intent.setClass(mContext, DisplayEntryActivity.class);
			// Fire the DisplayEntryActivity
			startActivity(intent);
			break;
		case Globals.INPUT_TYPE_GPS: // GPS mode
		case Globals.INPUT_TYPE_AUTOMATIC: // or automatic mode
			intent.setClass(mContext, MapDisplayActivity.class);
			intent.putExtra(MainActivity.TASK_TYPE, Globals.TASK_TYPE_HISTORY);
			startActivity(intent);
			break;
		default:
			Toast.makeText(mContext, "u r NOT supposed to see this!!", Toast.LENGTH_SHORT).show();
			intent = new Intent(getActivity(), MapDisplayActivity.class); 
			startActivity(intent);
			return;
		}
	}
	
	// Create a new CursorLoader with the following query parameters.
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
	    return new CursorLoader(getActivity(), HistoryProvider.CONTENT_URI,
	           null, null, null, null);		// mark
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
	    switch (loader.getId()) {
	      case LOADER_ID:
	        mAdapter.swapCursor(cursor);
	        break;
	    }
	}
	
	public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
	}

	// responsible for displaying message
	private class ActivityEntriesCursorAdapter extends CursorAdapter {

		private LayoutInflater mInflater;
		public ActivityEntriesCursorAdapter(Context context, Cursor c) {
			super(context, c, FLAG_REGISTER_CONTENT_OBSERVER);
			mInflater = LayoutInflater.from(context);
		}
		
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
	        int typeIndex;
	        String line1, line2;
	        
        	// line1: activity type and date_time
			typeIndex = cursor.getColumnIndex(Globals.KEY_ACTIVITY_TYPE);
			line1 = cursor.getString(typeIndex);
			int code = Integer.parseInt(line1);
			if(code != -1) {
				line1 = Globals.ACTIVITY_TYPES[code].substring(0);
				line1 = line1 + ", ";
				typeIndex = cursor.getColumnIndex(Globals.KEY_DATE_TIME);
				line1 = line1 + cursor.getString(typeIndex);
		        TextView text1 = (TextView) view.findViewById(android.R.id.text1);
		        text1.setText(line1);
		        
		        // line2: distance and duration
		        typeIndex = cursor.getColumnIndex(Globals.KEY_DISTANCE);
		        
		        // units
		        int inputTypeIndex = cursor.getColumnIndex(Globals.KEY_INPUT_TYPE);
		        String inputType = cursor.getString(inputTypeIndex);
		        
		        line2 = String.format("%1$.2f", Double.parseDouble(cursor.getString(typeIndex)));
		        if (inputType.equals("1") || inputType.equals("2"))
		        	line2 = line2 + " Meters, ";
		        else if (inputType.equals("0"))
		        	line2 = line2 + " Miles, ";
		        else{ }
		        
		        typeIndex = cursor.getColumnIndex(Globals.KEY_DURATION);
		        line2 = line2 + cursor.getString(typeIndex);	       
		        line2 = line2 + " Minutes, ";
		        
		        typeIndex = cursor.getColumnIndex(Globals.KEY_SWEAT_TOTAL);
		        line2 = line2 + cursor.getString(typeIndex);	       
		        line2 = line2 + " sweat lost, ";
		        
		        typeIndex = cursor.getColumnIndex(Globals.KEY_UV_EXPOSURE);
		        line2 = line2 + String.format("%.2f", Double.parseDouble(cursor.getString(typeIndex)));	       
		        line2 = line2 + " (J/s)/m^2 exposure";
		        
		        TextView text2 = (TextView) view.findViewById(android.R.id.text2);
		        text2.setText(line2);
			}
		}
		
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return mInflater.inflate(android.R.layout.two_line_list_item, null);
		}
	}
}
