package edu.dartmouth.cs.myruns5;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DBHelper extends SQLiteOpenHelper {
		// To create and upgrade a database in your app, you need to inherit the SQLiteOpenHelper class. 
	
		// Database name string
		private static final String DATABASE_NAME = "MyRunsDB";

		// Version code
		private static final int DATABASE_VERSION = 1;
		
		//In the constructor of your subclass you call the super() method of SQLiteOpenHelper, 
		//specifying the database name and the current database version. 
		
		public DBHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		// Called when no database exists in disk and the helper class needs
		// to create a new one.
		@Override
		public void onCreate(SQLiteDatabase db) {

			HistoryTable.onCreate(db);

		}

		// Called when there is a database version mismatch meaning that the
		// version
		// of the database on disk needs to be upgraded to the current version.
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

			HistoryTable.onUpgrade(db, oldVersion, newVersion);
		}
}