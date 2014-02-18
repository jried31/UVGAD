package edu.dartmouth.cs.myruns5;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import edu.dartmouth.cs.myruns5.UserBodyProfileDialog.OurView;

public class SpriteLegs {
	final int defaultHeight = 80;
	final int defaultWidth = 80;
	private int x, y, height, width;
	SpritePerson personSprite;
	Bitmap b;
	OurView ov;
	Context context;
	LegsType legs;
	
	private enum LegsType {
		NONE, SHORTS_MALE, SHORTS_FEMALE;
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
	
	public SpriteLegs(OurView ourView,SpritePerson person) {
		ov = ourView;
		personSprite = person;
		context = ov.getContext();
		b = null;
		legs = LegsType.NONE;
		height = defaultHeight;
		width = defaultWidth;
		x = ov.getWidth()/2 - width/2;
		y = ov.getHeight()*5/9 - height/2; // TODO: Use a member variable from SpritePerson
	}
	
	public void onDraw(Canvas canvas) {
		if (legs != LegsType.NONE) {
			Rect dst = new Rect(x, y, x + width, y + height);
			canvas.drawBitmap(b, null, dst, null);
		}
	}

	//Method invoked to toggle legs for male/female sprite
	public void toggle() {
		switch (legs) {
			case NONE: // Switch to shorts
				b = BitmapFactory.decodeResource(context.getResources(),R.drawable.shorts_man);
				legs = LegsType.SHORTS_MALE;
				break;
			case SHORTS_MALE: // Switch to none
				b = null;
			default:
				legs = LegsType.NONE;
		}
		
		height = b == null? defaultHeight : b.getHeight();
		width = b == null? defaultWidth : b.getWidth();
		x = ov.getWidth()/2 - width/2;
		y = ov.getHeight()*5/9 - height/2; // TODO: Use a member variable from SpritePerson
	}
}
