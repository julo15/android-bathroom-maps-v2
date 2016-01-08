package com.trublo.bathroommaps.googlemaps;

import android.net.Uri;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.trublo.bathroommaps.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by julianlo on 1/7/16.
 */
public class GoogleMaps {
    private static final String TAG = GoogleMaps.class.getSimpleName();

    private static final String API_KEY = "AIzaSyBCgkWIlFfCl2t-0bLvU_58A-Z70lVszw0";
    private static final Uri ENDPOINT = Uri.parse("https://maps.googleapis.com/maps/api");

    private OkHttpClient mHttpClient = new OkHttpClient();

    public String fetchWalkingTime(double[] from, double[] to) throws IOException, JSONException {
        String url = ENDPOINT
                .buildUpon()
                .appendPath("directions")
                .appendPath("json")
                .appendQueryParameter("origin", Util.coordsToString(from[0], from[1]))
                .appendQueryParameter("destination", Util.coordsToString(to[0], to[1]))
                .appendQueryParameter("mode", "walking")
                .appendQueryParameter("key", API_KEY)
                .build()
                .toString();
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = mHttpClient.newCall(request).execute();

        if (!response.isSuccessful()) {
            throw new IOException("HTTP failure response in fetchWalkingTime: " + response.code());
        }

        JSONObject responseJsonObject = new JSONObject(response.body().string());
        String timeText = responseJsonObject
                .getJSONArray("routes")
                .getJSONObject(0)
                .getJSONArray("legs")
                .getJSONObject(0)
                .getJSONObject("duration")
                .getString("text");
        return timeText;
    }
}
