 package edu.dartmouth.cs.myruns5;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

public class ManualInputActivity extends ListActivity {
	
	// dialog IDs
	public static final int LIST_ITEM_ID_DATE = 0;
	public static final int LIST_ITEM_ID_TIME = 1;
	public static final int LIST_ITEM_ID_DURATION = 2;
	public static final int LIST_ITEM_ID_DISTANCE = 3;
	public static final int LIST_ITEM_ID_SWEATRATE = 7;
	public static final int LIST_ITEM_ID_CALORIES = 4;
	public static final int LIST_ITEM_ID_HEARTRATE = 5;
	public static final int LIST_ITEM_ID_COMMENT = 6;
	
	private MyRunsDialogFragment dialog;
	private ExerciseEntry entry;
	private ExerciseEntryHelper entryHelper;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manual_input);
		entry = new ExerciseEntry();
		}
	
	public void onSaveClicked(View view){
		// save entry to DB 
		
		// save activity type and input type
		int activityType, inputType;
		Intent intent = getIntent();
		activityType = intent.getIntExtra(MainActivity.ACTIVITY_TYPE, -1);
		inputType = intent.getIntExtra(MainActivity.INPUT_TYPE, -1);
		
		
		entry.setActivityType(activityType);
		entry.setInputType(inputType);

		// pass saved temporary entry to private ExerciseEntryHelper object
		entryHelper = new ExerciseEntryHelper(entry);
		entryHelper.insertToDB(this);
		
		// clear history data
		entry = new ExerciseEntry();
		
		// return to MainActivity
		finish();
	}
	
	public void onCancelClicked(View view){
		/* do nothing */
		finish();
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){
//		Toast.makeText(getApplicationContext(), "onListItemClick", Toast.LENGTH_SHORT).show();
		super.onListItemClick(l, v, position, id);

		int dialogId=0;
		// Figuring out what dialog to show based on the position clicked
		
		switch (position) {
		case LIST_ITEM_ID_DATE:
			dialogId = MyRunsDialogFragment.DIALOG_ID_DATE;
			break;
		case LIST_ITEM_ID_TIME:
			dialogId = MyRunsDialogFragment.DIALOG_ID_TIME;
			break;
		case LIST_ITEM_ID_DURATION:
			dialogId = MyRunsDialogFragment.DIALOG_ID_DURATION;
			break;
		case LIST_ITEM_ID_DISTANCE:
			dialogId = MyRunsDialogFragment.DIALOG_ID_DISTANCE;
			break;
		case LIST_ITEM_ID_SWEATRATE:
			dialogId = MyRunsDialogFragment.DIALOG_ID_SWEATRATE;
		case LIST_ITEM_ID_CALORIES:
			dialogId = MyRunsDialogFragment.DIALOG_ID_CALORIES;
			break;
		case LIST_ITEM_ID_HEARTRATE:
			dialogId = MyRunsDialogFragment.DIALOG_ID_HEARTRATE;
			break;
		case LIST_ITEM_ID_COMMENT:
			dialogId = MyRunsDialogFragment.DIALOG_ID_COMMENT;
			break;
		default:
			dialogId = MyRunsDialogFragment.DIALOG_ID_ERROR;
		}

		displayDialog(dialogId);
	}
	/*************** dialog listeners ***************/
	public void onDurationAlertOKClick(){
		String s = null;
		if (dialog.textEntryView.getText().length()!=0)
			s = dialog.textEntryView.getText().toString();		
		int duration;
		if(s!=null){
			duration = Integer.parseInt(s);
			entry.setDuration(duration);
		}
	}
	public void onDistanceAlertOKClick(){
		String s = null;
		if (dialog.textEntryView.getText().length()!=0)
			s = dialog.textEntryView.getText().toString();
		double distance;
		if(s!=null){
			distance = Double.parseDouble(s);
			entry.setDistance(distance);
		}
	}
	public void onCalorieAlertOKClick(){
		String s = null;
		if (dialog.textEntryView.getText().length()!=0)
			s = dialog.textEntryView.getText().toString();
		int calorie;
		if(s!=null){
			calorie = Integer.parseInt(s);
			entry.setCalorie(calorie);
		}
	}
	public void onHeartRateAlertOKClick(){
		String s = null;
		if (dialog.textEntryView.getText().length()!=0)
			s = dialog.textEntryView.getText().toString();
		int heartrate;
		if(s!=null){
			heartrate = Integer.parseInt(s);
			entry.setHeartrate(heartrate);
		}
	}
	public void onCommentAlertOKClick(){
		String s = dialog.textEntryView.getText().toString();
		if(s!=null)
			entry.setComment(s);
	}
	
	public void onDateAlertDone(){
		entry.setDateTime(dialog.now.getTime());
	}
	public void onTimeAlertDone(){
		entry.setDateTime(dialog.now.getTime());
	}
	
	public void onAlertCancelClick(){/* do nothing */}
	
	/*************** helper function ****************/
	public void displayDialog(int id) {
		dialog = MyRunsDialogFragment.newInstance(id);
		dialog.show(getFragmentManager(),
				getString(R.string.photo_picker_tag)); // mark: tag may not be trivial...
	}
}
