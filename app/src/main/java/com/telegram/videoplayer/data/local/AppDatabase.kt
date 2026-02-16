package com.telegram.videoplayer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.telegram.videoplayer.data.local.dao.ChannelDao
import com.telegram.videoplayer.data.local.dao.VideoProgressDao
import com.telegram.videoplayer.data.local.entity.ChannelEntity
import com.telegram.videoplayer.data.local.entity.VideoProgressEntity

@Database(
    entities = [
        VideoProgressEntity::class,
        ChannelEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun videoProgressDao(): VideoProgressDao
    abstract fun channelDao(): ChannelDao
}
