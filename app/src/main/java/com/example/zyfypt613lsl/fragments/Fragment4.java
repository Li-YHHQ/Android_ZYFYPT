package com.example.zyfypt613lsl.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.zyfypt613lsl.R;

/**
 * 案例界面 - wangtaoF4
 * 案例：显示案例列表 (tcase)
 * 项目：显示项目列表 (project)
 */
public class Fragment4 extends ResourceListFragment {

    private Button btnCase;
    private Button btnProject;
    private boolean isCaseSelected = true;

    @Override
    protected String getModuleName() {
        return "tcase";
    }

    @Override
    protected String getSpecialModuleName() {
        return "project";
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment4;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        btnCase = view.findViewById(R.id.btn_latest);
        btnProject = view.findViewById(R.id.btn_special);
        
        if (btnCase != null && btnProject != null) {
            btnCase.setOnClickListener(v -> {
                if (!isCaseSelected) {
                    isCaseSelected = true;
                    updateButtonStyles();
                    switchToNormalMode();
                }
            });
            
            btnProject.setOnClickListener(v -> {
                if (isCaseSelected) {
                    isCaseSelected = false;
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
        
        if (isCaseSelected) {
            btnCase.setBackgroundTintList(android.content.res.ColorStateList.valueOf(primaryColor));
            btnCase.setTextColor(0xFFFFFFFF);
            btnProject.setBackgroundTintList(android.content.res.ColorStateList.valueOf(cardColor));
            btnProject.setTextColor(textSecondary);
        } else {
            btnCase.setBackgroundTintList(android.content.res.ColorStateList.valueOf(cardColor));
            btnCase.setTextColor(textSecondary);
            btnProject.setBackgroundTintList(android.content.res.ColorStateList.valueOf(primaryColor));
            btnProject.setTextColor(0xFFFFFFFF);
        }
    }
}
