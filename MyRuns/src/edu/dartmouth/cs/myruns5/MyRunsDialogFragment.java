package edu.dartmouth.cs.myruns5;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;



public class MyRunsDialogFragment extends DialogFragment {
		public static final int DIALOG_ID_ERROR = -1;
		public static final int DIALOG_ID_PHOTO_PICKER = 101;
		public static final int DIALOG_ID_DATE = 102;
		public static final int DIALOG_ID_TIME = 103;
		public static final int DIALOG_ID_DURATION = 104;
		public static final int DIALOG_ID_DISTANCE = 105;
		public static final int DIALOG_ID_CALORIES = 106;
		public static final int DIALOG_ID_HEARTRATE = 107;
		public static final int DIALOG_ID_COMMENT = 108;
		public static final int DIALOG_ID_BIRTHDAY = 109;

		public static final int ID_PHOTO_PICKER_FROM_CAMERA = 0;
		public static final int ID_PHOTO_PICKER_FROM_GALLERY = 1;

		public static final int DIALOG_ID_SWEATRATE = 110;
		private static final String DIALOG_ID_KEY = "dialog id";
		
		public EditText textEntryView;
		public Calendar now;
		public int hour, minute, year, month, day;

		public static MyRunsDialogFragment newInstance(int dialog_id) {
			MyRunsDialogFragment frag = new MyRunsDialogFragment();
			Bundle args = new Bundle();
			args.putInt(DIALOG_ID_KEY, dialog_id);
			frag.setArguments(args);
			return frag;
		}


		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			int dialog_id = getArguments().getInt(DIALOG_ID_KEY);
			final Activity parent = getActivity();
			AlertDialog.Builder builder = new AlertDialog.Builder(parent);
			DialogInterface.OnClickListener dlistener;
			textEntryView = new EditText(getActivity());
			
			switch (dialog_id) {
			case DIALOG_ID_PHOTO_PICKER:
				builder.setTitle(R.string.photo_picker_title);
				dlistener = new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						((ProfileActivity) parent).onPhotoPickerItemSelected(item);					
						}
				};
				builder.setItems(R.array.photo_picker_items, dlistener);
				return builder.create();
				
			case DIALOG_ID_DATE:
				now = Calendar.getInstance();
				DatePickerDialog dateDialog = new DatePickerDialog(getActivity(),
						new DatePickerDialog.OnDateSetListener() {
							public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
									now.set(Calendar.YEAR, year);
									now.set(Calendar.MONTH, month);
									now.set(Calendar.DAY_OF_MONTH, dayOfMonth);
									((ManualInputActivity)parent).onDateAlertDone();
							}
						},
						now.get(Calendar.YEAR),
						now.get(Calendar.MONTH),
						now.get(Calendar.DAY_OF_MONTH)
						);
				return dateDialog;
				
			case DIALOG_ID_BIRTHDAY:
				now = Calendar.getInstance();
				DatePickerDialog birthdayDialog = new DatePickerDialog(getActivity(),
						new DatePickerDialog.OnDateSetListener() {
							public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
									now.set(Calendar.YEAR, year);
									now.set(Calendar.MONTH, month);
									now.set(Calendar.DAY_OF_MONTH, dayOfMonth);
									((ProfileActivity)parent).onDateAlertDone(now);
							}
						},
						now.get(Calendar.YEAR),
						now.get(Calendar.MONTH),
						now.get(Calendar.DAY_OF_MONTH)
						);
				return birthdayDialog;
			case DIALOG_ID_TIME:
				now = Calendar.getInstance();
				TimePickerDialog timeDialog = new TimePickerDialog(getActivity(),
						new TimePickerDialog.OnTimeSetListener() {
							public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
									now.set(Calendar.HOUR_OF_DAY, hourOfDay);
									now.set(Calendar.MINUTE, minute);
									((ManualInputActivity)parent).onTimeAlertDone();
							}
						},
						now.get(Calendar.HOUR_OF_DAY),
						now.get(Calendar.MINUTE),
						false	// boolean: is24HourView
						);
				return timeDialog;
			case DIALOG_ID_DURATION:
				builder.setTitle(R.string.title_dialog_duration);
				textEntryView.setInputType(InputType.TYPE_CLASS_NUMBER);
				builder.setView(textEntryView);
				builder.setPositiveButton(R.string.title_button_alert_ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								((ManualInputActivity) parent)
										.onDurationAlertOKClick();
							}
						});
				builder.setNegativeButton(R.string.title_button_alert_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								((ManualInputActivity) parent)
										.onAlertCancelClick();
							}
						});
				return builder.create();
			case DIALOG_ID_DISTANCE:
				builder.setTitle(R.string.title_dialog_distance);
				textEntryView.setInputType(InputType.TYPE_CLASS_NUMBER);
				builder.setView(textEntryView);
				builder.setPositiveButton(R.string.title_button_alert_ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								((ManualInputActivity) parent)
										.onDistanceAlertOKClick();
							}
						});
				builder.setNegativeButton(R.string.title_button_alert_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								((ManualInputActivity) parent)
										.onAlertCancelClick();
							}
						});
				return builder.create();
			case DIALOG_ID_CALORIES:
				builder.setTitle(R.string.title_dialog_calories);
				textEntryView.setInputType(InputType.TYPE_CLASS_NUMBER);
				builder.setView(textEntryView);
				builder.setPositiveButton(R.string.title_button_alert_ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								((ManualInputActivity) parent)
										.onCalorieAlertOKClick();
							}
						});
				builder.setNegativeButton(R.string.title_button_alert_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								((ManualInputActivity) parent)
										.onAlertCancelClick();
							}
						});
				return builder.create();
			case DIALOG_ID_HEARTRATE:
				builder.setTitle(R.string.title_dialog_heart_rate);
				textEntryView.setInputType(InputType.TYPE_CLASS_NUMBER);
				builder.setView(textEntryView);
				builder.setPositiveButton(R.string.title_button_alert_ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								((ManualInputActivity) parent)
										.onHeartRateAlertOKClick();
							}
						});
				builder.setNegativeButton(R.string.title_button_alert_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								((ManualInputActivity) parent)
										.onAlertCancelClick();
							}
						});
				return builder.create();
			case DIALOG_ID_COMMENT:
				builder.setTitle(R.string.title_dialog_comment);
				textEntryView.setLines(3);
				textEntryView.setHint(R.string.dialog_comment_hint);
				builder.setView(textEntryView);
				builder.setPositiveButton(R.string.title_button_alert_ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								((ManualInputActivity) parent)
										.onCommentAlertOKClick();
							}
						});
				builder.setNegativeButton(R.string.title_button_alert_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								((ManualInputActivity) parent)
										.onAlertCancelClick();
							}
						});
				return builder.create();
			default:
				return null;
			}
		}
}
