package edu.dartmouth.cs.myruns5;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import edu.dartmouth.cs.myruns5.UserBodyProfileDialog.OurView;

public class SpriteHeadwear {
	final int defaultHeight = 60;
	final int defaultWidth = 60;
	private int x, y, height, width;
	SpritePerson personSprite;
	Bitmap b;
	OurView ov;
	Context context;
	HeadwearType headwear;
	
	// Enumeration for the types of headwear
	private static enum HeadwearType {
		NONE, BASEBALLCAP;
	}
	
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
	
	public SpriteHeadwear(OurView ourView,SpritePerson person) {
		ov = ourView;
		personSprite = person;
		context = ov.getContext();
		b = null;
		headwear = HeadwearType.NONE;
		height = defaultHeight;
		width = defaultWidth;
		x = ov.getWidth()/2 - width/2;
		y = ov.getHeight()/15 - height/2; // TODO: Use a member variable from SpritePerson
	}
	
	public void onDraw(Canvas canvas) {
		if (headwear != HeadwearType.NONE) {
			Rect dst = new Rect(x, y, x + width, y + height);
			canvas.drawBitmap(b, null, dst, null);
		}
	}

	//Method invoked to toggle headwear for male/female sprite
	public void toggle() {
		switch (headwear) {
			case NONE: // Switch to baseball cap
				b = BitmapFactory.decodeResource(context.getResources(),R.drawable.baseball_cap);
				headwear = HeadwearType.BASEBALLCAP;
				break;
			case BASEBALLCAP: // Switch to none
				b = null;
			default:
				headwear = HeadwearType.NONE;
		}
		
		height = b == null? defaultHeight : b.getHeight();
		width = b == null? defaultWidth : b.getWidth();
		x = ov.getWidth()/2 - width/2;
		y = ov.getHeight()/15 - height/2; // TODO: Use a member variable from SpritePerson
	}
}
