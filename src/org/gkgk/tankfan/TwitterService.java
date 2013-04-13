package org.gkgk.tankfan;

import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.HashtagEntity;
import twitter4j.conf.ConfigurationBuilder;


public class TwitterService extends IntentService {

    private static final String TAG = TwitterService.class.getSimpleName();

    private SQLiteDatabase db;

	public TwitterService() {
		super("org.gkgk.tankfan.TwitterService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		if (!this.isOnline()) {
			Log.d(TAG, "No network, not running service");
			return;
		}

        List<Status> timeline = this.fetch();

        /*
        for (Status status : timeline) {
            Log.d(TAG, status.getText());
        }
        */

		DBHelper dbHelper = new DBHelper(this, DBHelper.DB_NAME, null, DBHelper.DB_VERSION);
		this.db = dbHelper.getWritableDatabase();
        this.save(timeline);
        this.db.close();
    }

    /**
     * Download twitter status posts.
     */
    List<Status> fetch() {

        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setOAuthConsumerKey(fr(R.string.consumerKey));
        cb.setOAuthConsumerSecret(fr(R.string.consumerKeySecret));
        cb.setOAuthAccessToken(fr(R.string.accessToken));
        cb.setOAuthAccessTokenSecret(fr(R.string.accessTokenSecret));
        TwitterFactory factory = new TwitterFactory(cb.build());
        Twitter t = factory.getInstance();

        try {
            return t.getUserTimeline("coppertank_kits");
        }
        catch (TwitterException exc) {
            Log.e(TAG, "Error fetching twitter", exc);
            return null;
        }
    }

    /** (f)rom (r)esources: Little helper for fetchTimeline */
    String fr(int resId) {
        return getResources().getText(resId).toString();
    }

    /**
     * Save tweets to the database.
     */
    void save(List<Status> timeline) {

        db.beginTransaction();
        try {
            this.saveInner(timeline);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Really save tweets to the database.
     */
    void saveInner(List<Status> timeline) {

        db.delete(DBHelper.TWITTER_TABLE, null, null);

        for (Status st : timeline) {
            saveSingle(st);
        }
    }

    void saveSingle(Status st) {

        SimpleDateFormat sdf = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss.SSSZ",
                Locale.US);

        ContentValues vals = new ContentValues();
        vals.put("content", st.getText());
        vals.put("created", sdf.format(st.getCreatedAt()));

        String tags = "";
        for (HashtagEntity tag : st.getHashtagEntities()) {
            tags += tag.getText() +" ";
        }
        vals.put("tags", tags);

        try {
            db.insertOrThrow(DBHelper.TWITTER_TABLE, null, vals);
        }
        catch (SQLException exc) {
            Log.e(TAG, "SQLException saving " + vals +" to database.");
        }
    }

	/**
	 * Is the device connected to the network?
	 */
    boolean isOnline() {
		ConnectivityManager connMgr = (ConnectivityManager)
            this.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		return (networkInfo != null && networkInfo.isConnected());
	}

}
