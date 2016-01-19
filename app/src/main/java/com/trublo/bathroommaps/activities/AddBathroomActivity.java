package com.trublo.bathroommaps.activities;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.trublo.bathroommaps.R;
import com.trublo.bathroommaps.Util;
import com.trublo.bathroommaps.bathroommaps.Bathroom;
import com.trublo.bathroommaps.bathroommaps.BathroomMaps;
import com.trublo.bathroommaps.fragments.BathroomMapFragment;

/**
 * Created by julianlo on 1/17/16.
 */
public class AddBathroomActivity extends SingleFragmentActivity implements BathroomMapFragment.Callbacks {
    private static final String TAG = AddBathroomActivity.class.getSimpleName();

    private EditText mNameEditText;
    private Spinner mCategorySpinner;
    private Button mSubmitButton;
    private Toast mToast;

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, AddBathroomActivity.class);
        return intent;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_add_bathroom;
    }

    @Override
    protected Fragment createFragment() {
        return BathroomMapFragment.newInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mNameEditText = Util.findView(this, R.id.activity_add_bathroom_name_edit_text);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                BathroomMaps.CATEGORIES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCategorySpinner = Util.findView(this, R.id.activity_add_bathroom_category_spinner);
        //mCategorySpinner.setAdapter(new CategoryAdapter(BathroomMaps.CATEGORIES));
        mCategorySpinner.setAdapter(adapter);

        mSubmitButton = Util.findView(this, R.id.activity_add_bathroom_submit_button);
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = mNameEditText.getText().toString();
                if (name.length() == 0) {
                    showToast(R.string.add_bathroom_empty_name, Toast.LENGTH_SHORT);
                    return;
                }
                new AddBathroomTask(mNameEditText.getText().toString(),
                        mCategorySpinner.getSelectedItem().toString(),
                        getMapFragment().getTargetLocation())
                        .execute();
            }
        });

    }

    private BathroomMapFragment getMapFragment() {
        return Util.cast(getFragment());
    }

    @Override
    public void onFetchingBathrooms(boolean fetching) {

    }

    @Override
    public boolean onBathroomMarkerSelected(Bathroom bathroom) {
        return false;
    }

    private void showToast(@StringRes int resId, int length) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(this, resId, length);
        mToast.show();
    }

    private class AddBathroomTask extends AsyncTask<Void,Void,Void> {
        private Exception mException;
        private String mName;
        private String mCategory;
        private double mLatitude;
        private double mLongitude;

        public AddBathroomTask(String name, String category, LatLng latLng) {
            mName = name;
            mCategory = category;
            mLatitude = latLng.latitude;
            mLongitude = latLng.longitude;
        }

        @Override
        protected void onPreExecute() {
            mSubmitButton.setEnabled(false);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                new BathroomMaps().submitBathroom(mName, mCategory, mLatitude, mLongitude);
            } catch (Exception e) {
                Log.e(TAG, "Could not add bathroom", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (mException != null) {
                showToast(R.string.add_bathroom_failed, Toast.LENGTH_SHORT);
                mSubmitButton.setEnabled(true);
                return;
            }

            showToast(R.string.add_bathroom_succeeded, Toast.LENGTH_LONG);
            finish();
        }
    }
}
