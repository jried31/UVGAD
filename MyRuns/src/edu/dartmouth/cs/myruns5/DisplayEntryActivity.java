package edu.dartmouth.cs.myruns5;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

//Display the details of a "manual" entry.
//All data are passed from the launching activity. Another way
//of doing it is only passing the entry id, and query the database in this activity.
//More work, but making the activity more self-contained.


public class DisplayEntryActivity extends Activity {
	
	private static final int MENU_ID_DELETE = 0;

	EditText text1;
	EditText text2;
	EditText text3;
	EditText text4;
	EditText text5;
	EditText text6;
	EditText text7;
	
	
	private Context mContext;

	//skeleton
	//Display all the columns in the saved entry once the activity is created
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		mContext = this;
		Log.d(null, "DisplayEntry: onCreate");

		//inflate the entry layout. 
		setContentView(R.layout.activity_display_entry);
		
		text1 = (EditText) findViewById(R.id.edit_display_activity_type);
		text2 = (EditText) findViewById(R.id.edit_display_date_time);
		text3 = (EditText) findViewById(R.id.edit_display_duration);
		text4 = (EditText) findViewById(R.id.edit_display_distance);
		text5 = (EditText) findViewById(R.id.edit_display_calorie);
		text6 = (EditText) findViewById(R.id.edit_display_heart_rate);
	 	text7 = (EditText) findViewById(R.id.edit_display_comment);

		Intent intent = getIntent();
		Log.d(null, "DisplayEntry: 1");

		//display all the columns from the exercise entry to the list of TextView.
		String activityType = intent.getStringExtra(HistoryFragment.ACTIVITY_TYPE);
		String date_time = intent.getStringExtra(HistoryFragment.DATE_TIME);
		String duration = intent.getStringExtra(HistoryFragment.DURATION);
		String distance = intent.getStringExtra(HistoryFragment.DISTANCE);
		String sweatrate = intent.getStringExtra(HistoryFragment.SWEATRATE);
		String calorie = intent.getStringExtra(HistoryFragment.CALORIE);
		String heartrate = intent.getStringExtra(HistoryFragment.HEARTRATE);
		String comment = intent.getStringExtra(HistoryFragment.COMMENT);
		Log.d(null, "DisplayEntry: 2");

		text1.setText(activityType);
		text2.setText(date_time);
		text3.setText(duration);
		text4.setText(distance);
		text5.setText(calorie);
		text6.setText(heartrate);
		text7.setText(comment);

	}

	
	//Create the option menu to delete the current saved exercise entry.
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		super.onCreateOptionsMenu(menu);
		MenuItem menuitem;
		menuitem = menu.add(Menu.NONE, MENU_ID_DELETE, MENU_ID_DELETE, "Delete");
		menuitem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
//		menuitem = menu.add(Menu.NONE, MENU_ID_SAVE, MENU_ID_SAVE, "Save");
//		menuitem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		return true;
	}

	//skeleton
	//When you clicked "delete" button, 
	//you need to called the deleteEntryInDB to delete this entry in the database and quit the activity.
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ID_DELETE:
			ExerciseEntryHelper.deleteEntryInDB(mContext, getIntent().getIntExtra(HistoryFragment.ROW_ID, -1));
			
		default:
			finish();
			return false;
		}
	}
}

