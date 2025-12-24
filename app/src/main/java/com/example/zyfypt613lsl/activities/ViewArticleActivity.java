package com.example.zyfypt613lsl.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zyfypt613lsl.R;
import com.example.zyfypt613lsl.common.Common;
import com.example.zyfypt613lsl.service.CollectService;
import com.example.zyfypt613lsl.service.FocusService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ViewArticleActivity extends BaseActivity {
    private static final String TAG = "ViewArticleActivity";
    private static final String PREFS_LOGIN = "login";
    private static final String MODULE_NAME = "article";

    private SharedPreferences sp;
    private String sessionId;
    private TextView btnFollow;
    private TextView btnCollect;
    private ImageButton btnBack;
    private boolean isFollowing;
    private boolean isCollected;
    private int residValue = -1;
    private int idolId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_article2);

        // 获取传递的参数
        String resid = getIntent().getStringExtra("resid");
        String userid = getIntent().getStringExtra("userid");

        System.out.println("--文章详情--resid: " + resid);
        System.out.println("--文章详情--userid: " + userid);

        sp = getSharedPreferences(PREFS_LOGIN, MODE_PRIVATE);
        sessionId = sp.getString(Common.SESSION_HEADER, "");

        btnBack = findViewById(R.id.btn_back);
        btnFollow = findViewById(R.id.btn_follow);
        btnCollect = findViewById(R.id.btn_collect);

        setupHeaderActions();

        WebView webView = findViewById(R.id.webview);

        // 配置WebView
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true); // 启用JavaScript
        webSettings.setDomStorageEnabled(true); // 启用DOM存储
        webSettings.setLoadWithOverviewMode(true); // 缩放至屏幕大小
        webSettings.setUseWideViewPort(true); // 使用宽视口

        // 设置WebViewClient，防止在浏览器中打开
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false; // let WebView handle internal navigation such as toggle
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }
        });

        // 拼接完整URL并加载网页
        if (resid != null && !resid.isEmpty()) {
            try {
                residValue = Integer.parseInt(resid);
            } catch (NumberFormatException ignored) {
            }
            String articleUrl = Common.ARTICLEURL + resid;
            Log.d(TAG, "完整URL: " + articleUrl);
            webView.loadUrl(articleUrl);
            syncCollectState();
        } else {
            Log.w(TAG, "resid为空，无法加载文章");
        }

        if (userid != null && !userid.isEmpty()) {
            try {
                idolId = Integer.parseInt(userid);
            } catch (NumberFormatException ignored) {
            }
        }
        syncFollowState();
    }

    private void setupHeaderActions() {
        btnBack.setOnClickListener(v -> finish());

        btnFollow.setOnClickListener(v -> {
            if (sessionId == null || sessionId.isEmpty() || idolId <= 0) {
                Toast.makeText(this, "请先登录并确保作者信息完整", Toast.LENGTH_SHORT).show();
                return;
            }
            if (isFollowing) {
                sendFocusRequest(false);
            } else {
                sendFocusRequest(true);
            }
        });

        btnCollect.setOnClickListener(v -> {
            if (sessionId == null || sessionId.isEmpty() || residValue <= 0) {
                Toast.makeText(this, "请先登录并确保文章已经载入", Toast.LENGTH_SHORT).show();
                return;
            }
            if (isCollected) {
                sendCollectRequest(false);
            } else {
                sendCollectRequest(true);
            }
        });
    }

    private void syncFollowState() {
        if (sessionId.isEmpty() || idolId <= 0) {
            return;
        }
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Common.BASEURL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
        FocusService focusService = retrofit.create(FocusService.class);
        focusService.exists(idolId, sessionId).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // 接口文档：返回 "0" 表示已关注，"1" 表示未关注
                    isFollowing = "0".equals(response.body().trim());
                    btnFollow.setText(isFollowing ? "已关注" : "关注");
                    btnFollow.setBackgroundResource(isFollowing ? R.drawable.bg_btn_unfollow : R.drawable.bg_btn_follow);
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.w(TAG, "查询关注状态失败", t);
            }
        });
    }

    private void syncCollectState() {
        if (sessionId.isEmpty() || residValue <= 0) {
            return;
        }
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Common.BASEURL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
        CollectService collectService = retrofit.create(CollectService.class);
        collectService.exists(MODULE_NAME, residValue, sessionId).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // 接口文档：返回 "0" 表示已收藏，"1" 表示未收藏
                    isCollected = "0".equals(response.body().trim());
                    btnCollect.setText(isCollected ? "已收藏" : "收藏");
                    btnCollect.setBackgroundResource(isCollected ? R.drawable.bg_btn_unfollow : R.drawable.bg_btn_collect);
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.w(TAG, "查询收藏状态失败", t);
            }
        });
    }

    private void sendFocusRequest(boolean follow) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Common.BASEURL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
        FocusService focusService = retrofit.create(FocusService.class);
        Call<String> call = follow ? focusService.focus(idolId, sessionId) : focusService.unfocus(idolId, sessionId);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    isFollowing = follow;
                    btnFollow.setText(isFollowing ? "已关注" : "关注");
                    btnFollow.setBackgroundResource(isFollowing ? R.drawable.bg_btn_unfollow : R.drawable.bg_btn_follow);
                    Toast.makeText(ViewArticleActivity.this, follow ? "关注成功" : "取消关注成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ViewArticleActivity.this, "操作失败: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e(TAG, "关注操作失败", t);
                Toast.makeText(ViewArticleActivity.this, "网络异常，请重试", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendCollectRequest(boolean collect) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Common.BASEURL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
        CollectService collectService = retrofit.create(CollectService.class);
        Call<String> call = collect ? collectService.collect(MODULE_NAME, residValue, sessionId)
                : collectService.uncollect(MODULE_NAME, residValue, sessionId);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    isCollected = collect;
                    btnCollect.setText(isCollected ? "已收藏" : "收藏");
                    btnCollect.setBackgroundResource(isCollected ? R.drawable.bg_btn_unfollow : R.drawable.bg_btn_collect);
                    Toast.makeText(ViewArticleActivity.this, collect ? "收藏成功" : "取消收藏成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ViewArticleActivity.this, "操作失败: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e(TAG, "收藏操作失败", t);
                Toast.makeText(ViewArticleActivity.this, "网络异常，请重试", Toast.LENGTH_SHORT).show();
            }
        });
    }
}