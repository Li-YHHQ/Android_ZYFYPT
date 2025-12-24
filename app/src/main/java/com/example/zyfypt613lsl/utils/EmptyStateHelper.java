package com.example.zyfypt613lsl.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.zyfypt613lsl.R;
import com.google.android.material.button.MaterialButton;

/**
 * 空状态页面帮助类
 * 用于显示列表为空时的提示界面
 */
public class EmptyStateHelper {

    public interface OnRetryClickListener {
        void onRetry();
    }

    private ViewGroup container;
    private View emptyView;
    private View contentView;
    private OnRetryClickListener retryListener;

    public EmptyStateHelper(ViewGroup container, View contentView) {
        this.container = container;
        this.contentView = contentView;
    }

    /**
     * 显示空状态
     */
    public void showEmpty(String title, String message) {
        showEmpty(title, message, false);
    }

    /**
     * 显示空状态（带重试按钮）
     */
    public void showEmpty(String title, String message, boolean showRetry) {
        if (emptyView == null) {
            emptyView = LayoutInflater.from(container.getContext())
                    .inflate(R.layout.layout_empty_state, container, false);
            container.addView(emptyView);
        }

        TextView tvTitle = emptyView.findViewById(R.id.tv_empty_title);
        TextView tvMessage = emptyView.findViewById(R.id.tv_empty_message);
        MaterialButton btnRetry = emptyView.findViewById(R.id.btn_retry);

        tvTitle.setText(title);
        tvMessage.setText(message);
        btnRetry.setVisibility(showRetry ? View.VISIBLE : View.GONE);

        if (showRetry && retryListener != null) {
            btnRetry.setOnClickListener(v -> retryListener.onRetry());
        }

        emptyView.setVisibility(View.VISIBLE);
        if (contentView != null) {
            contentView.setVisibility(View.GONE);
        }
    }

    /**
     * 显示错误状态
     */
    public void showError(String message) {
        showEmpty("加载失败", message, true);
        if (emptyView != null) {
            ImageView ivEmpty = emptyView.findViewById(R.id.iv_empty);
            ivEmpty.setImageResource(R.drawable.ic_error);
        }
    }

    /**
     * 隐藏空状态
     */
    public void hide() {
        if (emptyView != null) {
            emptyView.setVisibility(View.GONE);
        }
        if (contentView != null) {
            contentView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 设置重试监听器
     */
    public void setOnRetryClickListener(OnRetryClickListener listener) {
        this.retryListener = listener;
    }

    /**
     * 设置自定义图标
     */
    public void setIcon(int iconResId) {
        if (emptyView != null) {
            ImageView ivEmpty = emptyView.findViewById(R.id.iv_empty);
            ivEmpty.setImageResource(iconResId);
        }
    }
}
