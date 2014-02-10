package edu.dartmouth.cs.myruns5;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import edu.dartmouth.cs.myruns5.UserBodyProfileDialog.OurView;

public class SpriteSkinType implements ChooseSkinTypeCaller {
	int x,y,
		height, width;
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

	SparseArray<Bitmap>skinToneMap;
	OurView ov;
	int selection = 0;
	Context context;
	
	public SpriteSkinType(OurView ourView) {
		ov = ourView;
		context = ourView.getContext();
		//Skin type Array for images
		skinToneMap = new SparseArray<Bitmap>(6);
		skinToneMap.put(0, BitmapFactory.decodeResource(context.getResources(),R.drawable.fitzpatrick_type_1));
		skinToneMap.put(1, BitmapFactory.decodeResource(context.getResources(),R.drawable.fitzpatrick_type_2));
		skinToneMap.put(2, BitmapFactory.decodeResource(context.getResources(),R.drawable.fitzpatrick_type_3));
		skinToneMap.put(3, BitmapFactory.decodeResource(context.getResources(),R.drawable.fitzpatrick_type_4));
		skinToneMap.put(4, BitmapFactory.decodeResource(context.getResources(),R.drawable.fitzpatrick_type_5));
		skinToneMap.put(5, BitmapFactory.decodeResource(context.getResources(),R.drawable.fitzpatrick_type_6));
		getSkinTypeFromSharedPreferences();//Grab the Gender of persion
		
		//Set the dimension variable for the default option
		setDimension();
		//position of the icon 
		y=6;
		x=ov.getWidth() - width;
	}

	public void getSkinTypeFromSharedPreferences(){
		int value = -1;
		SharedPreferences sharedPref = context.getSharedPreferences(Globals.TAG,Context.MODE_PRIVATE);
		try{
		value = sharedPref.getInt(context.getString(R.string.data_SkinTone),selection);
		selection = value;
		}catch(ClassCastException e){
			e.printStackTrace();
		}
	}

	//Save the Gender preference to the Preferences Editor
	private void saveSkinTypeToSharedPreferences(){
		if(context != null){
			SharedPreferences sharedPref = context.getSharedPreferences(Globals.TAG,Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putInt(context.getString(R.string.data_SkinTone),selection);
			editor.commit();
		}
	}
	
	
	@SuppressLint("DrawAllocation")
	public void onDraw(Canvas canvas) {
		setDimension();
		canvas.drawBitmap(skinToneMap.get(selection),x, y, null );
	}

	private void setDimension(){
		//set dimensions of the app
		Bitmap skintone = skinToneMap.get(selection);
		width = skintone.getWidth();
		height = skintone.getHeight();
	}
	
	public void onTouch(){
		final Dialog dialog = new Dialog(context);
	    GridView gridView = (GridView)dialog.getLayoutInflater().inflate(R.layout.choose_skin_type, null);
        dialog.setTitle(R.string.uvg_skin_type_hint);
        dialog.setCancelable(true);

	    final List<String> iconValues = SkinTypeIconUtils.getAllIconValues();
	    List<Integer> imageIds = new ArrayList<Integer>();
	    for (String iconValue : iconValues) {
	      imageIds.add(SkinTypeIconUtils.getIconDrawable(iconValue));
	    }

	    ChooseSkinTypeImageAdapter imageAdapter = new ChooseSkinTypeImageAdapter(context, imageIds);
	    gridView.setAdapter(imageAdapter);
	    gridView.setOnItemClickListener(new OnItemClickListener() {
	        @Override
	      public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	        	dialog.dismiss();
	        	onChooseSkinTypeDone(position);
	      }
	    });
	    
        dialog.setContentView(gridView);
        //now that the dialog is set up, it's time to show it    
        dialog.show();
	}

	  @Override
	public void onChooseSkinTypeDone(String iconValue) {
		// TODO Auto-generated method stub
		
	}
	  @Override
	public void onChooseSkinTypeDone(int position) {
		selection = position;
		saveSkinTypeToSharedPreferences();
	}
}
