package com.telegram.videoplayer.presentation.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telegram.videoplayer.domain.model.PlaybackProgress
import com.telegram.videoplayer.domain.repository.VideoRepository
import com.telegram.videoplayer.presentation.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val videoRepository: VideoRepository
) : ViewModel() {
    
    private val _fileState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val fileState: StateFlow<UiState<String>> = _fileState.asStateFlow()
    
    private val _progressState = MutableStateFlow<PlaybackProgress?>(null)
    val progressState: StateFlow<PlaybackProgress?> = _progressState.asStateFlow()
    
    private var progressSaveJob: Job? = null
    
    fun downloadVideoFile(fileId: String) {
        viewModelScope.launch {
            _fileState.value = UiState.Loading
            
            val result = videoRepository.downloadVideoFile(fileId)
            _fileState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Failed to download video") }
            )
        }
    }
    
    fun loadProgress(videoId: String) {
        viewModelScope.launch {
            val progress = videoRepository.getProgress(videoId)
            _progressState.value = progress
        }
    }
    
    fun startProgressTracking(videoId: String, getCurrentPosition: () -> Long, getDuration: () -> Long) {
        progressSaveJob?.cancel()
        progressSaveJob = viewModelScope.launch {
            while (true) {
                delay(5000) // Save every 5 seconds
                val position = getCurrentPosition()
                val duration = getDuration()
                
                if (duration > 0) {
                    val progress = PlaybackProgress(
                        videoId = videoId,
                        position = position,
                        duration = duration
                    )
                    videoRepository.saveProgress(progress)
                }
            }
        }
    }
    
    fun stopProgressTracking() {
        progressSaveJob?.cancel()
    }
    
    fun saveProgress(videoId: String, position: Long, duration: Long) {
        viewModelScope.launch {
            val progress = PlaybackProgress(
                videoId = videoId,
                position = position,
                duration = duration
            )
            videoRepository.saveProgress(progress)
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        stopProgressTracking()
    }
}
