package com.trublo.bathroommaps;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;
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
import java.util.Set;

/**
 * Created by julianlo on 1/15/16.
 */
public class GoogleMapCategorizer<T> {
    private static final String TAG = GoogleMapCategorizer.class.getSimpleName();

    private static final ParcelableBitmapDescriptor[] DEFAULT_CATEGORY_ICON_DESCRIPTORS = new ParcelableBitmapDescriptor[] {
            ParcelableBitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
            ParcelableBitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN),
            ParcelableBitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN),
            ParcelableBitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE),
            ParcelableBitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA),
            ParcelableBitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
            ParcelableBitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW),
    };

    private GoogleMap mMap;
    private HashMap<T, CategoryInfo<T>> mCategoryInfoMap = new HashMap<>();
    private List<CategoryInfo<T>> mCategoryInfoList = new ArrayList<>();     // needed only to support ordering
    private HashMap<Marker, T> mMarkerCategoryMap = new HashMap<>();         // needed only to support removeMarker
    private int mNextIconIndex;

    public static class CategoryDescriptor<T> {
        T mId;
        ParcelableBitmapDescriptor mIconDescriptor;

        public T getId() {
            return mId;
        }

        public CategoryDescriptor<T> setId(T id) {
            mId = id;
            return this;
        }

        public CategoryDescriptor<T> setIconResource(@DrawableRes int id) {
            mIconDescriptor = ParcelableBitmapDescriptorFactory.fromResource(id);
            return this;
        }

        public ParcelableBitmapDescriptor getIconDescriptor() {
            return mIconDescriptor;
        }
    }

    public static class CategoryInfo<T> {
        T mId;
        Set<Marker> mMarkers = new HashSet<>();
        ParcelableBitmapDescriptor mIconDescriptor;
        BitmapDescriptor mCachedIcon; // cached
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

        public ParcelableBitmapDescriptor getIconDescriptor() {
            return mIconDescriptor;
        }

        public void setIconDescriptor(ParcelableBitmapDescriptor iconDescriptor) {
            mIconDescriptor = iconDescriptor;
            mCachedIcon = (iconDescriptor != null) ? iconDescriptor.get() : null;
        }

        public BitmapDescriptor getCachedIcon() {
            return mCachedIcon;
        }

        public boolean isVisible() {
            return mIsVisible;
        }

        private void setIsVisible(boolean isVisible) {
            mIsVisible = isVisible;
        }
    }

    public static abstract class ParcelableBitmapDescriptor implements Parcelable {
        public abstract BitmapDescriptor get();
    }

    public static class HueBitmapDescriptor extends ParcelableBitmapDescriptor {
        private float mHue;

        public static final Parcelable.Creator<HueBitmapDescriptor> CREATOR = new Parcelable.Creator<HueBitmapDescriptor>() {
            @Override
            public HueBitmapDescriptor createFromParcel(Parcel source) {
                HueBitmapDescriptor descriptor = new HueBitmapDescriptor(source.readFloat());
                return descriptor;
            }

            @Override
            public HueBitmapDescriptor[] newArray(int size) {
                return new HueBitmapDescriptor[size];
            }
        };

        private HueBitmapDescriptor(float hue) {
            mHue = hue;
        }

        public float getHue() {
            return mHue;
        }

        @Override
        public BitmapDescriptor get() {
            return BitmapDescriptorFactory.defaultMarker(mHue);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeFloat(mHue);
        }
    }

    public static class IconResourceBitmapDescriptor extends ParcelableBitmapDescriptor {
        private int mResId;

        public static final Parcelable.Creator<IconResourceBitmapDescriptor> CREATOR = new Parcelable.Creator<IconResourceBitmapDescriptor>() {
            @Override
            public IconResourceBitmapDescriptor createFromParcel(Parcel source) {
                IconResourceBitmapDescriptor descriptor = new IconResourceBitmapDescriptor(source.readInt());
                return descriptor;
            }

            @Override
            public IconResourceBitmapDescriptor[] newArray(int size) {
                return new IconResourceBitmapDescriptor[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(mResId);
        }

        public int getResId() {
            return mResId;
        }

        @Override
        public BitmapDescriptor get() {
            return BitmapDescriptorFactory.fromResource(mResId);
        }

        private IconResourceBitmapDescriptor(@DrawableRes int resId) {
            mResId = resId;
        }
    }

    public static class ParcelableBitmapDescriptorFactory {
        public static ParcelableBitmapDescriptor defaultMarker(float hue) {
            return new HueBitmapDescriptor(hue);
        }

        public static ParcelableBitmapDescriptor fromResource(@DrawableRes int resId) {
            return new IconResourceBitmapDescriptor(resId);
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

                // Ensure the category has an icon. We do this to handle the case where you load up
                // the app and press the category filter button before we actually fetch the bathrooms.
                categoryInfo.setIconDescriptor(descriptor.getIconDescriptor() != null ?
                    descriptor.getIconDescriptor() : retrieveIconDescriptorForNewCategory(descriptor.getId()));
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
            if (categoryInfo.getIconDescriptor() == null) {
                categoryInfo.setIconDescriptor(retrieveIconDescriptorForNewCategory(category));
            }
            options.icon(categoryInfo.getCachedIcon());
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

    public Set<Marker> showCategory(T category, boolean show) {

        CategoryInfo<T> categoryInfo = mCategoryInfoMap.get(category);
        if (categoryInfo == null) {
            Log.w(TAG, "Could not find categoryinfo for category " + category.toString());
            return null;
        }

        for (Marker marker : categoryInfo.getMarkers()) {
            marker.setVisible(show);
        }
        categoryInfo.setIsVisible(show);
        return categoryInfo.getMarkers();
    }

    private ParcelableBitmapDescriptor retrieveIconDescriptorForNewCategory(T category) {
        if (mNextIconIndex == DEFAULT_CATEGORY_ICON_DESCRIPTORS.length) {
            throw new IllegalStateException("Ran out of unique icons for map categories");
        }
        return DEFAULT_CATEGORY_ICON_DESCRIPTORS[mNextIconIndex++];
    }
}
