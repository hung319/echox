package com.telegram.videoplayer.presentation.channel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telegram.videoplayer.domain.model.Channel
import com.telegram.videoplayer.domain.model.Video
import com.telegram.videoplayer.domain.repository.ChannelRepository
import com.telegram.videoplayer.domain.repository.VideoRepository
import com.telegram.videoplayer.presentation.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChannelViewModel @Inject constructor(
    private val channelRepository: ChannelRepository,
    private val videoRepository: VideoRepository
) : ViewModel() {
    
    private val _channelsState = MutableStateFlow<UiState<List<Channel>>>(UiState.Loading)
    val channelsState: StateFlow<UiState<List<Channel>>> = _channelsState.asStateFlow()
    
    private val _videosState = MutableStateFlow<UiState<List<Video>>>(UiState.Idle)
    val videosState: StateFlow<UiState<List<Video>>> = _videosState.asStateFlow()
    
    init {
        loadChannels()
    }
    
    fun loadChannels() {
        viewModelScope.launch {
            _channelsState.value = UiState.Loading
            
            // First load from local database
            channelRepository.getChannels().collect { channels ->
                if (channels.isEmpty()) {
                    refreshChannels()
                } else {
                    _channelsState.value = UiState.Success(channels)
                }
            }
        }
    }
    
    fun refreshChannels() {
        viewModelScope.launch {
            _channelsState.value = UiState.Loading
            
            val result = channelRepository.refreshChannels()
            result.fold(
                onSuccess = {
                    // Data will be emitted through Flow
                },
                onFailure = {
                    _channelsState.value = UiState.Error(it.message ?: "Failed to refresh channels")
                }
            )
        }
    }
    
    fun loadVideosForChannel(channelId: Long) {
        viewModelScope.launch {
            _videosState.value = UiState.Loading
            
            val result = videoRepository.getVideosFromChannel(channelId)
            _videosState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Failed to load videos") }
            )
        }
    }
    
    fun searchVideos(channelId: Long, query: String) {
        if (query.isEmpty()) {
            loadVideosForChannel(channelId)
            return
        }
        
        viewModelScope.launch {
            _videosState.value = UiState.Loading
            
            val result = videoRepository.searchVideos(channelId, query)
            _videosState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Search failed") }
            )
        }
    }
}
