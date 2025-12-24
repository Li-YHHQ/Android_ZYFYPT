package com.example.zyfypt613lsl.activities;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zyfypt613lsl.utils.PreferenceManager;

/**
 * 基础Activity，提供全局字体大小和主题设置支持
 * 所有Activity应继承此类以获得统一的设置支持
 */
public class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        // 应用字体大小设置
        Context context = applyFontScale(newBase);
        super.attachBaseContext(context);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // 应用主题设置
        PreferenceManager prefManager = PreferenceManager.getInstance(this);
        prefManager.applyTheme(prefManager.getThemeMode());
        super.onCreate(savedInstanceState);
    }

    /**
     * 应用字体缩放
     */
    private Context applyFontScale(Context context) {
        PreferenceManager prefManager = PreferenceManager.getInstance(context);
        float fontScale = prefManager.getFontScale();
        
        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        configuration.fontScale = fontScale;
        
        return context.createConfigurationContext(configuration);
    }

    /**
     * 重新创建Activity以应用新的设置
     */
    public void applySettings() {
        recreate();
    }
}
