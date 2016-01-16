package com.trublo.bathroommaps;

import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.HashSet;
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
    private HashMap<T, Set<Marker>> mCategoryMap = new HashMap<>();          // primary category container
    private HashMap<Marker, T> mMarkerCategoryMap = new HashMap<>();
    private HashMap<T, BitmapDescriptor> mCategoryIconMap = new HashMap<>(); // populated on-demand
    private HashMap<T, Boolean> mCategoryVisibilityMap = new HashMap<>();    // populated on category creation
    private int mNextIconIndex;

    public GoogleMapCategorizer(GoogleMap map) {
        mMap = map;
    }

    public GoogleMap getMap() {
        return mMap;
    }

    public Marker addMarker(MarkerOptions options, T category) {
        // Ensure the category has its set of markers initialized
        // And ensure the category has its visibility initialized
        if (!mCategoryMap.containsKey(category)) {
            mCategoryMap.put(category, new HashSet<Marker>());
            mCategoryVisibilityMap.put(category, Boolean.TRUE);
        }

        // Set the marker icon
        if (options.getIcon() == null) {
            // Ensure the category has an icon, then set it on the marker
            if (!mCategoryIconMap.containsKey(category)) {
                mCategoryIconMap.put(category, retrieveIconForNewCategory(category));
            }
            options.icon(mCategoryIconMap.get(category));
        } else {
            Log.i(TAG, "Icon already set on new marker. Will use that icon instead of category icon.");
        }

        // Set the marker visibility
        options.visible(mCategoryVisibilityMap.get(category));

        // Add the marker
        Marker marker = mMap.addMarker(options);
        mMarkerCategoryMap.put(marker, category);
        Set<Marker> markers = mCategoryMap.get(category);
        markers.add(marker);

        return marker;
    }

    public void removeMarker(Marker marker) {
        marker.remove();
        if (!mMarkerCategoryMap.containsKey(marker)) {
            // This is expected if the marker is uncategorized (null category)
            Log.i(TAG, "Could not find marker's category in remove");
            return;
        }

        T category = mMarkerCategoryMap.get(marker);
        Set<Marker> markers = mCategoryMap.get(category);
        if (markers == null) {
            throw new IllegalStateException("Could not find markers for category " + category + " in remove");
        }

        if (!markers.remove(marker)) {
            Log.w(TAG, "Marker was not present in its category set in remove");
        }
    }

    public Set<T> getCategories() {
        return mCategoryMap.keySet();
    }

    public Map<T, Boolean> getCategoryVisibilityMap() {
        return mCategoryVisibilityMap;
    }

    public void showCategory(T category, boolean show) {
        Set<Marker> markers = mCategoryMap.get(category);
        if (markers == null) {
            Log.w(TAG, "Could not find map category " + category.toString());
            return;
        }

        Boolean visible = mCategoryVisibilityMap.get(category);
        if (visible != show) {
            for (Marker marker : markers) {
                marker.setVisible(show);
            }
        }
    }

    private BitmapDescriptor retrieveIconForNewCategory(T category) {
        if (mNextIconIndex == DEFAULT_CATEGORY_ICONS.length) {
            throw new IllegalStateException("Ran out of unique icons for map categories");
        }
        return DEFAULT_CATEGORY_ICONS[mNextIconIndex++];
    }
}
