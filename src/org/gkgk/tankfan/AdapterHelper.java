package org.gkgk.tankfan;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class AdapterHelper {

    private static final String TAG = AdapterHelper.class.getSimpleName();

    Cursor cursor;
	Context context;
    String tableName;

    public AdapterHelper(Context context, String tableName) {
		this.context = context;
        this.tableName = tableName;
    }

    public List<Map<String, String>> load() {

        List<Map<String, String>> data = new ArrayList<Map<String, String>>();

		DBHelper helper = new DBHelper(
                this.context,
                DBHelper.DB_NAME,
                null,
                DBHelper.DB_VERSION);
	    SQLiteDatabase db = helper.getReadableDatabase();

		this.cursor = db.query(
				false,	// distinct,
                this.tableName,
				null,	// columns
				null,	// where
				null,	// where args
				null,	// group by
				null, 	// having
				null,	// order by
				null);	// limit

        while (this.cursor.moveToNext()) {
            data.add(this.loadNext());
        }
        this.cursor.close();
        db.close();

        return data;
    }

    /**
     * Load the next item from the cursor into a map.
     */
    private Map<String, String> loadNext() {
		// Object model? What object model?
		String value;
		Map<String, String> obj = new HashMap<String, String>();

		for (String col : this.cursor.getColumnNames()) {
			value = this.cursor.getString(this.cursor.getColumnIndex(col));
			obj.put(col, value);
		}

		return obj;
	}
}
