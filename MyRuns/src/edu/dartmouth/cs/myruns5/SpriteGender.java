package edu.dartmouth.cs.myruns5;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import edu.dartmouth.cs.myruns5.UserBodyProfileDialog.OurView;

public class SpriteGender {
	int x,y,
		height, width;
	SpritePerson personSprite;
	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	Bitmap b;
	OurView ov;
	int direction = 0;
	Context context;
	
	public SpriteGender(OurView ourView,SpritePerson person) {
		ov = ourView;
		personSprite = person;
		context = ov.getContext();
		b = BitmapFactory.decodeResource(context.getResources(),R.drawable.male_female_sprite);
		height = b.getHeight();
		width = b.getWidth()/2;//Split male/female
		x=y=6;
		getGenderFromSharedPreferences();//Grab the Gender of persion
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
		
		Rect src = new Rect(srcX,0,srcX + width,height);//Defines what we will cut out of the sprite sheet 
		Rect dst = new Rect(x,y,x + width, y + height);//Allows you to scale it such that you can say you can draw it at position x,y and scale it according to other 2 params 
		canvas.drawBitmap(b,src,dst, null);
	}

	//Method invoked to toggle the Male/Female icon
	public void toggle(){
		direction = direction == 0 ? 1:0;
		//update the person sprite
		personSprite.toggle();
	}
}
