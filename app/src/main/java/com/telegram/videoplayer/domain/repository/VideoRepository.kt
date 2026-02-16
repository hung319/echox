package com.telegram.videoplayer.domain.repository

import com.telegram.videoplayer.domain.model.PlaybackProgress
import com.telegram.videoplayer.domain.model.Video
import kotlinx.coroutines.flow.Flow

interface VideoRepository {
    suspend fun getVideosFromChannel(channelId: Long): Result<List<Video>>
    suspend fun searchVideos(channelId: Long, query: String): Result<List<Video>>
    suspend fun downloadVideoFile(fileId: String): Result<String>
    
    // Progress tracking
    suspend fun saveProgress(progress: PlaybackProgress)
    suspend fun getProgress(videoId: String): PlaybackProgress?
    fun observeProgress(videoId: String): Flow<PlaybackProgress?>
    fun getAllProgress(): Flow<List<PlaybackProgress>>
}
