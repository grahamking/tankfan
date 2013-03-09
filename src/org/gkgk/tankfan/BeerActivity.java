package org.gkgk.tankfan;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import android.content.Context;
import android.app.ListActivity;
import android.os.Bundle;
import android.os.AsyncTask;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.util.Log;
import android.graphics.Bitmap;

public class BeerActivity extends ListActivity {

    private static final String TAG = BeerActivity.class.getSimpleName();

    private UpdateReceiver receiver;
    List<Map<String, String>> data = new ArrayList<Map<String, String>>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.receiver = new UpdateReceiver();
    }

    @Override
    public void onStart() {
        super.onStart();

        this.refreshAdapter();
    }

    private void refreshAdapter() {
        new BeerActivity.DBTask().execute();
    }

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

    /* inner classes */

    class UpdateReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "BeerActivity.UpdateReceiver.onReceive");
            BeerActivity.this.refreshAdapter();
        }
    }

    class Holder {
        List<Map<String, String>> data = new ArrayList<Map<String, String>>();
        Map<String, Bitmap> images = new HashMap<String, Bitmap>();
    }

    class DBTask extends AsyncTask<Void, Void, Holder> {

        protected Holder doInBackground(Void...params) {
            AdapterHelper helper = new AdapterHelper(
                    BeerActivity.this,
                    DBHelper.BEERS_TABLE);

            List<Map<String, String>> data = helper.loadData();
            Map<String, Bitmap> images = helper.loadImages();

            Holder result = new Holder();
            result.data = data;
            result.images = images;
            return result;
        }

        protected void onPostExecute(Holder holder) {
            BeerAdapter adapter = new BeerAdapter(
                    BeerActivity.this,
                    holder.data,
                    holder.images);
            BeerActivity.this.setListAdapter(adapter);
        }
    }
}
