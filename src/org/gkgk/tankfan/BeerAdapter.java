package org.gkgk.tankfan;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

public class BeerAdapter implements ListAdapter {

	private static final String TAG = BeerAdapter.class.getSimpleName();
	
	Context context;
	SQLiteDatabase db;
	Cursor cursor;
	
	public BeerAdapter(Context context) {
		this.context = context;
		
		DBHelper helper = new DBHelper(this.context, DBHelper.DB_NAME, null, DBHelper.DB_VERSION);
		this.db = helper.getReadableDatabase();
		
		this.cursor = db.query(
				false,	// distinct,
				DBHelper.BEERS_TABLE,
				null,	// columns
				null,	// where
				null,	// where args
				null,	// group by
				null, 	// having
				null,	// order by
				null);	// limit
	}

	public void setPic(ImageView v, String urlStr) {

		Log.d(TAG, "urlStr: " + urlStr);
		
		String filename = String.valueOf(Math.abs(urlStr.hashCode())) + ".png";
		Log.d(TAG, "filename: " + filename);
		
		FileInputStream inputStream = null;
		try {
			inputStream = this.context.openFileInput(filename);
		}
		catch (FileNotFoundException exc) {
			Log.d(TAG, "FileNotFoundException: " + filename);
			return;
		}
		Log.d(TAG, "LOADED!");
		Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
		v.setImageBitmap(bitmap);
		
		try {
			inputStream.close();
		}
		catch (IOException exc) {
			Log.e(TAG, "IOException closing " + filename);
		}
	}
	
	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub		
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub
	}

	@Override
	public int getCount() {
		return this.cursor.getCount();
	}

	@Override
	public Map<String, String> getItem(int position) {
		
		this.cursor.moveToPosition(position);
		
		// Object model? What object model?
		String value;
		Map<String, String> obj = new HashMap<String, String>();
		
		for (String col : this.cursor.getColumnNames()) {
			value = this.cursor.getString(this.cursor.getColumnIndex(col));
			obj.put(col, value);
		}
		
		return obj;
	}

	@Override
	public long getItemId(int position) {
		return Long.valueOf(this.getItem(position).get(BaseColumns._ID));
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		Map<String, String> obj = this.getItem(position);
		
		View view = convertView;
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.beer_row, null);
		}

		String picURL = obj.get("pic");
		if (picURL == null || picURL.length() == 0) {
			picURL = this.getBreweryPic(obj.get("brewery"));
		}
		this.setPic((ImageView) view.findViewById(R.id.beerPic), picURL);
		
		((TextView) view.findViewById(R.id.beerBrewery)).setText(obj.get("brewery"));
		((TextView) view.findViewById(R.id.beerName)).setText(obj.get("name"));
		((TextView) view.findViewById(R.id.beerStyle)).setText(obj.get("style"));
		((TextView) view.findViewById(R.id.beerABV)).setText(obj.get("abv"));
		((TextView) view.findViewById(R.id.beerDescription)).setText(obj.get("description"));
		
		return view;
	}

	private String getBreweryPic(String breweryName) {
		
		String[] columns = new String[]{"logo"};
		String[] whereArgs = new String[]{breweryName};
		
		Cursor bCur = db.query(
				false,	// distinct,
				DBHelper.BREWERIES_TABLE,
				columns,	// columns
				"name = ?",	// where
				whereArgs,	// where args
				null,	// group by
				null, 	// having
				null,	// order by
				null);	// limit
		
		bCur.moveToFirst();
		String logo = bCur.getString(bCur.getColumnIndex("logo"));
		bCur.close();
		
		return logo;
	}
	
	@Override
	public int getItemViewType(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public boolean isEmpty() {
		return this.getCount() > 0;
	}

	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}

	@Override
	public boolean isEnabled(int position) {
		return true;
	}	
	
}
