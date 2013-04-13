package org.gkgk.tankfan;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import android.content.Context;
import android.widget.ListAdapter;
import android.database.DataSetObserver;
import android.provider.BaseColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.util.Log;
import android.text.format.DateUtils;

public class TwitterAdapter implements ListAdapter {

	private static final String TAG = TwitterAdapter.class.getSimpleName();

    Context context;
    List<Map<String, String>> data = new ArrayList<Map<String, String>>();

	public TwitterAdapter(Context context) {
		this.context = context;
        this.data = new AdapterHelper(context, DBHelper.TWITTER_TABLE).loadData();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		Map<String, String> obj = this.getItem(position);

		View view = convertView;
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.twitter_row, null);
        }

        String rel = this.relative(obj.get("created"));
		((TextView) view.findViewById(R.id.tweetDate)).setText(rel);
		((TextView) view.findViewById(R.id.tweetContent)).setText(obj.get("content"));

		return view;
	}

    /**
     * Parse theDate from ISO 8601, convert to relative date, and
     * return as string.
     */
    String relative(String theDate) {

        SimpleDateFormat sdf = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss.SSSZ",
                Locale.US);
        Date asDate = null;
        try {
            asDate = sdf.parse(theDate);
        }
        catch (ParseException exc) {
            Log.e(TAG, "Error parsing, expected ISO8601: " + theDate, exc);
            return "";
        }

        return DateUtils.getRelativeDateTimeString(
						this.context,
						asDate.getTime(),
						DateUtils.MINUTE_IN_MILLIS,
						DateUtils.WEEK_IN_MILLIS,
						0).toString();
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
