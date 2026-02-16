package com.telegram.videoplayer.di

import com.telegram.videoplayer.data.repository.AuthRepositoryImpl
import com.telegram.videoplayer.data.repository.ChannelRepositoryImpl
import com.telegram.videoplayer.data.repository.VideoRepositoryImpl
import com.telegram.videoplayer.domain.repository.AuthRepository
import com.telegram.videoplayer.domain.repository.ChannelRepository
import com.telegram.videoplayer.domain.repository.VideoRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
    
    @Binds
    @Singleton
    abstract fun bindChannelRepository(impl: ChannelRepositoryImpl): ChannelRepository
    
    @Binds
    @Singleton
    abstract fun bindVideoRepository(impl: VideoRepositoryImpl): VideoRepository
}
