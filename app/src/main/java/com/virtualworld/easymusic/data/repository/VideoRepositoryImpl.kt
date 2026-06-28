package com.virtualworld.easymusic.data.repository

import com.virtualworld.easymusic.data.datasource.MediaStoreVideoDataSource
import com.virtualworld.easymusic.data.preferences.MusicPreferences
import com.virtualworld.easymusic.domain.model.Video
import com.virtualworld.easymusic.domain.repository.VideoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoRepositoryImpl @Inject constructor(
    private val mediaStoreVideoDataSource: MediaStoreVideoDataSource,
    private val musicPreferences: MusicPreferences,
) : VideoRepository {

    private var cachedVideos: List<Video>? = null

    override suspend fun getVideos(): List<Video> {
        return cachedVideos ?: mediaStoreVideoDataSource.queryVideos().also { cachedVideos = it }
    }

    override suspend fun toggleFavoriteVideo(videoId: Long) {
        musicPreferences.toggleFavoriteVideoId(videoId)
    }

    override fun favoriteVideoIds(): Flow<Set<Long>> = musicPreferences.favoriteVideoIds()
}
