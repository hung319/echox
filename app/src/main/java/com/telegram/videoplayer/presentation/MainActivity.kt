package com.telegram.videoplayer.presentation

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.telegram.videoplayer.R
import com.telegram.videoplayer.databinding.ActivityMainBinding
import com.telegram.videoplayer.domain.repository.AuthRepository
import com.telegram.videoplayer.presentation.auth.AuthActivity
import com.telegram.videoplayer.presentation.channel.ChannelAdapter
import com.telegram.videoplayer.presentation.channel.ChannelViewModel
import com.telegram.videoplayer.presentation.player.PlayerActivity
import com.telegram.videoplayer.presentation.util.UiState
import com.telegram.videoplayer.presentation.util.hide
import com.telegram.videoplayer.presentation.util.show
import com.telegram.videoplayer.presentation.video.VideoListActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private val viewModel: ChannelViewModel by viewModels()
    
    @Inject
    lateinit var authRepository: AuthRepository
    
    private val channelAdapter by lazy {
        ChannelAdapter { channel ->
            // Navigate to video list for this channel
            val intent = Intent(this, VideoListActivity::class.java)
            intent.putExtra("CHANNEL_ID", channel.id)
            intent.putExtra("CHANNEL_NAME", channel.title)
            startActivity(intent)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // Check authentication
        if (!authRepository.isAuthenticated()) {
            navigateToAuth()
            return
        }
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupRecyclerView()
        setupSwipeRefresh()
        observeChannels()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.channels_title)
    }
    
    private fun setupRecyclerView() {
        binding.channelsRecyclerView.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 2)
            adapter = channelAdapter
        }
    }
    
    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshChannels()
        }
    }
    
    private fun observeChannels() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.channelsState.collect { state ->
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
                                channelAdapter.submitList(state.data)
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
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                showLogoutDialog()
                true
            }
            R.id.action_refresh -> {
                viewModel.refreshChannels()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun showLogoutDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.logout)
            .setMessage("Bạn có chắc muốn đăng xuất?")
            .setPositiveButton(R.string.logout) { _, _ ->
                lifecycleScope.launch {
                    authRepository.logout()
                    navigateToAuth()
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
    
    private fun showError(message: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.error_unknown)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }
    
    private fun navigateToAuth() {
        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
