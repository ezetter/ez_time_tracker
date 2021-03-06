package com.zetterbaum.timetracker.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.zetterbaum.timetracker.R;

public class DatabaseInstance {
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;
	private Context mCtx;
	private static DatabaseInstance currentInstance;

	public static void initialize(Context ctx) {
		if(currentInstance == null) {
			DatabaseInstance instance = new DatabaseInstance();
			currentInstance = instance;
			instance.mCtx = ctx;
		}
	}

	private void openInstance() {
		mDbHelper = new DatabaseHelper(mCtx, mCtx.getString(R.string.database_name));
		mDb = mDbHelper.getWritableDatabase();
	}

	public static void open() {
		if(DatabaseInstance.getDb() == null || !DatabaseInstance.getDb().isOpen()) {
			DatabaseInstance.currentInstance.openInstance();
		}
	}

	public static void close() {
		currentInstance.mDb.close();
		currentInstance.mDbHelper.close();
	}

	public static SQLiteDatabase getDb() {
		if(currentInstance.mDb != null && !currentInstance.mDb.isOpen()) {
			currentInstance.openInstance();
		}
		return currentInstance.mDb;
	}


}
