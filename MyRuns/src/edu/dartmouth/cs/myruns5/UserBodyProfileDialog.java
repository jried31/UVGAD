package edu.dartmouth.cs.myruns5;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Region;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;

public class UserBodyProfileDialog extends Activity implements OnTouchListener{
	OurView v;//Class we defined
	Bitmap arrowBitmap;
	float x,y;
	SpritePerson personSprite;
	SpriteGender genderSprite;
	SpriteSPF spfSprite;
	SpriteSkinType skinTypeSprite;
	SpriteHeadApparel headApparelSprite;
	SpriteUpperApparel upperApparelSprite;
	SpriteLowerApparel lowerApparelSprite;
	Region genderRegion=null, spfRegion=null, skinTypeRegion=null, headApparelRegion=null, upperApparelRegion=null, lowerApparelRegion=null;
	int sexOption = 0;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		v = new OurView(this);
		v.setOnTouchListener(this);
		arrowBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.arrow);
		x = y = 0;
		setContentView(v);
	}

	@Override
	protected void onPause(){
		super.onPause();
		v.pause();
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		v.resume();
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		try{
			Thread.sleep(50);
		}catch(InterruptedException e){
			e.printStackTrace();
		}
		x = event.getX();
		y = event.getY();
		
		switch(event.getAction()){
			case MotionEvent.ACTION_DOWN:
				//Click was in the Gender field
				if(genderRegion != null && genderRegion.contains((int)x,(int) y)){
					genderSprite.toggle();
				}else if(spfRegion != null && spfRegion.contains((int)x,(int) y)){
					spfSprite.onTouch();
				}else if(skinTypeRegion != null && skinTypeRegion.contains((int)x,(int) y)){
					skinTypeSprite.onTouch();
				}else if (headApparelRegion != null && headApparelRegion.contains((int)x, (int)y)){
					headApparelSprite.toggle();
				}else if (upperApparelRegion != null && upperApparelRegion.contains((int)x, (int)y)){
					upperApparelSprite.toggle();
				}else if (lowerApparelRegion != null && lowerApparelRegion.contains((int)x, (int)y)){
					lowerApparelSprite.toggle();
				}
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_MOVE:
		
				break;
		}
		return true;
	}

	@SuppressLint("WrongCall")
	public class OurView extends SurfaceView implements IRegionListener, Runnable{
		Thread t = null;
		SurfaceHolder holder;
		boolean isItOk = false;
		
		public OurView(Context context){
			super (context);
			holder = getHolder();
		}
		
		@Override
		public void updateRegion(Class type,Region region) {
			if(type == SpriteHeadApparel.class){
				headApparelRegion = region;
			}else if(type == SpriteUpperApparel.class){
				upperApparelRegion = region;
			}else if(type == SpriteLowerApparel.class){
				lowerApparelRegion = region;
			}else if(type == SpriteSPF.class){
				spfRegion = region;
			}else if(type == SpriteSkinType.class){
				skinTypeRegion = region;
			}else if(type == SpriteGender.class){
				genderRegion = region;
			}
		}
		
		public void run(){
			personSprite = new SpritePerson(this);
			
			genderSprite = new SpriteGender(this,personSprite);
			
			spfSprite = new SpriteSPF(this);
			skinTypeSprite = new SpriteSkinType(this);
			
			headApparelSprite = new SpriteHeadApparel(this);
			personSprite.registerGenderCallBack(headApparelSprite);
			personSprite.registerScaleCallBack(headApparelSprite);
			
			upperApparelSprite = new SpriteUpperApparel(this);
			personSprite.registerGenderCallBack(upperApparelSprite);
			personSprite.registerScaleCallBack(upperApparelSprite);
			
			lowerApparelSprite = new SpriteLowerApparel(this);
			personSprite.registerGenderCallBack(lowerApparelSprite);
			personSprite.registerScaleCallBack(lowerApparelSprite);

			
			while(isItOk){//If the surface (holder) view is NOT valid don't render
				if(!holder.getSurface().isValid()){
					continue;
				}
				
				Canvas c = holder.lockCanvas();//lock canvas 1st then draw
				onDraw(c);
				holder.unlockCanvasAndPost(c);
			}
		}
		
		protected void onDraw(Canvas canvas){
			canvas.drawARGB(255,150,150,10);//Draw background 1st
			personSprite.onDraw(canvas);//Handler for drawing human
			genderSprite.onDraw(canvas);//Draw the gender canvas
			skinTypeSprite.onDraw(canvas);
			spfSprite.onDraw(canvas);
			
			// Update clothing toggling
			lowerApparelSprite.onDraw(canvas);
			upperApparelSprite.onDraw(canvas);
			headApparelSprite.onDraw(canvas);
			
			canvas.drawBitmap(arrowBitmap,x - arrowBitmap.getWidth()/2,y - arrowBitmap.getHeight()/2, null);//draws ball
		}
		
		//when paused end the thread and force garbage collection (if activity pause)
		public void pause(){
			this.isItOk = false;
			while(true){
				try{
					t.join();
				}catch(InterruptedException e){
					e.printStackTrace();
				}
				break;
			}
			t = null;
		}
		
		//resumes the thread back
		public void resume(){
			this.isItOk = true;
			t = new Thread(this);
			t.start();
		}
	}
}
