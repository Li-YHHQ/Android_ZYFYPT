package com.example.zyfypt613lsl.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zyfypt613lsl.R;
import com.example.zyfypt613lsl.adapter.FollowAdapter;
import com.example.zyfypt613lsl.bean.FollowResultBean;
import com.example.zyfypt613lsl.bean.ResBean;
import com.example.zyfypt613lsl.common.Common;
import com.example.zyfypt613lsl.bean.UserBean;
import com.example.zyfypt613lsl.service.FocusService;
import com.example.zyfypt613lsl.service.ResService;
import com.example.zyfypt613lsl.service.UserService;
import com.example.zyfypt613lsl.utils.NetworkClient;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 我的关注页面 - 简洁版，直接显示所有关注的用户列表
 */
public class MyFocusMainActivity extends BaseActivity {

    private static final String TAG = "MyFocusMainActivity";
    private RecyclerView recyclerView;
    private FollowAdapter adapter;
    private View emptyView;
    private View loadingView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private final List<FollowResultBean> data = new ArrayList<>();
    private int page = 1;
    private boolean isLoading = false;
    private boolean hasMoreData = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_my_focus_main);

            // 初始化返回按钮
            findViewById(R.id.btn_back).setOnClickListener(v -> finish());
            
            // 初始化下拉刷新
            initSwipeRefresh();

            // 初始化RecyclerView
            initRecyclerView();
            
            // 初始化空视图和加载视图
            emptyView = findViewById(R.id.empty_view);
            loadingView = findViewById(R.id.loading_view);
            
            // 加载数据
            loadPage(1);

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "应用初始化出错: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void initSwipeRefresh() {
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setColorSchemeResources(
                    R.color.colorPrimary,
                    R.color.colorAccent,
                    R.color.colorSecondary
            );
            swipeRefreshLayout.setOnRefreshListener(() -> {
                page = 1;
                loadPage(1);
            });
        }
    }

    private void initRecyclerView() {
        recyclerView = findViewById(R.id.rv_focus_list);
        if (recyclerView == null) {
            Log.e(TAG, "RecyclerView not found!");
            return;
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        
        adapter = new FollowAdapter(this);
        recyclerView.setAdapter(adapter);

        // 添加滚动监听实现分页加载
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState != RecyclerView.SCROLL_STATE_IDLE) return;
                if (!hasMoreData || isLoading) return;
                
                int lastVisible = layoutManager.findLastVisibleItemPosition();
                if (lastVisible >= adapter.getItemCount() - 3) {
                    loadPage(page + 1);
                }
            }
        });
    }

    private void loadPage(int targetPage) {
        if (isLoading) return;
        
        String sessionId = getSessionID();
        if (sessionId == null || sessionId.isEmpty()) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            showEmptyState("请先登录后查看关注列表");
            return;
        }

        if (targetPage == 1) {
            hasMoreData = true;
        }
        
        isLoading = true;
        Log.d(TAG, "Loading page: " + targetPage);

        // 使用全局网络客户端 - 复用连接池，提高性能
        NetworkClient networkClient = NetworkClient.getInstance();
        FocusService service = networkClient.createService(FocusService.class);
        
        // 调试：获取原始JSON
        FocusService rawService = networkClient.createRawService(FocusService.class);
        rawService.getMyFollowListRaw(targetPage, sessionId).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "=== 原始JSON响应 ===");
                    Log.d(TAG, response.body());
                }
            }
            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Log.e(TAG, "获取原始JSON失败", t);
            }
        });
        
        Call<List<FollowResultBean>> call = service.getMyFollowList(targetPage, sessionId);

        call.enqueue(new Callback<List<FollowResultBean>>() {
            @Override
            public void onResponse(@NonNull Call<List<FollowResultBean>> call, @NonNull Response<List<FollowResultBean>> response) {
                isLoading = false;
                
                if (!response.isSuccessful() || response.body() == null) {
                    Log.e(TAG, "Response failed: " + response.message());
                    if (targetPage == 1) {
                        showEmptyState("获取关注列表失败");
                    }
                    return;
                }

                List<FollowResultBean> result = response.body();
                Log.d(TAG, "收到关注数据: " + result.size() + " 条");
                
                // 打印每条数据的详细信息用于调试
                for (int i = 0; i < result.size(); i++) {
                    FollowResultBean bean = result.get(i);
                    Log.d(TAG, "=== 关注用户 [" + i + "] ===");
                    Log.d(TAG, "  id=" + bean.getId());
                    Log.d(TAG, "  idolid=" + bean.getIdolid());
                    Log.d(TAG, "  idolname=" + bean.getIdolname());
                    Log.d(TAG, "  realname=" + bean.getRealname());
                    Log.d(TAG, "  username=" + bean.getUsername());
                    Log.d(TAG, "  author=" + bean.getAuthor());
                    Log.d(TAG, "  avatar=" + bean.getAvatar());
                    Log.d(TAG, "  rolename=" + bean.getRolename());
                    Log.d(TAG, "  description=" + bean.getDescription());
                    Log.d(TAG, "  email=" + bean.getEmail());
                    Log.d(TAG, "  phone=" + bean.getPhone());
                    Log.d(TAG, "  getDisplayName()=" + bean.getDisplayName());
                }

                if (result.isEmpty()) {
                    hasMoreData = false;
                    if (targetPage == 1) {
                        data.clear();
                        adapter.setData(data);
                        showEmptyState("暂无关注的用户");
                    }
                } else {
                    if (targetPage == 1) {
                        data.clear();
                    }
                    data.addAll(result);
                    page = targetPage;
                    adapter.setData(data);
                    hideEmptyState();
                    
                    // 为没有名字的用户获取作者名
                    fetchUserNames(result);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<FollowResultBean>> call, @NonNull Throwable t) {
                isLoading = false;
                Log.e(TAG, "Request failed", t);
                Toast.makeText(MyFocusMainActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                if (targetPage == 1) {
                    showEmptyState("网络错误，请稍后重试");
                }
            }
        });
    }

    /**
     * 为没有名字的用户获取作者名
     * 对所有用户都尝试获取详细信息，确保显示真实姓名
     */
    private void fetchUserNames(List<FollowResultBean> users) {
        String sessionId = getSessionID();
        if (sessionId == null || sessionId.isEmpty()) return;
        
        // 使用全局网络客户端
        NetworkClient networkClient = NetworkClient.getInstance();
        UserService userService = networkClient.createService(UserService.class);
        ResService resService = networkClient.createService(ResService.class);
        
        for (FollowResultBean user : users) {
            String idolIdStr = user.getIdolid();
            if (idolIdStr == null || idolIdStr.isEmpty()) continue;
            
            // 检查当前显示名称
            String currentName = user.getDisplayName();
            boolean needFetch = (currentName == null || 
                                 currentName.isEmpty() || 
                                 currentName.startsWith("用户#") ||
                                 currentName.equals("未知用户"));
            
            Log.d(TAG, "用户 " + idolIdStr + " 当前名称: " + currentName + ", 需要获取: " + needFetch);
            
            if (!needFetch) {
                continue;
            }
            
            try {
                int idolId = Integer.parseInt(idolIdStr);
                
                // 先尝试通过UserService获取用户信息
                fetchUserInfoDirectly(userService, resService, user, idolId, idolIdStr, sessionId);
                
            } catch (NumberFormatException e) {
                Log.e(TAG, "Invalid idolid: " + idolIdStr);
            }
        }
    }
    
    /**
     * 直接获取用户信息
     */
    private void fetchUserInfoDirectly(UserService userService, ResService resService, 
            FollowResultBean user, int idolId, String idolIdStr, String sessionId) {
        
        Log.d(TAG, "开始获取用户 " + idolIdStr + " 的详细信息...");
        
        userService.getUserInfo(idolId, sessionId).enqueue(new Callback<UserBean>() {
            @Override
            public void onResponse(@NonNull Call<UserBean> call, @NonNull Response<UserBean> response) {
                Log.d(TAG, "UserService响应: code=" + response.code() + ", successful=" + response.isSuccessful());
                
                if (response.isSuccessful() && response.body() != null) {
                    UserBean userBean = response.body();
                    Log.d(TAG, "UserBean: realname=" + userBean.getRealname() + ", username=" + userBean.getUsername());
                    
                    // 优先使用realname，其次username
                    String name = userBean.getRealname();
                    if (name == null || name.isEmpty()) {
                        name = userBean.getUsername();
                    }
                    if (name != null && !name.isEmpty()) {
                        Log.d(TAG, "✓ 获取到用户 " + idolIdStr + " 的名字: " + name + " (from UserService)");
                        // 同时设置多个字段，确保getDisplayName()能获取到
                        user.setIdolname(name);
                        user.setRealname(name);
                        runOnUiThread(() -> adapter.notifyDataSetChanged());
                        return;
                    }
                }
                // 如果UserService失败，尝试从资源获取
                Log.d(TAG, "UserService未返回名字，尝试从资源获取...");
                fetchUserNameFromResource(resService, user, idolId, idolIdStr, sessionId, "article");
            }
            
            @Override
            public void onFailure(@NonNull Call<UserBean> call, @NonNull Throwable t) {
                Log.e(TAG, "获取用户 " + idolIdStr + " 信息失败: " + t.getMessage());
                // 失败时尝试从资源获取
                fetchUserNameFromResource(resService, user, idolId, idolIdStr, sessionId, "article");
            }
        });
    }
    
    /**
     * 从用户的资源中获取作者名
     */
    private void fetchUserNameFromResource(ResService resService, FollowResultBean user, 
            int idolId, String idolIdStr, String sessionId, String mod) {
        
        Log.d(TAG, "尝试从 " + mod + " 资源获取用户 " + idolIdStr + " 的名字...");
        
        resService.getUserResourceList(mod, 1, idolId, sessionId)
                .enqueue(new Callback<List<ResBean>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<ResBean>> call, @NonNull Response<List<ResBean>> response) {
                        Log.d(TAG, mod + " 资源响应: code=" + response.code() + 
                                ", size=" + (response.body() != null ? response.body().size() : 0));
                        
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            ResBean res = response.body().get(0);
                            String authorName = res.getAuthor();
                            Log.d(TAG, mod + " 资源作者: " + authorName);
                            
                            if (authorName != null && !authorName.isEmpty()) {
                                Log.d(TAG, "✓ 获取到用户 " + idolIdStr + " 的名字: " + authorName + " (from " + mod + ")");
                                // 更新用户名 - 同时设置多个字段
                                user.setIdolname(authorName);
                                user.setRealname(authorName);
                                user.setAuthor(authorName);
                                // 刷新列表
                                runOnUiThread(() -> adapter.notifyDataSetChanged());
                                return;
                            }
                        }
                        
                        // 尝试下一个资源类型
                        if ("article".equals(mod)) {
                            fetchUserNameFromResource(resService, user, idolId, idolIdStr, sessionId, "video");
                        } else if ("video".equals(mod)) {
                            fetchUserNameFromResource(resService, user, idolId, idolIdStr, sessionId, "tware");
                        } else if ("tware".equals(mod)) {
                            fetchUserNameFromResource(resService, user, idolId, idolIdStr, sessionId, "tcase");
                        }
                    }
                    
                    @Override
                    public void onFailure(@NonNull Call<List<ResBean>> call, @NonNull Throwable t) {
                        Log.e(TAG, "获取用户 " + idolIdStr + " 的" + mod + "资源失败: " + t.getMessage());
                        // 失败时也尝试下一个类型
                        if ("article".equals(mod)) {
                            fetchUserNameFromResource(resService, user, idolId, idolIdStr, sessionId, "video");
                        } else if ("video".equals(mod)) {
                            fetchUserNameFromResource(resService, user, idolId, idolIdStr, sessionId, "tware");
                        } else if ("tware".equals(mod)) {
                            fetchUserNameFromResource(resService, user, idolId, idolIdStr, sessionId, "tcase");
                        }
                    }
                });
    }

    private void stopRefreshing() {
        if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void showEmptyState(String message) {
        stopRefreshing();
        if (recyclerView != null) {
            recyclerView.setVisibility(View.GONE);
        }
        if (emptyView != null) {
            emptyView.setVisibility(View.VISIBLE);
        }
    }

    private void hideEmptyState() {
        stopRefreshing();
        if (recyclerView != null) {
            recyclerView.setVisibility(View.VISIBLE);
        }
        if (emptyView != null) {
            emptyView.setVisibility(View.GONE);
        }
    }

    private String getSessionID() {
        return getSharedPreferences("login", MODE_PRIVATE)
                .getString(Common.SESSION_HEADER, null);
    }
}
