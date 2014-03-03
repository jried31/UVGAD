package edu.dartmouth.cs.myruns5;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.SparseArray;
import edu.dartmouth.cs.myruns5.UserBodyProfileDialog.OurView;

public class SpriteLowerApparel {
	final int defaultHeight = 80;
	final int defaultWidth = 80;
	private int x, y, height, width;
	SpritePerson personSprite;
	SparseArray<Bitmap> lowerApparelMap;
	OurView ov;
	Context context;
	LowerApparelType lowerApparel;
	
	public enum LowerApparelType {
		NONE(0), SHORTS_MALE(1), SHORTS_FEMALE(2);
		
		int value;
		LowerApparelType(int value) {
			this.value = value;
		}
		
		public int getValue() {
			return value;
		}
		
		public static LowerApparelType getTypeFromValue(int value) {
			if (value == LowerApparelType.NONE.getValue())
				return LowerApparelType.NONE;
			else if (value == LowerApparelType.SHORTS_MALE.getValue())
				return LowerApparelType.SHORTS_MALE;
			else if (value == LowerApparelType.SHORTS_FEMALE.getValue())
				return LowerApparelType.SHORTS_FEMALE;
			return LowerApparelType.NONE;
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
	
	public SpriteLowerApparel(OurView ourView,SpritePerson person) {
		ov = ourView;
		personSprite = person;
		context = ov.getContext();
		lowerApparelMap = new SparseArray<Bitmap>(3);
		lowerApparelMap.put(LowerApparelType.NONE.getValue(), null);
		
		double scale = person.getScale();
		Bitmap b = BitmapFactory.decodeResource(context.getResources(),R.drawable.shorts_man);
		lowerApparelMap.put(LowerApparelType.SHORTS_MALE.getValue(), 
				Bitmap.createScaledBitmap(b, (int)(b.getWidth()*scale), (int)(b.getHeight()*scale), false));
		b = BitmapFactory.decodeResource(context.getResources(),R.drawable.shorts_woman);
		lowerApparelMap.put(LowerApparelType.SHORTS_FEMALE.getValue(), 
				Bitmap.createScaledBitmap(b, (int)(b.getWidth()*scale), (int)(b.getHeight()*scale), false));
		
		SharedPreferences sharedPref = context.getSharedPreferences(Globals.TAG,Context.MODE_PRIVATE);
		int value = sharedPref.getInt(context.getString(R.string.data_ApparelBottom), LowerApparelType.NONE.getValue());
		lowerApparel = LowerApparelType.getTypeFromValue(value);
		height = lowerApparel == lowerApparel.NONE ? 
				defaultHeight : lowerApparelMap.get(lowerApparel.getValue()).getHeight();
		width = lowerApparel == lowerApparel.NONE ? 
				defaultWidth : lowerApparelMap.get(lowerApparel.getValue()).getWidth();
		x = ov.getWidth()/2 - width/2;
		y = ov.getHeight()*5/9 - height/2; // TODO: Use a member variable from SpritePerson
	}
	
	public void onDraw(Canvas canvas) {
		if (lowerApparel != LowerApparelType.NONE) {
			Rect dst = new Rect(x, y, x + width, y + height);
			canvas.drawBitmap(lowerApparelMap.get(lowerApparel.getValue()), null, dst, null);
		}
	}

	//Method invoked to toggle lower apparel for male/female sprite
	public void toggle() {
		switch (lowerApparel) {
			case NONE: // Switch to shorts
				lowerApparel = personSprite.getGenderFromSharedPreferences() == SpritePerson.Gender.MALE ? 
						LowerApparelType.SHORTS_MALE : LowerApparelType.SHORTS_FEMALE;
				break;
			case SHORTS_FEMALE:
			case SHORTS_MALE: // Switch to none
			default:
				lowerApparel = LowerApparelType.NONE;
		}
		
		height = lowerApparel == LowerApparelType.NONE ? 
				defaultHeight : lowerApparelMap.get(lowerApparel.getValue()).getHeight();
		width = lowerApparel == LowerApparelType.NONE ? 
				defaultWidth : lowerApparelMap.get(lowerApparel.getValue()).getWidth();
		
		x = ov.getWidth()/2 - width/2;
		y = personSprite.getY() + (int)(personSprite.getHeight()*0.45);
		saveHeadApparelPreferences();
	}
	
	private void saveHeadApparelPreferences() {
		if (context != null) {
			SharedPreferences sharedPref = context.getSharedPreferences(Globals.TAG,Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putInt(context.getString(R.string.data_ApparelBottom), lowerApparel.getValue());
			editor.commit();
		}
	}
	
	public void reset() {
		lowerApparel = LowerApparelType.NONE;
	}
}
