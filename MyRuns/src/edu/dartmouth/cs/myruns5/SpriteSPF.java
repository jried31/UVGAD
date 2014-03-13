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
import android.graphics.Rect;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import edu.dartmouth.cs.myruns5.UserBodyProfileDialog.OurView;

public class SpriteSPF implements ChooseSPFLevelCaller {
	int x, y, height, width;
	public static int[] positionToSPF = {0, 8, 15, 30, 45, 50};
	
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

	SparseArray<Bitmap>spfMap;
	OurView ov;
	int selection = 0;
	Context context;
	
	public SpriteSPF(OurView ourView) {
		ov = ourView;
		context = ourView.getContext();
		//SPF Array for images
		spfMap = new SparseArray<Bitmap>(6);
		spfMap.put(0, BitmapFactory.decodeResource(context.getResources(),R.drawable.spf_0));
		spfMap.put(1, BitmapFactory.decodeResource(context.getResources(),R.drawable.spf_8));
		spfMap.put(2, BitmapFactory.decodeResource(context.getResources(),R.drawable.spf_15));
		spfMap.put(3, BitmapFactory.decodeResource(context.getResources(),R.drawable.spf_30));
		spfMap.put(4, BitmapFactory.decodeResource(context.getResources(),R.drawable.spf_45));
		spfMap.put(5, BitmapFactory.decodeResource(context.getResources(),R.drawable.spf_50));
		
		getSPFFromSharedPreferences();
		
		//Set the dimension variable for the default option
		setDimension();
		//position of the icon 
		y=ov.getHeight() - height;
		x=ov.getWidth() - width;
	}

	public void getSPFFromSharedPreferences(){
		int value = -1;
		SharedPreferences sharedPref = context.getSharedPreferences(Globals.TAG,Context.MODE_PRIVATE);
		try{
		value = sharedPref.getInt(context.getString(R.string.data_SPF),selection);
		selection = value;
		}catch(ClassCastException e){
			e.printStackTrace();
		}
	}

	//Save the Gender preference to the Preferences Editor
	private void saveSPFToSharedPreferences(){
		if(context != null){
			SharedPreferences sharedPref = context.getSharedPreferences(Globals.TAG,Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putInt(context.getString(R.string.data_SPF),selection);
			editor.commit();
		}
	}
	
	
	@SuppressLint("DrawAllocation")
	public void onDraw(Canvas canvas) {
		setDimension();
		canvas.drawBitmap(spfMap.get(selection),x, y, null );
	}

	private void setDimension(){
		//set dimensions of the app
		Bitmap spf = spfMap.get(selection);
		width = spf.getWidth();
		height = spf.getHeight();
	}
	
	public void onTouch(){
		final Dialog dialog = new Dialog(context);
	    GridView gridView = (GridView)dialog.getLayoutInflater().inflate(R.layout.choose_spf_level, null);
        dialog.setTitle(R.string.uvg_spf_level_hint);
        dialog.setCancelable(true);

	    final List<String> iconValues = SPFLevelIconUtils.getAllIconValues();
	    List<Integer> imageIds = new ArrayList<Integer>();
	    for (String iconValue : iconValues) {
	      imageIds.add(SPFLevelIconUtils.getIconDrawable(iconValue));
	    }

	    ChooseSPFLevelImageAdapter imageAdapter = new ChooseSPFLevelImageAdapter(context, imageIds);
	    gridView.setAdapter(imageAdapter);
	    gridView.setOnItemClickListener(new OnItemClickListener() {
	        @Override
	      public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	        	dialog.dismiss();
	        	onChooseSPFLevelDone(position);
	      }
	    });
	    
        dialog.setContentView(gridView);
        //now that the dialog is set up, it's time to show it    
        dialog.show();
	}

	  @Override
	public void onChooseSPFLevelDone(String iconValue) {
		// TODO Auto-generated method stub
		
	}
	  @Override
	public void onChooseSPFLevelDone(int position) {
		selection = position;
		saveSPFToSharedPreferences();
	}
}
