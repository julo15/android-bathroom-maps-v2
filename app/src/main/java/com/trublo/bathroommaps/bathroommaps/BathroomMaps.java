package com.trublo.bathroommaps.bathroommaps;

import android.net.Uri;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by julianlo on 1/6/16.
 */
public class BathroomMaps {
    private static final String TAG = "BathroomMaps";

    private static final Uri ENDPOINT = Uri.parse("http://ec2-54-200-75-151.us-west-2.compute.amazonaws.com:8080");

    private static final int SERVER_RESPONSE_OK = 1;

    private OkHttpClient mHttpClient = new OkHttpClient();

    public List<Bathroom> fetchBathrooms(double latitude, double longitude, int distance)
        throws IOException, JSONException {
        String url = ENDPOINT
                .buildUpon()
                .appendPath("bathrooms")
                .appendQueryParameter("lat", String.valueOf(latitude))
                .appendQueryParameter("lon", String.valueOf(longitude))
                .appendQueryParameter("distance", String.valueOf(distance))
                .build()
                .toString();
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = mHttpClient.newCall(request).execute();

        if (!response.isSuccessful()) {
            throw new IOException("HTTP failure response in fetchBathrooms: " + response.code());
        }

        JSONObject responseJsonObject = new JSONObject(response.body().string());
        throwIfResponseFailed(responseJsonObject);

        JSONArray bathroomsJsonArray = responseJsonObject.getJSONArray("bathrooms");
        List<Bathroom> bathrooms = new ArrayList<>(bathroomsJsonArray.length());
        for (int i = 0; i < bathroomsJsonArray.length(); i++) {
            Bathroom bathroom = parseBathroom(bathroomsJsonArray.getJSONObject(i));
            bathrooms.add(bathroom);
        }
        return bathrooms;
    }

    private Bathroom parseBathroom(JSONObject bathroomJsonObject) throws JSONException {
        Bathroom bathroom = new Bathroom();
        bathroom.setId(bathroomJsonObject.getString("_id"));
        bathroom.setCategory(bathroomJsonObject.getString("category"));
        bathroom.setName(bathroomJsonObject.getString("name"));
        bathroom.setIsPending(bathroomJsonObject.getBoolean("pending"));
        bathroom.setLatitude(bathroomJsonObject.getDouble("lat"));
        bathroom.setLongitude(bathroomJsonObject.getDouble("lon"));

        JSONObject ratingJsonObject = bathroomJsonObject.getJSONObject("rating");
        bathroom.setAverageRating((float)ratingJsonObject.getDouble("avg"));
        bathroom.setReviewCount(ratingJsonObject.getInt("count"));

        List<Review> reviews = parseReviewsArray(ratingJsonObject.getJSONArray("reviews"));
        bathroom.setReviews(reviews);
        return bathroom;
    }

    private List<Review> parseReviewsArray(JSONArray reviewsJsonArray) throws JSONException {
        List<Review> reviews = new ArrayList<>(reviewsJsonArray.length());
        for (int i = 0; i < reviewsJsonArray.length(); i++) {
            reviews.add(parseReview(reviewsJsonArray.getJSONObject(i)));
        }
        return reviews;
    }

    private Review parseReview(JSONObject reviewJsonObject) throws JSONException {
        Review review = new Review();
        review.setRating(reviewJsonObject.getInt("rating"));
        review.setText(reviewJsonObject.getString("text"));
        return review;
    }

    private void throwIfResponseFailed(JSONObject responseJsonObject) throws IOException, JSONException {
        if (responseJsonObject.getJSONObject("result").getInt("ok") != SERVER_RESPONSE_OK) {
            throw new IOException("Result not ok");
        }
    }
}
