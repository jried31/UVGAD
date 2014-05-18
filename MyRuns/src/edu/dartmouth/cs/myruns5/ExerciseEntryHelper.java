package edu.dartmouth.cs.myruns5;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.content.ContentValues;
import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

public class ExerciseEntryHelper {
	
	// The ExerciseEntry 
	private ExerciseEntry mData;

	
	// Formatter
	public static final DecimalFormat decimalFormat = new DecimalFormat("#.##");

	public ExerciseEntryHelper() {
		mData = new ExerciseEntry();
	}
	public ExerciseEntryHelper(ExerciseEntry entry) {
		mData = entry;
	}
	// ************** Database operations ************** //
	
	// Write the current exercise entry into database.
	
	public long insertToDB(Context context) {
		//create a ContentValues object to store exercise entry.
		ContentValues values = new ContentValues();

		//put all the data saved in ExerciseEntry into the ContentValues object.
		// mark: GPS
		values.put(HistoryTable.KEY_ACTIVITY_TYPE, mData.getActivityType());
		values.put(HistoryTable.KEY_AVG_PACE, mData.getAvgPace());
		Log.d(null, "writing avgSpeed to values");
		values.put(HistoryTable.KEY_AVG_SPEED, mData.getAvgSpeed());
		values.put(HistoryTable.KEY_CALORIES, mData.getCalorie());
		Log.d(null, "writing climb to values");
		values.put(HistoryTable.KEY_CLIMB, mData.getClimb());
		Log.d(null, "wrote climb to values");
		
		values.put(HistoryTable.KEY_SPF, mData.getSPF());
		values.put(HistoryTable.KEY_CLOTHING_COVER, mData.getClothingCover());
		//values.put(HistoryTable.KEY_HEAD_APPAREL, mData.getHeadApparel().name());
		//values.put(HistoryTable.KEY_UPPER_APPAREL, mData.getUpperApparel().name());
		//values.put(HistoryTable.KEY_LOWER_APPAREL, mData.getLowerApparel().name());

		values.put(HistoryTable.KEY_UV_EXPOSURE, mData.getCumulativeHorizontalExposure());
		values.put(HistoryTable.KEY_UV_EXPOSURE_CHEST, mData.getCumulativeChestExposure());
		values.put(HistoryTable.KEY_UV_EXPOSURE_DORSAL_HAND, mData.getCumulativeHandExposure());
		values.put(HistoryTable.KEY_UV_EXPOSURE_FACE, mData.getCumulativeFaceExposure());
		values.put(HistoryTable.KEY_UV_EXPOSURE_FOREARM, mData.getCumulativeForearmExposure());
		values.put(HistoryTable.KEY_UV_EXPOSURE_LEG, mData.getCumulativeLegExposure());
		values.put(HistoryTable.KEY_UV_EXPOSURE_NECK, mData.getCumulativeNeckExposure());
		values.put(HistoryTable.KEY_VITAMIN_D, mData.getVitaminDExposureCumulative());
		values.put(HistoryTable.KEY_COMMENT, mData.getComment());
		values.put(HistoryTable.KEY_DATE_TIME, mData.getDateTime().toString());
		values.put(HistoryTable.KEY_DISTANCE, mData.getDistance());
		values.put(HistoryTable.KEY_SWEAT_TOTAL,mData.getSweatRate());
		values.put(HistoryTable.KEY_DURATION, mData.getDuration());
		values.put(HistoryTable.KEY_HEARTRATE, mData.getHeartrate());
		values.put(HistoryTable.KEY_INPUT_TYPE, mData.getInputType());
		values.put(HistoryTable.KEY_ROWID,	mData.getId());
		Log.d(null, "writing track...");

		ArrayList<Location> tmpList = mData.getLocationList();
		if (tmpList!=null) {
			Location[] tmp = (Location[]) tmpList.toArray(new Location[0]);
			values.put(HistoryTable.KEY_TRACK, Utils.fromLocationArrayToByteArray(tmp));
		}
		
		//get the content resolver, insert the ContentValues into HistoryProvider.
		Log.d(null, "insert values to HistoryProvider...");

        Uri uri = context.getContentResolver().insert(HistoryProvider.CONTENT_URI, values);
		
		//set current ExerciseEntry's id.
        mData.setId(Long.valueOf(uri.getLastPathSegment()));	// mark	
//		Toast.makeText(context, String.valueOf(mData.getId()), Toast.LENGTH_SHORT).show();

		//return the current ExerciseEntry's id.
        return Long.valueOf(uri.getLastPathSegment());
	}

