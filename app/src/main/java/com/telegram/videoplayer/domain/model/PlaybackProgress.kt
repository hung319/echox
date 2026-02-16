package com.telegram.videoplayer.domain.model

data class PlaybackProgress(
    val videoId: String,
    val position: Long, // in milliseconds
    val duration: Long, // in milliseconds
    val lastUpdated: Long = System.currentTimeMillis()
) {
    val progressPercentage: Int
        get() = if (duration > 0) ((position * 100) / duration).toInt() else 0
    
    val isCompleted: Boolean
        get() = progressPercentage >= 90
}
