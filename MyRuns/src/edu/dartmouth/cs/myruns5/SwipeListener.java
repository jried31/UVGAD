package edu.dartmouth.cs.myruns5;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnTouchListener;

public class SwipeListener implements OnTouchListener {
	private final GestureDetector gestureDetector;
	private final ChartFragment chartFragment;
	private final CurrentUVIFragment currentUVIFragment;
	private final RecommendFragment recommendFragment;
	private FragmentManager fm;
	private FragmentTransaction ft;
	private int state;

	public SwipeListener(Context context, FragmentManager fm, CurrentUVIFragment uvi
			, ChartFragment chart, RecommendFragment recommend, int state){
		chartFragment = chart;
		currentUVIFragment = uvi;
		recommendFragment = recommend;
		this.state = state;
		gestureDetector = new GestureDetector(context, new GestureListener());
		this.fm = fm;
	}
	
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		return gestureDetector.onTouchEvent(event);
	}
	
	private final class GestureListener extends SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight();
                        } else {
                            onSwipeLeft();
                        }
                    }
                    else
                    	System.out.println("No Event triggered!");
                } else {
                    if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            onSwipeBottom();
                        } else {
                            onSwipeTop();
                        }
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }
    }

    public void onSwipeLeft() {
    	if(fm == null)
    		return;
    	ft = fm.beginTransaction();
        
    	switch(state){
    	case 0:
    		ft.replace(R.id.fragment_container, currentUVIFragment);
    		break;
    	case 1:
    		ft.replace(R.id.fragment_container, chartFragment);
    		break;
    	case 2:
    		break;
    	}
        
        ft.commit();
    }

    public void onSwipeRight() {
    	if(fm == null)
    		return;
        ft = fm.beginTransaction();
        
        switch(state){
    	case 0:
    		break;
    	case 1:
    		ft.replace(R.id.fragment_container, recommendFragment);
    		break;
    	case 2:
    		ft.replace(R.id.fragment_container, currentUVIFragment);
    		break;
    	}
        ft.commit();
    }

    public void onSwipeTop() {
    }

    public void onSwipeBottom() {
    }

}
