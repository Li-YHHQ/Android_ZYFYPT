package com.example.zyfypt613lsl.utils

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.VideoView
import com.example.zyfypt613lsl.R
import java.util.concurrent.TimeUnit

class VideoPlayerHelper(
    private val activity: Activity,
    private val videoView: VideoView,
    private val container: View,
    private val controller: View
) {
    private val audioManager: AudioManager = activity.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val windowManager = activity.window
    private val handler = Handler(Looper.getMainLooper())
    private var isPlaying = false
    private var isFullscreen = false
    private var originalWidth = 0
    private var originalHeight = 0
    private var originalOrientation: Int = 0

    // Controller views
    private lateinit var btnPlayPause: ImageButton
    private lateinit var btnFullscreen: ImageButton
    private lateinit var btnVolume: ImageButton
    private lateinit var btnBrightness: ImageButton
    private lateinit var seekBar: SeekBar
    private lateinit var seekVolume: SeekBar
    private lateinit var seekBrightness: SeekBar
    private lateinit var txtCurrentTime: TextView
    private lateinit var txtTotalTime: TextView

    private val updateProgress = object : Runnable {
        override fun run() {
            if (videoView.isPlaying) {
                val currentPosition = videoView.currentPosition
                val duration = videoView.duration
                
                // Update seekbar
                if (duration > 0) {
                    val progress = (currentPosition * 100 / duration).toInt()
                    seekBar.progress = progress
                    txtCurrentTime.text = formatTime(currentPosition)
                }
                
                // Update total time if needed
                if (duration > 0 && txtTotalTime.text.isNullOrEmpty()) {
                    txtTotalTime.text = formatTime(duration)
                }
            }
            handler.postDelayed(this, 1000)
        }
    }

    fun initialize() {
        setupViews()
        setupListeners()
        setupControllerVisibility()
    }

    private fun setupViews() {
        // Initialize controller views
        btnPlayPause = controller.findViewById(R.id.btnPlayPause)
        btnFullscreen = controller.findViewById(R.id.btnFullscreen)
        btnVolume = controller.findViewById(R.id.btnVolume)
        btnBrightness = controller.findViewById(R.id.btnBrightness)
        seekBar = controller.findViewById(R.id.seekBar)
        seekVolume = controller.findViewById(R.id.seekVolume)
        seekBrightness = controller.findViewById(R.id.seekBrightness)
        txtCurrentTime = controller.findViewById(R.id.txtCurrentTime)
        txtTotalTime = controller.findViewById(R.id.txtTotalTime)

        // Set initial volume and brightness
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        seekVolume.max = maxVolume
        seekVolume.progress = currentVolume

        // Set initial brightness (0-255)
        val brightness = Settings.System.getInt(
            activity.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS
        )
        seekBrightness.progress = (brightness * 100 / 255)
    }

    private fun setupListeners() {
        // Play/Pause button
        btnPlayPause.setOnClickListener {
            if (isPlaying) {
                pauseVideo()
            } else {
                playVideo()
            }
        }

        // Fullscreen button
        btnFullscreen.setOnClickListener {
            toggleFullscreen()
        }

        // Volume button and seekbar
        btnVolume.setOnClickListener {
            // Toggle between mute and unmute
            val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            if (currentVolume > 0) {
                // Mute
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
                seekVolume.progress = 0
                btnVolume.setImageResource(android.R.drawable.ic_lock_silent_mode_off)
            } else {
                // Unmute to half volume
                val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume / 2, 0)
                seekVolume.progress = maxVolume / 2
                btnVolume.setImageResource(android.R.drawable.ic_audio_vol)
            }
        }

        // Volume seekbar
        seekVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0)
                    btnVolume.setImageResource(
                        if (progress == 0) android.R.drawable.ic_lock_silent_mode_off 
                        else android.R.drawable.ic_audio_vol
                    )
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Brightness button and seekbar
        btnBrightness.setOnClickListener {
            // Toggle between min and max brightness
            val currentBrightness = seekBrightness.progress
            if (currentBrightness > 50) {
                // Set to min brightness
                seekBrightness.progress = 10
                setBrightness(10)
            } else {
                // Set to max brightness
                seekBrightness.progress = 100
                setBrightness(100)
            }
        }

        // Brightness seekbar
        seekBrightness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    setBrightness(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Video seekbar
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val duration = videoView.duration
                    val newPosition = (duration * progress) / 100
                    videoView.seekTo(newPosition)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Pause updates while seeking
                handler.removeCallbacks(updateProgress)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Resume updates after seeking
                handler.post(updateProgress)
            }
        })

        // Video completion listener
        videoView.setOnCompletionListener {
            isPlaying = false
            btnPlayPause.setImageResource(android.R.drawable.ic_media_play)
            seekBar.progress = 100
            handler.removeCallbacks(updateProgress)
        }

        // Video prepared listener
        videoView.setOnPreparedListener {
            val duration = videoView.duration
            txtTotalTime.text = formatTime(duration)
            playVideo()
        }
    }

    private fun setupControllerVisibility() {
        // Show/hide controller on video click
        videoView.setOnClickListener {
            if (controller.visibility == View.VISIBLE) {
                controller.visibility = View.GONE
            } else {
                controller.visibility = View.VISIBLE
                // Auto-hide after 3 seconds
                handler.postDelayed({
                    if (isPlaying) {
                        controller.visibility = View.GONE
                    }
                }, 3000)
            }
        }
    }

    fun playVideo() {
        if (!videoView.isPlaying) {
            videoView.start()
            isPlaying = true
            btnPlayPause.setImageResource(android.R.drawable.ic_media_pause)
            handler.post(updateProgress)
        }
    }

    fun pauseVideo() {
        if (videoView.isPlaying) {
            videoView.pause()
            isPlaying = false
            btnPlayPause.setImageResource(android.R.drawable.ic_media_play)
            handler.removeCallbacks(updateProgress)
        }
    }

    fun stopVideo() {
        videoView.stopPlayback()
        isPlaying = false
        handler.removeCallbacks(updateProgress)
    }

    fun release() {
        stopVideo()
        handler.removeCallbacksAndMessages(null)
    }

    private fun toggleFullscreen() {
        if (isFullscreen) {
            // Exit fullscreen
            activity.requestedOrientation = originalOrientation
            val params = container.layoutParams
            params.width = originalWidth
            params.height = originalHeight
            container.layoutParams = params
            isFullscreen = false
            btnFullscreen.setImageResource(android.R.drawable.ic_menu_crop)
        } else {
            // Enter fullscreen
            originalWidth = container.width
            originalHeight = container.height
            originalOrientation = activity.requestedOrientation
            
            // Set to landscape
            activity.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            
            // Set fullscreen layout params
            val params = container.layoutParams
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            params.height = ViewGroup.LayoutParams.MATCH_PARENT
            container.layoutParams = params
            
            isFullscreen = true
            btnFullscreen.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
        }
    }

    private fun setBrightness(brightness: Int) {
        try {
            // Normalize brightness value (0-255)
            val normalizedBrightness = (brightness * 255) / 100
            
            // Set system brightness (requires WRITE_SETTINGS permission)
            Settings.System.putInt(
                activity.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                normalizedBrightness
            )
            
            // Update window brightness
            val layoutParams = windowManager.attributes
            layoutParams.screenBrightness = normalizedBrightness / 255f
            windowManager.attributes = layoutParams
            
            // Update brightness button icon
            btnBrightness.setImageResource(
                if (brightness < 30) android.R.drawable.ic_menu_day 
                else android.R.drawable.ic_menu_day
            )
        } catch (e: Exception) {
            Log.e("VideoPlayerHelper", "Error setting brightness", e)
        }
    }

    private fun formatTime(millis: Int): String {
        return String.format(
            "%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(millis.toLong()),
            TimeUnit.MILLISECONDS.toSeconds(millis.toLong()) - 
            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis.toLong()))
        )
    }
}
