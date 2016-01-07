package com.trublo.bathroommaps.bathroommaps;

/**
 * Created by julianlo on 1/6/16.
 */
public class Bathroom {

    private String mId;
    private String mName;
    private String mCategory;
    private boolean mIsPending;
    private double mLatitude;
    private double mLongitude;
    private double mAverageRating;
    private double mReviewCount;

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getCategory() {
        return mCategory;
    }

    public void setCategory(String category) {
        mCategory = category;
    }

    public boolean isPending() {
        return mIsPending;
    }

    public void setIsPending(boolean isPending) {
        mIsPending = isPending;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double latitude) {
        mLatitude = latitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }

    public double getAverageRating() {
        return mAverageRating;
    }

    public void setAverageRating(double averageRating) {
        mAverageRating = averageRating;
    }

    public double getReviewCount() {
        return mReviewCount;
    }

    public void setReviewCount(double reviewCount) {
        mReviewCount = reviewCount;
    }
}
