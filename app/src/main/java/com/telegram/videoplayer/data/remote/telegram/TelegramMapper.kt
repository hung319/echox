package com.telegram.videoplayer.data.remote.telegram

import com.telegram.videoplayer.domain.model.Channel
import com.telegram.videoplayer.domain.model.User
import com.telegram.videoplayer.domain.model.Video
import it.tdlight.jni.TdApi

object TelegramMapper {
    
    fun mapChatToChannel(chat: TdApi.Chat): Channel? {
        // Only map channels and supergroups
        if (chat.type !is TdApi.ChatTypeSupergroup) return null
        
        val supergroup = chat.type as TdApi.ChatTypeSupergroup
        
        return Channel(
            id = chat.id,
            title = chat.title,
            username = null, // Would need additional API call to get username
            photoUrl = null, // Would need to download photo
            memberCount = 0, // Would need additional API call
            description = null,
            isChannel = supergroup.isChannel
        )
    }
    
    fun mapMessageToVideo(message: TdApi.Message, channelId: Long): Video? {
        val content = message.content
        
        // Only process video messages
        if (content !is TdApi.MessageVideo) return null
        
        val video = content.video
        
        return Video(
            id = "${channelId}_${message.id}",
            messageId = message.id,
            channelId = channelId,
            title = video.fileName.substringBeforeLast('.'),
            thumbnailUrl = null, // Would need to download thumbnail
            fileId = video.video.id.toString(),
            fileName = video.fileName,
            fileSize = video.video.size,
            duration = video.duration,
            mimeType = video.mimeType,
            caption = if (content.caption.text.isNotEmpty()) content.caption.text else null,
            timestamp = message.date.toLong() * 1000
        )
    }
    
    fun mapUserToDomain(user: TdApi.User): User {
        return User(
            id = user.id,
            phoneNumber = user.phoneNumber,
            firstName = user.firstName,
            lastName = user.lastName,
            username = user.usernames?.activeUsernames?.firstOrNull()
        )
    }
}
