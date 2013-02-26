package org.gkgk.tankfan;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;

public class BeerAdapter extends SimpleCursorAdapter {

	private static final String TAG = BeerAdapter.class.getSimpleName();
	
	Context context;
	
	@SuppressWarnings("deprecation")
	public BeerAdapter(
			Context context,
			int layoutID, 
			Cursor cursor, 
			String[] fromColumns, 
			int []toViews) {
		super(context, layoutID, cursor, fromColumns, toViews);
		this.context = context;
	}

	@Override
	public void setViewImage(ImageView v, String urlStr) {
		//super.setViewImage(v, urlStr);
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

	/*
	@Override
	public void setViewText(TextView v, String text) {
		
		// Bold the beer name
		if (v.getId() == R.id.beerName) {
			v.setTypeface(null, Typeface.BOLD);
		}
		
		super.setViewText(v, text);
	}
	*/
}
