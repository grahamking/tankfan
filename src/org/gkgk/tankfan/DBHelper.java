package org.gkgk.tankfan;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {

	private static final String TAG = DBHelper.class.getSimpleName();
	
	public static final String DB_NAME = "tankfan";
	public static final int DB_VERSION = 4;
	
	public static final String BEERS_TABLE = "beers";
	public static final String EVENTS_TABLE = "events";
	public static final String BREWERIES_TABLE = "breweries";
	
	private static final String BEERS_CREATE = "CREATE TABLE " + BEERS_TABLE + "( " +
			BaseColumns._ID + " integer primary key autoincrement, " +
			"brewery text, " +
			"name text, " +
			"style text, " +
			"abv text, " +
			"pic text, " +
			"url text," +
			"description text" +
			")";
	
	private static final String EVENTS_CREATE = "CREATE TABLE " + EVENTS_TABLE + "( " +
			BaseColumns._ID + " integer primary key autoincrement, " +
			"title text, " +
			"eventdate text" +
			")";
	
	private static final String BREWERIES_CREATE = "CREATE TABLE " + BREWERIES_TABLE + "( " +
			BaseColumns._ID + " integer primary key autoincrement, " +
			"name text, " +
			"location text, " +
			"logo text " +
			")";
	
	public DBHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d(TAG, "DBHelper.onCreate");
		
		db.execSQL(BEERS_CREATE);
		db.execSQL(EVENTS_CREATE);
		db.execSQL(BREWERIES_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		db.execSQL("DROP TABLE IF EXISTS " + BEERS_TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + EVENTS_TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + BREWERIES_TABLE);
		
		this.onCreate(db);
	}

}
