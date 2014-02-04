package edu.dartmouth.cs.myrunscollector;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class CollectorActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}

	public void onLightClicked(View view) {

		Intent lightActivity = new Intent(this, LightCollectorActivity.class);
		startActivity(lightActivity);
	}

	public void onAccelerometerClicked(View view) {

		Intent accelActivity = new Intent(this, AccelerometerCollectorActivity.class);
		startActivity(accelActivity);
	}


	@Override
	public void onDestroy() {
		
		finish();
		super.onDestroy();
	}

}