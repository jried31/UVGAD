package edu.dartmouth.cs.myruns5;

import java.util.HashMap;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import edu.dartmouth.cs.myruns5.UserBodyProfileDialog.OurView;

public abstract class SpriteClothing {
	protected int x, y, height, width;
	OurView ov;
	Context context;
	SpritePerson personSprite;
	HashMap<String,Bitmap> clothingMap;
	Rect display;
	
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
	
	protected SpriteClothing(OurView ourView, SpritePerson person) {
		ov = ourView;
		personSprite = person;
		context = ov.getContext();
		clothingMap = new HashMap<String,Bitmap>();
	}
	
	// Draw clothing
	public abstract void onDraw(Canvas canvas);
	
	// Toggle change in clothing option
	public abstract void toggle();
	
	// Reset clothing option to none
	public abstract void reset();
	
	protected void saveClothingOptions(int resourceId, String typeName) {
		if (context != null) {
			SharedPreferences sharedPref = context.getSharedPreferences(Globals.TAG,Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putString(context.getString(resourceId), typeName);
			editor.commit();
		}
	}
	
	public static void saveClothingCover(Context c, 
			SpriteHeadApparel.HeadApparelType h, SpriteUpperApparel.UpperApparelType u, SpriteLowerApparel.LowerApparelType l) {
		if (c != null) {
			SharedPreferences sharedPref = c.getSharedPreferences(Globals.TAG,Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putFloat(c.getString(R.string.data_ClothingCover), h.getCover() + u.getCover() + l.getCover());
			editor.commit();
		}
	}
}
