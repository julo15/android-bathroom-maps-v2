package com.trublo.bathroommaps.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.trublo.bathroommaps.bathroommaps.Bathroom;
import com.trublo.bathroommaps.fragments.ReviewListFragment;

/**
 * Created by julianlo on 1/11/16.
 */
public class ReviewListActivity extends SingleFragmentActivity {

    private static final String EXTRA_BATHROOM = "com.trublo.bathroommaps.bathroom";

    public static Intent newIntent(Context context, Bathroom bathroom) {
        Intent intent = new Intent(context, ReviewListActivity.class);
        intent.putExtra(EXTRA_BATHROOM, bathroom);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        Bathroom bathroom = getIntent().getParcelableExtra(EXTRA_BATHROOM);
        return ReviewListFragment.newInstance(bathroom);
    }
}
