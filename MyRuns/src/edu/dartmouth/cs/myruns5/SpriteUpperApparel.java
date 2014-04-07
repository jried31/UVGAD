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
import edu.dartmouth.cs.myruns5.Person.HeadApparelType.UpperApparelFemaleType;
import edu.dartmouth.cs.myruns5.Person.HeadApparelType.UpperApparelMaleType;
import edu.dartmouth.cs.myruns5.UserBodyProfileDialog.OurView;

@SuppressLint("UseSparseArrays")
public class SpriteUpperApparel implements IScaleCallback,IGenderCallback{
	UpperApparelMaleType upperApparelMale;	
	final int defaultHeight = 100;
	final int defaultWidth = 100;
	UpperApparelFemaleType upperApparelFemale;
	protected int x, y, height, width;
	OurView ov;
	Context context;
	HashMap<Integer,Bitmap> clothingMaleMap,clothingFemaleMap;
	Rect display=null;
	Bitmap image = null;
	private Gender gender=null;
	private double scale;
	private int personSpriteY;
	private int personSpriteX;
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

	public SpriteUpperApparel(OurView ourView) {
		ov = ourView;
		context = ov.getContext();
		clothingFemaleMap = new HashMap<Integer,Bitmap>(6);
		clothingMaleMap = new HashMap<Integer,Bitmap>(4);
	}
	

	public void onDraw(Canvas canvas) {
		if (display != null)
			canvas.drawBitmap(image, null, display, null);
	}

