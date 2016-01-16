package com.trublo.bathroommaps.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.trublo.bathroommaps.R;
import com.trublo.bathroommaps.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by julianlo on 1/15/16.
 */
public class CategoryFilterFragment extends Fragment {

    private static final String ARG_CATEGORY_NAMES = "category_names";
    private static final String ARG_CATEGORY_VISIBILITIES = "category_visibilities";

    public static final String EXTRA_CATEGORY_NAMES = "com.trublo.bathroommaps.category_names";
    public static final String EXTRA_CATEGORY_VISIBILITIES = "com.trublo.bathrooms.category_visibilities";

    private RecyclerView mRecyclerView;

    public static CategoryFilterFragment newInstance(String[] categoryNames, Boolean[] categoryVisibilities) {
        Bundle args = new Bundle();

        args.putStringArray(ARG_CATEGORY_NAMES, categoryNames);
        args.putSerializable(ARG_CATEGORY_VISIBILITIES, categoryVisibilities);

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

        String[] names = getArguments().getStringArray(ARG_CATEGORY_NAMES);
        Boolean[] visibilities = (Boolean[])getArguments().getSerializable(ARG_CATEGORY_VISIBILITIES);
        setupAdapter(names, visibilities);

        return view;
    }



    private void setupAdapter(String[] names, Boolean[] visibilities) {
        List<Pair<String, Boolean>> categories = new ArrayList<>(names.length);
        for (int i = 0; i < names.length; i++) {
            if (names[i] != null) {
                categories.add(new Pair<>(names[i], visibilities[i]));
            }
        }
        mRecyclerView.setAdapter(new CategoryAdapter(categories));
    }

    private void sendResult() {
        CategoryAdapter adapter = Util.cast(mRecyclerView.getAdapter());
        String[] names = new String[adapter.mCategories.size()];
        Boolean[] visibilities = new Boolean[adapter.mCategories.size()];

        for (int i = 0; i < adapter.mCategories.size(); i++) {
            Pair<String, Boolean> pair = adapter.mCategories.get(i);
            names[i] = pair.first;
            visibilities[i] = pair.second;
        }

        int resultCode = Activity.RESULT_OK;
        Intent data = new Intent();
        data.putExtra(EXTRA_CATEGORY_NAMES, names);
        data.putExtra(EXTRA_CATEGORY_VISIBILITIES, visibilities);

        Fragment targetFragment = getTargetFragment();
        if (targetFragment != null) {
            targetFragment.onActivityResult(targetFragment.getTargetRequestCode(), resultCode, data);
        } else {
            getActivity().setResult(resultCode, data);
        }
    }

    private class CategoryHolder extends RecyclerView.ViewHolder {
        private Pair<String, Boolean> mCategory;
        private TextView mTextView;
        private ToggleButton mToggleButton;

        public CategoryHolder(View itemView) {
            super(itemView);
            mTextView = Util.findView(itemView, R.id.category_item_text_view);
            mToggleButton = Util.findView(itemView, R.id.category_item_toggle_button);
            mToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    // Pair<> is immutable, so we need to remove the old pair and add a new one
                    // to the List that is backing the adapter (which is *the* list).
                    // Ideally, we'd wait to set the result until the fragment goes away, but I'm
                    // not aware of a good hook point that would work there. (onPause doesn't work.)
                    // So we actively set the result every time a toggle changes.
                    CategoryAdapter adapter = Util.cast(mRecyclerView.getAdapter());
                    int index = adapter.mCategories.indexOf(mCategory);
                    adapter.mCategories.remove(index);
                    Pair<String, Boolean> category = new Pair<>(mCategory.first, isChecked);
                    adapter.mCategories.add(index, category);
                    mCategory = category;
                    sendResult();
                }
            });
        }

        public void bindCategory(Pair<String, Boolean> category) {
            mCategory = category;
            mTextView.setText(category.first);
            mToggleButton.setChecked(category.second);
        }
    }

    private class CategoryAdapter extends RecyclerView.Adapter<CategoryHolder> {
        private List<Pair<String, Boolean>> mCategories;

        public CategoryAdapter(List<Pair<String, Boolean>> categories) {
            mCategories = categories;
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
