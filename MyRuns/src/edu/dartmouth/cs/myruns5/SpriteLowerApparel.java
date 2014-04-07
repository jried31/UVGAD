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
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import edu.dartmouth.cs.myruns5.Person.Gender;
import edu.dartmouth.cs.myruns5.Person.HeadApparelType.LowerApparelFemaleType;
import edu.dartmouth.cs.myruns5.Person.HeadApparelType.LowerApparelMaleType;
import edu.dartmouth.cs.myruns5.Person.HeadApparelType.UpperApparelFemaleType;
import edu.dartmouth.cs.myruns5.Person.HeadApparelType.UpperApparelMaleType;
import edu.dartmouth.cs.myruns5.UserBodyProfileDialog.OurView;

@SuppressLint("UseSparseArrays")
public class SpriteLowerApparel implements IScaleCallback,IGenderCallback{
	final int defaultHeight = 80;
	final int defaultWidth = 80;
	LowerApparelMaleType lowerApparelMale;
	LowerApparelFemaleType lowerApparelFemale;
	protected int x, y, height, width;
	OurView ov;
	Context context;
	@SuppressLint("UseSparseArrays")
	HashMap<Integer,Bitmap> clothingMaleMap,clothingFemaleMap;
	Rect display=null;
	Bitmap image=null;
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
	

	public SpriteLowerApparel(OurView ourView) {
		ov = ourView;
		context = ov.getContext();
		clothingFemaleMap = new HashMap<Integer,Bitmap>(3);
		clothingMaleMap = new HashMap<Integer,Bitmap>(2);
	}
	
	public void onDraw(Canvas canvas) {
		if (display != null){
			canvas.drawBitmap(image, null, display, null);
		}
	}

	//Method invoked to toggle lower apparel for male/female sprite
	public void toggle() {
		if(gender == Gender.MALE){
			LowerApparelMaleType values[] = LowerApparelMaleType.values();
			int size = values.length,newIndex = lowerApparelMale.ordinal() + 1;
			
			lowerApparelMale = values[newIndex == size ? 0:newIndex];
			display = null;
			image = clothingMaleMap.get(lowerApparelMale.ordinal());
			height = lowerApparelMale == LowerApparelMaleType.NONE ? defaultHeight : image.getHeight();
			width = lowerApparelMale == LowerApparelMaleType.NONE ? defaultWidth : image.getWidth();
				
			saveClothingOptions(R.string.data_ApparelBottom, lowerApparelMale.ordinal());
		}else{
			LowerApparelFemaleType values[] = LowerApparelFemaleType.values();
			int size = values.length,newIndex = lowerApparelFemale.ordinal() + 1;
			
			lowerApparelFemale = values[newIndex == size ? 0:newIndex];
			display = null;
			image = clothingFemaleMap.get(lowerApparelFemale.ordinal());
			height = lowerApparelFemale == LowerApparelFemaleType.NONE ? defaultHeight : image.getHeight();
			width = lowerApparelFemale == LowerApparelFemaleType.NONE ? defaultWidth : image.getWidth();
			
			saveClothingOptions(R.string.data_ApparelBottom, lowerApparelFemale.ordinal());
		}

		this.x = ov.getWidth()/2 - width/2;
		this.y = (int) (ov.getHeight()*.49);

		if(image != null){
			display = new Rect(x, y, x + width, y + height);
			ov.updateRegion(SpriteLowerApparel.class,new Region(x,y,x+width,y+height ));
		}
	}

	@Override
	public void genderUpdate(Gender gender) {
		this.gender = gender;
		//reset the clothing
		if(gender == Gender.MALE){
			lowerApparelMale = LowerApparelMaleType.SHORTS;
			saveClothingOptions(R.string.data_ApparelBottom, lowerApparelMale.ordinal());
		}else{
			lowerApparelFemale =LowerApparelFemaleType.SHORTS;
			saveClothingOptions(R.string.data_ApparelBottom, lowerApparelFemale.ordinal());
		}
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
	public void displayScaleUpdate(double scale, int x, int y) {
		this.scale=scale;
		this.personSpriteX=x;
		this.personSpriteY=y;

		//Female
		clothingFemaleMap.put(LowerApparelFemaleType.NONE.ordinal(), null);
		image = BitmapFactory.decodeResource(context.getResources(), R.drawable.bikini_bottom);
		clothingFemaleMap.put(LowerApparelFemaleType.BIKINI_BOTTOM.ordinal(),Bitmap.createScaledBitmap(image, (int)(image.getWidth()*scale), (int)(image.getHeight()*scale), false));
		image = BitmapFactory.decodeResource(context.getResources(), R.drawable.shorts_woman);
		clothingFemaleMap.put(LowerApparelFemaleType.SHORTS.ordinal(),Bitmap.createScaledBitmap(image, (int)(image.getWidth()*scale), (int)(image.getHeight()*scale), false));
		image = BitmapFactory.decodeResource(context.getResources(), R.drawable.jeans_female);
		clothingFemaleMap.put(LowerApparelFemaleType.JEANS.ordinal(),Bitmap.createScaledBitmap(image, (int)(image.getWidth()*scale), (int)(image.getHeight()*scale), false));


		//Male
		clothingMaleMap.put(LowerApparelMaleType.NONE.ordinal(), null);
		image = BitmapFactory.decodeResource(context.getResources(), R.drawable.shorts_man);
		clothingMaleMap.put(LowerApparelMaleType.SHORTS.ordinal(),Bitmap.createScaledBitmap(image, (int)(image.getWidth()*scale), (int)(image.getHeight()*scale), false));
		image = BitmapFactory.decodeResource(context.getResources(), R.drawable.jeans_male);
		clothingMaleMap.put(LowerApparelMaleType.JEANS.ordinal(),Bitmap.createScaledBitmap(image, (int)(image.getWidth()*scale), (int)(image.getHeight()*scale), false));

		image=null;
		//Get the gender of the person
		SharedPreferences sharedPref = context.getSharedPreferences(Globals.TAG,Context.MODE_PRIVATE);
		switch(gender){
		case MALE:
			int value = sharedPref.getInt(context.getString(R.string.data_ApparelBottom), LowerApparelMaleType.NONE.ordinal());
			lowerApparelMale = LowerApparelMaleType.values()[value];
			
			image = clothingMaleMap.get(value);			
			height = lowerApparelMale == LowerApparelMaleType.NONE ? defaultHeight : image.getHeight();
			width = lowerApparelMale == LowerApparelMaleType.NONE ? defaultWidth : image.getWidth();
			break;
		case FEMALE:
			value = sharedPref.getInt(context.getString(R.string.data_ApparelBottom), LowerApparelFemaleType.NONE.ordinal());
			lowerApparelFemale = LowerApparelFemaleType.values()[value];
			
			image = clothingFemaleMap.get(value);			
			height = lowerApparelFemale == LowerApparelFemaleType.NONE ? defaultHeight : image.getHeight();
			width = lowerApparelFemale == LowerApparelFemaleType.NONE ? defaultWidth : image.getWidth();
		}
		
		this.x = ov.getWidth()/2 - width/2;
		this.y = (int) (ov.getHeight()*.49);
		
		if(image != null){
			display = new Rect(this.x, this.y, this.x + width, this.y + height);
			ov.updateRegion(SpriteLowerApparel.class,new Region(this.x,this.y,this.x+width,this.y+height));
		}
	}
}
