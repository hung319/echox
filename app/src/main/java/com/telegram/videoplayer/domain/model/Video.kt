package com.telegram.videoplayer.domain.model

data class Video(
    val id: String,
    val messageId: Long,
    val channelId: Long,
    val title: String,
    val thumbnailUrl: String?,
    val fileId: String,
    val fileName: String,
    val fileSize: Long,
    val duration: Int, // in seconds
    val mimeType: String,
    val caption: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
