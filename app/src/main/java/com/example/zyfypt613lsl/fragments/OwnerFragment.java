package com.example.zyfypt613lsl.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.example.zyfypt613lsl.R;
import com.example.zyfypt613lsl.activities.LoginActivity;
import com.example.zyfypt613lsl.activities.MyCollectMainActivity;
import com.example.zyfypt613lsl.activities.MyFocusMainActivity;
import com.example.zyfypt613lsl.activities.SettingsActivity;
import com.example.zyfypt613lsl.common.Common;
import com.example.zyfypt613lsl.service.UserService;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * "我的"界面：展示个人信息、学号、姓名，以及收藏 / 关注 / 设置入口。
 */
public class OwnerFragment extends BaseFragment {

    private SwipeRefreshLayout swipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_owner, container, false);

        // 初始化下拉刷新
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setColorSchemeResources(
                    R.color.colorPrimary,
                    R.color.colorAccent,
                    R.color.colorSecondary
            );
            swipeRefreshLayout.setOnRefreshListener(() -> {
                // 模拟刷新
                swipeRefreshLayout.postDelayed(() -> {
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 1000);
            });
        }

        TextView tvName = view.findViewById(R.id.tv_owner_name);
        TextView tvId = view.findViewById(R.id.tv_owner_id);
        View tvFavorite = view.findViewById(R.id.tv_owner_favorite);
        View tvFocus = view.findViewById(R.id.tv_owner_focus);
        View tvSettings = view.findViewById(R.id.tv_owner_settings);
        View tvLogout = view.findViewById(R.id.tv_owner_logout);

        // 按要求设置个人信息
        tvId.setText("ID: 23001010613");
        tvName.setText("李松伦");

        tvFavorite.setOnClickListener(v -> openMyCollect());
        tvFocus.setOnClickListener(v -> openMyFocus());
        tvSettings.setOnClickListener(v -> openSettings());
        tvLogout.setOnClickListener(v -> doLogout());

        return view;
    }

    private void openMyCollect() {
        FragmentActivity activity = getActivity();
        if (activity == null) return;
        activity.startActivity(new Intent(activity, MyCollectMainActivity.class));
    }

    private void openMyFocus() {
        FragmentActivity activity = getActivity();
        if (activity == null) return;
        activity.startActivity(new Intent(activity, MyFocusMainActivity.class));
    }

    private void openSettings() {
        FragmentActivity activity = getActivity();
        if (activity == null) return;
        activity.startActivity(new Intent(activity, SettingsActivity.class));
    }

    private void doLogout() {
        FragmentActivity activity = getActivity();
        if (activity == null) return;
        String sessionId = getSessionID();
        if (sessionId == null || sessionId.isEmpty()) {
            Toast.makeText(activity, "当前未登录", Toast.LENGTH_SHORT).show();
            return;
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Common.BASEURL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
        UserService userService = retrofit.create(UserService.class);
        userService.logout(sessionId).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                Toast.makeText(activity, "已注销登录", Toast.LENGTH_SHORT).show();
                // 清空本地 SessionID
                activity.getSharedPreferences("login", android.content.Context.MODE_PRIVATE)
                        .edit()
                        .remove(Common.SESSION_HEADER)
                        .apply();
                // 返回登录页
                activity.startActivity(new Intent(activity, LoginActivity.class));
                activity.finish();
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Toast.makeText(activity, "注销失败：" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
