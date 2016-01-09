package com.trublo.bathroommaps.bathroommaps;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;
import java.util.List;

/**
 * Created by julianlo on 1/6/16.
 */
public class Bathroom implements Parcelable {

    private String mId;
    private String mName;
    private String mCategory;
    private boolean mIsPending;
    private double mLatitude;
    private double mLongitude;
    private float mAverageRating;
    private int mReviewCount;
    private List<Review> mReviews;

    public static final Parcelable.Creator<Bathroom> CREATOR = new Parcelable.Creator<Bathroom>() {
        @Override
        public Bathroom createFromParcel(Parcel source) {
            Bathroom bathroom = new Bathroom();

            bathroom.mId = source.readString();
            bathroom.mName = source.readString();
            bathroom.mCategory = source.readString();
            boolean[] pending = new boolean[1];
            source.readBooleanArray(pending);
            bathroom.mIsPending = pending[0];
            bathroom.mLatitude = source.readDouble();
            bathroom.mLongitude = source.readDouble();
            bathroom.mAverageRating = source.readFloat();
            bathroom.mReviewCount = source.readInt();

            Parcelable[] parcelables = source.readParcelableArray(Review.class.getClassLoader());
            Review[] reviews = Arrays.copyOf(parcelables, parcelables.length, Review[].class);
            bathroom.mReviews = Arrays.asList(reviews);
            
            return bathroom;
        }

        @Override
        public Bathroom[] newArray(int size) {
            return new Bathroom[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mId);
        dest.writeString(mName);
        dest.writeString(mCategory);
        dest.writeBooleanArray(new boolean[]{mIsPending});
        dest.writeDouble(mLatitude);
        dest.writeDouble(mLongitude);
        dest.writeFloat(mAverageRating);
        dest.writeInt(mReviewCount);
        dest.writeParcelableArray(mReviews.toArray(new Review[mReviews.size()]), flags);
    }

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

    public float getAverageRating() {
        return mAverageRating;
    }

    public void setAverageRating(float averageRating) {
        mAverageRating = averageRating;
    }

    public int getReviewCount() {
        return mReviewCount;
    }

    public void setReviewCount(int reviewCount) {
        mReviewCount = reviewCount;
    }

    public List<Review> getReviews() {
        return mReviews;
    }

    public void setReviews(List<Review> reviews) {
        mReviews = reviews;
    }
}
