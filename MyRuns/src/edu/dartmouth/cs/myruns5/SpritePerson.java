package edu.dartmouth.cs.myruns5;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.SparseArray;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import edu.dartmouth.cs.myruns5.UserBodyProfileDialog.OurView;

public class SpritePerson {
	public enum Gender {
		MALE(0), FEMALE(1);
		
		private int value;
		Gender(int value) {
			this.value = value;
		}
		
		public int getValue() {
			return this.value;
		}
	}
	
	int x,y,
		xSpeed,ySpeed,
		height, width;
	double scale;
	SparseArray<Bitmap> personMap;
	OurView ov;
	public Gender gender;
	Context context;
	
	public SpritePerson(OurView ourView) {
		ov = ourView;
		x = y = 0;
		context = ov.getContext();
		gender = getGenderFromSharedPreferences(); //Grab the Gender of person
		personMap = new SparseArray<Bitmap>(2);
		personMap.put(Gender.MALE.getValue(), BitmapFactory.decodeResource(context.getResources(),R.drawable.male_body_sprite));
		personMap.put(Gender.FEMALE.getValue(), BitmapFactory.decodeResource(context.getResources(),R.drawable.female_body_sprite));
		height = personMap.get(gender.getValue()).getHeight();
		width = personMap.get(gender.getValue()).getWidth();
		
		// Resize image to fit the phone
		scale = 1.0;
		while (height > ov.getHeight() || width > ov.getWidth()) {
			height = height*9/10;
			width = width*9/10;
			scale *= 9.0/10.0;
		}
		personMap.put(Gender.MALE.getValue(), Bitmap.createScaledBitmap(
				BitmapFactory.decodeResource(context.getResources(),R.drawable.male_body_sprite), width, height, false));
		personMap.put(Gender.FEMALE.getValue(), Bitmap.createScaledBitmap(
				BitmapFactory.decodeResource(context.getResources(),R.drawable.female_body_sprite), width, height, false));
	}
	
	public double getScale() {
		return scale;
	}

	//Save the Gender preference to the Preferences Editor
	private void saveGenderPreference() {
		if (context != null) {
			SharedPreferences sharedPref = context.getSharedPreferences(Globals.TAG,Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putInt(context.getString(R.string.data_Gender), gender.getValue());
			editor.commit();
		}
	}
	
	public Gender getGenderFromSharedPreferences() {
		int value = -1;
		SharedPreferences sharedPref = context.getSharedPreferences(Globals.TAG,Context.MODE_PRIVATE);
		try {
			value = sharedPref.getInt(context.getString(R.string.data_Gender), 0);
		} catch(ClassCastException e) {
			e.printStackTrace();
		}
		
		return value == 0 ? Gender.MALE : Gender.FEMALE;
	}
	
	public void onDraw(Canvas canvas) {
		x = ov.getWidth()/2 - width/2;
		y = ov.getHeight()/2 - height/2;
		Rect locationInView = new Rect(x, y, x + width, y + height);//Allows you to scale it such that you can say you can draw it at position x,y and scale it according to other 2 params
		canvas.drawBitmap(personMap.get(gender.getValue()), null, locationInView, null);
	}

	//Method invoked to toggle the Male/Female icon
	public void toggle() {
		gender = gender == Gender.MALE ? Gender.FEMALE : Gender.MALE;
		height = personMap.get(gender.getValue()).getHeight();
		width = personMap.get(gender.getValue()).getWidth();
		
		saveGenderPreference();
	}
}
