package com.virtualworld.easymusic.data.repository

import android.app.Application
import com.virtualworld.easymusic.R
import com.virtualworld.easymusic.data.datasource.MediaStoreVideoDataSource
import com.virtualworld.easymusic.data.datasource.SongMetadataUpdateAttempt
import com.virtualworld.easymusic.data.preferences.MusicPreferences
import com.virtualworld.easymusic.domain.model.UpdateVideoTitleResult
import com.virtualworld.easymusic.domain.model.Video
import com.virtualworld.easymusic.domain.repository.VideoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoRepositoryImpl @Inject constructor(
    private val mediaStoreVideoDataSource: MediaStoreVideoDataSource,
    private val musicPreferences: MusicPreferences,
    private val app: Application,
) : VideoRepository {

    private var cachedVideos: List<Video>? = null

    override suspend fun getVideos(): List<Video> {
        return cachedVideos ?: mediaStoreVideoDataSource.queryVideos().also { cachedVideos = it }
    }

    override suspend fun toggleFavoriteVideo(videoId: Long) {
        musicPreferences.toggleFavoriteVideoId(videoId)
    }

    override fun favoriteVideoIds(): Flow<Set<Long>> = musicPreferences.favoriteVideoIds()

    override suspend fun updateVideoTitle(
        videoId: Long,
        title: String,
        writeAccessConfirmed: Boolean,
    ): UpdateVideoTitleResult {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isEmpty()) {
            return UpdateVideoTitleResult.Error(
                app.getString(R.string.video_title_required),
            )
        }
        val previousVideo = mediaStoreVideoDataSource.queryVideoById(videoId)
        val attempt = mediaStoreVideoDataSource.updateVideoTitle(
            videoId = videoId,
            title = trimmedTitle,
            writeAccessConfirmed = writeAccessConfirmed,
        )
        when (attempt) {
            is SongMetadataUpdateAttempt.PermissionRequired -> {
                return UpdateVideoTitleResult.NeedsWritePermission(attempt.intentSender)
            }
            SongMetadataUpdateAttempt.Failed -> {
                return UpdateVideoTitleResult.Error(
                    app.getString(R.string.metadata_save_failed),
                )
            }
            SongMetadataUpdateAttempt.Updated -> Unit
        }
        invalidateVideosCache()
        val refreshed = mediaStoreVideoDataSource.queryVideoById(videoId)
        val updatedVideo = refreshed
            ?: previousVideo?.copy(title = trimmedTitle)
            ?: return UpdateVideoTitleResult.Error(
                app.getString(R.string.metadata_save_failed),
            )
        return UpdateVideoTitleResult.Success(updatedVideo)
    }

    override fun invalidateVideosCache() {
        cachedVideos = null
    }
}
