package com.telegram.videoplayer.domain.repository

import com.telegram.videoplayer.domain.model.Channel
import kotlinx.coroutines.flow.Flow

interface ChannelRepository {
    fun getChannels(): Flow<List<Channel>>
    suspend fun refreshChannels(): Result<Unit>
    suspend fun getChannel(channelId: Long): Channel?
}
