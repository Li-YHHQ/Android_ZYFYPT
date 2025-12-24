package com.example.zyfypt613lsl.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class BaseFragment extends Fragment {
    private static final String TAG = "BaseFragment";
    private SharedPreferences sp;//获取sp
    protected Context context;//上下文

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach called");
        this.context = context;
        sp = context.getSharedPreferences("login", Context.MODE_PRIVATE);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate called");
    }

    //SessionID
    protected String getSessionID() {
        String sessionId = sp.getString("SessionID", "");
        Log.d(TAG, "getSessionID: " + sessionId);
        return sessionId;
    }

    //username
    protected String getUsername() {
        String username = sp.getString("username", "");
        Log.d(TAG, "getUsername: " + username);
        return username;
    }
}