package com.example.zyfypt613lsl.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.zyfypt613lsl.R;
import com.example.zyfypt613lsl.adapter.MyAdapter;
import com.example.zyfypt613lsl.bean.ResBean;
import com.example.zyfypt613lsl.service.ResService;
import com.example.zyfypt613lsl.utils.EmptyStateHelper;
import com.example.zyfypt613lsl.utils.NetworkClient;
import com.example.zyfypt613lsl.utils.RefreshHelper;
import com.example.zyfypt613lsl.utils.SkeletonLoader;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 资源列表通用 Fragment，子类只需提供模块标识和布局即可。
 * 支持下拉刷新、骨架屏加载、空状态显示
 */
public abstract class ResourceListFragment extends BaseFragment {

    private static final String TAG = "ResourceListFragment";
    protected RecyclerView recyclerView;
    protected MyAdapter adapter;
    private LinearLayoutManager layoutManager;
    protected final List<ResBean> data = new ArrayList<>();
    protected int page = 1;
    protected boolean isLoading = false;
    protected boolean hasMoreData = true;
    protected String currentSession;
    protected boolean isSpecialMode = false;

    // UI 增强组件
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout skeletonContainer;
    private ViewGroup emptyContainer;
    private RefreshHelper refreshHelper;
    private SkeletonLoader skeletonLoader;
    private EmptyStateHelper emptyStateHelper;
    private boolean isFirstLoad = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(getLayoutResId(), container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        initRecyclerView(view);
        setupRefresh();
        loadPage(1);
    }

    private void initViews(View root) {
        swipeRefreshLayout = root.findViewById(R.id.swipe_refresh);
        skeletonContainer = root.findViewById(R.id.skeleton_container);
        emptyContainer = root.findViewById(R.id.empty_container);

        // 初始化骨架屏
        if (skeletonContainer != null) {
            skeletonLoader = new SkeletonLoader(skeletonContainer, 5);
        }

        // 初始化空状态
        if (emptyContainer != null) {
            recyclerView = root.findViewById(R.id.rv);
            emptyStateHelper = new EmptyStateHelper(emptyContainer, recyclerView);
            emptyStateHelper.setOnRetryClickListener(this::refresh);
        }
    }

    private void setupRefresh() {
        if (swipeRefreshLayout != null) {
            refreshHelper = new RefreshHelper(swipeRefreshLayout);
            refreshHelper.setOnRefreshListener(this::refresh);
        }
    }

    /**
     * 刷新数据
     */
    public void refresh() {
        page = 1;
        hasMoreData = true;
        isFirstLoad = false;
        loadPage(1);
    }

