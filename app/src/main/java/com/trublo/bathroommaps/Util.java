package com.trublo.bathroommaps;

import android.app.Activity;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;
import com.trublo.bathroommaps.bathroommaps.Bathroom;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by julianlo on 1/6/16.
 */
public class Util {

    public static <T> T cast(Object o) {
        return (T)o;
    }

    public static <T> T findView(View view, int id) {
        return (T)view.findViewById(id);
    }

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

    public static <K, V> Map<K, V> createMapFromArrays(K[] keys, V[] values) {
        if (keys.length != values.length) {
            throw new IllegalArgumentException("Key and value arrays are not the same length");
        }
        Map<K, V> map = new HashMap<>(keys.length);
        for (int i = 0; i < keys.length; i++) {
            map.put(keys[i], values[i]);
        }
        return map;
    }
}
