package com.trublo.bathroommaps.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.trublo.bathroommaps.R;
import com.trublo.bathroommaps.Util;
import com.trublo.bathroommaps.activities.ReviewActivity;
import com.trublo.bathroommaps.bathroommaps.Bathroom;
import com.trublo.bathroommaps.bathroommaps.Review;

import java.util.List;

/**
 * Created by julianlo on 1/11/16.
 */
public class ReviewListFragment extends Fragment {

    private static final String ARG_BATHROOM = "bathroom";
    private static final int REQUEST_ADD_REVIEW = 1;

    private Bathroom mBathroom;
    private RecyclerView mRecyclerView;
    private View mAddReviewButton;

    public static ReviewListFragment newInstance(Bathroom bathroom) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_BATHROOM, bathroom);

        ReviewListFragment fragment = new ReviewListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBathroom = Util.cast(getArguments().getParcelable(ARG_BATHROOM));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_review_list, container, false);

        mRecyclerView = Util.findView(view, R.id.fragment_review_list_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mAddReviewButton = Util.findView(view, R.id.fragment_review_list_add_review_button);
        mAddReviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = ReviewActivity.newIntent(getActivity(), mBathroom);
                startActivityForResult(intent, REQUEST_ADD_REVIEW);
            }
        });

        setupAdapter();

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_ADD_REVIEW) {
            Toast.makeText(getActivity(), R.string.send_review_succeeded, Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void setupAdapter() {
        Bathroom bathroom = getArguments().getParcelable(ARG_BATHROOM);
        mRecyclerView.setAdapter(new ReviewAdapter(bathroom.getReviews()));
    }

    private class ReviewHolder extends RecyclerView.ViewHolder {

        private RatingBar mRatingBar;
        private TextView mTextTextView;
        private TextView mDateTextView;

        public ReviewHolder(View itemView) {
            super(itemView);
            mRatingBar = Util.findView(itemView, R.id.review_item_rating_bar);
            mTextTextView = Util.findView(itemView, R.id.review_item_text_text_view);
            mDateTextView = Util.findView(itemView, R.id.review_item_date_text_view);
        }

        public void bindReview(Review review) {
            mRatingBar.setNumStars(review.getRating());
            mTextTextView.setText(review.getText());

            CharSequence dateText = DateUtils.getRelativeDateTimeString(getActivity(),
                    review.getDateCreated().getMillis(),
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.WEEK_IN_MILLIS,
                    0);
            mDateTextView.setText(dateText);
        }
    }

    private class ReviewAdapter extends RecyclerView.Adapter<ReviewHolder> {
        List<Review> mReviews;

        public ReviewAdapter(List<Review> reviews) {
            mReviews = reviews;
        }

        @Override
        public ReviewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.review_item, parent, false);
            return new ReviewHolder(view);
        }

        @Override
        public void onBindViewHolder(ReviewHolder holder, int position) {
            holder.bindReview(mReviews.get(position));
        }

        @Override
        public int getItemCount() {
            return mReviews.size();
        }
    }
}
