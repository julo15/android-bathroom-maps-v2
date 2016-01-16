package com.trublo.bathroommaps.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v4.app.Fragment;

import com.trublo.bathroommaps.GoogleMapCategorizer;
import com.trublo.bathroommaps.Util;
import com.trublo.bathroommaps.fragments.CategoryFilterFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by julianlo on 1/15/16.
 */
public class CategoryFilterActivity extends SingleFragmentActivity {

    private static final String EXTRA_CATEGORY_FILTER_ITEMS = "com.trublo.bathroommaps.category_filter_items";

    public static Intent newIntent(Context context, List<GoogleMapCategorizer.CategoryInfo<String>> categories) {
        Intent intent = new Intent(context, CategoryFilterActivity.class);

        int size = categories.size();
        ArrayList<CategoryFilterFragment.CategoryFilterItem> items = new ArrayList<>(size);
        for (GoogleMapCategorizer.CategoryInfo<String> categoryInfo : categories) {
            CategoryFilterFragment.CategoryFilterItem item = new CategoryFilterFragment.CategoryFilterItem();
            item.setCategoryId(categoryInfo.getId());
            item.setIsVisible(categoryInfo.isVisible());
            items.add(item);
        }

        intent.putExtra(EXTRA_CATEGORY_FILTER_ITEMS, items);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        ArrayList<Parcelable> parcelables = getIntent()
                .getParcelableArrayListExtra(EXTRA_CATEGORY_FILTER_ITEMS);
        ArrayList<CategoryFilterFragment.CategoryFilterItem> categoryFilterItems = Util.cast(parcelables);

        return CategoryFilterFragment.newInstance(categoryFilterItems);
    }
}
