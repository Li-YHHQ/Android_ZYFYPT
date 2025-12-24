package com.example.zyfypt613lsl.utils;

import android.content.Context;
import android.os.AsyncTask;

import java.io.File;
import java.text.DecimalFormat;

/**
 * 缓存管理器
 * 用于计算和清理应用缓存
 */
public class CacheManager {
    
    public interface CacheCallback {
        void onCacheSizeCalculated(String size);
        void onCacheCleared(boolean success);
    }
    
    /**
     * 获取缓存大小
     */
    public static void getCacheSize(Context context, CacheCallback callback) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                long size = 0;
                try {
                    File cacheDir = context.getCacheDir();
                    size += getDirSize(cacheDir);
                    
                    File externalCacheDir = context.getExternalCacheDir();
                    if (externalCacheDir != null) {
                        size += getDirSize(externalCacheDir);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return formatSize(size);
            }
            
            @Override
            protected void onPostExecute(String size) {
                if (callback != null) {
                    callback.onCacheSizeCalculated(size);
                }
            }
        }.execute();
    }
    
    /**
     * 清除缓存
     */
    public static void clearCache(Context context, CacheCallback callback) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    File cacheDir = context.getCacheDir();
                    deleteDir(cacheDir);
                    
                    File externalCacheDir = context.getExternalCacheDir();
                    if (externalCacheDir != null) {
                        deleteDir(externalCacheDir);
                    }
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
            
            @Override
            protected void onPostExecute(Boolean success) {
                if (callback != null) {
                    callback.onCacheCleared(success);
                }
            }
        }.execute();
    }
    
    private static long getDirSize(File dir) {
        long size = 0;
        if (dir != null && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        size += file.length();
                    } else if (file.isDirectory()) {
                        size += getDirSize(file);
                    }
                }
            }
        }
        return size;
    }
    
    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDir(file);
                    } else {
                        file.delete();
                    }
                }
            }
        }
        return dir != null && dir.delete();
    }
    
    private static String formatSize(long size) {
        if (size <= 0) return "0 B";
        
        final String[] units = {"B", "KB", "MB", "GB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        digitGroups = Math.min(digitGroups, units.length - 1);
        
        DecimalFormat df = new DecimalFormat("#,##0.##");
        return df.format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
