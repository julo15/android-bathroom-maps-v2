package com.trublo.bathroommaps.bathroommaps;

import android.net.Uri;

import com.google.common.collect.ImmutableList;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.trublo.bathroommaps.R;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
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

    public static final ImmutableList<String> CATEGORIES = new ImmutableList.Builder<String>()
            .add("Public")
            .add("Coffee Shop")
            .add("Book Store")
            .add("Hotel")
            .add("Fast Food")
            .add("Other")
            .build();
    // Hack - the icons below correspond to the categories above. These should be in a common object instead.
    public static final ImmutableList<Integer> CATEGORY_ICONS = new ImmutableList.Builder<Integer>()
            .add(R.drawable.ic_poo_40x40_blue)
            .add(R.drawable.ic_poo_40x40_yellow)
            .add(R.drawable.ic_poo_40x40_purple)
            .add(R.drawable.ic_poo_40x40_teal)
            .add(R.drawable.ic_poo_40x40_orange)
            .add(R.drawable.ic_poo_40x40_red)
            .build();
    private static final int OTHER_CATEGORY_INDEX = 5; // If we receive any bathroom with a category not in our list,
                                                         // we'll make it show up in the Other category.

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
        bathroom.setCategory(sanitizeCategoryName(bathroomJsonObject.getString("category")));
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

        String dateText = reviewJsonObject.getString("date");
        DateTimeFormatter formatter = ISODateTimeFormat.dateTime();
        DateTime dateTime = formatter.parseDateTime(dateText);
        review.setDateCreated(dateTime);
        return review;
    }

    public Bathroom submitReview(String bathroomId, int rating, String text) throws IOException, JSONException {
        String url = ENDPOINT
                .buildUpon()
                .appendPath("addreview")
                .appendQueryParameter("id", bathroomId)
                .appendQueryParameter("rating", String.valueOf(rating))
                .appendQueryParameter("text", text)
                .build()
                .toString();
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = mHttpClient.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("HTTP failure response in submitReview: " + response.code());
        }

        JSONObject responseJsonObject = new JSONObject(response.body().string());
        throwIfResponseFailed(responseJsonObject);

        return parseBathroom(responseJsonObject.getJSONObject("bathroom"));
    }

    public void submitBathroom(String name, String category, double latitude, double longitude) throws IOException, JSONException {
        String url = ENDPOINT
                .buildUpon()
                .appendPath("addbathroom")
                .appendQueryParameter("name", name)
                .appendQueryParameter("lat", String.valueOf(latitude))
                .appendQueryParameter("lon", String.valueOf(longitude))
                .appendQueryParameter("cat", category)
                .build()
                .toString();
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = mHttpClient.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("HTTP failure response in submitBathroom: " + response.code());
        }

        JSONObject responseJsonObject = new JSONObject(response.body().string());
        throwIfResponseFailed(responseJsonObject);
    }

    private String sanitizeCategoryName(String categoryFromSomewhere) {
        // We ensure the casing is correct, and that all unknown categories get put into "Other".
        for (String category : CATEGORIES) {
            if (category.equalsIgnoreCase(categoryFromSomewhere)) {
                return category;
            }
        }
        return CATEGORIES.get(OTHER_CATEGORY_INDEX);
    }

    private void throwIfResponseFailed(JSONObject responseJsonObject) throws IOException, JSONException {
        if (responseJsonObject.getJSONObject("result").getInt("ok") != SERVER_RESPONSE_OK) {
            throw new IOException("Result not ok");
        }
    }
}
