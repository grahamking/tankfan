package org.gkgk.tankfan;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class MainActivity extends TabActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent fetcherIntent = new Intent(this, DataService.class);
		startService(fetcherIntent);
        this.setRepeatingService();

		setContentView(R.layout.activity_main);

        this.createTabs();
    }

    private void createTabs() {
        TabHost host = getTabHost();

        TabSpec beerSpec = host.newTabSpec("BEER");
        beerSpec.setIndicator(getResources().getText(R.string.beer));
        Intent beerIntent = new Intent(this, BeerActivity.class);
        beerSpec.setContent(beerIntent);
        host.addTab(beerSpec);

        TabSpec eventSpec = host.newTabSpec("EVENTS");
        eventSpec.setIndicator(getResources().getText(R.string.events));
        Intent eventIntent = new Intent(this, EventActivity.class);
        eventSpec.setContent(eventIntent);
        host.addTab(eventSpec);
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
