package edu.dartmouth.cs.myruns5;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;

public class ProfileActivity extends Activity {
	
	private Uri mImageCaptureUri;
	private ImageView mImageView;
	private boolean isTakenFromCamera;
	
	public static final int REQUEST_CODE_TAKE_FROM_CAMERA = 100;
	public static final int REQUEST_CODE_CROP_PHOTO = 101;
	public static final int REQUEST_CODE_SELECT_FROM_GALLERY = 102;

	private static final String IMAGE_UNSPECIFIED = "image/*";
	private static final String URI_INSTANCE_STATE_KEY = "saved_uri";
	
	private String mBirthMonth;
	private String mBirthDay;
	private String mBirthYear;
	private String mBirthday;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);
		
		// build an AutoCompleteTextView instead of EditText for user
		AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.EditMajor);
		String[] majors = getResources().getStringArray(R.array.MajorHints);
		ArrayAdapter<String> adapter = 
		        new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, majors);
		textView.setAdapter(adapter);
		
		loadProfile();
		
		mImageView = (ImageView) findViewById(R.id.avatar);

		if (savedInstanceState != null) {
			mImageCaptureUri = savedInstanceState
					.getParcelable(URI_INSTANCE_STATE_KEY);
		}

		loadSnap();
		
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// Save the image capture uri before the activity goes into background
		outState.putParcelable(URI_INSTANCE_STATE_KEY, mImageCaptureUri);
	}

	public void onChangeClicked(View view){
		displayDialog(MyRunsDialogFragment.DIALOG_ID_PHOTO_PICKER);
	}
	
	
	public void onSaveClicked(View view){
		saveProfile();
		saveSnap();

        finish();
	}
	
	public void onCancelClicked(View view){
        finish();
	}
	
	private void saveProfile(){
		SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		EditText name = (EditText)findViewById(R.id.EditTextName);
		EditText phone = (EditText)findViewById(R.id.EditTextPhone);
		EditText email = (EditText)findViewById(R.id.EditTextEmail);
		RadioButton genderF = (RadioButton)findViewById(R.id.RadioButtonGenderF);
		RadioButton genderM = (RadioButton)findViewById(R.id.RadioButtonGenderM);
		boolean genderFChecked = genderF.isChecked();
		boolean genderMChecked = genderM.isChecked();
		Spinner Class = (Spinner)findViewById(R.id.SpinnerClass);
		AutoCompleteTextView major = (AutoCompleteTextView)findViewById(R.id.EditMajor);
		
		// save key-value pairs...
		editor.putString(getString(R.string.data_Name), 
							name.getText().toString());
		editor.putString(getString(R.string.data_Email), 
							email.getText().toString());
		editor.putString(getString(R.string.data_Phone), 
							phone.getText().toString());	
		editor.putBoolean(getString(R.string.data_GenderF), genderFChecked);
		editor.putBoolean(getString(R.string.data_GenderM), genderMChecked);
		editor.putInt(getString(R.string.data_Class), Class.getSelectedItemPosition());
		editor.putString(getString(R.string.data_Major), major.getText().toString());
		editor.putString(getString(R.string.data_Birthday), mBirthday);
		editor.commit();
	}
	
	private void loadProfile(){
		SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
		
		EditText name = (EditText)findViewById(R.id.EditTextName);
		EditText phone = (EditText)findViewById(R.id.EditTextPhone);
		EditText email = (EditText)findViewById(R.id.EditTextEmail);
		RadioButton genderF = (RadioButton)findViewById(R.id.RadioButtonGenderF);
		RadioButton genderM = (RadioButton)findViewById(R.id.RadioButtonGenderM);
		Spinner Class = (Spinner)findViewById(R.id.SpinnerClass);
		AutoCompleteTextView major = (AutoCompleteTextView)findViewById(R.id.EditMajor);
		
		
		// load saved values 
		String Name = sharedPref.getString(getString(R.string.data_Name), "");
		name.setText(Name.toCharArray(), 0, Name.toCharArray().length);
		
		String Phone = sharedPref.getString(getString(R.string.data_Phone), "");
		phone.setText(Phone.toCharArray(), 0, Phone.toCharArray().length);
		
		String Email = sharedPref.getString(getString(R.string.data_Email), "");
		email.setText(Email.toCharArray(), 0, Email.toCharArray().length);	
	
		boolean genderFChecked = sharedPref.getBoolean(getString(R.string.data_GenderF), false);
		genderF.setChecked(genderFChecked);
		
		boolean genderMChecked = sharedPref.getBoolean(getString(R.string.data_GenderM), false);
		genderM.setChecked(genderMChecked);
		
		int item = sharedPref.getInt(getString(R.string.data_Class), 0);
		Class.setSelection(item);
		
		String Major = sharedPref.getString(getString(R.string.data_Major), "");
		major.setText(Major.toCharArray(), 0, Major.toCharArray().length);
	
		mBirthday = sharedPref.getString(getString(R.string.data_Birthday), "");
		EditText textBirthday = (EditText) findViewById(R.id.EditTextBirthday);
		textBirthday.setText(mBirthday);
	}
	
	public void displayDialog(int id) {
		DialogFragment fragment = MyRunsDialogFragment.newInstance(id);
		fragment.show(getFragmentManager(),
				getString(R.string.photo_picker_tag));
	}
	public void onPhotoPickerItemSelected(int item) {
		Intent intent;
		isTakenFromCamera = false;
		
		switch(item){
		case MyRunsDialogFragment.ID_PHOTO_PICKER_FROM_CAMERA:
			intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			mImageCaptureUri = Uri.fromFile(new File(Environment
					.getExternalStorageDirectory(), "tmp_"
					+ String.valueOf(System.currentTimeMillis()) + ".jpg"));
			intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
					mImageCaptureUri);
			intent.putExtra("return-data", true);
			try {
				startActivityForResult(intent, REQUEST_CODE_TAKE_FROM_CAMERA);
			} catch (ActivityNotFoundException e) {
				e.printStackTrace();
			}
			isTakenFromCamera = true;
			break;
			
		case MyRunsDialogFragment.ID_PHOTO_PICKER_FROM_GALLERY:
			intent = new Intent(Intent.ACTION_PICK);
			intent.setType("image/*");
			mImageCaptureUri = Uri.fromFile(new File(Environment
					.getExternalStorageDirectory(), "tmp_"
					+ String.valueOf(System.currentTimeMillis()) + ".jpg"));
			intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
					mImageCaptureUri);
			intent.putExtra("return-data", true);
			try{
				startActivityForResult(intent, REQUEST_CODE_SELECT_FROM_GALLERY);
			}catch(ActivityNotFoundException e){
				e.printStackTrace();
			}
			isTakenFromCamera = false;
			break;
			
		default:
			return;
		}
	}
	
	// Handle data after activity returns.
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK)
			return;

		switch (requestCode) {
		case REQUEST_CODE_TAKE_FROM_CAMERA:
			// Send image taken from camera for cropping
			cropImage();
			break;
			
		case REQUEST_CODE_SELECT_FROM_GALLERY:
			mImageCaptureUri = data.getData();
			cropImage();
			break;

		case REQUEST_CODE_CROP_PHOTO:
			// Update image view after image crop

			Bundle extras = data.getExtras();

			// Set the picture image in UI
			if (extras != null) {
				mImageView.setImageBitmap((Bitmap) extras.getParcelable("data"));
			}

			// Delete temporary image taken by camera after crop.
			if (isTakenFromCamera) {
				File f = new File(mImageCaptureUri.getPath());
				if (f.exists())
					f.delete();
			}

			break;
		}
	}

	private void loadSnap() {


		// Load profile photo from internal storage
		try {
			FileInputStream fis = openFileInput(getString(R.string.photo_filename));
			Bitmap bmap = BitmapFactory.decodeStream(fis);
			mImageView.setImageBitmap(bmap);
			fis.close();
		} catch (IOException e) {
			// Default profile photo if no photo saved before.
			mImageView.setImageResource(R.drawable.default_profile);
		}
	}
	
	private void saveSnap() {

	// Commit all the changes into preference file
		// Save profile image into internal storage.
		mImageView.buildDrawingCache();
		Bitmap bmap = mImageView.getDrawingCache();
		try {
			FileOutputStream fos = openFileOutput(
					getString(R.string.photo_filename), MODE_PRIVATE);
			bmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
			fos.flush();
			fos.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	// Crop and resize the image for profile
	private void cropImage() {
		// Use existing crop activity.
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(mImageCaptureUri, IMAGE_UNSPECIFIED);

		// Specify image size
		intent.putExtra("outputX", 100);
		intent.putExtra("outputY", 100);

		// Specify aspect ratio, 1:1
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("scale", true);
		intent.putExtra("return-data", true);
		// REQUEST_CODE_CROP_PHOTO is an integer tag you defined to
		// identify the activity in onActivityResult() when it returns
		startActivityForResult(intent, REQUEST_CODE_CROP_PHOTO);
	}
	
	public void onDateAlertDone(Calendar birthday){
		Date date = birthday.getTime();
		mBirthYear = String.valueOf(date.getYear()+1900);
		mBirthMonth = String.valueOf(date.getMonth()+1);
		mBirthDay = String.valueOf(date.getDate());
		mBirthday = mBirthMonth + "/" + mBirthDay + "/" + mBirthYear;
		EditText textBirthday = (EditText) findViewById(R.id.EditTextBirthday);
		textBirthday.setText(mBirthday);
	}
	
	public void onSelectBirthdayClicked(View view){
		DialogFragment birthDialog = MyRunsDialogFragment.newInstance(MyRunsDialogFragment.DIALOG_ID_BIRTHDAY);
		birthDialog.show(getFragmentManager(),getString(R.string.photo_picker_tag));	// mark tag
		}
}