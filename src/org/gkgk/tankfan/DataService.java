package org.gkgk.tankfan;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class DataService extends IntentService {

    public static final String DATA_UPDATED = "org.gkgk.tankfan.DATA_UPDATED";

	private static final String TAG = DataService.class.getSimpleName();
	private static final String URL = "http://darkcoding.net/tank.json";

	private Random random;
	private String jsonData;

	private SQLiteDatabase db;

	private JSONArray beerJSON;
	private JSONArray eventJSON;
	private JSONArray breweriesJSON;

	public DataService() {
		super("org.gkgk.tankfan.DataService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		this.random = new Random();

		DBHelper dbHelper = new DBHelper(this, DBHelper.DB_NAME, null, DBHelper.DB_VERSION);
		this.db = dbHelper.getWritableDatabase();

		this.fetch();
		this.parse();
		this.save();
		this.downloadPics(this.allURLs());

		this.broadcastComplete();
	}

	/**
	 * Tell rest of the app (MainActivity) that we're finished fetching.
	 */
	private void broadcastComplete() {
        Intent done = new Intent(DataService.DATA_UPDATED);
        sendBroadcast(done);
	}

	/**
	 * Read JSON from network and store in this.jsonData
	 */
	private void fetch() {

		try {
			this.jsonData = this.fetchURL(URL);
			Log.d(TAG, this.jsonData);
		}
		catch (IOException exc) {
			Log.e(TAG, "IOException fetching " + URL, exc);
			return;
		}
	}

	/**
	 * Extract beer and event JSON arrays
	 */
	private void parse() {

		try {
			JSONObject jobj = (JSONObject) new JSONTokener(this.jsonData).nextValue();

			this.beerJSON = jobj.getJSONArray("beers");
			this.eventJSON = jobj.getJSONArray("events");
			this.breweriesJSON = jobj.getJSONArray("breweries");
		}
		catch (JSONException exc) {
			Log.e(TAG, "JSONException parsing data above");
			return;
		}

	}

	/**
	 * Store this.beers in the database.
	 */
	private void save() {

		// First wipe all previous data.

		// In future we should compare what's there, and notify on new staff pick

		db.delete(DBHelper.BEERS_TABLE, null, null);
		db.delete(DBHelper.EVENTS_TABLE, null, null);
		db.delete(DBHelper.BREWERIES_TABLE, null, null);

		try {
			this.saveJSON(this.beerJSON, DBHelper.BEERS_TABLE);
			this.saveJSON(this.eventJSON, DBHelper.EVENTS_TABLE);
			this.saveJSON(this.breweriesJSON, DBHelper.BREWERIES_TABLE);
		}
		catch (JSONException exc) {
			Log.e(TAG, "JSONException saving.", exc);
		}

	}

	private void saveJSON(JSONArray arr, String tableName) throws JSONException {

		for (int i = 0; i < arr.length(); i++) {
			JSONObject jobj = arr.getJSONObject(i);

			ContentValues vals = new ContentValues();

			Iterator<String> keys = jobj.keys();
			while (keys.hasNext()) {
				String key = keys.next();
				vals.put(key, jobj.getString(key));
			}

			try {
				db.insertOrThrow(tableName, null, vals);
			}
			catch (SQLException exc) {
				Log.e(TAG, "SQLException saving " + vals +" to database.");
			}

		}
	}

	/**
	 * Fetch contents of a URL.
	 * @param urlStr A URL to fetch.
	 */
	private String fetchURL(String urlStr) throws IOException {

		URL url = null;
		String cacheBuster = "?rnd=" + Math.abs(this.random.nextInt());
        try {
            url = new URL(urlStr + cacheBuster);
        } catch (MalformedURLException exc) {
            Log.e(TAG, "MalformedURLException on: "+ urlStr, exc);
            return "";
        }

        StringBuffer dataRead = new StringBuffer();
        byte[] buf = new byte[2048];
        int num_read = 0;

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();

        InputStream stream = conn.getInputStream();

        num_read = stream.read(buf);
        while (num_read != -1) {
        	dataRead.append(new String(buf, 0, num_read, "UTF-8"));
        	num_read = stream.read(buf);
        }

        stream.close();
        conn.disconnect();

        return dataRead.toString();
	}

	/**
	 * Load all the image URLs from the database.
	 */
	private List<String> allURLs() {

		List<String> result = new ArrayList<String>();

		result.addAll(this.loadURLs(DBHelper.BEERS_TABLE, "pic"));
		result.addAll(this.loadURLs(DBHelper.BREWERIES_TABLE, "logo"));

		return result;
	}

	private List<String> loadURLs(String tableName, String columnName) {

		List<String> result = new ArrayList<String>();

		String[] theColumn = new String[]{columnName};
		Cursor cursor = db.query(
				true,
				tableName,
				theColumn,
				null,
				null,
				null,
				null,
				null,
				null);
		while (cursor.moveToNext()) {
			String url = cursor.getString(0);
			result.add(url);
		}

		return result;
	}

	/**
	 *  Download urls to local files.
	 */
	private void downloadPics(List<String> allURLs) {

		for (String urlStr : allURLs) {

			String filename = String.valueOf(Math.abs(urlStr.hashCode())) + ".png";
			Log.d(TAG, filename + " = " + urlStr);

			File fullpath = new File(getFilesDir(), filename);
			if (fullpath.exists()) {
				Log.d(TAG, "Already exists, skipping");
				continue;
			}

			Log.d(TAG, "Downloading: " + urlStr);
			URL newurl;
			try{
				newurl = new URL(urlStr);
			}
			catch (MalformedURLException exc) {
				Log.d(TAG, "MalformedURLException: "+ urlStr);
				continue;
			}

			Bitmap image;
			try {
				image = BitmapFactory.decodeStream(newurl.openConnection() .getInputStream());
			}
			catch (IOException exc) {
				Log.e(TAG, "IOException saving " + urlStr + " data to file.", exc);
				continue;
			}

			try {
				FileOutputStream outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
				image.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
			}
			catch (FileNotFoundException exc) {
				Log.e(TAG, "FileNotFoundException creating " + filename, exc);
			}
		}

	}
}
