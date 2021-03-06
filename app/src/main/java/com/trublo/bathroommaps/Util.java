package com.trublo.bathroommaps;

import android.app.Activity;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.maps.model.LatLng;
import com.trublo.bathroommaps.bathroommaps.Bathroom;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by julianlo on 1/6/16.
 */
public class Util {

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object o) {
        return (T)o;
    }

    @SuppressWarnings("unchecked")
    public static <T> T findView(View view, int id) {
        return (T)view.findViewById(id);
    }

    @SuppressWarnings("unchecked")
    public static <T> T findView(Activity activity, int id) {
        return (T)activity.findViewById(id);
    }

    public static void showView(View view, boolean show) {
        view.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public static LatLng createBathroomLatLng(Bathroom bathroom) {
        return new LatLng(bathroom.getLatitude(), bathroom.getLongitude());
    }

    public static String coordsToString(double latitude, double longitude) {
        return String.format("%f,%f", latitude, longitude);
    }

    public static <K, V> void updateValueInMap(Map<K, V> map, V updatedValue, Comparator<V> comparator) {
        for (Iterator<Map.Entry<K, V>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry<K, V> entry = iterator.next();
            V value = entry.getValue();
            if (comparator.compare(value, updatedValue) == 0) {
                iterator.remove();
                map.put(entry.getKey(), updatedValue);
                break;
            }
        }
    }

    public static @ColorInt int bathroomHueToColor(float hue) {
        return Color.HSVToColor(new float[]{hue, 1f, 1f});
    }

    public static void setBathroomIcon(ImageView view, GoogleMapCategorizer.ParcelableBitmapDescriptor iconDescriptor) {
        int color = Color.TRANSPARENT;
        int resId = android.R.color.transparent;
        if (iconDescriptor instanceof GoogleMapCategorizer.HueBitmapDescriptor) {
            GoogleMapCategorizer.HueBitmapDescriptor hueBitmapDescriptor = Util.cast(iconDescriptor);
            color = Util.bathroomHueToColor(hueBitmapDescriptor.getHue());
        } else if (iconDescriptor instanceof GoogleMapCategorizer.IconResourceBitmapDescriptor) {
            GoogleMapCategorizer.IconResourceBitmapDescriptor iconBitmapDescriptor = Util.cast(iconDescriptor);
            resId = iconBitmapDescriptor.getResId();
        }
        view.setImageResource(resId);
        view.setBackgroundColor(color);
    }
}
