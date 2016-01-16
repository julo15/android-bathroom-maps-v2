package com.trublo.bathroommaps.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.trublo.bathroommaps.R;
import com.trublo.bathroommaps.Util;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by julianlo on 1/15/16.
 */
public class CategoryFilterFragment extends Fragment {

    private static final String ARG_CATEGORY_FILTER_ITEMS = "category_filter_items";
    public static final String EXTRA_CATEGORY_FILTER_ITEMS = "com.trublo.bathroommaps.category_filter_items";

    private RecyclerView mRecyclerView;

    public static class CategoryFilterItem implements Parcelable {
        private String mCategoryId;
        private boolean mIsVisible;

        public static final Parcelable.Creator<CategoryFilterItem> CREATOR = new Parcelable.Creator<CategoryFilterItem>() {
            @Override
            public CategoryFilterItem createFromParcel(Parcel source) {
                CategoryFilterItem item = new CategoryFilterItem();

                item.mCategoryId = source.readString();
                boolean[] visible = new boolean[1];
                source.readBooleanArray(visible);
                item.mIsVisible = visible[0];

                return item;
            }

            @Override
            public CategoryFilterItem[] newArray(int size) {
                return new CategoryFilterItem[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(mCategoryId);
            boolean[] visible = new boolean[] { mIsVisible };
            dest.writeBooleanArray(visible);
        }

        public String getCategoryId() {
            return mCategoryId;
        }

        public void setCategoryId(String categoryId) {
            mCategoryId = categoryId;
        }

        public boolean isVisible() {
            return mIsVisible;
        }

        public void setIsVisible(boolean isVisible) {
            mIsVisible = isVisible;
        }
    }

    public static CategoryFilterFragment newInstance(ArrayList<CategoryFilterItem> categoryFilterItems) {
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_CATEGORY_FILTER_ITEMS, categoryFilterItems);

        CategoryFilterFragment fragment = new CategoryFilterFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category_filter, container, false);

        mRecyclerView = Util.findView(view, R.id.fragment_category_filter_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        ArrayList<Parcelable> parcelables = getArguments().getParcelableArrayList(ARG_CATEGORY_FILTER_ITEMS);
        ArrayList<CategoryFilterItem> categoryFilterItems = Util.cast(parcelables);

        // BathroomMapFragment adds a null category marker for the selected marker.
        // Find it and remove it so that we don't have a null category showing in the filter list.
        for (Iterator<CategoryFilterItem> iterator = categoryFilterItems.iterator(); iterator.hasNext();) {
            CategoryFilterItem item = iterator.next();
            if (item.getCategoryId() == null) {
                iterator.remove();
                break;
            }
        }

        setupAdapter(categoryFilterItems);

        return view;
    }

    private void setupAdapter(ArrayList<CategoryFilterItem> categoryFilterItems) {
        mRecyclerView.setAdapter(new CategoryAdapter(categoryFilterItems));
    }

    private void sendResult() {
        CategoryAdapter adapter = Util.cast(mRecyclerView.getAdapter());

        int resultCode = Activity.RESULT_OK;
        Intent data = new Intent();
        data.putExtra(EXTRA_CATEGORY_FILTER_ITEMS, adapter.mCategories);

        Fragment targetFragment = getTargetFragment();
        if (targetFragment != null) {
            targetFragment.onActivityResult(targetFragment.getTargetRequestCode(), resultCode, data);
        } else {
            getActivity().setResult(resultCode, data);
        }
    }

    private class CategoryHolder extends RecyclerView.ViewHolder {
        private CategoryFilterItem mCategory;
        private TextView mTextView;
        private ToggleButton mToggleButton;

        public CategoryHolder(View itemView) {
            super(itemView);
            mTextView = Util.findView(itemView, R.id.category_item_text_view);
            mToggleButton = Util.findView(itemView, R.id.category_item_toggle_button);
            mToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    // Ideally, we'd wait to set the result until the fragment goes away, but I'm
                    // not aware of a good hook point that would work there. (onPause doesn't work.)
                    // So we actively set the result every time a toggle changes.
                    mCategory.setIsVisible(isChecked);
                    sendResult();
                }
            });
        }

        public void bindCategory(CategoryFilterItem category) {
            mCategory = category;
            mTextView.setText(category.getCategoryId());
            mToggleButton.setChecked(category.isVisible());
        }
    }

    private class CategoryAdapter extends RecyclerView.Adapter<CategoryHolder> {
        // Use ArrayList explicitly so that we can easily pop it back into intent extras
        private ArrayList<CategoryFilterItem> mCategories;

        public CategoryAdapter(ArrayList<CategoryFilterItem> categoryFilterItems) {
            mCategories = categoryFilterItems;
        }

        @Override
        public CategoryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.category_item, parent, false);
            return new CategoryHolder(view);
        }

        @Override
        public void onBindViewHolder(CategoryHolder holder, int position) {
            holder.bindCategory(mCategories.get(position));
        }

        @Override
        public int getItemCount() {
            return mCategories.size();
        }
    }
}