	//Method invoked to toggle body apparel for male/female sprite
	public void toggle() {
		if(gender == Gender.MALE){
			UpperApparelMaleType values[] = UpperApparelMaleType.values();
			int size = values.length,newIndex = upperApparelMale.ordinal() + 1;
			upperApparelMale = values[newIndex == size ? 0:newIndex];
			
			display = null;
			image = clothingMaleMap.get(upperApparelMale.ordinal());
			height = upperApparelMale == UpperApparelMaleType.NONE ? defaultHeight : image.getHeight();
			width = upperApparelMale == UpperApparelMaleType.NONE ? defaultWidth : image.getWidth();
			
			saveClothingOptions(R.string.data_ApparelTop, upperApparelMale.ordinal());
		}else{
			UpperApparelFemaleType values[] = UpperApparelFemaleType.values();
			int size = values.length,newIndex = upperApparelFemale.ordinal() + 1;
			upperApparelFemale = values[newIndex == size ? 0:newIndex];
			
			display = null;
			image = clothingFemaleMap.get(upperApparelFemale.ordinal());
			height = upperApparelFemale == UpperApparelFemaleType.NONE ? defaultHeight : image.getHeight();
			width = upperApparelFemale == UpperApparelFemaleType.NONE ? defaultWidth : image.getWidth();
			
			saveClothingOptions(R.string.data_ApparelTop, upperApparelFemale.ordinal());
		}

		x = ov.getWidth()/2 - width/2;
		y = (int) (ov.getHeight()/2.7 - height/2);
		
		if(image != null){
			display = new Rect(x, y, x + width, y + height);
			ov.updateRegion(SpriteUpperApparel.class,new Region(x,y,x+width,y+height ));
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
	public void genderUpdate(Gender gender) {
		this.gender = gender;
		//reset the clothing
		if(gender == Gender.MALE){
			upperApparelMale = UpperApparelMaleType.TEESHIRT_SHORTSLEEVE;
			saveClothingOptions(R.string.data_ApparelBottom, upperApparelMale.ordinal());
		}else{
			upperApparelFemale =UpperApparelFemaleType.TEESHIRT_SHORTSLEEVE;
			saveClothingOptions(R.string.data_ApparelBottom, upperApparelFemale.ordinal());
		}
	}
	
	@Override
	public void displayScaleUpdate(double scale,int x,int y) {
		this.scale=scale;
		this.personSpriteX=x;
		this.personSpriteY=y;

		//Male
		clothingMaleMap.put(UpperApparelMaleType.NONE.ordinal(), null);
		image = BitmapFactory.decodeResource(context.getResources(),R.drawable.tank_top_man);
		clothingMaleMap.put(UpperApparelMaleType.TANK_TOP.ordinal(), Bitmap.createScaledBitmap(image, (int)(image.getWidth()*scale), (int)(image.getHeight()*scale), false));
		image = BitmapFactory.decodeResource(context.getResources(),R.drawable.teeshirt_shortsleeve_man);
		clothingMaleMap.put(UpperApparelMaleType.TEESHIRT_SHORTSLEEVE.ordinal(),  Bitmap.createScaledBitmap(image, (int)(image.getWidth()*scale), (int)(image.getHeight()*scale), false));
		image = BitmapFactory.decodeResource(context.getResources(),R.drawable.teeshirt_longsleeve_man);
		clothingMaleMap.put(UpperApparelMaleType.TEESHIRT_LONGSLEEVE.ordinal(),  Bitmap.createScaledBitmap(image, (int)(image.getWidth()*scale), (int)(image.getHeight()*scale), false));

		//Female
		clothingFemaleMap.put(UpperApparelFemaleType.NONE.ordinal(), null);
		image = BitmapFactory.decodeResource(context.getResources(),R.drawable.bikini_top);
		clothingFemaleMap.put(UpperApparelFemaleType.BIKINI_TOP.ordinal(), Bitmap.createScaledBitmap(image, (int)(image.getWidth()*scale), (int)(image.getHeight()*scale), false));
		image = BitmapFactory.decodeResource(context.getResources(),R.drawable.sports_bra);
		clothingFemaleMap.put(UpperApparelFemaleType.SPORTS_BRA.ordinal(), Bitmap.createScaledBitmap(image, (int)(image.getWidth()*scale), (int)(image.getHeight()*scale), false));
		image = BitmapFactory.decodeResource(context.getResources(),R.drawable.tank_top_woman);
		clothingFemaleMap.put(UpperApparelFemaleType.TANK_TOP.ordinal(), Bitmap.createScaledBitmap(image, (int)(image.getWidth()*scale), (int)(image.getHeight()*scale), false));
		image = BitmapFactory.decodeResource(context.getResources(),R.drawable.teeshirt_shortsleeve_woman);
		clothingFemaleMap.put(UpperApparelFemaleType.TEESHIRT_SHORTSLEEVE.ordinal(),  Bitmap.createScaledBitmap(image, (int)(image.getWidth()*scale), (int)(image.getHeight()*scale), false));
		image = BitmapFactory.decodeResource(context.getResources(),R.drawable.teeshirt_longsleeve_woman);
		clothingFemaleMap.put(UpperApparelFemaleType.TEESHIRT_LONGSLEEVE.ordinal(),  Bitmap.createScaledBitmap(image, (int)(image.getWidth()*scale), (int)(image.getHeight()*scale), false));

		image=null;
		//Load saved apparel
		SharedPreferences sharedPref = context.getSharedPreferences(Globals.TAG,Context.MODE_PRIVATE);
		switch(gender){
		case MALE:
			int value = sharedPref.getInt(context.getString(R.string.data_ApparelTop), UpperApparelMaleType.NONE.ordinal());
			upperApparelMale = UpperApparelMaleType.values()[value];
			image = clothingMaleMap.get(value);
			height = upperApparelMale == UpperApparelMaleType.NONE ? defaultHeight : image.getHeight();
			width = upperApparelMale == UpperApparelMaleType.NONE ? defaultWidth : image.getWidth();
			break;
		case FEMALE:
			value = sharedPref.getInt(context.getString(R.string.data_ApparelTop), UpperApparelFemaleType.NONE.ordinal());
			upperApparelFemale = UpperApparelFemaleType.values()[value];
			image = clothingFemaleMap.get(value);
			height = upperApparelFemale == UpperApparelFemaleType.NONE ? defaultHeight : image.getHeight();
			width = upperApparelFemale == UpperApparelFemaleType.NONE ? defaultWidth : image.getWidth();
		}

		this.x = ov.getWidth()/2 - width/2;
		this.y = (int) (ov.getHeight()/2.7 - height/2);

		if(image != null){
			display = new Rect(this.x, this.y, this.x + width, this.y + height);
			ov.updateRegion(SpriteUpperApparel.class,new Region(this.x,this.y,this.x+width,this.y+height));
		}
	}
}
