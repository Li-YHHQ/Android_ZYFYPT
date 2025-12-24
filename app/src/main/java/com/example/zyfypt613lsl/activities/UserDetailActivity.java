package com.example.zyfypt613lsl.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zyfypt613lsl.R;
import com.example.zyfypt613lsl.adapter.MyAdapter;
import com.example.zyfypt613lsl.bean.ResBean;
import com.example.zyfypt613lsl.common.Common;
import com.example.zyfypt613lsl.service.ResService;
import com.example.zyfypt613lsl.utils.NetworkClient;
import com.squareup.picasso.Picasso;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 用户详情页面 - 显示用户信息和所有作品列表
 */
public class UserDetailActivity extends BaseActivity {

    private static final String TAG = "UserDetailActivity";
    
    public static final String EXTRA_USER_ID = "user_id";
    public static final String EXTRA_USER_NAME = "user_name";
    public static final String EXTRA_USER_ROLE = "user_role";
    public static final String EXTRA_USER_DESC = "user_desc";
    public static final String EXTRA_USER_AVATAR = "user_avatar";

    private RecyclerView rvWorks;
    private View emptyView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MyAdapter adapter;
    private List<ResBean> worksList = new ArrayList<>();
    private int userId;
    private String sessionId;
    private int loadingCount = 0; // 正在加载的请求数

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

        // 获取传入的用户信息
        userId = getIntent().getIntExtra(EXTRA_USER_ID, -1);
        String userName = getIntent().getStringExtra(EXTRA_USER_NAME);
        String userRole = getIntent().getStringExtra(EXTRA_USER_ROLE);
        String userDesc = getIntent().getStringExtra(EXTRA_USER_DESC);
        String userAvatar = getIntent().getStringExtra(EXTRA_USER_AVATAR);

        // 初始化视图
        ImageButton btnBack = findViewById(R.id.btn_back);
        TextView tvTitle = findViewById(R.id.tv_title);
        ImageView ivAvatar = findViewById(R.id.iv_avatar);
        TextView tvUserName = findViewById(R.id.tv_user_name);
        TextView tvUserRole = findViewById(R.id.tv_user_role);
        TextView tvUserDesc = findViewById(R.id.tv_user_desc);
        rvWorks = findViewById(R.id.rv_works);
        emptyView = findViewById(R.id.emptyView);

        // 设置标题
        tvTitle.setText("用户详情");

        // 返回按钮
        btnBack.setOnClickListener(v -> finish());

        // 设置用户信息
        tvUserName.setText(!TextUtils.isEmpty(userName) ? userName : "未知用户");
        
        if (!TextUtils.isEmpty(userRole)) {
            tvUserRole.setText(userRole);
            tvUserRole.setVisibility(View.VISIBLE);
        } else {
            tvUserRole.setVisibility(View.GONE);
        }
        
        tvUserDesc.setText(!TextUtils.isEmpty(userDesc) ? userDesc : "这个人很懒，什么都没写~");

        // 加载头像
        if (!TextUtils.isEmpty(userAvatar)) {
            Picasso.get()
                    .load(Common.buildUploadUrl(userAvatar))
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(ivAvatar);
        }

        // 初始化下拉刷新
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setColorSchemeResources(
                    R.color.colorPrimary,
                    R.color.colorAccent,
                    R.color.colorSecondary
            );
            swipeRefreshLayout.setOnRefreshListener(this::loadUserWorks);
        }

        // 设置RecyclerView
        rvWorks.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyAdapter(this, "article");
        sessionId = getSharedPreferences("login", MODE_PRIVATE)
                .getString(Common.SESSION_HEADER, "");
        adapter.setSessionId(sessionId);
        adapter.setList(worksList);
        rvWorks.setAdapter(adapter);

        // 加载用户作品
        if (userId > 0) {
            loadUserWorks();
        } else {
            showEmpty();
        }
    }

    private void loadUserWorks() {
        // 使用全局网络客户端
        ResService resService = NetworkClient.getInstance().createService(ResService.class);
        
        worksList.clear();
        loadingCount = 0;
        
        // 同时加载所有类型的资源
        String[] modules = {"article", "video", "tware", "tcase", "project"};
        loadingCount = modules.length;
        
        for (String mod : modules) {
            loadModuleWorks(resService, mod);
        }
    }
    
    private void loadModuleWorks(ResService resService, String mod) {
        resService.getUserResourceList(mod, 1, userId, sessionId)
                .enqueue(new Callback<List<ResBean>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<ResBean>> call, @NonNull Response<List<ResBean>> response) {
                        loadingCount--;
                        
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            Log.d(TAG, "加载 " + mod + " 成功，数量: " + response.body().size());
                            worksList.addAll(response.body());
                            adapter.notifyDataSetChanged();
                            showList();
                        }
                        
                        // 所有请求完成后检查是否有数据
                        if (loadingCount <= 0 && worksList.isEmpty()) {
                            showEmpty();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<ResBean>> call, @NonNull Throwable t) {
                        loadingCount--;
                        Log.e(TAG, "加载 " + mod + " 失败: " + t.getMessage());
                        
                        // 所有请求完成后检查是否有数据
                        if (loadingCount <= 0 && worksList.isEmpty()) {
                            showEmpty();
                        }
                    }
                });
    }

    private void stopRefreshing() {
        if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void showEmpty() {
        stopRefreshing();
        emptyView.setVisibility(View.VISIBLE);
        rvWorks.setVisibility(View.GONE);
    }

    private void showList() {
        stopRefreshing();
        emptyView.setVisibility(View.GONE);
        rvWorks.setVisibility(View.VISIBLE);
    }
}
