package edu.dartmouth.cs.myruns5;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Region;
import android.view.ViewTreeObserver;
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

	Bitmap image;
	OurView ov;
	Context context;
	
	public SpriteGender(OurView ourView,SpritePerson person) {
		ov = ourView;
		context = ov.getContext();
		personSprite = person;
		ov.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
	        @Override
	        public void onGlobalLayout() {
	        	
	        	if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
	        		ov.getViewTreeObserver().removeOnGlobalLayoutListener(this);
	        	else
	        		ov.getViewTreeObserver().removeGlobalOnLayoutListener(this);
	        	image = BitmapFactory.decodeResource(context.getResources(),R.drawable.male_female_sprite);
	        	height = image.getHeight();
	        	width = image.getWidth()/2;//Split male/female
	        	x=y=6;
	    		updateGenderImage();
	    		ov.updateRegion(SpriteGender.class, new Region(x,y,x+width,y+height));
	        }
	    });
		
	}
	
	void updateGenderImage(){
		int srcX = personSprite.getGender() == Person.Gender.MALE ? width:0;
		src = new Rect(srcX,0,srcX + width,height);//Defines what we will cut out of the sprite sheet 
		dst = new Rect(x,y,x + width, y + height);//Allows you to scale it such that you can say you can draw it at position x,y and scale it according to other 2 params 	
	}
	
	Rect src=null,dst=null;
	public void onDraw(Canvas canvas) {
		canvas.drawBitmap(image,src,dst, null);
	}

	//Method invoked to toggle the Male/Female icon
	public void toggle() {
		//update the person sprite
		personSprite.toggle();
		updateGenderImage();
	}
}
