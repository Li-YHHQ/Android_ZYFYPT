package com.example.zyfypt613lsl.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zyfypt613lsl.R;
import com.example.zyfypt613lsl.adapter.CollectAdapter;
import com.example.zyfypt613lsl.bean.CollectResultBean;
import com.example.zyfypt613lsl.bean.ResBean;
import com.example.zyfypt613lsl.common.Common;
import com.example.zyfypt613lsl.service.CollectService;
import com.example.zyfypt613lsl.service.ResService;
import com.example.zyfypt613lsl.utils.NetworkClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 我的收藏页面 - 获取资源详细信息版本
 */
public class MyCollectMainActivity extends BaseActivity {

    private static final String TAG = "MyCollectMainActivity";
    
    private RecyclerView recyclerView;
    private View emptyView;
    private ProgressBar loadingView;
    private BottomNavigationView bottomNavView;
    private SwipeRefreshLayout swipeRefreshLayout;
    
    private CollectAdapter adapter;
    private final List<CollectResultBean> dataList = new ArrayList<>();
    private String currentModule = "article";
    private boolean isLoading = false;
    private String sessionId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_collect_main);
        
        // 获取SessionID
        sessionId = getSharedPreferences("login", MODE_PRIVATE)
                .getString(Common.SESSION_HEADER, "");
        Log.d(TAG, "SessionID: " + sessionId);
        
        initViews();
        initBottomNavigation();
        
        // 加载默认数据
        loadData("article");
    }

    private void initViews() {
        // 返回按钮
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        
        // 下拉刷新
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorAccent,
                R.color.colorSecondary
        );
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadData(currentModule);
        });
        
        // RecyclerView
        recyclerView = findViewById(R.id.rv_collect);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // 初始化适配器
        adapter = new CollectAdapter(this, currentModule);
        recyclerView.setAdapter(adapter);
        
        // 空视图和加载视图
        emptyView = findViewById(R.id.empty_view);
        loadingView = findViewById(R.id.loading_view);
    }

    private void initBottomNavigation() {
        bottomNavView = findViewById(R.id.bnv);
        if (bottomNavView == null) {
            Log.e(TAG, "BottomNavigationView not found!");
            return;
        }
        // 底部导航视图对象    // 设置选项选择监听器的方法    // 创建监听器实例
        bottomNavView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                String module;
                
                if (itemId == R.id.article) {
                    module = "article";
                } else if (itemId == R.id.tware) {
                    module = "tware";
                } else if (itemId == R.id.video) {
                    module = "video";
                } else if (itemId == R.id.sample) {
                    module = "tcase";
                } else {
                    return false;
                }
                
                if (!module.equals(currentModule)) {
                    currentModule = module;
                    adapter = new CollectAdapter(MyCollectMainActivity.this, module);
                    recyclerView.setAdapter(adapter);
                    loadData(module);
                }
                return true;
            }
        });
    }

    private void loadData(String moduleName) {
        if (isLoading) return;
        
        Log.d(TAG, "loadData: module=" + moduleName);
        
        if (sessionId == null || sessionId.isEmpty()) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            showEmpty();
            return;
        }
        
        isLoading = true;
        showLoading();
        
        // 先获取原始响应调试
        CollectService rawService = NetworkClient.getInstance().createRawService(CollectService.class);
        rawService.getMyCollectListRaw(moduleName, 1, sessionId)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        Log.d(TAG, "Raw API: " + call.request().url());
                        Log.d(TAG, "Raw Response: " + response.body());
                    }
                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        Log.e(TAG, "Raw request failed: " + t.getMessage());
                    }
                });
        
        // 正式请求
        CollectService service = NetworkClient.getInstance().createService(CollectService.class);
        service.getMyCollectList(moduleName, 1, sessionId)
                .enqueue(new Callback<List<CollectResultBean>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<CollectResultBean>> call,
                                         @NonNull Response<List<CollectResultBean>> response) {
                        
                        if (!response.isSuccessful() || response.body() == null) {
                            isLoading = false;
                            showEmpty();
                            return;
                        }
                        
                        List<CollectResultBean> list = response.body();
                        Log.d(TAG, "收到 " + list.size() + " 条收藏记录");
                        
                        if (list.isEmpty()) {
                            isLoading = false;
                            showEmpty();
                            return;
                        }
                        
                        // 检查第一条数据是否有完整信息
                        CollectResultBean first = list.get(0);
                        Log.d(TAG, "第一条: resid=" + first.getResid() + ", name=" + first.getName() + ", author=" + first.getAuthor());
                        
                        // 如果没有name，需要获取资源详情
                        if (first.getName() == null || first.getName().isEmpty()) {
                            Log.d(TAG, "收藏数据缺少详情，开始获取资源详情...");
                            fetchResourceDetails(list, moduleName);
                        } else {
                            // 已有完整数据，直接显示
                            isLoading = false;
                            dataList.clear();
                            dataList.addAll(list);
                            adapter.setData(dataList);
                            showList();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<CollectResultBean>> call, @NonNull Throwable t) {
                        isLoading = false;
                        Log.e(TAG, "Request failed: " + t.getMessage());
                        Toast.makeText(MyCollectMainActivity.this, "获取收藏失败", Toast.LENGTH_SHORT).show();
                        showEmpty();
                    }
                });
    }
    
    /**
     * 根据resid获取每个资源的详细信息
     */
    private void fetchResourceDetails(List<CollectResultBean> collectList, String moduleName) {
        ResService resService = NetworkClient.getInstance().createService(ResService.class);
        
        dataList.clear();
        AtomicInteger pendingCount = new AtomicInteger(collectList.size());
        
        Log.d(TAG, "开始获取 " + collectList.size() + " 个资源的详情...");
        
        for (CollectResultBean collect : collectList) {
            String residStr = collect.getResid();
            if (residStr == null || residStr.isEmpty()) {
                Log.w(TAG, "跳过空resid");
                if (pendingCount.decrementAndGet() == 0) {
                    finishLoading();
                }
                continue;
            }
            
            try {
                int resid = Integer.parseInt(residStr);
                Log.d(TAG, "获取资源详情: mod=" + moduleName + ", id=" + resid);
                
                // 先尝试Header方式
                fetchDetailWithHeader(resService, collect, moduleName, resid, residStr, pendingCount);
                        
            } catch (NumberFormatException e) {
                Log.e(TAG, "Invalid resid: " + residStr);
                if (pendingCount.decrementAndGet() == 0) {
                    finishLoading();
                }
            }
        }
    }
    
    /**
     * 使用Header方式获取资源详情
     */
    private void fetchDetailWithHeader(ResService resService, CollectResultBean collect, 
            String moduleName, int resid, String residStr, AtomicInteger pendingCount) {
        
        resService.getDetailWithHeader(moduleName, resid, sessionId)
                .enqueue(new Callback<ResBean>() {
                    @Override
                    public void onResponse(@NonNull Call<ResBean> call, @NonNull Response<ResBean> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ResBean res = response.body();
                            if (res.getName() != null && !res.getName().isEmpty()) {
                                Log.d(TAG, "✓ [Header] 资源 " + residStr + ": " + res.getName());
                                updateCollectFromRes(collect, res);
                                addToListAndCheck(collect, pendingCount);
                                return;
                            }
                        }
                        // Header方式失败，尝试路径方式
                        Log.d(TAG, "Header方式失败，尝试路径方式: " + residStr);
                        fetchDetailWithPath(resService, collect, moduleName, resid, residStr, pendingCount);
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResBean> call, @NonNull Throwable t) {
                        Log.e(TAG, "Header方式失败: " + t.getMessage());
                        // 尝试路径方式
                        fetchDetailWithPath(resService, collect, moduleName, resid, residStr, pendingCount);
                    }
                });
    }
    
    /**
     * 使用路径方式获取资源详情
     */
    private void fetchDetailWithPath(ResService resService, CollectResultBean collect, 
            String moduleName, int resid, String residStr, AtomicInteger pendingCount) {
        
        resService.getDetail(moduleName, resid, sessionId)
                .enqueue(new Callback<ResBean>() {
                    @Override
                    public void onResponse(@NonNull Call<ResBean> call, @NonNull Response<ResBean> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ResBean res = response.body();
                            if (res.getName() != null && !res.getName().isEmpty()) {
                                Log.d(TAG, "✓ [Path] 资源 " + residStr + ": " + res.getName());
                                updateCollectFromRes(collect, res);
                            } else {
                                Log.w(TAG, "资源 " + residStr + " 返回空name，可能已被删除");
                                // 设置一个提示名称
                                collect.setName("资源已删除 #" + residStr);
                            }
                        } else {
                            Log.e(TAG, "路径方式也失败: " + residStr + ", code=" + response.code());
                            // 资源可能已被删除
                            collect.setName("资源不存在 #" + residStr);
                        }
                        addToListAndCheck(collect, pendingCount);
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResBean> call, @NonNull Throwable t) {
                        Log.e(TAG, "路径方式失败: " + residStr + " - " + t.getMessage());
                        collect.setName("加载失败 #" + residStr);
                        addToListAndCheck(collect, pendingCount);
                    }
                });
    }
    
    /**
     * 从ResBean更新CollectResultBean
     */
    private void updateCollectFromRes(CollectResultBean collect, ResBean res) {
        collect.setName(res.getName());
        collect.setAuthor(res.getAuthor());
        collect.setThumb(res.getThumb());
        collect.setUpdatetime(res.getUpdate_time());
        collect.setUserid(res.getUserId());
    }
    
    /**
     * 添加到列表并检查是否完成
     */
    private void addToListAndCheck(CollectResultBean collect, AtomicInteger pendingCount) {
        synchronized (dataList) {
            dataList.add(collect);
        }
        if (pendingCount.decrementAndGet() == 0) {
            finishLoading();
        }
    }
    
    private void finishLoading() {
        runOnUiThread(() -> {
            isLoading = false;
            stopRefreshing();
            if (dataList.isEmpty()) {
                showEmpty();
            } else {
                adapter.setData(new ArrayList<>(dataList));
                showList();
            }
        });
    }
    
    private void stopRefreshing() {
        if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void showLoading() {
        if (loadingView != null) loadingView.setVisibility(View.VISIBLE);
        if (recyclerView != null) recyclerView.setVisibility(View.GONE);
        if (emptyView != null) emptyView.setVisibility(View.GONE);
    }

    private void showEmpty() {
        stopRefreshing();
        if (loadingView != null) loadingView.setVisibility(View.GONE);
        if (recyclerView != null) recyclerView.setVisibility(View.GONE);
        if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
    }

    private void showList() {
        stopRefreshing();
        if (loadingView != null) loadingView.setVisibility(View.GONE);
        if (recyclerView != null) recyclerView.setVisibility(View.VISIBLE);
        if (emptyView != null) emptyView.setVisibility(View.GONE);
    }
}
