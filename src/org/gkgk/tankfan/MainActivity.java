package org.gkgk.tankfan;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.TimeZone;

import android.os.StrictMode;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.os.Bundle;
import android.os.AsyncTask;
import android.view.Menu;
import android.widget.TextView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.format.DateUtils;
import android.util.Log;

public class MainActivity extends TabActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    //private UpdateReceiver receiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

        //StrictMode.enableDefaults();

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        TextView statusView = (TextView) findViewById(R.id.status);
        statusView.setText(getResources().getText(R.string.loading));

        //this.receiver = new UpdateReceiver();

        this.createTabs();

        this.displayUpdatedDate();

		Intent fetcherIntent = new Intent(this, DataService.class);
		startService(fetcherIntent);
        this.setRepeatingService();

        Intent tweetIntent = new Intent(this, TwitterService.class);
        startService(tweetIntent);
    }

    /*
    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(DataService.DATA_UPDATED);
        registerReceiver(this.receiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(this.receiver);
    }
    */

    private void createTabs() {
        TabHost host = getTabHost();

        TabSpec beerSpec = host.newTabSpec("BEER");
        beerSpec.setIndicator(getResources().getText(R.string.beer));
        Intent beerIntent = new Intent(this, BeerActivity.class);
        beerSpec.setContent(beerIntent);
        host.addTab(beerSpec);

        TabSpec bSpec = host.newTabSpec("TWITTER");
        bSpec.setIndicator(getResources().getText(R.string.twitter));
        Intent bIntent = new Intent(this, TwitterActivity.class);
        bSpec.setContent(bIntent);
        host.addTab(bSpec);
    }

    /**
     * Show the updated date on screen.
     */
    void displayUpdatedDate() {

        // Doesn't apply to Twitter view
        // - need to move to Staff Picks tab only...
        //new MainActivity.LoadUpdated().execute();

        // ..so for now just make it blank
        TextView statusView = (TextView) findViewById(R.id.status);
        statusView.setText("");
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
                AlarmManager.INTERVAL_HALF_DAY,
                AlarmManager.INTERVAL_HALF_DAY,
                alarmIntent);
    }

    /*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
    */

    /* inner classes */

    /*
    class UpdateReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            MainActivity.this.displayUpdatedDate();
        }
    }
    */

    /**
     * Load the updated date from the db and put it on screen.
     */
    class LoadUpdated extends AsyncTask<Void, Void, CharSequence> {

        protected CharSequence doInBackground(Void...params) {

            return loadUpdatedDate(MainActivity.this);
        }

        /**
        * Load the date the remote data was last updated.
        */
        CharSequence loadUpdatedDate(Context context) {

            DBHelper helper = new DBHelper(
                    context, DBHelper.DB_NAME, null, DBHelper.DB_VERSION);
            SQLiteDatabase db = helper.getReadableDatabase();

            Cursor cursor = db.query(
                    false,	// distinct,
                    DBHelper.UPDATED_TABLE,
                    null,	// columns
                    null,	// where
                    null,	// where args
                    null,	// group by
                    null, 	// having
                    null,	// order by
                    null);	// limit

            CharSequence updated = null;

            if (cursor.moveToNext()) {

                String dbUpdated = cursor.getString(cursor.getColumnIndex("updated"));

                // Truncate the nanoseconds
                dbUpdated = dbUpdated.substring(0, 19);

                cursor.close();
                db.close();

                SimpleDateFormat df = new SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ss",
                        Locale.US);
                df.setTimeZone(TimeZone.getTimeZone("UTC"));

                Date updatedDate = null;
                try {
                    updatedDate = df.parse(dbUpdated);
                }
                catch (ParseException exc) {
                    Log.e(TAG, "Error parsing date: " + dbUpdated, exc);
                    return null;
                }

                updated = DateUtils.getRelativeDateTimeString(
                        context,
                        updatedDate.getTime(),
                        DateUtils.DAY_IN_MILLIS,
                        DateUtils.YEAR_IN_MILLIS,
                        0);
            } else {
                cursor.close();
                db.close();
            }

            return updated;
        }

        protected void onPostExecute(CharSequence updatedDate) {

            if (updatedDate != null && updatedDate.length() != 0) {
                TextView status = (TextView) MainActivity.this.findViewById(
                        R.id.status);
                status.setText("Updated " + updatedDate);
            }
        }
    }
}
