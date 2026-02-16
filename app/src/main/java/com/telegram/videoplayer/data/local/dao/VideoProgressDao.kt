package com.telegram.videoplayer.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.telegram.videoplayer.data.local.entity.VideoProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoProgressDao {
    
    @Query("SELECT * FROM video_progress WHERE videoId = :videoId")
    suspend fun getProgress(videoId: String): VideoProgressEntity?
    
    @Query("SELECT * FROM video_progress WHERE videoId = :videoId")
    fun getProgressFlow(videoId: String): Flow<VideoProgressEntity?>
    
    @Query("SELECT * FROM video_progress ORDER BY lastUpdated DESC")
    fun getAllProgress(): Flow<List<VideoProgressEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: VideoProgressEntity)
    
    @Query("DELETE FROM video_progress WHERE videoId = :videoId")
    suspend fun deleteProgress(videoId: String)
    
    @Query("DELETE FROM video_progress")
    suspend fun clearAll()
}
