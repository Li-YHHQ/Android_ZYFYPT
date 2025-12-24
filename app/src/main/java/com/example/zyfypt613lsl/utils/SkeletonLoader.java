package com.example.zyfypt613lsl.utils;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.zyfypt613lsl.R;

/**
 * 骨架屏加载器
 * 用于在数据加载时显示占位动画
 */
public class SkeletonLoader {

    private ViewGroup container;
    private int itemCount;
    private int layoutResId;
    private View[] skeletonViews;
    private ObjectAnimator[] animators;
    private boolean isShowing = false;

    public SkeletonLoader(ViewGroup container) {
        this(container, 5, R.layout.layout_skeleton_item);
    }

    public SkeletonLoader(ViewGroup container, int itemCount) {
        this(container, itemCount, R.layout.layout_skeleton_item);
    }

    public SkeletonLoader(ViewGroup container, int itemCount, int layoutResId) {
        this.container = container;
        this.itemCount = itemCount;
        this.layoutResId = layoutResId;
    }

    /**
     * 显示骨架屏
     */
    public void show() {
        if (isShowing) return;
        isShowing = true;

        container.removeAllViews();
        skeletonViews = new View[itemCount];
        animators = new ObjectAnimator[itemCount];

        LayoutInflater inflater = LayoutInflater.from(container.getContext());

        for (int i = 0; i < itemCount; i++) {
            View skeletonView = inflater.inflate(layoutResId, container, false);
            container.addView(skeletonView);
            skeletonViews[i] = skeletonView;

            // 创建闪烁动画
            ObjectAnimator animator = ObjectAnimator.ofFloat(skeletonView, "alpha", 1f, 0.5f, 1f);
            animator.setDuration(1500);
            animator.setRepeatCount(ValueAnimator.INFINITE);
            animator.setStartDelay(i * 100L); // 错开动画开始时间
            animator.start();
            animators[i] = animator;
        }
    }

    /**
     * 隐藏骨架屏
     */
    public void hide() {
        if (!isShowing) return;
        isShowing = false;

        // 停止所有动画
        if (animators != null) {
            for (ObjectAnimator animator : animators) {
                if (animator != null) {
                    animator.cancel();
                }
            }
        }

        // 移除骨架视图
        container.removeAllViews();
        skeletonViews = null;
        animators = null;
    }

    /**
     * 是否正在显示
     */
    public boolean isShowing() {
        return isShowing;
    }
}
