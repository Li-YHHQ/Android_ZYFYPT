package com.example.zyfypt613lsl.activities;

import android.app.AlertDialog;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zyfypt613lsl.R;
import com.example.zyfypt613lsl.utils.CacheManager;
import com.example.zyfypt613lsl.utils.PreferenceManager;
import com.google.android.material.switchmaterial.SwitchMaterial;

/**
 * 设置页面
 * 包含主题切换、字体大小、缓存管理等功能
 */
public class SettingsActivity extends BaseActivity {

    private PreferenceManager prefManager;
    private TextView tvThemeValue;
    private TextView tvFontValue;
    private TextView tvCacheSize;
    private TextView tvVersion;
    private SwitchMaterial switchAutoPlay;
    private SwitchMaterial switchCache;
    private SwitchMaterial switchNotification;

    private final String[] themeOptions = {"浅色模式", "深色模式", "跟随系统"};
    private final String[] fontOptions = {"小", "标准", "大", "特大"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefManager = PreferenceManager.getInstance(this);
        initViews();
        loadSettings();
        setupListeners();
    }

    private void initViews() {
        // 返回按钮
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // 设置项
        tvThemeValue = findViewById(R.id.tv_theme_value);
        tvFontValue = findViewById(R.id.tv_font_value);
        tvCacheSize = findViewById(R.id.tv_cache_size);
        tvVersion = findViewById(R.id.tv_version);

        // 开关
        switchAutoPlay = findViewById(R.id.switch_auto_play);
        switchCache = findViewById(R.id.switch_cache);
        switchNotification = findViewById(R.id.switch_notification);
    }

    private void loadSettings() {
        // 加载主题设置
        int themeMode = prefManager.getThemeMode();
        tvThemeValue.setText(themeOptions[themeMode]);

        // 加载字体设置
        int fontSize = prefManager.getFontSize();
        tvFontValue.setText(fontOptions[fontSize]);

        // 加载开关状态
        switchAutoPlay.setChecked(prefManager.isAutoPlayVideo());
        switchCache.setChecked(prefManager.isCacheEnabled());
        switchNotification.setChecked(prefManager.isNotificationEnabled());

        // 加载缓存大小
        CacheManager.getCacheSize(this, new CacheManager.CacheCallback() {
            @Override
            public void onCacheSizeCalculated(String size) {
                tvCacheSize.setText(size);
            }

            @Override
            public void onCacheCleared(boolean success) {}
        });

        // 加载版本号
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            tvVersion.setText(pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            tvVersion.setText("1.0.0");
        }
    }

    private void setupListeners() {
        // 主题设置
        findViewById(R.id.setting_theme).setOnClickListener(v -> showThemeDialog());

        // 字体设置
        findViewById(R.id.setting_font).setOnClickListener(v -> showFontDialog());

        // 清除缓存
        findViewById(R.id.setting_clear_cache).setOnClickListener(v -> showClearCacheDialog());

        // 自动播放开关
        switchAutoPlay.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefManager.setAutoPlayVideo(isChecked);
        });

        // 缓存开关
        switchCache.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefManager.setCacheEnabled(isChecked);
        });

        // 通知开关
        switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefManager.setNotificationEnabled(isChecked);
        });
    }

    private void showThemeDialog() {
        int currentTheme = prefManager.getThemeMode();
        new AlertDialog.Builder(this)
                .setTitle("选择主题")
                .setSingleChoiceItems(themeOptions, currentTheme, (dialog, which) -> {
                    prefManager.setThemeMode(which);
                    tvThemeValue.setText(themeOptions[which]);
                    dialog.dismiss();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showFontDialog() {
        int currentFont = prefManager.getFontSize();
        new AlertDialog.Builder(this)
                .setTitle("选择字体大小")
                .setSingleChoiceItems(fontOptions, currentFont, (dialog, which) -> {
                    prefManager.setFontSize(which);
                    tvFontValue.setText(fontOptions[which]);
                    dialog.dismiss();
                    // 立即应用字体大小设置
                    Toast.makeText(this, "正在应用字体设置...", Toast.LENGTH_SHORT).show();
                    recreate(); // 重新创建Activity以应用新的字体大小
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showClearCacheDialog() {
        new AlertDialog.Builder(this)
                .setTitle("清除缓存")
                .setMessage("确定要清除所有缓存数据吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    tvCacheSize.setText("清理中...");
                    CacheManager.clearCache(this, new CacheManager.CacheCallback() {
                        @Override
                        public void onCacheSizeCalculated(String size) {}

                        @Override
                        public void onCacheCleared(boolean success) {
                            if (success) {
                                tvCacheSize.setText("0 B");
                                Toast.makeText(SettingsActivity.this, "缓存已清除", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(SettingsActivity.this, "清除缓存失败", Toast.LENGTH_SHORT).show();
                                // 重新计算缓存大小
                                CacheManager.getCacheSize(SettingsActivity.this, new CacheManager.CacheCallback() {
                                    @Override
                                    public void onCacheSizeCalculated(String size) {
                                        tvCacheSize.setText(size);
                                    }
                                    @Override
                                    public void onCacheCleared(boolean success) {}
                                });
                            }
                        }
                    });
                })
                .setNegativeButton("取消", null)
                .show();
    }
}
