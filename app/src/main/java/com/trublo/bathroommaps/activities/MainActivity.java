package com.trublo.bathroommaps.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.trublo.bathroommaps.R;
import com.trublo.bathroommaps.Util;
import com.trublo.bathroommaps.bathroommaps.Bathroom;
import com.trublo.bathroommaps.fragments.BathroomMapFragment;
import com.trublo.bathroommaps.googlemaps.GoogleMaps;

public class MainActivity extends SingleFragmentActivity implements BathroomMapFragment.Callbacks {
    private static final String TAG = "MainActivity";

    private static final String STATE_SELECTED_BATHROOM = "selected_bathroom";

    private View mProgressView;
    private View mToolbarRootView;
    private TextView mToolbarNameTextView;
    private TextView mToolbarTimeTextView;
    private View mToolbarDirectionsButton;
    private Bathroom mSelectedBathroom;
    private FetchWalkingTimeTask mFetchWalkingTimeTask;

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
        mProgressView = Util.findView(this, R.id.activity_main_progress_bar);
        mToolbarRootView = Util.findView(this, R.id.bathroom_toolbar_root_view);
        mToolbarNameTextView = Util.findView(this, R.id.bathroom_toolbar_name_text_view);
        mToolbarTimeTextView = Util.findView(this, R.id.bathroom_toolbar_time_text_view);

        mToolbarDirectionsButton = Util.findView(this, R.id.bathroom_toolbar_directions_button);
        mToolbarDirectionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("google.navigation:mode=w&z=15&" +
                    "q=" + Util.coordsToString(mSelectedBathroom.getLatitude(), mSelectedBathroom.getLongitude()));
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setPackage("com.google.android.apps.maps");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, R.string.cant_launch_maps, Toast.LENGTH_LONG)
                            .show();
                }
            }
        });

        if ((savedInstanceState != null) && savedInstanceState.containsKey(STATE_SELECTED_BATHROOM)) {
            mSelectedBathroom = Util.cast(savedInstanceState.getParcelable(STATE_SELECTED_BATHROOM));
            updateToolbar(mSelectedBathroom);
            Util.showView(mToolbarRootView, true);
        }
    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mSelectedBathroom != null) {
            outState.putParcelable(STATE_SELECTED_BATHROOM, mSelectedBathroom);
        }
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
    public void onBathroomMarkerSelected(Bathroom bathroom) {
        mSelectedBathroom = bathroom;

        if (bathroom != null) {
            Log.v(TAG, "Received bathroom clicked: " + bathroom.getName());
            updateToolbar(bathroom);
        } else {
            Log.v(TAG, "Received bathroom unclicked");
        }

        Util.showView(mToolbarRootView, (bathroom != null));
    }

    private void updateToolbar(Bathroom bathroom) {
        mToolbarNameTextView.setText(bathroom.getName());
        mToolbarTimeTextView.setText(R.string.fetch_walking_time_progress_text);

        if (mFetchWalkingTimeTask != null) {
            mFetchWalkingTimeTask.cancel(false);
        }

        mFetchWalkingTimeTask = new FetchWalkingTimeTask();
        double latitude = 47.621782;
        double longitude = -122.331998;
        mFetchWalkingTimeTask.execute(latitude, longitude, bathroom.getLatitude(), bathroom.getLongitude());
    }

    private class FetchWalkingTimeTask extends AsyncTask<Double,Void,String> {
        private Exception mException;

        @Override
        protected String doInBackground(Double... params) {
            try {
                return new GoogleMaps().fetchWalkingTime(
                        new double[] { params[0], params[1] },
                        new double[] { params[2], params[3] });
            } catch (Exception e) {
                mException = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String text) {
            if (mException != null) {
                Log.w(TAG, "HTTP failure fetching walking time", mException);
                String toastText = getResources().getString(R.string.fetch_walking_time_ioe_failure);
                Toast.makeText(MainActivity.this, toastText, Toast.LENGTH_SHORT)
                        .show();
                mToolbarTimeTextView.setText(R.string.unknown_walking_time);
            }
            else {
                mToolbarTimeTextView.setText(text);
            }
        }
    }
}
