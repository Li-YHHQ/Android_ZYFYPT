package com.example.zyfypt613lsl.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.zyfypt613lsl.R;
import com.example.zyfypt613lsl.fragments.BaseFragment;
import com.example.zyfypt613lsl.fragments.Fragment1;
import com.example.zyfypt613lsl.fragments.Fragment2;
import com.example.zyfypt613lsl.fragments.Fragment4;
import com.example.zyfypt613lsl.fragments.OwnerFragment;
import com.example.zyfypt613lsl.fragments.Fragment3;
import com.example.zyfypt613lsl.utils.NetworkUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";
    private ViewPager2 viewPager;
    private BottomNavigationView bottomNavView;
    private List<BaseFragment> fragmentList;
    private FragmentStateAdapter fragmentStateAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setTheme(R.style.Theme_ZYFYPT617wagntao);
            setContentView(R.layout.activity_main);

            // 初始化调试日志
            setupDebugLogging();

            // 检查网络连接
            checkNetworkConnection();

            // 初始化UI组件
            initFragmentList();
            initViewPager();
            initBottomNavigation();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "应用初始化出错: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupDebugLogging() {
        // 添加Fragment生命周期回调
        getSupportFragmentManager().registerFragmentLifecycleCallbacks(
                new FragmentManager.FragmentLifecycleCallbacks() {
                    @Override
                    public void onFragmentViewCreated(@NonNull FragmentManager fm,
                                                      @NonNull Fragment f,
                                                      @NonNull View v,
                                                      @Nullable Bundle savedInstanceState) {
                        super.onFragmentViewCreated(fm, f, v, savedInstanceState);
                        Log.d("FragmentLifecycle", "View created: " + f.getClass().getSimpleName());
                    }

                    @Override
                    public void onFragmentViewDestroyed(@NonNull FragmentManager fm, @NonNull Fragment f) {
                        super.onFragmentViewDestroyed(fm, f);
                        Log.d("FragmentLifecycle", "View destroyed: " + f.getClass().getSimpleName());
                    }
                },
                true
        );
    }

    private void checkNetworkConnection() {
        new Thread(() -> {
            try {
                Log.d(TAG, "正在检查网络连接...");
                boolean reachable = NetworkUtils.isServerReachable("http://43.143.162.173:9001/");
                Log.d(TAG, "服务器可达: " + reachable);
                if (!reachable) {
                    runOnUiThread(() -> {
                        try {
                            Log.e(TAG, "无法连接到服务器，请检查网络连接");
                            Toast.makeText(MainActivity.this,
                                    "网络连接异常，部分功能可能无法使用",
                                    Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Log.e(TAG, "显示网络错误提示时出错", e);
                        }
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "检查网络连接时出错", e);
            }
        }).start();
    }

    // 初始化Fragment列表
    private void initFragmentList() {
        try {
            Log.d(TAG, "初始化Fragment列表...");
            fragmentList = new ArrayList<>();

            // 创建Fragment实例
            fragmentList.add(new Fragment1());           // 0 文章
            fragmentList.add(new Fragment2());           // 1 课件
            fragmentList.add(new Fragment3());   // 2 视频（最新 / 专题）
            fragmentList.add(new Fragment4());      // 3 案例（案例 / 项目）
            fragmentList.add(new OwnerFragment());       // 4 我的

            Log.d(TAG, "成功初始化了 " + fragmentList.size() + " 个Fragment");

        } catch (Exception e) {
            Log.e(TAG, "初始化Fragment列表时出错", e);
            if (fragmentList == null) {
                fragmentList = new ArrayList<>();
            }
        }
    }

    // 初始化ViewPager2
    private void initViewPager() {
        Log.d(TAG, "初始化ViewPager2...");
        viewPager = findViewById(R.id.viewPager2);
        if (viewPager == null) {
            Log.e(TAG, "ViewPager2 未找到!");
            return;
        }

        // 确保Fragment列表已初始化
        if (fragmentList == null || fragmentList.isEmpty()) {
            Log.d(TAG, "Fragment列表为空，正在初始化...");
            initFragmentList();
        }

        // 创建适配器
        fragmentStateAdapter = new FragmentStateAdapter(this) {
            @Override
            public int getItemCount() {
                return fragmentList != null ? fragmentList.size() : 0;
            }

            @NonNull
            @Override
            public Fragment createFragment(int position) {
                if (fragmentList != null && position < fragmentList.size()) {
                    Log.d(TAG, "创建Fragment位置: " + position);
                    return fragmentList.get(position);
                }
                Log.e(TAG, "无法创建Fragment，位置: " + position);
                return new Fragment1(); // 默认返回第一个fragment
            }
        };

        // 设置适配器
        viewPager.setAdapter(fragmentStateAdapter);

        // 设置页面变化监听
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Log.d(TAG, "页面已选择: " + position);
                if (bottomNavView != null) {
                    switch (position) {
                        case 0:
                            bottomNavView.setSelectedItemId(R.id.article);
                            break;
                        case 1:
                            bottomNavView.setSelectedItemId(R.id.tware);
                            break;
                        case 2:
                            bottomNavView.setSelectedItemId(R.id.video);
                            break;
                        case 3:
                            bottomNavView.setSelectedItemId(R.id.sample);
                            break;
                        case 4:
                            bottomNavView.setSelectedItemId(R.id.owner);
                            break;
                    }
                }
            }
        });
    }

    // 初始化底部导航
    private void initBottomNavigation() {
        try {
            bottomNavView = findViewById(R.id.bnv);
            if (bottomNavView == null) {
                Log.e(TAG, "BottomNavigationView 未找到!");
                return;
            }

            bottomNavView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    try {
                        int itemId = item.getItemId();
                        Log.d(TAG, "导航项被点击: " + item.getTitle());

                        if (viewPager == null || viewPager.getAdapter() == null) {
                            Log.e(TAG, "ViewPager2 或适配器未初始化");
                            return false;
                        }

                        int position = 0;
                        if (itemId == R.id.article) {
                            position = 0;
                        } else if (itemId == R.id.tware) {
                            position = 1;
                        } else if (itemId == R.id.video) {
                            position = 2;
                        } else if (itemId == R.id.sample) {
                            position = 3;
                        } else if (itemId == R.id.owner) {
                            position = 4;
                        }

                        if (position < viewPager.getAdapter().getItemCount()) {
                            viewPager.setCurrentItem(position, false);
                            return true;
                        } else {
                            Log.e(TAG, "无效的位置: " + position);
                            return false;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "处理导航项点击时出错", e);
                        return false;
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "初始化底部导航栏时出错", e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 临时注释：避免在 onResume 中强制切换页面，先验证是否与黑屏有关
        // if (viewPager != null && bottomNavView != null && viewPager.getAdapter() != null) {
        //     new Handler(Looper.getMainLooper()).postDelayed(() -> {
        //         try {
        //             if (viewPager.getCurrentItem() != 0) {
        //                 viewPager.setCurrentItem(0, false);
        //                 Log.d(TAG, "在onResume中设置初始页面为0");
        //             }
        //             bottomNavView.setSelectedItemId(R.id.article);
        //         } catch (Exception e) {
        //             Log.e(TAG, "onResume中设置页面时出错", e);
        //         }
        //     }, 100);
        // }
    }
}