package com.example.zyfypt613lsl.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.zyfypt613lsl.R;
import com.example.zyfypt613lsl.bean.ResBean;
import com.example.zyfypt613lsl.common.Common;
import com.example.zyfypt613lsl.service.CollectService;
import com.example.zyfypt613lsl.service.FocusService;
import com.example.zyfypt613lsl.service.ResService;
import com.github.barteksc.pdfviewer.PDFView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class DetailActivity extends BaseActivity implements SeekBar.OnSeekBarChangeListener {
    // Video player related constants
    private static final int REQUEST_WRITE_STORAGE = 112;
    private static final int UPDATE_INTERVAL = 1000; // 1 second

    // Video player UI elements
    private FrameLayout videoContainer;
    private VideoView videoView;
    private View controllerView;
    private ImageButton btnPlayPause;
    private ImageButton btnFullscreen;
    private ImageButton btnBrightness;
    private ImageButton btnVolume;
    private ImageButton btnMute;
    private SeekBar seekBar;
    private SeekBar seekVolume;
    private SeekBar seekBrightness;
    private TextView txtCurrentTime;
    private TextView txtTotalTime;
    private TextView txtBrightnessValue;
    private TextView txtVolumeValue;
    private View brightnessContainer;
    private View volumeContainer;
    private View touchOverlay;

    // Video player state
    private boolean isPlaying = false;
    private boolean isFullscreen = false;
    private boolean isMuted = false;
    private int savedVolume = 0;
    private int originalOrientation;
    private int originalWidth;
    private int originalHeight;
    private AudioManager audioManager;
    private Handler handler = new Handler();
    private Runnable updateProgressRunnable;

    private ViewPager2 previewPager;
    private ResourcePreviewAdapter previewAdapter;
    private TextView title;
    private TextView author;
    private TextView time;
    private int residValue = -1;
    private int idolId = -1;
    private TextView description;
    private TextView contentBody;
    private TextView attach;
    private WebView contentWebView;
    private PDFView pdfViewer;
    private View btnPdfPrev;
    private View btnPdfNext;
    private TextView tvPdfPage;
    private int currentPdfPage = 0;
    private int totalPdfPages = 0;
    private static final String PREFS_LOGIN = "login";
    private String moduleName = "tware"; // 动态设置，根据传入的 mod 参数
    private SharedPreferences sp;
    private String sessionId;
    private View btnFollow;
    private View btnCollect;
    private TextView tvFollowText;
    private TextView tvCollectText;
    private ImageButton btnBack;
    private boolean isFollowing;
    private boolean isCollected;
    private final OkHttpClient okHttpClient = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resource_detail);

        // Initialize views
        previewPager = findViewById(R.id.pager_resource_preview);
        title = findViewById(R.id.tv_resource_title);
        author = findViewById(R.id.tv_resource_author);
        time = findViewById(R.id.tv_resource_time);
        description = findViewById(R.id.tv_resource_description);
        contentBody = findViewById(R.id.tv_resource_content);
        attach = findViewById(R.id.tv_resource_attach);
        contentWebView = findViewById(R.id.webview_resource_content);
        pdfViewer = findViewById(R.id.pdf_viewer);
        btnPdfPrev = findViewById(R.id.btn_pdf_prev);
        btnPdfNext = findViewById(R.id.btn_pdf_next);
        tvPdfPage = findViewById(R.id.tv_pdf_page);
        btnFollow = findViewById(R.id.btn_follow);
        btnCollect = findViewById(R.id.btn_collect);
        tvFollowText = findViewById(R.id.tv_follow_text);
        tvCollectText = findViewById(R.id.tv_collect_text);
        btnBack = findViewById(R.id.btn_back);

        // Initialize video player views
        videoContainer = findViewById(R.id.video_container);
        videoView = findViewById(R.id.video_view);
        
        // Find views from included layout - must search within videoContainer
        if (videoContainer != null) {
            controllerView = videoContainer.findViewById(R.id.controller_layout);
            btnPlayPause = videoContainer.findViewById(R.id.btnPlayPause);
            btnFullscreen = videoContainer.findViewById(R.id.btnFullscreen);
            btnBrightness = videoContainer.findViewById(R.id.btnBrightness);
            btnVolume = videoContainer.findViewById(R.id.btnVolume);
            btnMute = videoContainer.findViewById(R.id.btnMute);
            seekBar = videoContainer.findViewById(R.id.seekBar);
            seekVolume = videoContainer.findViewById(R.id.seekVolume);
            seekBrightness = videoContainer.findViewById(R.id.seekBrightness);
            txtCurrentTime = videoContainer.findViewById(R.id.txtCurrentTime);
            txtTotalTime = videoContainer.findViewById(R.id.txtTotalTime);
            txtBrightnessValue = videoContainer.findViewById(R.id.txtBrightnessValue);
            txtVolumeValue = videoContainer.findViewById(R.id.txtVolumeValue);
            brightnessContainer = videoContainer.findViewById(R.id.brightnessContainer);
            volumeContainer = videoContainer.findViewById(R.id.volumeContainer);
            touchOverlay = videoContainer.findViewById(R.id.touchOverlay);
        }

        // Initialize audio manager
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        // Initialize preview adapter
        previewAdapter = new ResourcePreviewAdapter(this);
        previewPager.setAdapter(previewAdapter);

        // Initialize video player
        initVideoPlayer();
        
        // Request WRITE_SETTINGS permission for brightness control
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(this)) {
                // Note: We'll handle this gracefully without forcing the user to grant permission
                Log.w("VideoPlayer", "WRITE_SETTINGS permission not granted");
            }
        }

        int id = getIntent().getIntExtra("id", -1);
        String mod = getIntent().getStringExtra("mod");
        String sessionID = getIntent().getStringExtra("sessionID");
        if (id <= 0 || TextUtils.isEmpty(mod) || TextUtils.isEmpty(sessionID)) {
            Toast.makeText(this, "课件信息不完整", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        sessionId = sessionID;
        moduleName = mod; // 根据传入的 mod 动态设置模块名

        // 根据模块类型设置标题
        TextView headerTitle = findViewById(R.id.tv_header_title);
        if (headerTitle != null) {
            switch (mod) {
                case "tware":
                    headerTitle.setText("课件详情");
                    break;
                case "video":
                    headerTitle.setText("视频详情");
                    break;
                case "tcase":
                    headerTitle.setText("案例详情");
                    break;
                case "project":
                    headerTitle.setText("项目详情");
                    break;
                default:
                    headerTitle.setText("详情");
                    break;
            }
        }

        loadResourceDetail(mod, id, sessionID);
        setupHeaderActions();
    }

    private void setupHeaderActions() {
        btnBack.setOnClickListener(v -> finish());

        btnFollow.setOnClickListener(v -> {
            if (sessionId == null || sessionId.isEmpty()) {
                Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
                return;
            }
            if (idolId > 0) {
                toggleFollow();
            } else {
                Toast.makeText(this, "无法关注，用户ID无效", Toast.LENGTH_SHORT).show();
            }
        });

        btnCollect.setOnClickListener(v -> {
            if (sessionId == null || sessionId.isEmpty()) {
                Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
                return;
            }
            if (residValue > 0) {
                toggleCollect();
            } else {
                Toast.makeText(this, "无法收藏，资源ID无效", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadResourceDetail(String mod, int id, String sessionID) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Common.BASEURL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ResService resService = retrofit.create(ResService.class);
        resService.getDetail(mod, id, sessionID).enqueue(new retrofit2.Callback<ResBean>() {
            @Override
            public void onResponse(retrofit2.Call<ResBean> call, retrofit2.Response<ResBean> response) {
                if (response.isSuccessful() && response.body() != null) {
                    populate(response.body());
                } else {
                    Toast.makeText(DetailActivity.this, "获取失败：" + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ResBean> call, Throwable t) {
                Toast.makeText(DetailActivity.this, "网络异常，请稍后重试", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<PreviewItem> buildPreviewItems(ResBean data) {
        List<PreviewItem> items = new ArrayList<>();
        
        // 添加封面图片
        String thumbUrl = data.getThumb();
        if (!TextUtils.isEmpty(thumbUrl)) {
            String fullThumbUrl = Common.buildUploadUrl(thumbUrl);
            Log.d("DetailActivity", "课件封面图片URL: " + fullThumbUrl);
            items.add(new PreviewItem(fullThumbUrl, PreviewType.IMAGE, "封面"));
        } else {
            Log.d("DetailActivity", "课件没有封面图片");
        }
        
        // 添加PDF预览
        if (!TextUtils.isEmpty(data.getPdfattach())) {
            String pdfUrl = Common.buildUploadUrl(data.getPdfattach());
            Log.d("DetailActivity", "PDF附件URL: " + pdfUrl);
            items.add(new PreviewItem(pdfUrl, PreviewType.PDF, "PDF附件"));
        }
        
        // 添加视频预览
        if (!TextUtils.isEmpty(data.getVideopath())) {
            String videoUrl = Common.buildVideoUrl(data.getVideopath());
            Log.d("DetailActivity", "视频URL: " + videoUrl);
            items.add(new PreviewItem(videoUrl, PreviewType.VIDEO, "视频附件"));
        }
        
        // 如果没有任何预览内容，显示占位符
        if (items.isEmpty()) {
            Log.d("DetailActivity", "没有预览内容，显示占位符");
            items.add(new PreviewItem("", PreviewType.PLACEHOLDER, "暂无预览"));
        }
        
        Log.d("DetailActivity", "预览项目数量: " + items.size());
        return items;
    }

    private void populate(ResBean data) {
        // 调试日志 - 打印接收到的数据
        Log.d("DetailActivity", "=== 课件详情数据 ===");
        Log.d("DetailActivity", "ID: " + data.getId());
        Log.d("DetailActivity", "名称: " + data.getName());
        Log.d("DetailActivity", "作者: " + data.getAuthor());
        Log.d("DetailActivity", "封面图(thumb): " + data.getThumb());
        Log.d("DetailActivity", "PDF附件: " + data.getPdfattach());
        Log.d("DetailActivity", "视频路径: " + data.getVideopath());
        Log.d("DetailActivity", "描述: " + data.getDescription());
        Log.d("DetailActivity", "========================");
        
        title.setText(data.getName());
        author.setText(data.getAuthor());
        time.setText(data.getUpdate_time());
        description.setText(data.getDescription());
        previewAdapter.setItems(buildPreviewItems(data));
        String contentText = !TextUtils.isEmpty(data.getDescription()) ? data.getDescription() : "暂无课件内容描述";
        contentBody.setText(contentText);

        String pdfUrl = Common.buildUploadUrl(data.getPdfattach());
        String videoUrl = Common.buildVideoUrl(data.getVideopath());

        // 启用JavaScript和插件支持
        WebSettings webSettings = contentWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);

        boolean hasPdf = !TextUtils.isEmpty(data.getPdfattach());
        boolean hasVideo = !TextUtils.isEmpty(data.getVideopath());
        boolean hasThumb = !TextUtils.isEmpty(data.getThumb());

        // 获取PDF和视频卡片容器
        View pdfCard = findViewById(R.id.pdf_card);
        View videoCard = findViewById(R.id.video_card);
        View previewCard = findViewById(R.id.preview_card);

        // 隐藏WebView，我们不再使用它
        contentWebView.setVisibility(View.GONE);

        if (hasPdf) {
            // 课件模式：显示PDF查看器，不显示封面
            if (pdfCard != null) pdfCard.setVisibility(View.VISIBLE);
            pdfViewer.setVisibility(View.VISIBLE);
            if (videoCard != null) videoCard.setVisibility(View.GONE);
            if (videoContainer != null) videoContainer.setVisibility(View.GONE);
            if (previewCard != null) previewCard.setVisibility(View.GONE);
            if (previewPager != null) previewPager.setVisibility(View.GONE);
            attach.setVisibility(View.GONE);
            
            // 直接在页面内加载PDF
            showPdfInsidePage(pdfUrl);
        } else if (hasVideo) {
            // 显示视频播放器卡片
            if (videoCard != null) videoCard.setVisibility(View.VISIBLE);
            videoContainer.setVisibility(View.VISIBLE);
            if (pdfCard != null) pdfCard.setVisibility(View.GONE);
            pdfViewer.setVisibility(View.GONE);
            previewPager.setVisibility(View.GONE); // 视频模式隐藏预览图
            attach.setVisibility(View.GONE);

            // Re-initialize video player views after container is visible
            reinitializeVideoControls();

            // Set video URI and prepare for playback
            try {
                Log.d("VideoPlayer", "Loading video from: " + videoUrl);
                Uri videoUri = Uri.parse(videoUrl);
                
                // Clear any previous video
                if (videoView != null) {
                    videoView.stopPlayback();
                    videoView.setVideoURI(null);
                    
                    // Set new video URI
                    videoView.setVideoURI(videoUri);
                    videoView.requestFocus();
                }
                
                // Start in paused state
                isPlaying = false;
                updatePlayPauseButton();
                
                // Show controls
                showController();
            } catch (Exception e) {
                Toast.makeText(this, "视频加载失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("VideoPlayer", "Error loading video", e);
            }
        } else {
            // 没有PDF和视频，只显示封面和描述
            if (videoCard != null) videoCard.setVisibility(View.GONE);
            videoContainer.setVisibility(View.GONE);
            if (pdfCard != null) pdfCard.setVisibility(View.GONE);
            pdfViewer.setVisibility(View.GONE);
            previewPager.setVisibility(View.VISIBLE);
            attach.setVisibility(View.VISIBLE);
            attach.setText("暂无附件");
        }

        if (data != null) {
            residValue = parseSafeInt(data.getId());
            idolId = parseSafeInt(data.getUserId());
            syncCollectState();
            syncFollowState();
        }
    }

    private void reinitializeVideoControls() {
        // Re-find views from included layout after container is visible
        if (videoContainer != null) {
            controllerView = videoContainer.findViewById(R.id.controller_layout);
            btnPlayPause = videoContainer.findViewById(R.id.btnPlayPause);
            btnFullscreen = videoContainer.findViewById(R.id.btnFullscreen);
            btnBrightness = videoContainer.findViewById(R.id.btnBrightness);
            btnVolume = videoContainer.findViewById(R.id.btnVolume);
            btnMute = videoContainer.findViewById(R.id.btnMute);
            seekBar = videoContainer.findViewById(R.id.seekBar);
            seekVolume = videoContainer.findViewById(R.id.seekVolume);
            seekBrightness = videoContainer.findViewById(R.id.seekBrightness);
            txtCurrentTime = videoContainer.findViewById(R.id.txtCurrentTime);
            txtTotalTime = videoContainer.findViewById(R.id.txtTotalTime);
            txtBrightnessValue = videoContainer.findViewById(R.id.txtBrightnessValue);
            txtVolumeValue = videoContainer.findViewById(R.id.txtVolumeValue);
            brightnessContainer = videoContainer.findViewById(R.id.brightnessContainer);
            volumeContainer = videoContainer.findViewById(R.id.volumeContainer);
            touchOverlay = videoContainer.findViewById(R.id.touchOverlay);
            
            Log.d("VideoPlayer", "Controls reinitialized: btnPlayPause=" + btnPlayPause + 
                  ", btnVolume=" + btnVolume + ", btnBrightness=" + btnBrightness + ", btnMute=" + btnMute);
            
            // Re-setup click listeners
            initVideoPlayer();
        }
    }

    private void initVideoPlayer() {
        // Check if views are properly initialized
        if (videoView == null) {
            Log.e("VideoPlayer", "videoView is null");
            return;
        }
        
        if (btnPlayPause == null) {
            Log.e("VideoPlayer", "btnPlayPause is null - controls may not be found");
        }
        
        // Set up video view - ensure proper rendering
        videoView.setOnPreparedListener(mp -> {
            Log.d("VideoPlayer", "Video prepared, duration: " + videoView.getDuration());
            // Update total time when video is prepared
            int duration = videoView.getDuration();
            if (txtTotalTime != null) {
                txtTotalTime.setText(formatTime(duration));
            }
            
            // Set initial progress
            updateProgress();
            
            // Start in paused state
            videoView.pause();
            isPlaying = false;
            updatePlayPauseButton();
            
            // Show controls
            showController();
            
            // Enable video scaling
            try {
                mp.setVideoScalingMode(android.media.MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
            } catch (Exception e) {
                Log.e("VideoPlayer", "Error setting video scaling mode", e);
            }
        });
        
        // Handle video completion
        videoView.setOnCompletionListener(mp -> {
            isPlaying = false;
            updatePlayPauseButton();
            videoView.seekTo(0);
        });
        
        // Handle video errors
        videoView.setOnErrorListener((mp, what, extra) -> {
            Log.e("VideoPlayer", "Video error: what=" + what + ", extra=" + extra);
            Toast.makeText(this, "视频播放错误: " + what, Toast.LENGTH_SHORT).show();
            return true;
        });

        // Set up play/pause button
        if (btnPlayPause != null) {
            btnPlayPause.setOnClickListener(v -> {
                Log.d("VideoPlayer", "Play/Pause button clicked");
                togglePlayPause();
            });
        }

        // Set up fullscreen button
        if (btnFullscreen != null) {
            btnFullscreen.setOnClickListener(v -> {
                Log.d("VideoPlayer", "Fullscreen button clicked");
                toggleFullscreen();
            });
        }

        // Set up brightness button
        if (btnBrightness != null) {
            btnBrightness.setOnClickListener(v -> {
                Log.d("VideoPlayer", "Brightness button clicked");
                if (brightnessContainer.getVisibility() == View.VISIBLE) {
                    brightnessContainer.setVisibility(View.GONE);
                } else {
                    brightnessContainer.setVisibility(View.VISIBLE);
                    volumeContainer.setVisibility(View.GONE);
                }
                showController();
            });
        }

        // Set up volume button
        if (btnVolume != null) {
            btnVolume.setOnClickListener(v -> {
                Log.d("VideoPlayer", "Volume button clicked");
                if (volumeContainer.getVisibility() == View.VISIBLE) {
                    volumeContainer.setVisibility(View.GONE);
                } else {
                    volumeContainer.setVisibility(View.VISIBLE);
                    brightnessContainer.setVisibility(View.GONE);
                }
                showController();
            });
        }

        // Set up mute button
        if (btnMute != null) {
            btnMute.setOnClickListener(v -> {
                Log.d("VideoPlayer", "Mute button clicked");
                toggleMute();
            });
        }

        // Set up volume controls
        if (seekVolume != null && audioManager != null) {
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            seekVolume.setMax(maxVolume);
            seekVolume.setProgress(currentVolume);
            seekVolume.setOnSeekBarChangeListener(this);
            if (txtVolumeValue != null) {
                txtVolumeValue.setText(currentVolume * 100 / maxVolume + "%");
            }
        }

        // Set up brightness controls
        if (seekBrightness != null) {
            seekBrightness.setMax(100);
            try {
                int brightness = Settings.System.getInt(
                        getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS
                );
                int progress = (int) (brightness / 2.55f);
                seekBrightness.setProgress(progress);
                if (txtBrightnessValue != null) {
                    txtBrightnessValue.setText(progress + "%");
                }
            } catch (Settings.SettingNotFoundException e) {
                seekBrightness.setProgress(50);
                if (txtBrightnessValue != null) {
                    txtBrightnessValue.setText("50%");
                }
            }
            seekBrightness.setOnSeekBarChangeListener(this);
        }

        // Set up seek bar
        if (seekBar != null) {
            seekBar.setOnSeekBarChangeListener(this);
        }

        // Set up touch overlay for gesture control
        if (touchOverlay != null) {
            touchOverlay.setOnTouchListener(new View.OnTouchListener() {
                private float touchStartX = 0;
                private float touchStartY = 0;
                private boolean isScrolling = false;
                
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            touchStartX = event.getX();
                            touchStartY = event.getY();
                            isScrolling = false;
                            return true;
                            
                        case MotionEvent.ACTION_MOVE:
                            float deltaX = Math.abs(event.getX() - touchStartX);
                            float deltaY = Math.abs(event.getY() - touchStartY);
                            
                            // Detect vertical scroll gesture
                            if (deltaY > 30 && deltaY > deltaX) {
                                isScrolling = true;
                                float screenWidth = v.getWidth();
                                
                                // Left half - brightness control
                                if (touchStartX < screenWidth / 2) {
                                    brightnessContainer.setVisibility(View.VISIBLE);
                                    volumeContainer.setVisibility(View.GONE);
                                    showController();
                                }
                                // Right half - volume control
                                else {
                                    volumeContainer.setVisibility(View.VISIBLE);
                                    brightnessContainer.setVisibility(View.GONE);
                                    showController();
                                }
                            }
                            return true;
                            
                        case MotionEvent.ACTION_UP:
                            if (!isScrolling) {
                                // Simple tap - toggle controls
                                if (controllerView.getVisibility() == View.VISIBLE) {
                                    hideController();
                                } else {
                                    showController();
                                }
                            }
                            return true;
                    }
                    return false;
                }
            });
        }

        // Show/hide controls on video view click
        videoView.setOnClickListener(v -> {
            if (controllerView.getVisibility() == View.VISIBLE) {
                hideController();
            } else {
                showController();
            }
        });

        // Show controller initially
        showController();
        
        Log.d("VideoPlayer", "Video player initialized successfully");
    }

    private void showController() {
        if (controllerView != null) {
            controllerView.setVisibility(View.VISIBLE);
            // Auto-hide after 5 seconds
            handler.removeCallbacks(hideRunnable);
            handler.postDelayed(hideRunnable, 5000);
        }
    }

    private void hideController() {
        if (controllerView != null) {
            controllerView.setVisibility(View.GONE);
        }
        if (brightnessContainer != null) {
            brightnessContainer.setVisibility(View.GONE);
        }
        if (volumeContainer != null) {
            volumeContainer.setVisibility(View.GONE);
        }
    }

    private final Runnable hideRunnable = this::hideController;

    private void updatePlayPauseButton() {
        if (btnPlayPause != null) {
            btnPlayPause.setImageResource(
                    isPlaying ?
                            android.R.drawable.ic_media_pause :
                            android.R.drawable.ic_media_play
            );
        }
        // Reset auto-hide timer when controls are used
        showController();
    }

    private void updateVolumeButton(boolean hasVolume) {
        // Volume button removed from UI, keeping method for compatibility
    }

    private void toggleMute() {
        if (audioManager == null) return;
        
        if (isMuted) {
            // 恢复音量
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, savedVolume, 0);
            isMuted = false;
            if (btnMute != null) {
                btnMute.setImageResource(R.drawable.ic_volume);
            }
            Toast.makeText(this, "已取消静音", Toast.LENGTH_SHORT).show();
        } else {
            // 保存当前音量并静音
            savedVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
            isMuted = true;
            if (btnMute != null) {
                btnMute.setImageResource(android.R.drawable.ic_lock_silent_mode);
            }
            Toast.makeText(this, "已静音", Toast.LENGTH_SHORT).show();
        }
        showController();
    }

    private void togglePlayPause() {
        Log.d("VideoPlayer", "togglePlayPause called, videoView=" + videoView);
        if (videoView == null) {
            Log.e("VideoPlayer", "videoView is null!");
            return;
        }
        
        try {
            if (videoView.isPlaying()) {
                Log.d("VideoPlayer", "Pausing video");
                videoView.pause();
                isPlaying = false;
                handler.removeCallbacks(updateProgressRunnable);
            } else {
                Log.d("VideoPlayer", "Starting video");
                videoView.start();
                isPlaying = true;
                startProgressUpdate();
            }
            updatePlayPauseButton();
        } catch (Exception e) {
            Log.e("VideoPlayer", "Error in togglePlayPause", e);
            Toast.makeText(this, "播放出错: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void startProgressUpdate() {
        if (updateProgressRunnable != null) {
            handler.removeCallbacks(updateProgressRunnable);
        }

        updateProgressRunnable = new Runnable() {
            @Override
            public void run() {
                updateProgress();
                if (isPlaying) {
                    handler.postDelayed(this, UPDATE_INTERVAL);
                }
            }
        };

        handler.post(updateProgressRunnable);
    }

    private void updateProgress() {
        if (videoView != null && videoView.getDuration() > 0) {
            int currentPosition = videoView.getCurrentPosition();
            int duration = videoView.getDuration();

            // Update seek bar
            if (duration > 0) {
                int progress = (int) (((float) currentPosition / duration) * 100);
                if (seekBar != null) {
                    seekBar.setProgress(progress);
                }
                if (txtCurrentTime != null) {
                    txtCurrentTime.setText(formatTime(currentPosition));
                }
            }
        }
    }

    private String formatTime(int millis) {
        int seconds = (millis / 1000) % 60;
        int minutes = (millis / (1000 * 60)) % 60;
        int hours = millis / (1000 * 60 * 60);

        if (hours > 0) {
            return String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        }
    }

    private void toggleFullscreen() {
        if (isFullscreen) {
            // Exit fullscreen - return to portrait
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            if (getSupportActionBar() != null) {
                getSupportActionBar().show();
            }
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            
            // Remove from decor view
            ViewGroup decorView = (ViewGroup) getWindow().getDecorView();
            decorView.removeView(videoContainer);
            
            // Restore the video container to its original position in the layout
            View scrollContent = findViewById(R.id.scroll_content);
            if (scrollContent instanceof android.widget.ScrollView) {
                android.widget.ScrollView scrollView = (android.widget.ScrollView) scrollContent;
                if (scrollView.getChildCount() > 0) {
                    ViewGroup contentLayout = (ViewGroup) scrollView.getChildAt(0);
                    if (contentLayout != null) {
                        // Find the correct position (after pdf_viewer)
                        View pdfViewer = findViewById(R.id.pdf_viewer);
                        int insertPosition = 2; // Default position
                        if (pdfViewer != null && pdfViewer.getParent() == contentLayout) {
                            insertPosition = contentLayout.indexOfChild(pdfViewer) + 1;
                        }
                        
                        // Restore layout params
                        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            (int) (220 * getResources().getDisplayMetrics().density)
                        );
                        videoContainer.setLayoutParams(params);
                        
                        contentLayout.addView(videoContainer, Math.min(insertPosition, contentLayout.getChildCount()));
                    }
                }
            }
            
            isFullscreen = false;
            btnFullscreen.setImageResource(android.R.drawable.ic_menu_crop);
        } else {
            // Enter fullscreen - landscape mode
            ViewGroup parent = (ViewGroup) videoContainer.getParent();
            if (parent != null) {
                // Remove from current parent
                parent.removeView(videoContainer);
                
                // Add to decor view for fullscreen
                ViewGroup decorView = (ViewGroup) getWindow().getDecorView();
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                );
                decorView.addView(videoContainer, params);
            }
            
            // Enter fullscreen with immersive mode
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
                    
            if (getSupportActionBar() != null) {
                getSupportActionBar().hide();
            }
            
            // Force landscape orientation for fullscreen
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            
            isFullscreen = true;
            btnFullscreen.setImageResource(android.R.drawable.ic_menu_revert);
        }
        
        // Show controls after toggling fullscreen
        showController();
    }

    private void setBrightness(int brightness) {
        try {
            // Update the window brightness (this works without WRITE_SETTINGS permission)
            WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
            layoutParams.screenBrightness = brightness / 100.0f;
            getWindow().setAttributes(layoutParams);
            
            // Try to save system brightness if permission is granted
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.System.canWrite(this)) {
                    Settings.System.putInt(
                            getContentResolver(),
                            Settings.System.SCREEN_BRIGHTNESS_MODE,
                            Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
                    );
                    int brightnessValue = (int) (brightness * 2.55); // Convert 0-100 to 0-255
                    Settings.System.putInt(
                            getContentResolver(),
                            Settings.System.SCREEN_BRIGHTNESS,
                            brightnessValue
                    );
                }
            }
        } catch (Exception e) {
            Log.e("VideoPlayer", "Error setting brightness", e);
        }
    }

    // Handle touch events for showing/hiding volume and brightness controls
    private float startX, startY;
    private static final int MIN_DISTANCE = 50;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();
                return true;

            case MotionEvent.ACTION_UP:
                float endX = event.getX();
                float endY = event.getY();

                // Calculate the distance moved
                float deltaX = Math.abs(endX - startX);
                float deltaY = Math.abs(endY - startY);

                // If the movement was mostly horizontal
                if (deltaX > MIN_DISTANCE && deltaX > deltaY) {
                    // Left side of screen for brightness
                    if (startX < getWindowManager().getDefaultDisplay().getWidth() / 4) {
                        brightnessContainer.setVisibility(
                                brightnessContainer.getVisibility() == View.VISIBLE ?
                                        View.GONE : View.VISIBLE);
                        volumeContainer.setVisibility(View.GONE);
                        showController();
                        return true;
                    }
                    // Right side of screen for volume
                    else if (startX > getWindowManager().getDefaultDisplay().getWidth() * 3 / 4) {
                        volumeContainer.setVisibility(
                                volumeContainer.getVisibility() == View.VISIBLE ?
                                        View.GONE : View.VISIBLE);
                        brightnessContainer.setVisibility(View.GONE);
                        showController();
                        return true;
                    }
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (!fromUser) return;
        
        Log.d("VideoPlayer", "onProgressChanged: progress=" + progress + ", seekBar=" + seekBar.getId());

        int id = seekBar.getId();
        if (id == R.id.seekBar) {
            if (videoView != null && videoView.getDuration() > 0) {
                int duration = videoView.getDuration();
                int newPosition = (int) ((progress / 100.0) * duration);
                videoView.seekTo(newPosition);
                if (txtCurrentTime != null) {
                    txtCurrentTime.setText(formatTime(newPosition));
                }
            }
        } else if (id == R.id.seekVolume) {
            if (audioManager != null) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                Log.d("VideoPlayer", "Volume set to: " + progress);
            }
            if (txtVolumeValue != null && seekVolume != null && seekVolume.getMax() > 0) {
                txtVolumeValue.setText(progress * 100 / seekVolume.getMax() + "%");
            }
            showController();
        } else if (id == R.id.seekBrightness) {
            setBrightness(progress);
            Log.d("VideoPlayer", "Brightness set to: " + progress);
            if (txtBrightnessValue != null) {
                txtBrightnessValue.setText(progress + "%");
            }
            showController();
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        handler.removeCallbacks(updateProgressRunnable);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (isPlaying) {
            startProgressUpdate();
        }
    }

    private void showPdfInsidePage(String pdfUrl) {
        // 隐藏封面预览
        View previewCard = findViewById(R.id.preview_card);
        if (previewCard != null) previewCard.setVisibility(View.GONE);
        if (previewPager != null) previewPager.setVisibility(View.GONE);
        
        attach.setVisibility(View.GONE);
        contentWebView.setVisibility(View.GONE);
        pdfViewer.setVisibility(View.VISIBLE);
        pdfViewer.useBestQuality(true);

        // 设置PDF翻页按钮点击事件
        if (btnPdfPrev != null) {
            btnPdfPrev.setOnClickListener(v -> {
                if (currentPdfPage > 0) {
                    currentPdfPage--;
                    pdfViewer.jumpTo(currentPdfPage, true);
                    updatePdfPageText();
                }
            });
        }
        
        if (btnPdfNext != null) {
            btnPdfNext.setOnClickListener(v -> {
                if (currentPdfPage < totalPdfPages - 1) {
                    currentPdfPage++;
                    pdfViewer.jumpTo(currentPdfPage, true);
                    updatePdfPageText();
                }
            });
        }

        Request request = new Request.Builder().url(pdfUrl).build();
        okHttpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(DetailActivity.this, "PDF 加载失败，请稍后重试", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull okhttp3.Response response) throws IOException {
                ResponseBody body = response.body();
                if (!response.isSuccessful() || body == null) {
                    runOnUiThread(() ->
                            Toast.makeText(DetailActivity.this, "PDF 加载失败", Toast.LENGTH_SHORT).show());
                    return;
                }
                File cacheFile = new File(getCacheDir(), "resource_preview.pdf");
                try (FileOutputStream fos = new FileOutputStream(cacheFile)) {
                    fos.write(body.bytes());
                } catch (IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(DetailActivity.this, "保存 PDF 失败", Toast.LENGTH_SHORT).show());
                    return;
                }
                runOnUiThread(() ->
                        pdfViewer.fromFile(cacheFile)
                                .swipeHorizontal(true)
                                .pageSnap(true)
                                .autoSpacing(true)
                                .pageFling(true)
                                .enableDoubletap(true)
                                .onPageChange((page, pageCount) -> {
                                    currentPdfPage = page;
                                    totalPdfPages = pageCount;
                                    updatePdfPageText();
                                })
                                .onLoad(nbPages -> {
                                    totalPdfPages = nbPages;
                                    currentPdfPage = 0;
                                    updatePdfPageText();
                                })
                                .load());
            }
        });
    }
    
    private void updatePdfPageText() {
        if (tvPdfPage != null) {
            tvPdfPage.setText("第 " + (currentPdfPage + 1) + " / " + totalPdfPages + " 页");
        }
    }

    private void syncCollectState() {
        if (sessionId == null || sessionId.isEmpty() || residValue <= 0) {
            updateCollectUI();
            return;
        }
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Common.BASEURL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
        CollectService collectService = retrofit.create(CollectService.class);
        collectService.exists(moduleName, residValue, sessionId).enqueue(new retrofit2.Callback<String>() {
            @Override
            public void onResponse(retrofit2.Call<String> call, retrofit2.Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // 接口文档：返回 "0" 表示已收藏，"1" 表示未收藏
                    isCollected = "0".equals(response.body().trim());
                    updateCollectUI();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<String> call, Throwable t) {
                Log.w("DetailActivity", "查询收藏状态失败", t);
            }
        });
    }

    private void updateCollectUI() {
        if (btnCollect != null) {
            // 直接设置按钮文字（MaterialButton）
            if (btnCollect instanceof com.google.android.material.button.MaterialButton) {
                ((com.google.android.material.button.MaterialButton) btnCollect).setText(isCollected ? "已收藏" : "收藏");
            }
            btnCollect.setBackgroundResource(isCollected ? R.drawable.bg_btn_unfollow : R.drawable.bg_btn_collect);
        }
        if (tvCollectText != null) {
            tvCollectText.setText(isCollected ? "已收藏" : "收藏");
        }
    }

    private void syncFollowState() {
        if (sessionId == null || sessionId.isEmpty() || idolId <= 0) {
            updateFollowUI();
            return;
        }
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Common.BASEURL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
        FocusService focusService = retrofit.create(FocusService.class);
        focusService.exists(idolId, sessionId).enqueue(new retrofit2.Callback<String>() {
            @Override
            public void onResponse(retrofit2.Call<String> call, retrofit2.Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // 接口文档：返回 "0" 表示已关注，"1" 表示未关注
                    isFollowing = "0".equals(response.body().trim());
                    updateFollowUI();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<String> call, Throwable t) {
                Log.w("DetailActivity", "查询关注状态失败", t);
            }
        });
    }

    private void updateFollowUI() {
        if (btnFollow != null) {
            // 直接设置按钮文字（MaterialButton）
            if (btnFollow instanceof com.google.android.material.button.MaterialButton) {
                ((com.google.android.material.button.MaterialButton) btnFollow).setText(isFollowing ? "已关注" : "关注");
            }
            btnFollow.setBackgroundResource(isFollowing ? R.drawable.bg_btn_unfollow : R.drawable.bg_btn_follow);
        }
        if (tvFollowText != null) {
            tvFollowText.setText(isFollowing ? "已关注" : "关注");
        }
    }

    private void toggleCollect() {
        // 实现收藏/取消收藏的逻辑
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Common.BASEURL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
        CollectService collectService = retrofit.create(CollectService.class);

        if (isCollected) {
            // 取消收藏
            collectService.uncollect(moduleName, residValue, sessionId)
                    .enqueue(new retrofit2.Callback<String>() {
                        @Override
                        public void onResponse(retrofit2.Call<String> call, retrofit2.Response<String> response) {
                            if (response.isSuccessful()) {
                                isCollected = false;
                                updateCollectUI();
                                Toast.makeText(DetailActivity.this, "取消收藏成功", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(DetailActivity.this, "取消收藏失败", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(retrofit2.Call<String> call, Throwable t) {
                            Toast.makeText(DetailActivity.this, "网络错误：" + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // 添加收藏
            collectService.collect(moduleName, residValue, sessionId)
                    .enqueue(new retrofit2.Callback<String>() {
                        @Override
                        public void onResponse(retrofit2.Call<String> call, retrofit2.Response<String> response) {
                            if (response.isSuccessful()) {
                                isCollected = true;
                                updateCollectUI();
                                Toast.makeText(DetailActivity.this, "收藏成功", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(DetailActivity.this, "收藏失败", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(retrofit2.Call<String> call, Throwable t) {
                            Toast.makeText(DetailActivity.this, "网络错误：" + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void toggleFollow() {
        // 实现关注/取消关注的逻辑
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Common.BASEURL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
        FocusService focusService = retrofit.create(FocusService.class);

        if (isFollowing) {
            // 取消关注
            focusService.unfocus(idolId, sessionId)
                    .enqueue(new retrofit2.Callback<String>() {
                        @Override
                        public void onResponse(retrofit2.Call<String> call, retrofit2.Response<String> response) {
                            if (response.isSuccessful()) {
                                isFollowing = false;
                                updateFollowUI();
                                Toast.makeText(DetailActivity.this, "取消关注成功", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(DetailActivity.this, "取消关注失败", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(retrofit2.Call<String> call, Throwable t) {
                            Toast.makeText(DetailActivity.this, "网络错误：" + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // 添加关注
            focusService.focus(idolId, sessionId)
                    .enqueue(new retrofit2.Callback<String>() {
                        @Override
                        public void onResponse(retrofit2.Call<String> call, retrofit2.Response<String> response) {
                            if (response.isSuccessful()) {
                                isFollowing = true;
                                updateFollowUI();
                                Toast.makeText(DetailActivity.this, "关注成功", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(DetailActivity.this, "关注失败", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(retrofit2.Call<String> call, Throwable t) {
                            Toast.makeText(DetailActivity.this, "网络错误：" + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private int parseSafeInt(String value) {
        if (TextUtils.isEmpty(value)) {
            return -1;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static class PreviewItem {
        final String url;
        final PreviewType type;
        final String label;

        PreviewItem(String url, PreviewType type, String label) {
            this.url = url;
            this.type = type;
            this.label = label;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause video when activity is paused
        if (videoView != null && videoView.isPlaying()) {
            videoView.pause();
            isPlaying = false;
            updatePlayPauseButton();
        }

        // Remove any pending callbacks
        if (handler != null) {
            handler.removeCallbacks(updateProgressRunnable);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Restart progress updates if video was playing
        if (videoView != null && isPlaying) {
            videoView.start();
            startProgressUpdate();
        }
    }

    @Override
    protected void onDestroy() {
        // Release video player resources
        if (videoView != null) {
            videoView.stopPlayback();
            videoView = null;
        }

        // Remove all callbacks and messages
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }

        // Cancel any ongoing network requests
        if (okHttpClient != null) {
            okHttpClient.dispatcher().cancelAll();
        }

        super.onDestroy();
    }

    private enum PreviewType {
        IMAGE, PDF, VIDEO, PLACEHOLDER
    }

    private static class ResourcePreviewAdapter extends RecyclerView.Adapter<ResourcePreviewAdapter.PreviewViewHolder> {

        private final List<PreviewItem> items = new ArrayList<>();
        private final LayoutInflater inflater;
        private final Context context;

        ResourcePreviewAdapter(Context context) {
            this.context = context;
            this.inflater = LayoutInflater.from(context);
        }

        void setItems(List<PreviewItem> previewItems) {
            items.clear();
            items.addAll(previewItems);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public PreviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.item_resource_preview, parent, false);
            return new PreviewViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PreviewViewHolder holder, int position) {
            PreviewItem item = items.get(position);
            holder.label.setText(item.label);
            holder.label.setVisibility(TextUtils.isEmpty(item.label) ? View.GONE : View.VISIBLE);
            
            Log.d("PreviewAdapter", "绑定预览项: type=" + item.type + ", url=" + item.url);
            
            if (item.type == PreviewType.IMAGE) {
                holder.image.setVisibility(View.VISIBLE);
                if (!TextUtils.isEmpty(item.url)) {
                    Log.d("PreviewAdapter", "加载图片: " + item.url);
                    Picasso.get()
                            .load(item.url)
                            .placeholder(R.mipmap.ic_launcher)
                            .error(R.mipmap.ic_launcher)
                            .into(holder.image, new com.squareup.picasso.Callback() {
                                @Override
                                public void onSuccess() {
                                    Log.d("PreviewAdapter", "图片加载成功: " + item.url);
                                }
                                
                                @Override
                                public void onError(Exception e) {
                                    Log.e("PreviewAdapter", "图片加载失败: " + item.url, e);
                                }
                            });
                } else {
                    holder.image.setImageResource(R.mipmap.ic_launcher);
                }
            } else if (item.type == PreviewType.VIDEO) {
                holder.image.setVisibility(View.VISIBLE);
                holder.image.setImageResource(R.drawable.ic_play_circle);
            } else if (item.type == PreviewType.PDF) {
                holder.image.setVisibility(View.VISIBLE);
                holder.image.setImageResource(R.drawable.ic_keynote);
            } else {
                holder.image.setVisibility(View.VISIBLE);
                holder.image.setImageResource(R.mipmap.ic_launcher);
            }
            
            // 点击预览项不再跳转到外部，PDF和视频已经在页面内显示
            holder.itemView.setOnClickListener(v -> {
                if (item.type == PreviewType.PDF) {
                    Toast.makeText(context, "请在下方查看PDF内容，左右滑动翻页", Toast.LENGTH_SHORT).show();
                } else if (item.type == PreviewType.VIDEO) {
                    Toast.makeText(context, "请在下方播放视频", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class PreviewViewHolder extends RecyclerView.ViewHolder {
            final ImageView image;
            final TextView label;

            PreviewViewHolder(@NonNull View itemView) {
                super(itemView);
                image = itemView.findViewById(R.id.iv_preview);
                label = itemView.findViewById(R.id.tv_preview_label);
            }
        }
    }
}