	// Delete a entry specified by the argument id.
	// Static method, more general.
	public static void deleteEntryInDB(Context context, long id) {
		String where = HistoryTable.KEY_ROWID + " =?";
		String rawId = Long.toString(id);
		String[] whereparams = new String[]{rawId};
		context.getContentResolver().delete(HistoryProvider.CONTENT_URI, where, whereparams);
	}

	// Overloading class function to delete current entry.
	public void deleteEntryInDB(Context context) {
		context.getContentResolver().delete(HistoryProvider.CONTENT_URI, null, null);  // mark: current?
	}	
	
	// Standard setters and getters. May have some conversions.

	public int getActivityType() {
		return mData.getActivityType();
	}

	public double getDistance() {
		return mData.getDistance();
	}

	public double getSweatRate() {
		return mData.getSweatRate();
	}
	public int getCalories() {
		return mData.getCalorie();
	}

	public double getClimb() {
		return mData.getClimb();
	}

	public void setDate(int year, int month, int day) {

		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(mData.getDateTime());

		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);
		int second = cal.get(Calendar.SECOND);

		cal = new GregorianCalendar(year, month, day, hour, minute, second);

		mData.setDateTime(cal.getTime());
	}

	public void setTime(int hour, int minute, int second) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(mData.getDateTime());

		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);
		
		cal = new GregorianCalendar(year, month, day, hour, minute, second);

		mData.setDateTime(cal.getTime());
	}


	public void setVitaminDExposureCumulative(double vitaminDExposureCum) {
		mData.setVitaminDExposureCumulative(vitaminDExposureCum);
	}
	
	public void setUvExposureHorizontalCumulative(double uvExposureCum) {
		mData.setCumulativeHorizontalExposure(uvExposureCum);
	}
	
	public void setCumulativeBackExposure(double uvExposureCum) {
		mData.setCumulativeBackExposure(uvExposureCum);
	}
	
	public void setCumulativeHandExposure(double uvExposureCum) {
		mData.setCumulativeHandExposure(uvExposureCum);
	}
	
	public void setCumulativeChestExposure(double uvExposureCum) {
		mData.setCumulativeChestExposure(uvExposureCum);
	}
	
	public void setCumulativeFaceExposure(double uvExposureCum) {
		mData.setCumulativeFaceExposure(uvExposureCum);
	}
	
	public void setCumulativeLegExposure(double uvExposureCum) {
		mData.setCumulativeLegExposure(uvExposureCum);
	}

	public void setCumulativeForearmExposure(double uvExposureCum) {
		mData.setCumulativeForearmExposure(uvExposureCum);
	}

	public void setCumulativeNeckExposure(double uvExposureCum) {
		mData.setCumulativeNeckExposure(uvExposureCum);
	}
	
	public void setDateTime(long timeInMS) {
		mData.setDateTime(new Date(timeInMS));
	}

	public void setDuration(int durationInSeconds) {
		mData.setDuration(durationInSeconds);
	}

	public void setDistance(int distanceInMeters) {
		mData.setDistance(distanceInMeters);
	}
	
	public void setSweatRate(double sweatRate) {
		mData.setSweatCumulative(sweatRate);
	}

	public void setCalories(int calories) {
		mData.setCalorie(calories);
	}

	public void setHeartrate(int heartrate) {
		mData.setHeartrate(heartrate);
	}

	public void setComment(String comment) {
		mData.setComment(comment);
	}

	public void setActivityType(int activityTypeCode) {
		mData.setActivityType(activityTypeCode);
	}

	public void setInputType(int inputTypeCode) {
		mData.setInputType(inputTypeCode);
	}

	public void setClimb(int climb) {
		mData.setClimb(climb);
	}

	//public void setAvgSpeed(double speed) {
	//	mData.setAvgSpeed(speed);
	//}

	public void setID(long id) {
		mData.setId(Long.valueOf(id));
	}

	public long getID() {
		return mData.getId().longValue();
	}
	
	public int getInputType() {
		return mData.getInputType();
	}
	
	
	public int getSPF() {
		return mData.getSPF();
	}
	
	public float getClothingCover() {
		return mData.getClothingCover();
	}
	
	/*
	public SpriteHeadApparel.HeadApparelType getHeadApparel() {
		return mData.getHeadApparel();
	}
	
	public SpriteUpperApparel.UpperApparelType getUpperApparel() {
		return mData.getUpperApparel();
	}
	
	public SpriteLowerApparel.LowerApparelType getLowerApparel() {
		return mData.getLowerApparel();
	}
	*/
}
