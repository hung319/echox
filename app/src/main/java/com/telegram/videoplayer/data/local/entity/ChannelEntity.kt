package com.telegram.videoplayer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.telegram.videoplayer.domain.model.Channel

@Entity(tableName = "channels")
data class ChannelEntity(
    @PrimaryKey
    val id: Long,
    val title: String,
    val username: String?,
    val photoUrl: String?,
    val memberCount: Int,
    val description: String?,
    val isChannel: Boolean,
    val lastUpdated: Long
) {
    fun toDomain() = Channel(
        id = id,
        title = title,
        username = username,
        photoUrl = photoUrl,
        memberCount = memberCount,
        description = description,
        isChannel = isChannel
    )
    
    companion object {
        fun fromDomain(channel: Channel) = ChannelEntity(
            id = channel.id,
            title = channel.title,
            username = channel.username,
            photoUrl = channel.photoUrl,
            memberCount = channel.memberCount,
            description = channel.description,
            isChannel = channel.isChannel,
            lastUpdated = System.currentTimeMillis()
        )
    }
}
