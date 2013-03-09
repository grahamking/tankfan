package org.gkgk.tankfan;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class AdapterHelper {

    private static final String TAG = AdapterHelper.class.getSimpleName();

    Cursor cursor;
	Context context;
    String tableName;

    List<Map<String, String>> data;

    public AdapterHelper(Context context, String tableName) {
		this.context = context;
        this.tableName = tableName;
    }

    public List<Map<String, String>> loadData() {

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

        // Save it because loadImages needs
        this.data = data;

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

    /**
     * Load all the images off disk into memory, so that main
     * thread never blocks on i/o.
     */
    public Map<String, Bitmap> loadImages() {

        Map<String, Bitmap> result = new HashMap<String, Bitmap>();

        for (Map<String, String> obj : this.data) {

            if (! obj.containsKey("pic") ) {
                continue;
            }

            String picURL = obj.get("pic");
            if (picURL == null || picURL.length() == 0) {
                continue;
            }

            String filename = String.valueOf(Math.abs(picURL.hashCode())) + ".png";
            result.put(filename, loadPic(filename));
        }

        return result;
    }

    /**
     * Load a single image off disk, by url.
     */
    Bitmap loadPic(String filename) {

		FileInputStream inputStream = null;
		try {
			inputStream = this.context.openFileInput(filename);
		}
		catch (FileNotFoundException exc) {
			Log.d(TAG, "FileNotFoundException: " + filename);
			return null;
		}

		Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

		try {
			inputStream.close();
		}
		catch (IOException exc) {
			Log.e(TAG, "IOException closing " + filename);
		}

        return bitmap;
    }
}
