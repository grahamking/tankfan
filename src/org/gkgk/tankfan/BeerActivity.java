package org.gkgk.tankfan;

import android.content.Context;
import android.app.ListActivity;
import android.os.Bundle;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.util.Log;

public class BeerActivity extends ListActivity {

    private static final String TAG = BeerActivity.class.getSimpleName();

    private UpdateReceiver receiver;

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
		BeerAdapter adapter = new BeerAdapter(this);
        setListAdapter(adapter);
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
}
