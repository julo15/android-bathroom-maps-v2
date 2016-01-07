package com.trublo.bathroommaps.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.trublo.bathroommaps.R;
import com.trublo.bathroommaps.Util;
import com.trublo.bathroommaps.bathroommaps.Bathroom;
import com.trublo.bathroommaps.fragments.BathroomMapFragment;

public class MainActivity extends SingleFragmentActivity implements BathroomMapFragment.Callbacks {
    private static final String TAG = "MainActivity";

    private View mProgressView;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_main;
    }

    @Override
    protected Fragment createFragment() {
        return BathroomMapFragment.newInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProgressView = findViewById(R.id.activity_main_progress_bar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onFetchingBathrooms(boolean fetching) {
        Util.showView(mProgressView, fetching);
    }

    @Override
    public void onBathroomMarkerSelected(Bathroom bathroom, boolean selected) {
        Log.v(TAG, "Received bathroom clicked: " + bathroom.getName());
    }
}
