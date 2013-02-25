package org.gkgk.tankfan;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class MainActivity extends Activity {

	private SQLiteDatabase db;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent fetcherIntent = new Intent(this, DataService.class);
		startService(fetcherIntent);
		
		setContentView(R.layout.activity_main);
		
		DBHelper helper = new DBHelper(this, DBHelper.DB_NAME, null, DBHelper.DB_VERSION);
		this.db = helper.getReadableDatabase();
		
		this.fillBeer();
		this.fillEvents();
	}

	private void fillBeer() {
		
		Cursor cursor = this.db.query(
				false,	// distinct,
				DBHelper.BEERS_TABLE,
				null,	// columns
				null,	// where
				null,	// where args
				null,	// group by
				null, 	// having
				null,	// order by
				null);	// limit

		String[] fromColumns = new String[]{"brewery", "name", "description"};
		int[] toViews = new int[]{R.id.beerBrewery, R.id.beerName, R.id.beerDescription};
		
		@SuppressWarnings("deprecation")
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(
				this, 
				R.layout.beer_row, 
				cursor, 
				fromColumns, 
				toViews);
		
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
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
