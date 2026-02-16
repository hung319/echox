package com.telegram.videoplayer.di

import android.content.Context
import androidx.room.Room
import com.telegram.videoplayer.data.local.AppDatabase
import com.telegram.videoplayer.data.local.dao.ChannelDao
import com.telegram.videoplayer.data.local.dao.VideoProgressDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "telegram_video_player.db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }
    
    @Provides
    @Singleton
    fun provideVideoProgressDao(database: AppDatabase): VideoProgressDao {
        return database.videoProgressDao()
    }
    
    @Provides
    @Singleton
    fun provideChannelDao(database: AppDatabase): ChannelDao {
        return database.channelDao()
    }
}
