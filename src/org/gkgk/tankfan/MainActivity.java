package org.gkgk.tankfan;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.util.Log;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

	private SQLiteDatabase db;
    private UpdateReceiver receiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        this.receiver = new UpdateReceiver();

		Intent fetcherIntent = new Intent(this, DataService.class);
		startService(fetcherIntent);
        this.setRepeatingService();

		setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();

        this.refreshDisplay();

        IntentFilter filter = new IntentFilter();
        filter.addAction(DataService.DATA_UPDATED);
        registerReceiver(this.receiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(this.receiver);
    }

    /**
     * Set an alarm to run the service regularly,
     * to keep local database up to date.
     */
    private void setRepeatingService() {

        Intent fetcherIntent = new Intent(this, DataService.class);
        PendingIntent alarmIntent = PendingIntent.getService(
                this, 0, fetcherIntent, 0);

        AlarmManager aman = (AlarmManager) getSystemService(
                Context.ALARM_SERVICE);

        aman.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME,
                AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                alarmIntent);
    }

    /**
     * Load data from db and display it.
     */
    void refreshDisplay() {

		DBHelper helper = new DBHelper(
                this,
                DBHelper.DB_NAME,
                null,
                DBHelper.DB_VERSION);
		this.db = helper.getReadableDatabase();

		this.fillBeer();
		this.fillEvents();

        this.db.close();
	}

	private void fillBeer() {

		BeerAdapter adapter = new BeerAdapter(this);

		ListView beerList = (ListView) findViewById(R.id.beerList);
		beerList.setAdapter(adapter);
	}

	private void fillEvents() {

		Cursor cursor = this.db.query(
				false,	// distinct,
				DBHelper.EVENTS_TABLE,
				null,	// columns
				null,	// where
				null,	// where args
				null,	// group by
				null, 	// having
				null,	// order by
				null);	// limit

		String[] fromColumns = new String[]{"title", "eventdate"};
		int[] toViews = new int[]{R.id.eventTitle, R.id.eventDate};

		@SuppressWarnings("deprecation")
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(
				this,
				R.layout.event_row,
				cursor,
				fromColumns,
				toViews);

		ListView eventList = (ListView) findViewById(R.id.eventList);
		eventList.setAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

    /* inner classes */

    class UpdateReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "UpdateReceiver.onReceive");
            MainActivity.this.refreshDisplay();
        }
    }

}
