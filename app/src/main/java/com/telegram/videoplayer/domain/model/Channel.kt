package com.telegram.videoplayer.domain.model

data class Channel(
    val id: Long,
    val title: String,
    val username: String?,
    val photoUrl: String?,
    val memberCount: Int,
    val description: String? = null,
    val isChannel: Boolean = true
)
