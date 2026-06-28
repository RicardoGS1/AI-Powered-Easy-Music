package com.virtualworld.easymusic.domain.repository

import com.virtualworld.easymusic.domain.model.Video
import kotlinx.coroutines.flow.Flow

interface VideoRepository {
    suspend fun getVideos(): List<Video>
    suspend fun toggleFavoriteVideo(videoId: Long)
    fun favoriteVideoIds(): Flow<Set<Long>>
}
