package edu.dartmouth.cs.myruns5;

import android.content.Context;
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
	Context context;
	
	public SpriteGender(OurView ourView,SpritePerson person) {
		ov = ourView;
		personSprite = person;
		context = ov.getContext();
		b = BitmapFactory.decodeResource(context.getResources(),R.drawable.male_female_sprite);
		height = b.getHeight();
		width = b.getWidth()/2;//Split male/female
		x=y=6;
	}
	
	public void onDraw(Canvas canvas) {
		int srcX;
		if (personSprite.getGenderFromSharedPreferences() == SpritePerson.Gender.MALE)
			srcX = width;
		else
			srcX = 0;
		
		Rect src = new Rect(srcX,0,srcX + width,height);//Defines what we will cut out of the sprite sheet 
		Rect dst = new Rect(x,y,x + width, y + height);//Allows you to scale it such that you can say you can draw it at position x,y and scale it according to other 2 params 
		canvas.drawBitmap(b,src,dst, null);
	}

	//Method invoked to toggle the Male/Female icon
	public void toggle() {
		//update the person sprite
		personSprite.toggle();
	}
}
