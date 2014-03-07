package edu.dartmouth.cs.myruns5;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.SparseArray;
import edu.dartmouth.cs.myruns5.UserBodyProfileDialog.OurView;

public class SpriteUpperApparel extends SpriteClothing {
	final int defaultHeight = 100;
	final int defaultWidth = 100;
	UpperApparelType upperApparel;
	
	public enum UpperApparelType {
		NONE(0f), TANK_TOP_MALE(0.20f), TANK_TOP_FEMALE(0.20f), TEESHIRT_LONGSLEEVE_MALE(0.35f), 
		TEESHIRT_LONGSLEEVE_FEMALE(0.35f), TEESHIRT_SHORTSLEEVE_MALE(0.25f), TEESHIRT_SHORTSLEEVE_FEMALE(0.25f), 
		SPORTS_BRA_FEMALE(0.05f);

		float cover;
		UpperApparelType(float cover) {
			this.cover = cover;
		}

		public float getCover() {
			return cover;
		}
	}
	
	public SpriteUpperApparel(OurView ourView,SpritePerson person) {
		super(ourView, person);
		
		double scale = person.getScale();
		clothingMap.put(UpperApparelType.NONE.name(), null);
		
		Bitmap b = BitmapFactory.decodeResource(context.getResources(),R.drawable.tank_top_man);
		clothingMap.put(UpperApparelType.TANK_TOP_MALE.name(), 
				Bitmap.createScaledBitmap(b, (int)(b.getWidth()*scale), (int)(b.getHeight()*scale), false));
		b = BitmapFactory.decodeResource(context.getResources(),R.drawable.tank_top_woman);
		clothingMap.put(UpperApparelType.TANK_TOP_FEMALE.name(), 
				Bitmap.createScaledBitmap(b, (int)(b.getWidth()*scale), (int)(b.getHeight()*scale), false));
		b = BitmapFactory.decodeResource(context.getResources(),R.drawable.teeshirt_longsleeve_man);
		clothingMap.put(UpperApparelType.TEESHIRT_LONGSLEEVE_MALE.name(), 
				Bitmap.createScaledBitmap(b, (int)(b.getWidth()*scale), (int)(b.getHeight()*scale), false));
		b = BitmapFactory.decodeResource(context.getResources(),R.drawable.teeshirt_longsleeve_woman);
		clothingMap.put(UpperApparelType.TEESHIRT_LONGSLEEVE_FEMALE.name(), 
				Bitmap.createScaledBitmap(b, (int)(b.getWidth()*scale), (int)(b.getHeight()*scale), false));
		b = BitmapFactory.decodeResource(context.getResources(),R.drawable.teeshirt_shortsleeve_man);
		clothingMap.put(UpperApparelType.TEESHIRT_SHORTSLEEVE_MALE.name(), 
				Bitmap.createScaledBitmap(b, (int)(b.getWidth()*scale), (int)(b.getHeight()*scale), false));
		b = BitmapFactory.decodeResource(context.getResources(),R.drawable.teeshirt_shortsleeve_woman);
		clothingMap.put(UpperApparelType.TEESHIRT_SHORTSLEEVE_FEMALE.name(), 
				Bitmap.createScaledBitmap(b, (int)(b.getWidth()*scale), (int)(b.getHeight()*scale), false));
		b = BitmapFactory.decodeResource(context.getResources(),R.drawable.sports_bra);
		clothingMap.put(UpperApparelType.SPORTS_BRA_FEMALE.name(), 
				Bitmap.createScaledBitmap(b, (int)(b.getWidth()*scale), (int)(b.getHeight()*scale), false));
		
		SharedPreferences sharedPref = context.getSharedPreferences(Globals.TAG,Context.MODE_PRIVATE);
		String value = sharedPref.getString(context.getString(R.string.data_ApparelTop), UpperApparelType.NONE.name());
		upperApparel = UpperApparelType.valueOf(value);
		height = upperApparel == UpperApparelType.NONE ? 
				defaultHeight : clothingMap.get(upperApparel.name()).getHeight();
		width = upperApparel == UpperApparelType.NONE ? 
				defaultWidth : clothingMap.get(upperApparel.name()).getWidth();
		x = ov.getWidth()/2 - width/2;
		y = ov.getHeight()/3 - height/2; // TODO: Use a member variable from SpritePerson
		
		display = new Rect(x, y, x + width, y + height);
	}
	
	@Override
	public void onDraw(Canvas canvas) {
		if (upperApparel != UpperApparelType.NONE)
			canvas.drawBitmap(clothingMap.get(upperApparel.name()), null, display, null);
	}

	//Method invoked to toggle body apparel for male/female sprite
	@Override
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
				defaultHeight : clothingMap.get(upperApparel.name()).getHeight();
		width = upperApparel == UpperApparelType.NONE ? 
				defaultWidth : clothingMap.get(upperApparel.name()).getWidth();
		
		x = ov.getWidth()/2 - width/2;
		y = personSprite.getY() + (int)(personSprite.getHeight()*0.3) - (int)(height*0.4);
		
		display = new Rect(x, y, x + width, y + height);
		
		super.saveClothingOptions(R.string.data_ApparelTop, upperApparel.name());
	}
	
	@Override
	public void reset() {
		upperApparel = UpperApparelType.NONE;
	}
}
