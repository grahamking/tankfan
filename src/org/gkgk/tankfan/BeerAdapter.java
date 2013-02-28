package org.gkgk.tankfan;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

public class BeerAdapter implements ListAdapter {

	private static final String TAG = BeerAdapter.class.getSimpleName();

    Cursor cursor;
	Context context;
    DBHelper helper;

    List<Map<String, String>> data = new ArrayList<Map<String, String>>();

	public BeerAdapter(Context context) {
		this.context = context;

		this.helper = new DBHelper(this.context, DBHelper.DB_NAME, null, DBHelper.DB_VERSION);
	    SQLiteDatabase db = this.helper.getReadableDatabase();

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

        while (this.cursor.moveToNext()) {
            data.add(this.loadNext());
        }
        this.cursor.close();
        db.close();
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

	public void setPic(ImageView v, String urlStr) {

		String filename = String.valueOf(Math.abs(urlStr.hashCode())) + ".png";

		FileInputStream inputStream = null;
		try {
			inputStream = this.context.openFileInput(filename);
		}
		catch (FileNotFoundException exc) {
			Log.d(TAG, "FileNotFoundException: " + filename);
			return;
		}

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
		return this.data.size();
	}

	@Override
	public Map<String, String> getItem(int position) {
        return this.data.get(position);
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

		final String linkURL = obj.get("url");
		if (linkURL != null && linkURL.length() != 0) {

			View.OnClickListener clicky = new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(linkURL));
					BeerAdapter.this.context.startActivity(intent);
				}
			};

			((LinearLayout) view.findViewById(R.id.beerRow)).setOnClickListener(clicky);
			((LinearLayout) view.findViewById(R.id.beerRowText1)).setOnClickListener(clicky);
			((LinearLayout) view.findViewById(R.id.beerRowText2)).setOnClickListener(clicky);
		}

		return view;
	}

	private String getBreweryPic(String breweryName) {
        Log.d(TAG, "getBreweryPic: " + breweryName);

	    SQLiteDatabase db = this.helper.getReadableDatabase();

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

        db.close();

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
