package edu.dartmouth.cs.myruns5;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.SparseArray;
import edu.dartmouth.cs.myruns5.UserBodyProfileDialog.OurView;

public class SpriteHeadApparel {
	final int defaultHeight = 60;
	final int defaultWidth = 60;
	private int x, y, height, width;
	SpritePerson personSprite;
	SparseArray<Bitmap> headApparelMap;
	OurView ov;
	Context context;
	HeadApparelType headApparel;
	
	// Enumeration for the types of head apparel
	public static enum HeadApparelType {
		NONE(0), BASEBALLCAP(1);
		
		int value;
		HeadApparelType(int value) {
			this.value = value;
		}
		
		public int getValue() {
			return value;
		}
		
		public static HeadApparelType getTypeFromValue(int value) {
			if (value == HeadApparelType.NONE.getValue())
				return HeadApparelType.NONE;
			else if (value == HeadApparelType.BASEBALLCAP.getValue())
				return HeadApparelType.BASEBALLCAP;
			return HeadApparelType.NONE;
		}
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
	
	public SpriteHeadApparel(OurView ourView,SpritePerson person) {
		ov = ourView;
		personSprite = person;
		context = ov.getContext();
		headApparelMap = new SparseArray<Bitmap>();
		headApparelMap.put(HeadApparelType.NONE.getValue(), null);
		headApparelMap.put(HeadApparelType.BASEBALLCAP.getValue(), 
				BitmapFactory.decodeResource(context.getResources(),R.drawable.baseball_cap));
		
		SharedPreferences sharedPref = context.getSharedPreferences(Globals.TAG,Context.MODE_PRIVATE);
		int value = sharedPref.getInt(context.getString(R.string.data_Hat), HeadApparelType.NONE.getValue());
		headApparel = HeadApparelType.getTypeFromValue(value);
		height = headApparel == HeadApparelType.NONE ? 
				defaultHeight : headApparelMap.get(headApparel.getValue()).getHeight();
		width = headApparel == HeadApparelType.NONE ? 
				defaultWidth : headApparelMap.get(headApparel.getValue()).getWidth();
		x = ov.getWidth()/2 - width/2;
		y = ov.getHeight()/15 - height/2; // TODO: Use a member variable from SpritePerson
	}
	
	public void onDraw(Canvas canvas) {
		if (headApparel != HeadApparelType.NONE) {
			Rect dst = new Rect(x, y, x + width, y + height);
			canvas.drawBitmap(headApparelMap.get(headApparel.getValue()), null, dst, null);
		}
	}

	//Method invoked to toggle head apparel for male/female sprite
	public void toggle() {
		switch (headApparel) {
			case NONE: // Switch to baseball cap
				headApparel = HeadApparelType.BASEBALLCAP;
				height = headApparelMap.get(headApparel.getValue()).getHeight();
				width = headApparelMap.get(headApparel.getValue()).getWidth();
				break;
			case BASEBALLCAP: // Switch to none
			default:
				headApparel = HeadApparelType.NONE;
				height = defaultHeight;
				width = defaultWidth;
		}
		
		x = ov.getWidth()/2 - width/2;
		y = personSprite.getY() - height/4;
		saveHeadApparelPreferences();
	}
	
	private void saveHeadApparelPreferences() {
		if (context != null) {
			SharedPreferences sharedPref = context.getSharedPreferences(Globals.TAG,Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putInt(context.getString(R.string.data_Hat), headApparel.getValue());
			editor.commit();
		}
	}
	
	public void reset() {
		headApparel = HeadApparelType.NONE;
	}
}
