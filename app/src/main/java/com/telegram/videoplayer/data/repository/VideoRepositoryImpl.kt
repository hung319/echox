package com.telegram.videoplayer.data.repository

import com.telegram.videoplayer.data.local.dao.VideoProgressDao
import com.telegram.videoplayer.data.local.entity.VideoProgressEntity
import com.telegram.videoplayer.data.remote.telegram.TelegramClient
import com.telegram.videoplayer.data.remote.telegram.TelegramMapper
import com.telegram.videoplayer.domain.model.PlaybackProgress
import com.telegram.videoplayer.domain.model.Video
import com.telegram.videoplayer.domain.repository.VideoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoRepositoryImpl @Inject constructor(
    private val telegramClient: TelegramClient,
    private val videoProgressDao: VideoProgressDao
) : VideoRepository {
    
    override suspend fun getVideosFromChannel(channelId: Long): Result<List<Video>> {
        return try {
            val messages = telegramClient.getChatHistory(channelId, 100)
            val videos = messages.mapNotNull { message ->
                TelegramMapper.mapMessageToVideo(message, channelId)
            }
            Result.success(videos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun searchVideos(channelId: Long, query: String): Result<List<Video>> {
        return try {
            // Get all videos and filter locally
            val result = getVideosFromChannel(channelId)
            result.map { videos ->
                videos.filter { video ->
                    video.title.contains(query, ignoreCase = true) ||
                    video.caption?.contains(query, ignoreCase = true) == true
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun downloadVideoFile(fileId: String): Result<String> {
        return try {
            val filePath = telegramClient.downloadFile(fileId.toInt())
            Result.success(filePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun saveProgress(progress: PlaybackProgress) {
        val entity = VideoProgressEntity.fromDomain(progress)
        videoProgressDao.insertProgress(entity)
    }
    
    override suspend fun getProgress(videoId: String): PlaybackProgress? {
        return videoProgressDao.getProgress(videoId)?.toDomain()
    }
    
    override fun observeProgress(videoId: String): Flow<PlaybackProgress?> {
        return videoProgressDao.getProgressFlow(videoId).map { it?.toDomain() }
    }
    
    override fun getAllProgress(): Flow<List<PlaybackProgress>> {
        return videoProgressDao.getAllProgress().map { entities ->
            entities.map { it.toDomain() }
        }
    }
}
