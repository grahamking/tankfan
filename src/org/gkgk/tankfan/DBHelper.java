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
	public static final int DB_VERSION = 6;

	static final String BEERS_TABLE = "beers";
    static final String[] BEERS_COLUMNS = new String[]{
            "brewery",
            "name",
            "style",
            "abv",
            "pic",
            "url",
            "location",
            "description"};

	static final String EVENTS_TABLE = "events";
	static final String[] EVENTS_COLUMNS = new String[]{
			"title",
            "eventdate"};

    static final String TWITTER_TABLE = "twitter";
    static final String[] TWITTER_COLUMNS = new String[]{
            "content",
            "created",
            "tags"};

    static final String UPDATED_TABLE = "updated";
    static final String[] UPDATED_COLUMNS = new String[] {
        "updated"};

	public DBHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d(TAG, "DBHelper.onCreate");

		db.execSQL(this.makeCreate(BEERS_TABLE, BEERS_COLUMNS));
		db.execSQL(this.makeCreate(EVENTS_TABLE, EVENTS_COLUMNS));
		db.execSQL(this.makeCreate(UPDATED_TABLE, UPDATED_COLUMNS));
        db.execSQL(this.makeCreate(TWITTER_TABLE, TWITTER_COLUMNS));
	}

    /**
     * Make the SQL CREATE statement for the given tableName and columns.
     * All columns are 'text'.
     * An integer primary key called BaseColumns._ID is added.
     */
    String makeCreate(String tableName, String[] columns) {
	    StringBuilder res = new StringBuilder("CREATE TABLE " + tableName + " ( ");
        res.append(BaseColumns._ID + " integer primary key autoincrement, ");

        int colLen = columns.length;
        for (int i = 0; i < colLen; i++) {
            res.append(columns[i]);
            res.append(" text");
            if (i != (colLen - 1)) {
                res.append(", ");
            }
        }
        res.append(")");
        return res.toString();
    }

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		db.execSQL("DROP TABLE IF EXISTS " + BEERS_TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + EVENTS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + UPDATED_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + TWITTER_TABLE);
		db.execSQL("DROP TABLE IF EXISTS breweries");   // From old version

		this.onCreate(db);
	}

}
