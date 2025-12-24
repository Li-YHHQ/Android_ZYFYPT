package com.example.zyfypt613lsl.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.zyfypt613lsl.R;

/**
 * 视频界面 - wangtaoF3
 * 最新：显示最新视频列表 (video)
 * 专题：显示视频专题列表
 */
public class Fragment3 extends ResourceListFragment {

    private Button btnLatest;
    private Button btnSpecial;
    private boolean isLatestSelected = true;

    @Override
    protected String getModuleName() {
        return "video";
    }

    @Override
    protected String getSpecialModuleName() {
        return "video"; // 专题视频使用特殊接口
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment3;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnLatest = view.findViewById(R.id.btn_latest);
        btnSpecial = view.findViewById(R.id.btn_special);

        if (btnLatest != null && btnSpecial != null) {
            btnLatest.setOnClickListener(v -> {
                if (!isLatestSelected) {
                    isLatestSelected = true;
                    updateButtonStyles();
                    switchToNormalMode();
                }
            });

            btnSpecial.setOnClickListener(v -> {
                if (isLatestSelected) {
                    isLatestSelected = false;
                    updateButtonStyles();
                    switchToSpecialMode();
                }
            });
        }
    }

    private void updateButtonStyles() {
        // 使用主题色
        int primaryColor = getResources().getColor(R.color.colorPrimary, null);
        int cardColor = getResources().getColor(R.color.background_card, null);
        int textSecondary = getResources().getColor(R.color.text_secondary, null);
        
        if (isLatestSelected) {
            btnLatest.setBackgroundTintList(android.content.res.ColorStateList.valueOf(primaryColor));
            btnLatest.setTextColor(0xFFFFFFFF);
            btnSpecial.setBackgroundTintList(android.content.res.ColorStateList.valueOf(cardColor));
            btnSpecial.setTextColor(textSecondary);
        } else {
            btnLatest.setBackgroundTintList(android.content.res.ColorStateList.valueOf(cardColor));
            btnLatest.setTextColor(textSecondary);
            btnSpecial.setBackgroundTintList(android.content.res.ColorStateList.valueOf(primaryColor));
            btnSpecial.setTextColor(0xFFFFFFFF);
        }
    }
}
