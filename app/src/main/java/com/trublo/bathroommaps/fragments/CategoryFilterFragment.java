package com.trublo.bathroommaps.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.trublo.bathroommaps.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by julianlo on 1/15/16.
 */
public class CategoryFilterFragment extends Fragment {

    private static final String ARG_CATEGORY_NAMES = "category_names";
    private static final String ARG_CATEGORY_VISIBILITIES = "category_visibilities";

    private Map<String, Boolean> mCategoryMap;

    public static CategoryFilterFragment newInstance(String[] categoryNames, Boolean[] categoryVisibilities) {
        Bundle args = new Bundle();

        args.putStringArray(ARG_CATEGORY_NAMES, categoryNames);
        args.putSerializable(ARG_CATEGORY_VISIBILITIES, categoryVisibilities);

        CategoryFilterFragment fragment = new CategoryFilterFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String[] names = getArguments().getStringArray(ARG_CATEGORY_NAMES);
        Boolean[] visibilities = (Boolean[])getArguments().getSerializable(ARG_CATEGORY_VISIBILITIES);

        initializeMap(names, visibilities);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category_filter, container, false);

        return view;
    }

    private void initializeMap(String[] names, Boolean[] visibilities) {
        mCategoryMap = new HashMap<>(names.length);
        for (int i = 0; i < names.length; i++) {
            mCategoryMap.put(names[i], visibilities[i]);
        }
    }
}
