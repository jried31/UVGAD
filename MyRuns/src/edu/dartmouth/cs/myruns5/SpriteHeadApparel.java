package edu.dartmouth.cs.myruns5;

import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Region;
import edu.dartmouth.cs.myruns5.Person.Gender;
import edu.dartmouth.cs.myruns5.Person.HeadApparelType;
import edu.dartmouth.cs.myruns5.Person.HeadApparelType.LowerApparelFemaleType;
import edu.dartmouth.cs.myruns5.Person.HeadApparelType.LowerApparelMaleType;
import edu.dartmouth.cs.myruns5.UserBodyProfileDialog.OurView;

public class SpriteHeadApparel implements IGenderCallback,IScaleCallback {
	HeadApparelType headApparel;	
	final int defaultHeight = 60;
	final int defaultWidth = 60;
	protected int x, y, height, width;
	OurView ov;
	Context context;
	@SuppressLint("UseSparseArrays")
	HashMap<Integer,Bitmap> clothingMap=new HashMap<Integer,Bitmap>(2);
	Rect display=null;
	Bitmap image = null;
	private Gender gender;
	private double scale;
	private int personSpriteX;
	private int personSpriteY;
	
	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}
	public int getWidth() {
		return width;
	}
	public int getHeight() {
		return height;
	}
	
	
	public SpriteHeadApparel(OurView ourView) {
		ov = ourView;
		context = ov.getContext();
	}
	
	public void onDraw(Canvas canvas) {
		if (display != null){
			canvas.drawBitmap(image, null, display, null);
		}
	}

	public void updateDisplay(HeadApparelType headApparel){
		image = clothingMap.get(headApparel.ordinal());
		display = null;
		height = headApparel == HeadApparelType.NONE ? defaultHeight : image.getHeight();
		width = headApparel == HeadApparelType.NONE ? defaultWidth : image.getWidth();
		x = ov.getWidth()/2 - width/2;
		y = personSpriteY+200 - height/2;
		if(headApparel != HeadApparelType.NONE)
			display = new Rect(x, y, x + width, y + height);

		ov.updateRegion(SpriteHeadApparel.class,new Region(x,y,x+width,y+height ));
	}
	
	
	public void toggle() {
		HeadApparelType values[] = HeadApparelType.values();
		int size = values.length,newIndex = headApparel.ordinal() + 1;
		
		headApparel = values[newIndex == size ? 0:newIndex];
		updateDisplay(headApparel);

		saveClothingOptions(R.string.data_Hat, headApparel.ordinal());
	}

	protected void saveClothingOptions(int resourceId, int index) {
		if (context != null) {
			SharedPreferences sharedPref = context.getSharedPreferences(Globals.TAG,Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putInt(context.getString(resourceId), index);
			editor.commit();
		}
	}
	
	@Override
	public void genderUpdate(Gender gender) {
		this.gender = gender;
		headApparel = HeadApparelType.NONE;
		display = null;
		saveClothingOptions(R.string.data_Hat, headApparel.ordinal());
	}
	
	@Override
	public void displayScaleUpdate(double scale,int x,int y) {
		this.scale=scale;
		this.personSpriteX=x;
		this.personSpriteY=y;
		image = BitmapFactory.decodeResource(context.getResources(), R.drawable.baseball_cap);
		width = (int)(image.getWidth() * scale);
		height = (int)(image.getHeight() * scale);
		image = Bitmap.createScaledBitmap(image, width,height, false);
		
		// Setup Two items
		clothingMap.put(HeadApparelType.NONE.ordinal(), null);
		clothingMap.put(HeadApparelType.BASEBALLCAP.ordinal(),image);
		image=null;
		
		SharedPreferences sharedPref = context.getSharedPreferences(Globals.TAG, Context.MODE_PRIVATE);
		int value = sharedPref.getInt(context.getString(R.string.data_Hat), HeadApparelType.NONE.ordinal());
		headApparel = HeadApparelType.values()[value];
		updateDisplay(headApparel);
	}
}
