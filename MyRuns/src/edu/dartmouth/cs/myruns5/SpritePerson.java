package edu.dartmouth.cs.myruns5;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.SparseArray;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import edu.dartmouth.cs.myruns5.Person.Gender;
import edu.dartmouth.cs.myruns5.UserBodyProfileDialog.OurView;

public class SpritePerson {
	ArrayList<IGenderCallback> genderCallbacks = new ArrayList<IGenderCallback>(4);
	ArrayList<IScaleCallback> scaleCallbacks = new ArrayList<IScaleCallback>(4);
	
	int x,y,
		xSpeed,ySpeed,
		height, width;
	double scale;
	SparseArray<Bitmap> personMap=null;
	OurView ov;
	Gender gender=null;
	Rect display=null;
	Bitmap image=null;
	Context context;
	boolean scaleReady=false;
	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}
	public int getWidth() {
		return width;
	}
	public int getHeight() {
		return height;
	}
	
	public void registerGenderCallBack(IGenderCallback genderCallback) {
		//if Gender has not been retrieved yet, set an array list
		genderCallbacks.add(genderCallback);
		
		if(gender != null)
			genderCallback.genderUpdate(gender);
    }
	
	
	public void registerScaleCallBack(IScaleCallback scaleCallback) {
		//if Gender has not been retrieved yet, set an array list
		scaleCallbacks.add(scaleCallback);
		if(scaleReady)
			scaleCallback.displayScaleUpdate(scale, x, y);
    }
	
	public SpritePerson(OurView ourView) {
		ov = ourView;
		context = ov.getContext();
		
		//Load sex from shared preference
		SharedPreferences sharedPref = context.getSharedPreferences(Globals.TAG,Context.MODE_PRIVATE);
		int value = sharedPref.getInt(context.getString(R.string.data_Gender),Gender.FEMALE.ordinal());
		gender = Gender.values()[value]; 
		//Notify listeners who are queued
		notifyListenersGenderUpdate();
		
		//Properly scale the image when the Global View is Rendered (Google's Stupidness)
		ourView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			 @SuppressLint("NewApi")
			 @SuppressWarnings("deprecation")
			 @Override
			  public void onGlobalLayout() {
			   //now we can retrieve the width and height
			   image = BitmapFactory.decodeResource(context.getResources(),R.drawable.female_body_sprite);
			   height = image.getHeight();
			   width = image.getWidth();
			   scale = 1;
				
			   int screenWidth = ov.getWidth(),
					   screenHeight = ov.getHeight();
				
			   double scaleWidth = width/screenWidth,
					scaleHeight = height/screenHeight;
				
			   //Scale the person image down if necessary
			   if(scaleWidth > 1){
				   scale = scaleWidth;
			   }else if(scaleHeight > 1){
				   scale = scaleHeight;
			   }
				
			   width=(int)(scale*width);
			   height = (int)(scale*height);
				// Resize image to fit the phone
				personMap = new SparseArray<Bitmap>(2);
				personMap.put(Gender.MALE.ordinal(), Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(),R.drawable.male_body_sprite),width,height, false));
				personMap.put(Gender.FEMALE.ordinal(), Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(),R.drawable.female_body_sprite), width,height, false));		
				scaleReady=true;
				//Notifiy listeners of the Body scale who have been queued
				notifyListenersDisplayScaleUpdate();

				updateDisplay(gender);
				if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
					ov.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				else
					ov.getViewTreeObserver().removeGlobalOnLayoutListener(this);
			 }
		});
		

	}
	
	public double getScale() {
		return scale;
	}

	//Save the Gender preference to the Preferences Editor
	private void saveGenderPreference() {
		if (context != null) {
			SharedPreferences sharedPref = context.getSharedPreferences(Globals.TAG,Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putInt(context.getString(R.string.data_Gender), gender.ordinal());
			editor.commit();
		}
	}
	
	public Gender getGender() {
		return gender;
	}
	
	public void onDraw(Canvas canvas) {
		if(display != null)
			canvas.drawBitmap(image, null, display, null);
	}
	
	private void notifyListenersGenderUpdate(){
		for(IGenderCallback l:genderCallbacks){
			l.genderUpdate(gender);
		}
	}
	
	private void notifyListenersDisplayScaleUpdate(){
		for(IScaleCallback l:scaleCallbacks){
			l.displayScaleUpdate(scale, x, y);
		}
	}
	
	public void updateDisplay(Gender gender){
		image = personMap.get(gender.ordinal());
		width = image.getWidth();
		height = image.getHeight();
		x = ov.getWidth()/2 - width/2;
		y = ov.getHeight()/2 - height/2;
		display = new Rect(x, y, x + width, y + height);//Allows you to scale it such that you can say you can draw it at position x,y and scale it according to other 2 params
	}
	
	//Method invoked to toggle the Male/Female icon
	public void toggle() {
		gender = gender == Gender.MALE ? Gender.FEMALE : Gender.MALE;
		updateDisplay(gender);
		saveGenderPreference();
		notifyListenersGenderUpdate();
	}
}
