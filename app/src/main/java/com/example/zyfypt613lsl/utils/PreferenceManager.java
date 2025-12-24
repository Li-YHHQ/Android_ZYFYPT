package com.example.zyfypt613lsl.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * 用户偏好设置管理器
 * 管理主题、字体大小等用户设置
 */
public class PreferenceManager {
    
    private static final String PREF_NAME = "app_settings";
    private static final String KEY_THEME_MODE = "theme_mode";
    private static final String KEY_FONT_SIZE = "font_size";
    private static final String KEY_CACHE_ENABLED = "cache_enabled";
    private static final String KEY_AUTO_PLAY_VIDEO = "auto_play_video";
    private static final String KEY_NOTIFICATION_ENABLED = "notification_enabled";
    
    // 主题模式
    public static final int THEME_LIGHT = 0;
    public static final int THEME_DARK = 1;
    public static final int THEME_SYSTEM = 2;
    
    // 字体大小
    public static final int FONT_SMALL = 0;
    public static final int FONT_NORMAL = 1;
    public static final int FONT_LARGE = 2;
    public static final int FONT_EXTRA_LARGE = 3;
    
    private final SharedPreferences prefs;
    private static PreferenceManager instance;
    
    private PreferenceManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    
    public static synchronized PreferenceManager getInstance(Context context) {
        if (instance == null) {
            instance = new PreferenceManager(context);
        }
        return instance;
    }
    
    // 主题设置
    public void setThemeMode(int mode) {
        prefs.edit().putInt(KEY_THEME_MODE, mode).apply();
        applyTheme(mode);
    }
    
    public int getThemeMode() {
        return prefs.getInt(KEY_THEME_MODE, THEME_LIGHT);
    }
    
    public void applyTheme(int mode) {
        switch (mode) {
            case THEME_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case THEME_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case THEME_SYSTEM:
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }
    
    // 字体大小设置
    public void setFontSize(int size) {
        prefs.edit().putInt(KEY_FONT_SIZE, size).apply();
    }
    
    public int getFontSize() {
        return prefs.getInt(KEY_FONT_SIZE, FONT_NORMAL);
    }
    
    public float getFontScale() {
        switch (getFontSize()) {
            case FONT_SMALL: return 0.85f;
            case FONT_LARGE: return 1.15f;
            case FONT_EXTRA_LARGE: return 1.3f;
            case FONT_NORMAL:
            default: return 1.0f;
        }
    }
    
    // 缓存设置
    public void setCacheEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_CACHE_ENABLED, enabled).apply();
    }
    
    public boolean isCacheEnabled() {
        return prefs.getBoolean(KEY_CACHE_ENABLED, true);
    }
    
    // 自动播放视频
    public void setAutoPlayVideo(boolean enabled) {
        prefs.edit().putBoolean(KEY_AUTO_PLAY_VIDEO, enabled).apply();
    }
    
    public boolean isAutoPlayVideo() {
        return prefs.getBoolean(KEY_AUTO_PLAY_VIDEO, false);
    }
    
    // 通知设置
    public void setNotificationEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_NOTIFICATION_ENABLED, enabled).apply();
    }
    
    public boolean isNotificationEnabled() {
        return prefs.getBoolean(KEY_NOTIFICATION_ENABLED, true);
    }
    
    // 清除所有设置
    public void clearAll() {
        prefs.edit().clear().apply();
    }
}
