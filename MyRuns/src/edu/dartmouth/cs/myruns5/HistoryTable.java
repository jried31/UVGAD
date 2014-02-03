package edu.dartmouth.cs.myruns5;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

//HistoryTable contains constants for the table name and the columns. 
public class HistoryTable{
	
			// Table name string. (Only one table)
			public static final String TABLE_NAME_ENTRIES = "RUN_HISTORY_TABLE";

			// Table column names
			public static final String KEY_ROWID = Globals.KEY_ROWID;
			public static final String KEY_INPUT_TYPE = Globals.KEY_INPUT_TYPE;
			public static final String KEY_ACTIVITY_TYPE = Globals.KEY_ACTIVITY_TYPE;
			public static final String KEY_DATE_TIME = Globals.KEY_DATE_TIME;
			public static final String KEY_DURATION = Globals.KEY_DURATION;
			public static final String KEY_DISTANCE = Globals.KEY_DISTANCE;
			public static final String KEY_SWEATRATE = Globals.KEY_SWEATRATE;
			public static final String KEY_AVG_PACE = Globals.KEY_AVG_PACE;
			public static final String KEY_AVG_SPEED = Globals.KEY_AVG_SPEED;
			public static final String KEY_CALORIES = Globals.KEY_CALORIES;
			public static final String KEY_CLIMB = Globals.KEY_CLIMB;
			public static final String KEY_HEARTRATE = Globals.KEY_HEARTRATE;
			public static final String KEY_COMMENT = Globals.KEY_COMMENT;
			public static final String KEY_PRIVACY = Globals.KEY_PRIVACY;
			public static final String KEY_GPS_DATA = Globals.KEY_GPS_DATA;
			public static final String KEY_VITAMIN_D = Globals.KEY_VITAMIN_D;
			public static final String KEY_UV_EXPOSURE = Globals.KEY_UV_EXPOSURE;
			public static final String KEY_TRACK = Globals.KEY_TRACK;

			// SQL query to create the table for the first time
			// Data types are defined below
			public static final String CREATE_TABLE_ENTRIES = "CREATE TABLE IF NOT EXISTS "
					+ TABLE_NAME_ENTRIES
					+ " ("
					+ KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ KEY_INPUT_TYPE + " INTEGER NOT NULL, "
					+ KEY_ACTIVITY_TYPE + " INTEGER NOT NULL, "
					+ KEY_DATE_TIME + " DATETIME NOT NULL, "
					+ KEY_DURATION + " INTEGER NOT NULL, "
					+ KEY_DISTANCE + " FLOAT, "
					+ KEY_SWEATRATE + " FLOAT, "
					+ KEY_AVG_PACE + " FLOAT, "
					+ KEY_AVG_SPEED + " FLOAT,"
					+ KEY_CALORIES + " INTEGER, "
					+ KEY_CLIMB + " FLOAT, "
					+ KEY_UV_EXPOSURE + " FLOAT, "
					+ KEY_VITAMIN_D + " FLOAT, "
					+ KEY_HEARTRATE + " INTEGER, "
					+ KEY_COMMENT + " TEXT, "
					+ KEY_PRIVACY + " INTEGER, " 
					+ KEY_TRACK + " BLOB " + ");";
			
			public static void onCreate(SQLiteDatabase database) {
			    database.execSQL(CREATE_TABLE_ENTRIES);
			  }

			  public static void onUpgrade(SQLiteDatabase database, int oldVersion,
			      int newVersion) {
			    Log.w(HistoryTable.class.getName(), "Upgrading database from version "
			        + oldVersion + " to " + newVersion + ", which will destroy all old data");
			    database.execSQL("DROP TABLE IF EXISTS ");
			    onCreate(database);
			  }
}
