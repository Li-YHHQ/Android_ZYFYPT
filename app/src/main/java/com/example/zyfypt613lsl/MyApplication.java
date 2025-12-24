package com.example.zyfypt613lsl;

import android.app.Application;

import com.example.zyfypt613lsl.utils.PreferenceManager;

/**
 * 自定义 Application 类
 * 用于在应用启动时初始化全局设置
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        
        // 应用用户保存的主题设置
        PreferenceManager prefManager = PreferenceManager.getInstance(this);
        prefManager.applyTheme(prefManager.getThemeMode());
    }
}
