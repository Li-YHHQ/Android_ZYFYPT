package com.example.zyfypt613lsl.utils;

import android.content.Context;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.zyfypt613lsl.R;

/**
 * 下拉刷新帮助类
 * 统一配置 SwipeRefreshLayout 的样式
 */
public class RefreshHelper {

    public interface OnRefreshListener {
        void onRefresh();
    }

    private SwipeRefreshLayout swipeRefreshLayout;
    private OnRefreshListener listener;

    public RefreshHelper(SwipeRefreshLayout swipeRefreshLayout) {
        this.swipeRefreshLayout = swipeRefreshLayout;
        setupStyle();
    }

    private void setupStyle() {
        if (swipeRefreshLayout == null) return;
        
        Context context = swipeRefreshLayout.getContext();
        
        // 设置刷新指示器颜色
        swipeRefreshLayout.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorSecondary,
                R.color.colorAccent
        );
        
        // 设置背景颜色
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.background_card);
    }

    public void setOnRefreshListener(OnRefreshListener listener) {
        this.listener = listener;
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(() -> {
                if (this.listener != null) {
                    this.listener.onRefresh();
                }
            });
        }
    }

    /**
     * 开始刷新
     */
    public void startRefresh() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
        }
    }

    /**
     * 结束刷新
     */
    public void stopRefresh() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    /**
     * 是否正在刷新
     */
    public boolean isRefreshing() {
        return swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing();
    }

    /**
     * 设置是否启用刷新
     */
    public void setEnabled(boolean enabled) {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setEnabled(enabled);
        }
    }
}
