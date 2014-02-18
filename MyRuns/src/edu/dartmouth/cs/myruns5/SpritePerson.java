package edu.dartmouth.cs.myruns5;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import edu.dartmouth.cs.myruns5.UserBodyProfileDialog.OurView;

public class SpritePerson {
	int x,y,
		xSpeed,ySpeed,
		height, width;
	Bitmap b;
	OurView ov;
	int direction = 0;//1=female | 0 = male;
	Context context;
	
	public SpritePerson(OurView ourView) {
		ov = ourView;
		x = y = 0;
		context = ov.getContext();
		getGenderFromSharedPreferences();//Grab the Gender of person
		b = BitmapFactory.decodeResource(context.getResources(),R.drawable.human_body_sprite);
		height = b.getHeight();
		width = b.getWidth()/2; // Split image between female/male parts
		
		// Resize image to fit the phone
		while (height > ov.getHeight() || width > ov.getWidth()) {
			height = height*9/10;
			width = width*9/10;
		}
		b = Bitmap.createScaledBitmap(b, width*2, height, false);
	}

	//Save the Gender preference to the Preferences Editor
	private void saveGenderPreference(){
		if(context != null){
			SharedPreferences sharedPref = context.getSharedPreferences(Globals.TAG,Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putInt(context.getString(R.string.data_Gender),direction);
			editor.commit();
		}
	}
	
	public void getGenderFromSharedPreferences(){
		int value = -1;
		SharedPreferences sharedPref = context.getSharedPreferences(Globals.TAG,Context.MODE_PRIVATE);
		try{
		value = sharedPref.getInt(context.getString(R.string.data_Gender),direction);
		direction = value;
		}catch(ClassCastException e){
			e.printStackTrace();
		}
	}
	
	public void onDraw(Canvas canvas) {
		int srcX = direction * width;//if facing up direction 0 x height;
		
		Rect objectMask = new Rect(srcX, 0, srcX + width, height);//Defines what we will cut out of the sprite sheet 
		
		x = ov.getWidth()/2 - width/2;
		y = ov.getHeight()/2 - height/2;
		Rect locationInView = new Rect(x, y, x + width, y + height);//Allows you to scale it such that you can say you can draw it at position x,y and scale it according to other 2 params
		canvas.drawBitmap(b,objectMask,locationInView, null);
	}

	//Method invoked to toggle the Male/Female icon
	public void toggle(){
		direction = direction == 0 ? 1:0;
		saveGenderPreference();
	}
}
