package com.telegram.videoplayer.presentation.player

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.telegram.videoplayer.databinding.ActivityPlayerBinding
import com.telegram.videoplayer.presentation.util.UiState
import com.telegram.videoplayer.presentation.util.formatDuration
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PlayerActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityPlayerBinding
    private val viewModel: PlayerViewModel by viewModels()
    
    private var player: ExoPlayer? = null
    private var videoId: String = ""
    private var videoTitle: String = ""
    private var fileId: String = ""
    private var videoDuration: Int = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Keep screen on during playback
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        // Hide system UI for fullscreen
        hideSystemUI()
        
        videoId = intent.getStringExtra("VIDEO_ID") ?: ""
        videoTitle = intent.getStringExtra("VIDEO_TITLE") ?: ""
        fileId = intent.getStringExtra("FILE_ID") ?: ""
        videoDuration = intent.getIntExtra("VIDEO_DURATION", 0)
        
        setupPlayer()
        observeStates()
        
        // Load saved progress
        viewModel.loadProgress(videoId)
        
        // Download video file
        viewModel.downloadVideoFile(fileId)
    }
    
    private fun setupPlayer() {
        player = ExoPlayer.Builder(this).build().also { exoPlayer ->
            binding.playerView.player = exoPlayer
            
            exoPlayer.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_READY -> {
                            binding.loadingView.visibility = View.GONE
                        }
                        Player.STATE_BUFFERING -> {
                            binding.loadingView.visibility = View.VISIBLE
                        }
                        Player.STATE_ENDED -> {
                            finish()
                        }
                    }
                }
            })
        }
    }
    
    private fun observeStates() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.fileState.collect { state ->
                        when (state) {
                            is UiState.Loading -> {
                                binding.loadingView.visibility = View.VISIBLE
                            }
                            is UiState.Success -> {
                                playVideo(state.data)
                            }
                            is UiState.Error -> {
                                binding.loadingView.visibility = View.GONE
                                showError(state.message)
                            }
                            else -> {}
                        }
                    }
                }
                
                launch {
                    viewModel.progressState.collect { progress ->
                        progress?.let {
                            if (it.position > 0 && it.position < it.duration - 5000) {
                                showResumeDialog(it.position)
                            }
                        }
                    }
                }
            }
        }
    }
    
    private fun playVideo(filePath: String) {
        val mediaItem = MediaItem.fromUri("file://$filePath")
        player?.setMediaItem(mediaItem)
        player?.prepare()
        
        // Start progress tracking
        viewModel.startProgressTracking(
            videoId = videoId,
            getCurrentPosition = { player?.currentPosition ?: 0 },
            getDuration = { player?.duration ?: 0 }
        )
    }
    
    private fun showResumeDialog(position: Long) {
        val formattedPosition = (position / 1000).formatDuration()
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Resume Playback")
            .setMessage("Resume from $formattedPosition?")
            .setPositiveButton("Resume") { _, _ ->
                player?.seekTo(position)
                player?.play()
            }
            .setNegativeButton("Start Over") { _, _ ->
                player?.play()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun showError(message: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        )
    }
    
    override fun onPause() {
        super.onPause()
        player?.let {
            viewModel.saveProgress(videoId, it.currentPosition, it.duration)
        }
        player?.pause()
    }
    
    override fun onStop() {
        super.onStop()
        viewModel.stopProgressTracking()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        player?.let {
            viewModel.saveProgress(videoId, it.currentPosition, it.duration)
        }
        player?.release()
        player = null
    }
}
