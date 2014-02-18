package edu.dartmouth.cs.myruns5;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import edu.dartmouth.cs.myruns5.UserBodyProfileDialog.OurView;

public class SpriteTorso {
	final int defaultHeight = 100;
	final int defaultWidth = 100;
	private int x, y, height, width;
	SpritePerson personSprite;
	Bitmap b;
	OurView ov;
	Context context;
	TorsoType torso;
	
	private enum TorsoType {
		NONE, TANK_TOP_MALE, TANK_TOP_FEMALE, TEESHIRT_LONGSLEEVE_MALE, TEESHIRT_LONGSLEEVE_FEMALE,
		TEESHIRT_SHORTSLEEVE_MALE, TEESHIRT_SHORTSLEEVE_FEMALE, SPORTS_BRA_FEMALE;
	}
	
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
	
	public SpriteTorso(OurView ourView,SpritePerson person) {
		ov = ourView;
		personSprite = person;
		context = ov.getContext();
		b = null;
		torso = TorsoType.NONE;
		height = defaultHeight;
		width = defaultWidth;
		x = ov.getWidth()/2 - width/2;
		y = ov.getHeight()/3 - height/2; // TODO: Use a member variable from SpritePerson
	}
	
	public void onDraw(Canvas canvas) {
		if (torso != TorsoType.NONE) {
			Rect dst = new Rect(x, y, x + width, y + height);
			canvas.drawBitmap(b, null, dst, null);
		}
	}

	//Method invoked to toggle torso for male/female sprite
	public void toggle() {
		switch (torso) {
			case NONE: // Switch to tank top
				b = BitmapFactory.decodeResource(context.getResources(),R.drawable.tank_top_man);
				torso = TorsoType.TANK_TOP_MALE;
				break;
			case TANK_TOP_MALE: // Switch to long sleeve
				b = BitmapFactory.decodeResource(context.getResources(),R.drawable.teeshirt_longsleeve_man);
				torso = TorsoType.TEESHIRT_LONGSLEEVE_MALE;
				break;
			case TEESHIRT_LONGSLEEVE_MALE: // Switch to short sleeve
				b = BitmapFactory.decodeResource(context.getResources(),R.drawable.teeshirt_shortsleeve_man);
				torso = TorsoType.TEESHIRT_SHORTSLEEVE_MALE;
				break;
			case TEESHIRT_SHORTSLEEVE_MALE: // Switch to short sleeve
				b = null;
			default:
				torso = TorsoType.NONE;
		}
		
		height = b == null? defaultHeight : b.getHeight();
		width = b == null? defaultWidth : b.getWidth();
		x = ov.getWidth()/2 - width/2;
		y = ov.getHeight()/3 - height/2; // TODO: Use a member variable from SpritePerson
	}
}
