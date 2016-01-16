package com.trublo.bathroommaps.activities;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.trublo.bathroommaps.R;
import com.trublo.bathroommaps.Util;
import com.trublo.bathroommaps.bathroommaps.Bathroom;
import com.trublo.bathroommaps.fragments.BathroomMapFragment;
import com.trublo.bathroommaps.fragments.ReviewListFragment;
import com.trublo.bathroommaps.googlemaps.GoogleMaps;

public class MainActivity extends SingleFragmentActivity implements BathroomMapFragment.Callbacks {
    private static final String TAG = "MainActivity";

    private static final String STATE_SELECTED_BATHROOM = "selected_bathroom";
    private static final int REQUEST_SHOW_REVIEWS = 1;
    private static final int REQUEST_FILTER_CATEGORIES = 2;

    private View mProgressView;
    private View mLocateButton;
    private View mFilterButton;
    private View mToolbarRootView;
    private TextView mToolbarNameTextView;
    private TextView mToolbarTimeTextView;
    private ProgressBar mToolbarTimeProgressBar;
    private View mToolbarDirectionsButton;
    private RatingBar mToolbarRatingBar;
    private TextView mToolbarReviewCountTextView;
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

        mLocateButton = Util.findView(this, R.id.activity_main_locate_button);
        mLocateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getMapFragment().centerMap();
            }
        });

        mFilterButton = Util.findView(this, R.id.activity_main_filter_button);
        mFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = CategoryFilterActivity.newIntent(MainActivity.this, getMapFragment().getCategoryVisibilityMap());
                startActivityForResult(intent, REQUEST_FILTER_CATEGORIES);
            }
        });

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

        mToolbarTimeProgressBar = Util.findView(this, R.id.bathroom_toolbar_time_progress_bar);
        mToolbarRatingBar = Util.findView(this, R.id.bathroom_toolbar_rating_bar);
        mToolbarReviewCountTextView = Util.findView(this, R.id.bathroom_toolbar_review_count);

        View.OnClickListener launchReviewListClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = ReviewListActivity.newIntent(MainActivity.this, mSelectedBathroom);
                startActivityForResult(intent, REQUEST_SHOW_REVIEWS);
            }
        };

        mToolbarReviewCountTextView.setOnClickListener(launchReviewListClickListener);

        if ((savedInstanceState != null) && savedInstanceState.containsKey(STATE_SELECTED_BATHROOM)) {
            mSelectedBathroom = Util.cast(savedInstanceState.getParcelable(STATE_SELECTED_BATHROOM));

            // If we're restoring from instance state, then we rely on the textview already containing
            // the walking time (via freezesText), so don't call into GoogleMaps again to find out.
            // This avoids a problem where BathroomMapFragment.getCurrentLocation returns null since
            // the GoogleApiClient is not connected yet.
            updateToolbar(mSelectedBathroom, false);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_SHOW_REVIEWS) {
            Bathroom updatedBathroom = data.getParcelableExtra(ReviewListFragment.EXTRA_UPDATED_BATHROOM);
            getMapFragment().notifyBathroomUpdated(updatedBathroom);
            mSelectedBathroom = updatedBathroom;
            updateToolbar(mSelectedBathroom, false);
        } else if (requestCode == REQUEST_FILTER_CATEGORIES) {

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
            updateToolbar(bathroom, true);
        } else {
            Log.v(TAG, "Received bathroom unclicked");
        }

        Util.showView(mToolbarRootView, (bathroom != null));
    }

    private BathroomMapFragment getMapFragment() {
        return (BathroomMapFragment)getFragment();
    }

    private void updateToolbar(Bathroom bathroom, boolean updateTime) {
        mToolbarNameTextView.setText(bathroom.getName());

        int reviews = bathroom.getReviewCount();
        mToolbarRatingBar.setRating(bathroom.getAverageRating());
        Util.showView(mToolbarRatingBar, reviews > 0);
        mToolbarReviewCountTextView.setText(getResources().getQuantityString(R.plurals.review_count, reviews, reviews));

        if (mFetchWalkingTimeTask != null) {
            mFetchWalkingTimeTask.cancel(false);
        }

        if (updateTime) {
            mFetchWalkingTimeTask = new FetchWalkingTimeTask();
            Location currentLocation = ((BathroomMapFragment) getFragment()).getCurrentLocation();
            if (currentLocation == null) {
                Toast.makeText(this, R.string.current_location_unavailable, Toast.LENGTH_SHORT)
                        .show();
                mToolbarTimeTextView.setText(R.string.unknown_walking_time);
                return;
            }

            mFetchWalkingTimeTask.execute(currentLocation.getLatitude(), currentLocation.getLongitude(), bathroom.getLatitude(), bathroom.getLongitude());
        }
    }

    private class FetchWalkingTimeTask extends AsyncTask<Double,Void,String> {
        private Exception mException;

        @Override
        protected void onPreExecute() {
            Util.showView(mToolbarTimeTextView, false);
            Util.showView(mToolbarTimeProgressBar, true);
        }

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

            Util.showView(mToolbarTimeTextView, true);
            Util.showView(mToolbarTimeProgressBar, false);
        }
    }
}
