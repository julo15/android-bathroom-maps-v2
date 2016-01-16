package com.trublo.bathroommaps;

import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by julianlo on 1/15/16.
 */
public class GoogleMapCategorizer<T> {
    private static final String TAG = GoogleMapCategorizer.class.getSimpleName();

    private static final BitmapDescriptor[] DEFAULT_CATEGORY_ICONS = new BitmapDescriptor[] {
            BitmapDescriptorFactory.defaultMarker(),
            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE),
            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN),
            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN),
    };

    private GoogleMap mMap;
    private HashMap<T, CategoryInfo<T>> mCategoryInfoMap = new HashMap<>();
    private List<CategoryInfo<T>> mCategoryInfoList = new ArrayList<>();     // needed only to support ordering
    private HashMap<Marker, T> mMarkerCategoryMap = new HashMap<>();         // needed only to support removeMarker
    private int mNextIconIndex;

    public static class CategoryDescriptor<T> {
        T mId;
        BitmapDescriptor mIcon;

        public T getId() {
            return mId;
        }

        public CategoryDescriptor<T> setId(T id) {
            mId = id;
            return this;
        }

        public BitmapDescriptor getIcon() {
            return mIcon;
        }

        public CategoryDescriptor<T> setIcon(BitmapDescriptor icon) {
            mIcon = icon;
            return this;
        }
    }

    public static class CategoryInfo<T> {
        T mId;
        Set<Marker> mMarkers = new HashSet<>();
        BitmapDescriptor mIcon;
        boolean mIsVisible = true;

        private CategoryInfo(T id) {
            mId = id;
        }

        public T getId() {
            return mId;
        }

        private Set<Marker> getMarkers() {
            return mMarkers;
        }

        public BitmapDescriptor getIcon() {
            return mIcon;
        }

        private void setIcon(BitmapDescriptor icon) {
            mIcon = icon;
        }

        public boolean isVisible() {
            return mIsVisible;
        }

        private void setIsVisible(boolean isVisible) {
            mIsVisible = isVisible;
        }
    }

    public GoogleMapCategorizer(GoogleMap map) {
        this(map, null);
    }

    public GoogleMapCategorizer(GoogleMap map, CategoryDescriptor<T>[] categoryDescriptors) {
        mMap = map;

        if (categoryDescriptors != null) {
            for (CategoryDescriptor<T> descriptor : categoryDescriptors) {
                CategoryInfo<T> categoryInfo = new CategoryInfo<>(descriptor.getId());
                categoryInfo.setIcon(descriptor.getIcon());
                addCategory(categoryInfo);
            }
        }
    }

    public GoogleMap getMap() {
        return mMap;
    }

    private void addCategory(CategoryInfo<T> categoryInfo) {
        mCategoryInfoMap.put(categoryInfo.getId(), categoryInfo);
        mCategoryInfoList.add(categoryInfo);
    }

    public Marker addMarker(MarkerOptions options, T category) {
        // Ensure the category has its set of markers initialized
        // And ensure the category has its visibility initialized
        if (!mCategoryInfoMap.containsKey(category)) {
            addCategory(new CategoryInfo<T>(category));
        }

        CategoryInfo<T> categoryInfo = mCategoryInfoMap.get(category);

        // Set the marker icon
        if (options.getIcon() == null) {
            // Ensure the category has an icon, then set it on the marker
            if (categoryInfo.getIcon() == null) {
                categoryInfo.setIcon(retrieveIconForNewCategory(category));
            }
            options.icon(categoryInfo.getIcon());
        } else {
            Log.i(TAG, "Icon already set on new marker. Will use that icon instead of category icon.");
        }

        // Set the marker visibility
        options.visible(categoryInfo.isVisible());

        // Add the marker
        Marker marker = mMap.addMarker(options);
        mMarkerCategoryMap.put(marker, category);
        categoryInfo.getMarkers().add(marker);

        return marker;
    }

    public void removeMarker(Marker marker) {
        marker.remove();
        if (!mMarkerCategoryMap.containsKey(marker)) {
            Log.w(TAG, "Could not find marker's category in remove");
            return;
        }

        T category = mMarkerCategoryMap.get(marker);
        CategoryInfo<T> categoryInfo = mCategoryInfoMap.get(category);
        if (categoryInfo == null) {
            throw new IllegalStateException("Could not find category info for category " + category + " in remove");
        }

        if (!categoryInfo.getMarkers().remove(marker)) {
            Log.w(TAG, "Marker was not present in its category set in remove");
        }
    }

    public List<CategoryInfo<T>> getCategories() {
        return mCategoryInfoList;
    }

    public void showCategory(T category, boolean show) {
        CategoryInfo<T> categoryInfo = mCategoryInfoMap.get(category);
        if (categoryInfo == null) {
            Log.w(TAG, "Could not find categoryinfo for category " + category.toString());
            return;
        }

        boolean visible = categoryInfo.isVisible();
        if (visible != show) {
            for (Marker marker : categoryInfo.getMarkers()) {
                marker.setVisible(show);
            }
            categoryInfo.setIsVisible(show);
        }
    }

    private BitmapDescriptor retrieveIconForNewCategory(T category) {
        if (mNextIconIndex == DEFAULT_CATEGORY_ICONS.length) {
            throw new IllegalStateException("Ran out of unique icons for map categories");
        }
        return DEFAULT_CATEGORY_ICONS[mNextIconIndex++];
    }
}
