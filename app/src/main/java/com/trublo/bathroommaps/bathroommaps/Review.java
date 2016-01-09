package com.trublo.bathroommaps.bathroommaps;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by julianlo on 1/8/16.
 */
public class Review implements Parcelable {
    private int mRating;
    private String mText;
    private Date mDateCreated;

    public static final Parcelable.Creator<Review> CREATOR = new Parcelable.Creator<Review>() {
        @Override
        public Review createFromParcel(Parcel source) {
            Review review = new Review();
            review.setRating(source.readInt());
            review.setText(source.readString());
            return review;
        }

        @Override
        public Review[] newArray(int size) {
            return new Review[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(getRating());
        dest.writeString(getText());
    }

    public int getRating() {
        return mRating;
    }

    public void setRating(int rating) {
        mRating = rating;
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }

    public Date getDateCreated() {
        return mDateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        mDateCreated = dateCreated;
    }
}
