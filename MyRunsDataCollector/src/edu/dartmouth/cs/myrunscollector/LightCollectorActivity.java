package edu.dartmouth.cs.myrunscollector;

import java.io.File;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class LightCollectorActivity extends Activity {

	private enum State {
		IDLE, COLLECTING, TRAINING, CLASSIFYING
	};

	private final String[] mLabels = {Globals.CLASS_LABEL_IN_DOORS,Globals.CLASS_LABEL_IN_SHADE,Globals.CLASS_LABEL_IN_SUN,Globals.CLASS_LABEL_IN_PARTIAL_CLOUD,Globals.CLASS_LABEL_IN_CLOUD,Globals.CLASS_LABEL_OTHER};

	private RadioGroup radioGroup;
	private final RadioButton[] radioBtns = new RadioButton[6];
	private Intent mServiceIntent;
	private File mFeatureFilePhone,mFeatureFileArdurino;

	private State mState;
	private Button btnDelete;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.light);
		radioGroup = (RadioGroup) findViewById(R.id.radioGroupLabels);
		radioBtns[0] = (RadioButton) findViewById(R.id.radioIndoors);
		radioBtns[1] = (RadioButton) findViewById(R.id.radioShade);
		radioBtns[2] = (RadioButton) findViewById(R.id.radioSun);
		radioBtns[3] = (RadioButton) findViewById(R.id.radioPartialCloud);
		radioBtns[4] = (RadioButton) findViewById(R.id.radioCloud);
		radioBtns[5] = (RadioButton) findViewById(R.id.radioOther);
		
		btnDelete = (Button) findViewById(R.id.btnDeleteData);

		mState = State.IDLE;
		mFeatureFilePhone = new File(getExternalFilesDir(null), Globals.FEATURE_LIGHT_FILE_NAME);
		mFeatureFileArdurino = new File(getExternalFilesDir(null), Globals.FEATURE_LIGHT_FILE_NAME_ARDUINO);
		mServiceIntent = new Intent(this, LightSensorService.class);
	}

	public void onCollectClicked(View view) {

		if (mState == State.IDLE) {
			mState = State.COLLECTING;
			((Button) view).setText(R.string.ui_collector_button_stop_title);
			btnDelete.setEnabled(false);
			radioBtns[0].setEnabled(false);
			radioBtns[1].setEnabled(false);
			radioBtns[2].setEnabled(false);
			radioBtns[3].setEnabled(false);
			radioBtns[4].setEnabled(false);
			radioBtns[5].setEnabled(false);
			int acvitivtyId = radioGroup.indexOfChild(findViewById(radioGroup.getCheckedRadioButtonId()));
			String label = mLabels[acvitivtyId];

			Bundle extras = new Bundle();
			extras.putString(Globals.CLASS_LABEL_KEY, label);
			mServiceIntent.putExtras(extras);

			startService(mServiceIntent);

		} else if (mState == State.COLLECTING) {
			mState = State.IDLE;
			((Button) view).setText(R.string.ui_collector_button_start_title);
			btnDelete.setEnabled(true);
			radioBtns[0].setEnabled(true);
			radioBtns[1].setEnabled(true);
			radioBtns[2].setEnabled(true);
			radioBtns[3].setEnabled(true);
			radioBtns[4].setEnabled(true);
			radioBtns[5].setEnabled(true);

			stopService(mServiceIntent);
			((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
					.cancel(Globals.NOTIFICATION_ID);
		}
	}

	public void onDeleteDataClicked(View view) {

		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			if (mFeatureFilePhone.exists()) {
				mFeatureFilePhone.delete();
			}
			if (mFeatureFileArdurino.exists()) {
				mFeatureFileArdurino.delete();
			}
			Toast.makeText(getApplicationContext(), R.string.ui_collector_toast_file_deleted, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onBackPressed() {

		if (mState == State.TRAINING) {
			return;
		} else if (mState == State.COLLECTING || mState == State.CLASSIFYING) {
			stopService(mServiceIntent);
			((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
					.cancel(Globals.NOTIFICATION_ID);
		}
		super.onBackPressed();
	}

	@Override
	public void onDestroy() {
		// Stop the service and the notification.
		// Need to check whether the mSensorService is null or not.
		if (mState == State.TRAINING) {
			return;
		} else if (mState == State.COLLECTING || mState == State.CLASSIFYING) {
			stopService(mServiceIntent);
			((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
					.cancel(Globals.NOTIFICATION_ID);
		}
		finish();
		super.onDestroy();
	}

}