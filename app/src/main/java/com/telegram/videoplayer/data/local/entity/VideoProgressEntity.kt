package com.telegram.videoplayer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.telegram.videoplayer.domain.model.PlaybackProgress

@Entity(tableName = "video_progress")
data class VideoProgressEntity(
    @PrimaryKey
    val videoId: String,
    val position: Long,
    val duration: Long,
    val lastUpdated: Long
) {
    fun toDomain() = PlaybackProgress(
        videoId = videoId,
        position = position,
        duration = duration,
        lastUpdated = lastUpdated
    )
    
    companion object {
        fun fromDomain(progress: PlaybackProgress) = VideoProgressEntity(
            videoId = progress.videoId,
            position = progress.position,
            duration = progress.duration,
            lastUpdated = progress.lastUpdated
        )
    }
}
