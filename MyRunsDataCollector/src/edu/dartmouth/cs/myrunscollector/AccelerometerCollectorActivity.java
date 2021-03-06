package edu.dartmouth.cs.myrunscollector;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class AccelerometerCollectorActivity extends Activity {

	private enum State {
		IDLE, COLLECTING, TRAINING, CLASSIFYING
	};

	private final String[] mLabels = { 
			Globals.CLASS_LABEL_STANDING,Globals.CLASS_LABEL_WALKING,
			Globals.CLASS_LABEL_JOGGING,
			Globals.CLASS_LABEL_SPRINTING,Globals.CLASS_LABEL_OTHER};

	private RadioGroup radioGroup;
	private final RadioButton[] radioBtns = new RadioButton[5];
	private Intent mServiceIntent;
	private File mFeatureFile;

	private State mState;
	private Button btnDelete;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.accelerometer);
		
		radioGroup = (RadioGroup) findViewById(R.id.radioGroupLabels);
		radioBtns[0] = (RadioButton) findViewById(R.id.radioStanding);
		radioBtns[1] = (RadioButton) findViewById(R.id.radioWalking);
		radioBtns[2] = (RadioButton) findViewById(R.id.radioJogging);
		radioBtns[3] = (RadioButton) findViewById(R.id.radioSprinting);
		radioBtns[4] = (RadioButton) findViewById(R.id.radioOther);
		
		btnDelete = (Button) findViewById(R.id.btnDeleteData);

		mState = State.IDLE;
		mFeatureFile = new File(getExternalFilesDir(null), Globals.FEATURE_MOTION_FILE_NAME);
		mServiceIntent = new Intent(this, AccelerometerSensorService.class);
	}

	public void onStartClicked(View view) {

		if (mState == State.IDLE) {
			mState = State.COLLECTING;
			//((Button) view).setText(R.string.ui_collector_button_stop_title);
			((Button) view).setEnabled(false);//((Button) view).setText(R.string.ui_collector_button_start_title);
			
			Button stopButton = (Button) findViewById(R.id.btnStop);
			stopButton.setEnabled(true);
			
			btnDelete.setEnabled(false);
			radioBtns[0].setEnabled(false);
			radioBtns[1].setEnabled(false);
			radioBtns[2].setEnabled(false);
			radioBtns[3].setEnabled(false);
			radioBtns[4].setEnabled(false);
			
			int acvitivtyId = radioGroup.indexOfChild(findViewById(radioGroup.getCheckedRadioButtonId()));
			String label = mLabels[acvitivtyId];

			Bundle extras = new Bundle();
			extras.putString(Globals.CLASS_LABEL_KEY, label);
			mServiceIntent.putExtras(extras);

			startService(mServiceIntent);
		} 
	}

	public void onStopClicked(View view)
	{
		if (mState == State.COLLECTING) {
			mState = State.IDLE;
			((Button) view).setEnabled(false);//((Button) view).setText(R.string.ui_collector_button_start_title);
			
			Button startButton = (Button) findViewById(R.id.btnStart);
			startButton.setEnabled(true);
			
			btnDelete.setEnabled(true);
			radioBtns[0].setEnabled(true);
			radioBtns[1].setEnabled(true);
			radioBtns[2].setEnabled(true);
			radioBtns[3].setEnabled(true);
			radioBtns[4].setEnabled(true);

			stopService(mServiceIntent);
			((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(Globals.NOTIFICATION_ID);
		}
	}
	
	public void onDeleteDataClicked(View view) {

		new AlertDialog.Builder(this)
        	.setIcon(android.R.drawable.ic_dialog_alert)
        	.setTitle("Delete ARFF File")
        	.setMessage("Are you sure you want to delete the ARFF file?")
        	.setPositiveButton("Yes", 
        			new DialogInterface.OnClickListener()
				    {
				        public void onClick(DialogInterface dialog, int which) {
				    		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
				    			if (mFeatureFile.exists()) {
				    				mFeatureFile.delete();
				    			}
		
				    			Toast.makeText(getApplicationContext(),R.string.ui_collector_toast_file_deleted, Toast.LENGTH_SHORT).show();
				    		} 
				        }
				    }
        	)
		    .setNegativeButton("No", null)
		    .show();
	}

	@Override
	public void onBackPressed() {

		if (mState == State.TRAINING) {
			return;
		} else if (mState == State.COLLECTING || mState == State.CLASSIFYING) {
			stopService(mServiceIntent);
			((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(Globals.NOTIFICATION_ID);
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
			((NotificationManager) getSystemService(NOTIFICATION_SERVICE)) .cancel(Globals.NOTIFICATION_ID);
		}
		finish();
		super.onDestroy();
	}

}