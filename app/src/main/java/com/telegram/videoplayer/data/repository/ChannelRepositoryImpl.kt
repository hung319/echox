package com.telegram.videoplayer.data.repository

import com.telegram.videoplayer.data.local.dao.ChannelDao
import com.telegram.videoplayer.data.local.entity.ChannelEntity
import com.telegram.videoplayer.data.remote.telegram.TelegramClient
import com.telegram.videoplayer.data.remote.telegram.TelegramMapper
import com.telegram.videoplayer.domain.model.Channel
import com.telegram.videoplayer.domain.repository.ChannelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChannelRepositoryImpl @Inject constructor(
    private val telegramClient: TelegramClient,
    private val channelDao: ChannelDao
) : ChannelRepository {
    
    override fun getChannels(): Flow<List<Channel>> {
        return channelDao.getAllChannels().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun refreshChannels(): Result<Unit> {
        return try {
            val chats = telegramClient.getChats()
            val channels = chats.mapNotNull { chat ->
                TelegramMapper.mapChatToChannel(chat)
            }
            
            val entities = channels.map { ChannelEntity.fromDomain(it) }
            channelDao.insertChannels(entities)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getChannel(channelId: Long): Channel? {
        return channelDao.getChannel(channelId)?.toDomain()
    }
}
