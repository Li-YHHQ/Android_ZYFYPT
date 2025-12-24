package com.example.zyfypt613lsl.utils;

import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkUtils {
    private static final String TAG = "NetworkUtils";
    
    public static boolean isServerReachable(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            
            int responseCode = connection.getResponseCode();
            Log.d(TAG, "Server response code: " + responseCode);
            return responseCode == 200;
        } catch (IOException e) {
            Log.e(TAG, "Server connection failed: " + e.getMessage());
            return false;
        }
    }
}