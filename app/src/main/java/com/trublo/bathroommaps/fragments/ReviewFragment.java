package com.trublo.bathroommaps.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RatingBar;

import com.trublo.bathroommaps.R;
import com.trublo.bathroommaps.Util;
import com.trublo.bathroommaps.bathroommaps.Bathroom;
import com.trublo.bathroommaps.bathroommaps.BathroomMaps;

/**
 * Created by julianlo on 1/11/16.
 */
public class ReviewFragment extends Fragment {

    private static final String ARG_BATHROOM = "bathroom";
    public static final String EXTRA_UPDATED_BATHROOM = "com.trublo.bathroommaps.updated_bathroom";

    private Bathroom mBathroom;
    private View mSendReviewButton;
    private RatingBar mRatingBar;
    private EditText mTextEditText;

    public static ReviewFragment newInstance(Bathroom bathroom) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_BATHROOM, bathroom);

        ReviewFragment fragment = new ReviewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBathroom = getArguments().getParcelable(ARG_BATHROOM);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_review, container, false);

        mSendReviewButton = Util.findView(view, R.id.fragment_review_send_review_button);
        mSendReviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SendReviewTask(mBathroom.getId(), (int)mRatingBar.getRating(), mTextEditText.getText().toString()).execute();
            }
        });

        mRatingBar = Util.findView(view, R.id.fragment_review_rating_bar);
        mTextEditText = Util.findView(view, R.id.fragment_review_text_edit_text);

        return view;
    }

    private void sendResult(Bathroom updatedBathroom) {
        Fragment targetFragment = getTargetFragment();
        Intent data = new Intent();
        data.putExtra(EXTRA_UPDATED_BATHROOM, updatedBathroom);
        if (targetFragment != null) {
            targetFragment.onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, data);
        } else {
            getActivity().setResult(Activity.RESULT_OK, data);
            getActivity().finish();
        }
    }

    private class SendReviewTask extends AsyncTask<Void,Void,Void> {

        private String mBathroomId;
        private int mRating;
        private String mText;
        private Exception mException;
        private Bathroom mUpdatedBathroom;

        public SendReviewTask(String bathroomId, int rating, String text) {
            mBathroomId = bathroomId;
            mRating = rating;
            mText = text;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                mUpdatedBathroom = new BathroomMaps().submitReview(mBathroomId, mRating, mText);
            } catch (Exception e) {
                mException = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (mException != null) {
                return;
            }

            sendResult(mUpdatedBathroom);
        }
    }
}
