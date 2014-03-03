package edu.dartmouth.cs.myruns5;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.SparseArray;
import edu.dartmouth.cs.myruns5.UserBodyProfileDialog.OurView;

public class SpriteUpperApparel {
	final int defaultHeight = 100;
	final int defaultWidth = 100;
	private int x, y, height, width;
	SpritePerson personSprite;
	SparseArray<Bitmap> upperApparelMap;
	OurView ov;
	Context context;
	UpperApparelType upperApparel;
	
	public enum UpperApparelType {
		NONE(0), TANK_TOP_MALE(1), TANK_TOP_FEMALE(2), TEESHIRT_LONGSLEEVE_MALE(3), TEESHIRT_LONGSLEEVE_FEMALE(4),
		TEESHIRT_SHORTSLEEVE_MALE(5), TEESHIRT_SHORTSLEEVE_FEMALE(6), SPORTS_BRA_FEMALE(7);
		
		int value;
		UpperApparelType(int value) {
			this.value = value;
		}
		
		public int getValue() {
			return value;
		}
		
		public static UpperApparelType getTypeFromValue(int value) {
			if (value == UpperApparelType.NONE.getValue())
				return UpperApparelType.NONE;
			else if (value == UpperApparelType.TANK_TOP_MALE.getValue())
				return UpperApparelType.TANK_TOP_MALE;
			else if (value == UpperApparelType.TANK_TOP_FEMALE.getValue())
				return UpperApparelType.TANK_TOP_FEMALE;
			else if (value == UpperApparelType.TEESHIRT_LONGSLEEVE_MALE.getValue())
				return UpperApparelType.TEESHIRT_LONGSLEEVE_MALE;
			else if (value == UpperApparelType.TEESHIRT_LONGSLEEVE_FEMALE.getValue())
				return UpperApparelType.TEESHIRT_LONGSLEEVE_FEMALE;
			else if (value == UpperApparelType.TEESHIRT_SHORTSLEEVE_MALE.getValue())
				return UpperApparelType.TEESHIRT_SHORTSLEEVE_MALE;
			else if (value == UpperApparelType.TEESHIRT_SHORTSLEEVE_FEMALE.getValue())
				return UpperApparelType.TEESHIRT_SHORTSLEEVE_FEMALE;
			else if (value == UpperApparelType.SPORTS_BRA_FEMALE.getValue())
				return UpperApparelType.SPORTS_BRA_FEMALE;
			return UpperApparelType.NONE;
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
	
	public SpriteUpperApparel(OurView ourView,SpritePerson person) {
		ov = ourView;
		personSprite = person;
		context = ov.getContext();
		
		double scale = person.getScale();
		upperApparelMap = new SparseArray<Bitmap>(8);
		upperApparelMap.put(UpperApparelType.NONE.getValue(), null);
		
		Bitmap b = BitmapFactory.decodeResource(context.getResources(),R.drawable.tank_top_man);
		upperApparelMap.put(UpperApparelType.TANK_TOP_MALE.getValue(), 
				Bitmap.createScaledBitmap(b, (int)(b.getWidth()*scale), (int)(b.getHeight()*scale), false));
		b = BitmapFactory.decodeResource(context.getResources(),R.drawable.tank_top_woman);
		upperApparelMap.put(UpperApparelType.TANK_TOP_FEMALE.getValue(), 
				Bitmap.createScaledBitmap(b, (int)(b.getWidth()*scale), (int)(b.getHeight()*scale), false));
		b = BitmapFactory.decodeResource(context.getResources(),R.drawable.teeshirt_longsleeve_man);
		upperApparelMap.put(UpperApparelType.TEESHIRT_LONGSLEEVE_MALE.getValue(), 
				Bitmap.createScaledBitmap(b, (int)(b.getWidth()*scale), (int)(b.getHeight()*scale), false));
		b = BitmapFactory.decodeResource(context.getResources(),R.drawable.teeshirt_longsleeve_woman);
		upperApparelMap.put(UpperApparelType.TEESHIRT_LONGSLEEVE_FEMALE.getValue(), 
				Bitmap.createScaledBitmap(b, (int)(b.getWidth()*scale), (int)(b.getHeight()*scale), false));
		b = BitmapFactory.decodeResource(context.getResources(),R.drawable.teeshirt_shortsleeve_man);
		upperApparelMap.put(UpperApparelType.TEESHIRT_SHORTSLEEVE_MALE.getValue(), 
				Bitmap.createScaledBitmap(b, (int)(b.getWidth()*scale), (int)(b.getHeight()*scale), false));
		b = BitmapFactory.decodeResource(context.getResources(),R.drawable.teeshirt_shortsleeve_woman);
		upperApparelMap.put(UpperApparelType.TEESHIRT_SHORTSLEEVE_FEMALE.getValue(), 
				Bitmap.createScaledBitmap(b, (int)(b.getWidth()*scale), (int)(b.getHeight()*scale), false));
		b = BitmapFactory.decodeResource(context.getResources(),R.drawable.sports_bra);
		upperApparelMap.put(UpperApparelType.SPORTS_BRA_FEMALE.getValue(), 
				Bitmap.createScaledBitmap(b, (int)(b.getWidth()*scale), (int)(b.getHeight()*scale), false));
		
		SharedPreferences sharedPref = context.getSharedPreferences(Globals.TAG,Context.MODE_PRIVATE);
		int value = sharedPref.getInt(context.getString(R.string.data_ApparelTop), UpperApparelType.NONE.getValue());
		upperApparel = UpperApparelType.getTypeFromValue(value);
		height = upperApparel == upperApparel.NONE ? 
				defaultHeight : upperApparelMap.get(upperApparel.getValue()).getHeight();
		width = upperApparel == upperApparel.NONE ? 
				defaultWidth : upperApparelMap.get(upperApparel.getValue()).getWidth();
		x = ov.getWidth()/2 - width/2;
		y = ov.getHeight()/3 - height/2; // TODO: Use a member variable from SpritePerson
	}
	
	public void onDraw(Canvas canvas) {
		if (upperApparel != UpperApparelType.NONE) {
			Rect dst = new Rect(x, y, x + width, y + height);
			canvas.drawBitmap(upperApparelMap.get(upperApparel.getValue()), null, dst, null);
		}
	}

	//Method invoked to toggle body apparel for male/female sprite
	public void toggle() {
		switch (upperApparel) {
			case NONE: // Switch to tank top
				upperApparel = personSprite.getGenderFromSharedPreferences() == SpritePerson.Gender.MALE ? 
						UpperApparelType.TANK_TOP_MALE : UpperApparelType.TANK_TOP_FEMALE;
				break;
			case TANK_TOP_FEMALE:
			case TANK_TOP_MALE: // Switch to long sleeve
				upperApparel = personSprite.getGenderFromSharedPreferences() == SpritePerson.Gender.MALE ? 
						UpperApparelType.TEESHIRT_LONGSLEEVE_MALE : UpperApparelType.TEESHIRT_LONGSLEEVE_FEMALE;
				break;
			case TEESHIRT_LONGSLEEVE_FEMALE:
			case TEESHIRT_LONGSLEEVE_MALE: // Switch to short sleeve
				upperApparel = personSprite.getGenderFromSharedPreferences() == SpritePerson.Gender.MALE ? 
						UpperApparelType.TEESHIRT_SHORTSLEEVE_MALE : UpperApparelType.TEESHIRT_SHORTSLEEVE_FEMALE;
				break;
			case TEESHIRT_SHORTSLEEVE_FEMALE: // Switch to sports bra
				upperApparel = UpperApparelType.SPORTS_BRA_FEMALE;
				break;
			case SPORTS_BRA_FEMALE:
			case TEESHIRT_SHORTSLEEVE_MALE: // Switch to short sleeve
			default:
				upperApparel = UpperApparelType.NONE;
		}
		
		height = upperApparel == UpperApparelType.NONE ? 
				defaultHeight : upperApparelMap.get(upperApparel.getValue()).getHeight();
		width = upperApparel == UpperApparelType.NONE ? 
				defaultWidth : upperApparelMap.get(upperApparel.getValue()).getWidth();
		
		x = ov.getWidth()/2 - width/2;
		y = personSprite.getY() + (int)(personSprite.getHeight()*0.3) - (int)(height*0.4);
		saveHeadApparelPreferences();
	}
	
	private void saveHeadApparelPreferences() {
		if (context != null) {
			SharedPreferences sharedPref = context.getSharedPreferences(Globals.TAG,Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putInt(context.getString(R.string.data_ApparelTop), upperApparel.getValue());
			editor.commit();
		}
	}
	
	public void reset() {
		upperApparel = UpperApparelType.NONE;
	}
}
