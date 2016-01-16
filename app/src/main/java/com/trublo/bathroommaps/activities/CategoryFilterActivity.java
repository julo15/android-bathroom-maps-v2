package com.trublo.bathroommaps.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.trublo.bathroommaps.fragments.CategoryFilterFragment;

import java.util.Map;

/**
 * Created by julianlo on 1/15/16.
 */
public class CategoryFilterActivity extends SingleFragmentActivity {

    private static final String EXTRA_CATEGORY_NAMES = "com.julo.android.bathroommaps.category_names";
    private static final String EXTRA_CATEGORY_VISIBILITIES = "com.julo.android.bathroommaps.category_visibilities";

    public static Intent newIntent(Context context, Map<String, Boolean> categoryVisibilityMap) {
        Intent intent = new Intent(context, CategoryFilterActivity.class);
        int size = categoryVisibilityMap.size();

        String[] names = categoryVisibilityMap.keySet().toArray(new String[size]);
        intent.putExtra(EXTRA_CATEGORY_NAMES, names);

        Boolean[] visibilities = categoryVisibilityMap.values().toArray(new Boolean[size]);
        intent.putExtra(EXTRA_CATEGORY_VISIBILITIES, visibilities);

        return intent;
    }

    @Override
    protected Fragment createFragment() {
        String[] names = getIntent().getStringArrayExtra(EXTRA_CATEGORY_NAMES);
        Boolean[] visibilities = (Boolean[])getIntent().getSerializableExtra(EXTRA_CATEGORY_VISIBILITIES);

        return CategoryFilterFragment.newInstance(names, visibilities);
    }
}
