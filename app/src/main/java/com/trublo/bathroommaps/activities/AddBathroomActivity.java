package com.trublo.bathroommaps.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.trublo.bathroommaps.R;
import com.trublo.bathroommaps.bathroommaps.Bathroom;
import com.trublo.bathroommaps.fragments.BathroomMapFragment;

/**
 * Created by julianlo on 1/17/16.
 */
public class AddBathroomActivity extends SingleFragmentActivity implements BathroomMapFragment.Callbacks {

    private enum State {

    }

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, AddBathroomActivity.class);
        return intent;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_add_bathroom;
    }

    @Override
    protected Fragment createFragment() {
        return BathroomMapFragment.newInstance();
    }

    @Override
    public void onFetchingBathrooms(boolean fetching) {

    }

    @Override
    public void onBathroomMarkerSelected(Bathroom bathroom) {

    }
}
