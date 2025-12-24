package com.example.zyfypt613lsl.fragments;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.zyfypt613lsl.R;

public class Fragment1 extends ResourceListFragment {
    private static final String TAG = "Fragment1";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate called");
    }

    @Override
    protected String getModuleName() {
        Log.d(TAG, "getModuleName: article");
        return "article";
    }

    @Override
    protected int getLayoutResId() {
        Log.d(TAG, "getLayoutResId: R.layout.fragment1");
        return R.layout.fragment1;
    }
}