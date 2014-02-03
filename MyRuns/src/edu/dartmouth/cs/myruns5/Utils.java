package edu.dartmouth.cs.myruns5;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;

import com.google.android.gms.maps.model.LatLng;



// Handy utilities you'll need here and there

public class Utils {
	
	// Read from preference, the unit used for displaying distance meter/feet
	// mark
	public static boolean getIsMetricFromPerf(Context context){
		
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		
		String [] unit_display_options = context.getResources()
				.getStringArray(R.array.unit_display_value);
		
		String option = settings.getString(
				context.getString(R.string.preference_key_unit_display), unit_display_options[0]);
		
		String option_metric = context.getString(R.string.kilometers);
		
		if (option.equals(option_metric)) 
			return true;
		else
			return false;
	}
	
	// Convert Location array to byte array, to store in SQLite database
	public static  byte[] fromLocationArrayToByteArray(Location[] locationArray) {
//		Log.d(null, "enter convertion");

		int[] intArray = new int[locationArray.length * 2];

		for (int i = 0; i < locationArray.length; i++) {
			intArray[i * 2] = (int) (locationArray[i].getLatitude() * 1E6);
			intArray[(i * 2) + 1] = (int) (locationArray[i].getLongitude() * 1E6);
		}
//		Log.d(null, "after for");

		ByteBuffer byteBuffer = ByteBuffer.allocate(intArray.length
				* Integer.SIZE);
		IntBuffer intBuffer = byteBuffer.asIntBuffer();
		intBuffer.put(intArray);
//		Log.d(null, "before return");
		return byteBuffer.array();
	}

	public static  Location[] fromByteArrayToLocationArray(byte[] bytePointArray) {

		ByteBuffer byteBuffer = ByteBuffer.wrap(bytePointArray);
		IntBuffer intBuffer = byteBuffer.asIntBuffer();

		int[] intArray = new int[bytePointArray.length / Integer.SIZE];
		intBuffer.get(intArray);

		Location[] locationArray = new Location[intArray.length / 2];

		assert (locationArray != null);

		for (int i = 0; i < locationArray.length; i++) {
			locationArray[i] = new Location("");
			locationArray[i].setLatitude((double) intArray[i * 2] / 1E6F);
			locationArray[i].setLongitude((double) intArray[i * 2 + 1] / 1E6F);
		}
		return locationArray;
	}
	

	public static LatLng fromLocationToLatLng(Location location){
		return new LatLng(location.getLatitude(), location.getLongitude());
		
	}

}
