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
	Region genderRegion, spfRegion, skinTypeRegion, headApparelRegion, upperApparelRegion, lowerApparelRegion;
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
				if(genderRegion.contains((int)x,(int) y)){
					genderSprite.toggle();
					headApparelSprite.reset();
					upperApparelSprite.reset();
					lowerApparelSprite.reset();
					break;
				}
				
				if(spfRegion.contains((int)x,(int) y)){
					spfSprite.onTouch();
					break;
				}
				
				if(skinTypeRegion.contains((int)x,(int) y)){
					skinTypeSprite.onTouch();
					break;
				}
				if (headApparelRegion.contains((int)x, (int)y)) {
					headApparelSprite.toggle();
					break;
				}
				if (upperApparelRegion.contains((int)x, (int)y)) {
					upperApparelSprite.toggle();
					break;
				}
				if (lowerApparelRegion.contains((int)x, (int)y)) {
					lowerApparelSprite.toggle();
					break;
				}
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_MOVE:
		
				break;
		}
		return true;
	}

	@SuppressLint("WrongCall")
	public class OurView extends SurfaceView implements Runnable{
		Thread t = null;
		SurfaceHolder holder;
		boolean isItOk = false;
		
		public OurView(Context context){
			super (context);
			holder = getHolder();
		}
		
		public void run(){
			personSprite = new SpritePerson(this);
			genderSprite = new SpriteGender(this,personSprite);
			genderRegion = new Region(genderSprite.getX(),genderSprite.getY(), genderSprite.getX() + genderSprite.getWidth(),genderSprite.getY() + genderSprite.getHeight());

			spfSprite = new SpriteSPF(this);
			spfRegion = new Region(spfSprite.getX(),spfSprite.getY(), spfSprite.getX() + spfSprite.getWidth(),spfSprite.getY() + spfSprite.getHeight());
			
			skinTypeSprite = new SpriteSkinType(this);
			skinTypeRegion = new Region(skinTypeSprite.getX(),skinTypeSprite.getY(), skinTypeSprite.getX() + skinTypeSprite.getWidth(),skinTypeSprite.getY() + skinTypeSprite.getHeight());
			
			headApparelSprite = new SpriteHeadApparel(this, personSprite);
			headApparelRegion = new Region(headApparelSprite.getX(), headApparelSprite.getY(), 
					headApparelSprite.getX() + headApparelSprite.getWidth(), headApparelSprite.getY() + headApparelSprite.getHeight());
			
			upperApparelSprite = new SpriteUpperApparel(this, personSprite);
			upperApparelRegion = new Region(upperApparelSprite.getX(), upperApparelSprite.getY(), 
					upperApparelSprite.getX() + upperApparelSprite.getWidth(), upperApparelSprite.getY() + upperApparelSprite.getHeight());
			
			lowerApparelSprite = new SpriteLowerApparel(this, personSprite);
			lowerApparelRegion = new Region(lowerApparelSprite.getX(), lowerApparelSprite.getY(), 
					lowerApparelSprite.getX() + lowerApparelSprite.getWidth(), lowerApparelSprite.getY() + lowerApparelSprite.getHeight());
			
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
			lowerApparelRegion.set(lowerApparelSprite.getX(), lowerApparelSprite.getY(), 
					lowerApparelSprite.getX() + lowerApparelSprite.getWidth(), lowerApparelSprite.getY() + lowerApparelSprite.getHeight());
			upperApparelSprite.onDraw(canvas);
			upperApparelRegion.set(upperApparelSprite.getX(), upperApparelSprite.getY(), 
					upperApparelSprite.getX() + upperApparelSprite.getWidth(), upperApparelSprite.getY() + upperApparelSprite.getHeight());
			headApparelSprite.onDraw(canvas);
			headApparelRegion.set(headApparelSprite.getX(), headApparelSprite.getY(), 
					headApparelSprite.getX() + headApparelSprite.getWidth(), headApparelSprite.getY() + headApparelSprite.getHeight());
			
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
