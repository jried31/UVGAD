/* 
 * codes for checkColomns() are written by Lars Vogel
 */

package edu.dartmouth.cs.myruns5;


import java.util.Arrays;
import java.util.HashSet;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;



public class HistoryProvider extends ContentProvider {

	  // database
	  private DBHelper database;

	  private static final String AUTHORITY = "edu.dartmouth.cs.myruns5.historyprovider";
	  
	  public static final int ENTRIES_DIR = 300;
	  public static final int ENTRIES_ID = 310;
	  private static final String BASE_PATH= "history";
	  public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/"+BASE_PATH);
	  public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
				+ "/ExerciseEntry";
	  
	  private static final UriMatcher sURIMatcher = new UriMatcher(
		        UriMatcher.NO_MATCH);
	  
		static 
		{
		    sURIMatcher.addURI(AUTHORITY, BASE_PATH, ENTRIES_DIR);
		    sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", ENTRIES_ID);
		}
		
		
      //used to initialize this content provider. this method runs on ui thread, so should be quick. 
	  //good place to instantiate database helper, if using database. 
	  @Override
	  public boolean onCreate() {
	    database = new DBHelper(getContext());
	    return false;
	  }
	  
	  
      //queries the provider for the records specified by either uri or 'selection'.
	  @Override
	  public Cursor query(Uri uri, String[] projection, String selection,
	      String[] selectionArgs, String sortOrder) {
			SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

			checkColumns(projection);

			queryBuilder.setTables(HistoryTable.TABLE_NAME_ENTRIES);

			int uriType = sURIMatcher.match(uri);
			switch (uriType) {
			case ENTRIES_DIR:
				break;
			case ENTRIES_ID:
				queryBuilder.appendWhere(HistoryTable.KEY_ROWID + "="
						+ uri.getLastPathSegment());
				break;
			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
			}

			SQLiteDatabase db = database.getWritableDatabase();
			Cursor cursor = queryBuilder.query(db, projection, selection,
					selectionArgs, null, null, sortOrder);
			cursor.setNotificationUri(getContext().getContentResolver(), uri);

			return cursor;	    
	  }

	  @Override
	  public String getType(Uri uri) {
	    return null;
	  }
	  
	  //insert the ContentValues into SQlite database.
	  @Override
	  public Uri insert(Uri uri, ContentValues values) {
		//put the values in ContentValues to the ContentProvider which is denoted by the Uri. 
			int uriType = sURIMatcher.match(uri);
			SQLiteDatabase sqlDB = database.getWritableDatabase();
			long id = 0;
			switch (uriType) {
			case ENTRIES_DIR:
				Log.d(null, "inserting to database");
				id = sqlDB.insert(HistoryTable.TABLE_NAME_ENTRIES, null, values);
				Log.d(null, "insert success");

//				Toast.makeText(getContext(), "HistoryProvider: insert", Toast.LENGTH_SHORT).show();
				break;
			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
			}
			getContext().getContentResolver().notifyChange(uri, null);
			return Uri.parse(BASE_PATH + "/" + id);
	  }

	  //Deletes row(s) specified by a content URI.
	  @Override
	  public int delete(Uri uri, String selection, String[] selectionArgs) {
			int uriType = sURIMatcher.match(uri);
			SQLiteDatabase sqlDB = database.getWritableDatabase();
			int rowsDeleted = 0;
			switch (uriType) {
			case ENTRIES_DIR:
				rowsDeleted = sqlDB.delete(HistoryTable.TABLE_NAME_ENTRIES, selection,
						selectionArgs);
				break;
			case ENTRIES_ID:
				String id = uri.getLastPathSegment();
				if (TextUtils.isEmpty(selection)) {
					rowsDeleted = sqlDB.delete(HistoryTable.TABLE_NAME_ENTRIES,
							HistoryTable.KEY_ROWID + "=" + id, null);
				} else {
					rowsDeleted = sqlDB.delete(HistoryTable.TABLE_NAME_ENTRIES,
							HistoryTable.KEY_ROWID + "=" + id + " and " + selection,
							selectionArgs);
				}
				break;
			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
			}
			getContext().getContentResolver().notifyChange(uri, null);
			return rowsDeleted;
	  }
	  
	  
	  //Update row(s) in a content URI.
		@Override
		public int update(Uri uri, ContentValues values, String selection,
				String[] selectionArgs) {

			int uriType = sURIMatcher.match(uri);
			SQLiteDatabase sqlDB = database.getWritableDatabase();
			int rowsUpdated = 0;
			switch (uriType) {
			case ENTRIES_DIR:
				rowsUpdated = sqlDB.update(HistoryTable.TABLE_NAME_ENTRIES, values, selection,
						selectionArgs);
				break;
			case ENTRIES_ID:
				String id = uri.getLastPathSegment();
				if (TextUtils.isEmpty(selection)) {
					rowsUpdated = sqlDB.update(HistoryTable.TABLE_NAME_ENTRIES, values,
							HistoryTable.KEY_ROWID + "=" + id, null);
				} else {
					rowsUpdated = sqlDB.update(HistoryTable.TABLE_NAME_ENTRIES, values,
							HistoryTable.KEY_ROWID + "=" + id + " and " + selection,
							selectionArgs);
				}
				break;
			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
			}
			getContext().getContentResolver().notifyChange(uri, null);
			return rowsUpdated;
		}

		private void checkColumns(String[] projection) {
			String[] available = {	HistoryTable.KEY_ACTIVITY_TYPE,
									HistoryTable.KEY_AVG_PACE,
									HistoryTable.KEY_AVG_SPEED,
									HistoryTable.KEY_CALORIES,
									HistoryTable.KEY_UV_EXPOSURE,
									HistoryTable.KEY_VITAMIN_D,
									HistoryTable.KEY_CLIMB,
									HistoryTable.KEY_COMMENT,
									HistoryTable.KEY_DATE_TIME,
									HistoryTable.KEY_DISTANCE,
									HistoryTable.KEY_SWEATRATE,
									HistoryTable.KEY_DURATION,
									HistoryTable.KEY_GPS_DATA,
									HistoryTable.KEY_HEARTRATE,
									HistoryTable.KEY_INPUT_TYPE,
									HistoryTable.KEY_PRIVACY,
									HistoryTable.KEY_TRACK
									};
			if (projection != null) {
				HashSet<String> requestedColumns = new HashSet<String>(
						Arrays.asList(projection));
				HashSet<String> availableColumns = new HashSet<String>(
						Arrays.asList(available));
				// Check if all columns which are requested are available
				if (!availableColumns.containsAll(requestedColumns)) {
					throw new IllegalArgumentException(
							"Unknown columns in projection");
				}
			}
		}
}
