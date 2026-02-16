package com.telegram.videoplayer.presentation.video

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.telegram.videoplayer.R
import com.telegram.videoplayer.databinding.ActivityVideoListBinding
import com.telegram.videoplayer.domain.model.Video
import com.telegram.videoplayer.presentation.channel.ChannelViewModel
import com.telegram.videoplayer.presentation.player.PlayerActivity
import com.telegram.videoplayer.presentation.util.UiState
import com.telegram.videoplayer.presentation.util.hide
import com.telegram.videoplayer.presentation.util.show
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class VideoListActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityVideoListBinding
    private val viewModel: ChannelViewModel by viewModels()
    
    private var channelId: Long = 0
    private var channelName: String = ""
    
    private val videoAdapter by lazy {
        VideoAdapter(
            onVideoClick = { video ->
                openPlayer(video)
            }
        )
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        channelId = intent.getLongExtra("CHANNEL_ID", 0)
        channelName = intent.getStringExtra("CHANNEL_NAME") ?: ""
        
        setupToolbar()
        setupRecyclerView()
        setupSwipeRefresh()
        observeVideos()
        
        viewModel.loadVideosForChannel(channelId)
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = channelName
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    
    private fun setupRecyclerView() {
        binding.videosRecyclerView.apply {
            layoutManager = GridLayoutManager(this@VideoListActivity, 2)
            adapter = videoAdapter
        }
    }
    
    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadVideosForChannel(channelId)
        }
    }
    
    private fun observeVideos() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.videosState.collect { state ->
                    binding.swipeRefreshLayout.isRefreshing = false
                    
                    when (state) {
                        is UiState.Loading -> {
                            binding.progressBar.show()
                            binding.emptyView.hide()
                        }
                        is UiState.Success -> {
                            binding.progressBar.hide()
                            if (state.data.isEmpty()) {
                                binding.emptyView.show()
                            } else {
                                binding.emptyView.hide()
                                videoAdapter.submitList(state.data)
                            }
                        }
                        is UiState.Error -> {
                            binding.progressBar.hide()
                            binding.emptyView.show()
                            showError(state.message)
                        }
                        else -> {
                            binding.progressBar.hide()
                        }
                    }
                }
            }
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.video_list_menu, menu)
        
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as? SearchView
        
        searchView?.apply {
            queryHint = getString(R.string.search_videos)
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let { viewModel.searchVideos(channelId, it) }
                    return true
                }
                
                override fun onQueryTextChange(newText: String?): Boolean {
                    newText?.let {
                        if (it.isEmpty()) {
                            viewModel.loadVideosForChannel(channelId)
                        }
                    }
                    return true
                }
            })
        }
        
        return true
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    
    private fun openPlayer(video: Video) {
        val intent = Intent(this, PlayerActivity::class.java)
        intent.putExtra("VIDEO_ID", video.id)
        intent.putExtra("VIDEO_TITLE", video.title)
        intent.putExtra("FILE_ID", video.fileId)
        intent.putExtra("VIDEO_DURATION", video.duration)
        startActivity(intent)
    }
    
    private fun showError(message: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}
