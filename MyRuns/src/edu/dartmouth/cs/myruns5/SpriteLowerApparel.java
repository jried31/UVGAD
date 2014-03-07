package edu.dartmouth.cs.myruns5;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import edu.dartmouth.cs.myruns5.UserBodyProfileDialog.OurView;

public class SpriteLowerApparel extends SpriteClothing {
	final int defaultHeight = 80;
	final int defaultWidth = 80;
	LowerApparelType lowerApparel;
	
	public enum LowerApparelType {
		NONE(0f), SHORTS_MALE(0.15f), SHORTS_FEMALE(0.15f), JEANS(0.30f);
		
		float cover;
		LowerApparelType(float cover) {
			this.cover = cover;
		}

		public float getCover() {
			return cover;
		}
	}
	
	public SpriteLowerApparel(OurView ourView,SpritePerson person) {
		super(ourView, person);

		clothingMap.put(LowerApparelType.NONE.name(), null);
		
		double scale = person.getScale();
		Bitmap b = BitmapFactory.decodeResource(context.getResources(), R.drawable.shorts_man);
		clothingMap.put(LowerApparelType.SHORTS_MALE.name(), 
				Bitmap.createScaledBitmap(b, (int)(b.getWidth()*scale), (int)(b.getHeight()*scale), false));
		b = BitmapFactory.decodeResource(context.getResources(), R.drawable.shorts_woman);
		clothingMap.put(LowerApparelType.SHORTS_FEMALE.name(), 
				Bitmap.createScaledBitmap(b, (int)(b.getWidth()*scale), (int)(b.getHeight()*scale), false));
		b = BitmapFactory.decodeResource(context.getResources(), R.drawable.jeans);
		clothingMap.put(LowerApparelType.JEANS.name(), 
				Bitmap.createScaledBitmap(b, (int)(b.getWidth()*scale), (int)(b.getHeight()*scale), false));
		
		SharedPreferences sharedPref = context.getSharedPreferences(Globals.TAG,Context.MODE_PRIVATE);
		String value = sharedPref.getString(context.getString(R.string.data_ApparelBottom), LowerApparelType.NONE.name());
		lowerApparel = LowerApparelType.valueOf(value);
		height = lowerApparel == LowerApparelType.NONE ? 
				defaultHeight : clothingMap.get(lowerApparel.name()).getHeight();
		width = lowerApparel == LowerApparelType.NONE ? 
				defaultWidth : clothingMap.get(lowerApparel.name()).getWidth();
		x = ov.getWidth()/2 - width/2;
		y = personSprite.getY() + (int)(personSprite.getHeight()*0.45); // TODO: Use a member variable from SpritePerson
		
		display = new Rect(x, y, x + width, y + height);
	}
	
	@Override
	public void onDraw(Canvas canvas) {
		if (lowerApparel != LowerApparelType.NONE)
			canvas.drawBitmap(clothingMap.get(lowerApparel.name()), null, display, null);
	}

	//Method invoked to toggle lower apparel for male/female sprite
	@Override
	public void toggle() {
		switch (lowerApparel) {
			case NONE: // Switch to shorts
				lowerApparel = personSprite.getGenderFromSharedPreferences() == SpritePerson.Gender.MALE ? 
						LowerApparelType.SHORTS_MALE : LowerApparelType.SHORTS_FEMALE;
				break;
			case SHORTS_FEMALE:
			case SHORTS_MALE: // Switch to jeans
				lowerApparel = LowerApparelType.JEANS;
				break;
			case JEANS: // Switch to none
			default:
				lowerApparel = LowerApparelType.NONE;
		}
		
		height = lowerApparel == LowerApparelType.NONE ? 
				defaultHeight : clothingMap.get(lowerApparel.name()).getHeight();
		width = lowerApparel == LowerApparelType.NONE ? 
				defaultWidth : clothingMap.get(lowerApparel.name()).getWidth();
		
		x = ov.getWidth()/2 - width/2;
		y = personSprite.getY() + (int)(personSprite.getHeight()*0.45);
		
		display = new Rect(x, y, x + width, y + height);
		
		super.saveClothingOptions(R.string.data_ApparelBottom, lowerApparel.name());
	}
	
	@Override
	public void reset() {
		lowerApparel = LowerApparelType.NONE;
	}
}
