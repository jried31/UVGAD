package edu.dartmouth.cs.myruns5;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.widget.ToggleButton;
import edu.dartmouth.cs.myruns5.UserBodyProfileDialog.OurView;

public class CopyOfSprite {
	int x,y,
		xSpeed,ySpeed,
		height, width;
	Bitmap b;

	OurView ov;
	int currentFrame=0, direction = 0;
	ToggleButton maleFemaleToggle;
	
	public CopyOfSprite(OurView ourView, Bitmap blob) {
		b = blob;
		ov = ourView;
		height = b.getHeight();
		width = b.getWidth()/2;//Split male/female
		x = y = 0;
		xSpeed = 5;ySpeed = 0;//going to move to right
		maleFemaleToggle = new ToggleButton(ourView.getContext());
		
	}

	public void onDraw(Canvas canvas) {
		update();
		
		int srcX = direction * width;//if facing up direction 0 x height;
		
		Rect src = new Rect(srcX,0,srcX + width,height);//Defines what we will cut out of the sprite sheet 
		Rect dst = new Rect(x,y,x + width, y + height);//Allows you to scale it such that you can say you can draw it at position x,y and scale it according to other 2 params 
		canvas.drawBitmap(b,src,dst, null);
	}

	//Method invoked to toggle the Male/Female icon
	public void toggle(){
		direction = direction == 0 ? 1:0;
	}
	
	private void update(){
		
		//note: Left side male, Right side female
		//Facing down 
		//if image hits the right side (ie: horizontal)
		if(x > ov.getWidth() - width - xSpeed){
			xSpeed = 0;ySpeed = 5;//Move it downwards
			direction = 0;//male
		}
		//Going left
		if(y > ov.getHeight() - height - ySpeed){
			xSpeed = -5;ySpeed = 0;//Move it upwards

			direction = 1;//female
		}
		
		//Going up screen
		if(x + xSpeed < 0){
			x = 0;//shoot it to left
			xSpeed = 0;ySpeed = -5;
			direction = 0;//male
		}
		//Facing right
		if(y + ySpeed < 0){//shoot it back to the right
			y = 0;
			xSpeed = 5;ySpeed = 0;
			direction = 1;//female
		}
		
		x+=xSpeed;
		y+=ySpeed;
	}
}
