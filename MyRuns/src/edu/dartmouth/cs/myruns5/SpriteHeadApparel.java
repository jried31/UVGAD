package edu.dartmouth.cs.myruns5;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import edu.dartmouth.cs.myruns5.UserBodyProfileDialog.OurView;

public class SpriteHeadApparel extends SpriteClothing {
	final int defaultHeight = 60;
	final int defaultWidth = 60;
	HeadApparelType headApparel;
	
	// Enumeration for the types of head apparel
	public static enum HeadApparelType {
		NONE(0f), BASEBALLCAP(0.05f);
		
		float cover;
		HeadApparelType(float cover) {
			this.cover = cover;
		}

		public float getCover() {
			return cover;
		}

	}
		
	public SpriteHeadApparel(OurView ourView, SpritePerson person) {
		super(ourView, person);
		
		clothingMap.put(HeadApparelType.NONE.name(), null);
		
		double scale = person.getScale();
		Bitmap b = BitmapFactory.decodeResource(context.getResources(),R.drawable.baseball_cap); 
		clothingMap.put(HeadApparelType.BASEBALLCAP.name(), 
				Bitmap.createScaledBitmap(b, (int)(b.getWidth()*scale), (int)(b.getHeight()*scale), false));
		
		SharedPreferences sharedPref = context.getSharedPreferences(Globals.TAG,Context.MODE_PRIVATE);
		String value = sharedPref.getString(context.getString(R.string.data_Hat), HeadApparelType.NONE.name());
		headApparel = HeadApparelType.valueOf(value);
		height = headApparel == HeadApparelType.NONE ? 
				defaultHeight : clothingMap.get(headApparel.name()).getHeight();
		width = headApparel == HeadApparelType.NONE ? 
				defaultWidth : clothingMap.get(headApparel.name()).getWidth();
		x = ov.getWidth()/2 - width/2;
		y = ov.getHeight()/15 - height/2; // TODO: Use a member variable from SpritePerson
		
		display = new Rect(x, y, x + width, y + height);
	}
	
	@Override
	public void onDraw(Canvas canvas) {
		if (headApparel != HeadApparelType.NONE)
			canvas.drawBitmap(clothingMap.get(headApparel.name()), null, display, null);
	}

	//Method invoked to toggle head apparel for male/female sprite
	@Override
	public void toggle() {
		switch (headApparel) {
			case NONE: // Switch to baseball cap
				headApparel = HeadApparelType.BASEBALLCAP;
				break;
			case BASEBALLCAP: // Switch to none
			default:
				headApparel = HeadApparelType.NONE;
		}
		
		height = headApparel == HeadApparelType.NONE ? 
				defaultHeight : clothingMap.get(headApparel.name()).getHeight();
		width = headApparel == HeadApparelType.NONE ? 
				defaultWidth : clothingMap.get(headApparel.name()).getWidth();
		
		x = ov.getWidth()/2 - width/2;
		y = personSprite.getY() - height/4;
		
		display = new Rect(x, y, x + width, y + height);
		
		super.saveClothingOptions(R.string.data_Hat, headApparel.name());
	}
	
	@Override
	public void reset() {
		headApparel = HeadApparelType.NONE;
	}
}