    private void initRecyclerView(View root) {
        recyclerView = root.findViewById(R.id.rv);
        if (recyclerView == null) return;

        layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        adapter = new MyAdapter(context, getModuleName());
        adapter.setSessionId(currentSession);
        adapter.setList(data);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState != RecyclerView.SCROLL_STATE_IDLE) {
                    return;
                }
                if (!hasMoreData || isLoading) {
                    return;
                }
                int lastVisible = layoutManager.findLastVisibleItemPosition();
                if (lastVisible >= adapter.getItemCount() - 3) {
                    loadPage(page + 1);
                }
            }
        });
    }

    protected void showEmptyView(boolean show) {
        Log.d(TAG, "showEmptyView: " + show);
        if (emptyStateHelper != null) {
            if (show) {
                emptyStateHelper.showEmpty("暂无数据", "快去探索更多内容吧", true);
            } else {
                emptyStateHelper.hide();
            }
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    protected void showError(String message) {
        if (emptyStateHelper != null) {
            emptyStateHelper.showError(message);
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(View.GONE);
        }
    }

    private void showLoading(boolean show) {
        if (isFirstLoad && skeletonLoader != null) {
            if (show) {
                if (skeletonContainer != null) {
                    skeletonContainer.setVisibility(View.VISIBLE);
                }
                skeletonLoader.show();
                if (recyclerView != null) {
                    recyclerView.setVisibility(View.GONE);
                }
            } else {
                skeletonLoader.hide();
                if (skeletonContainer != null) {
                    skeletonContainer.setVisibility(View.GONE);
                }
            }
        }
    }

    private void stopRefreshing() {
        if (refreshHelper != null) {
            refreshHelper.stopRefresh();
        }
    }

    /** 切换到普通模式并重新加载 */
    public void switchToNormalMode() {
        isSpecialMode = false;
        data.clear();
        page = 1;
        hasMoreData = true;
        isFirstLoad = true;
        if (adapter != null) {
            adapter.setModuleName(getModuleName());
            adapter.notifyDataSetChanged();
        }
        loadPage(1);
    }

    /** 切换到专题/项目模式并重新加载 */
    public void switchToSpecialMode() {
        isSpecialMode = true;
        data.clear();
        page = 1;
        hasMoreData = true;
        isFirstLoad = true;
        if (adapter != null) {
            adapter.setModuleName(getSpecialModuleName());
            adapter.notifyDataSetChanged();
        }
        loadPage(1);
    }

    protected void loadPage(int targetPage) {
        String moduleName = isSpecialMode ? getSpecialModuleName() : getModuleName();
        Log.d(TAG, "loadPage: " + targetPage + ", moduleName: " + moduleName + ", isSpecial: " + isSpecialMode);

        if (isLoading) {
            return;
        }
        String sessionId = getSessionID();
        if (sessionId == null || sessionId.isEmpty()) {
            Log.w(TAG, "loadPage aborted because SessionID is missing");
            stopRefreshing();
            showLoading(false);
            showError("请先登录以获取 SessionID");
            return;
        }
        if (targetPage == 1) {
            hasMoreData = true;
            if (isFirstLoad) {
                showLoading(true);
            }
        }
        currentSession = sessionId;
        if (adapter != null) {
            adapter.setSessionId(sessionId);
        }
        isLoading = true;

        // 使用全局网络客户端 - 复用连接池，提高性能
        ResService service = NetworkClient.getInstance().createService(ResService.class);

        Call<List<ResBean>> call;
        if (isSpecialMode && "video".equals(getModuleName())) {
            call = service.getSpecialVideoList(targetPage, sessionId);
        } else {
            call = service.getList(moduleName, targetPage, sessionId);
        }

        call.enqueue(new Callback<List<ResBean>>() {
            @Override
            public void onResponse(@NonNull Call<List<ResBean>> call, @NonNull Response<List<ResBean>> response) {
                isLoading = false;
                stopRefreshing();
                showLoading(false);
                isFirstLoad = false;

                Log.d(TAG, "onResponse: successful=" + response.isSuccessful() + ", body=" + (response.body() != null ? response.body().size() : "null"));

                if (!response.isSuccessful() || response.body() == null) {
                    if (targetPage == 1) {
                        showError("加载失败: " + response.message());
                    } else {
                        handleError(response.message());
                    }
                    return;
                }
                List<ResBean> res = response.body();
                if (res.isEmpty()) {
                    hasMoreData = false;
                    if (targetPage == 1) {
                        data.clear();
                        adapter.notifyDataSetChanged();
                        showEmptyView(true);
                    }
                } else {
                    if (targetPage == 1) {
                        data.clear();
                    }
                    data.addAll(res);
                    page = targetPage;
                    adapter.notifyDataSetChanged();
                    showEmptyView(false);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ResBean>> call, @NonNull Throwable t) {
                isLoading = false;
                stopRefreshing();
                showLoading(false);
                isFirstLoad = false;

                Log.e(TAG, "onFailure: ", t);
                if (targetPage == 1) {
                    showError("网络连接失败，请检查网络设置");
                } else {
                    handleError(t.getMessage());
                }
            }
        });
    }

    private void handleError(String msg) {
        Log.e(TAG, "handleError: " + msg);
        Toast.makeText(context, "获取失败: " + msg, Toast.LENGTH_SHORT).show();
    }

    /** 子类返回对应的 mod 参数，例如 article/tware/video 等。 */
    protected abstract String getModuleName();

    /** 子类返回专题/项目模式的 mod 参数，默认返回普通模块名 */
    protected String getSpecialModuleName() {
        return getModuleName();
    }

    /** 子类提供各自的布局资源 id（fragment1~fragment5） */
    protected abstract int getLayoutResId();
}
