package com.trublo.bathroommaps;

import android.app.Activity;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;
import com.trublo.bathroommaps.bathroommaps.Bathroom;

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
}
