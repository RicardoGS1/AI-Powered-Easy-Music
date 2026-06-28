package com.virtualworld.easymusic.domain.usecase

import com.virtualworld.easymusic.domain.repository.VideoRepository
import javax.inject.Inject

class ToggleFavoriteVideoUseCase @Inject constructor(
    private val repository: VideoRepository,
) {
    suspend operator fun invoke(videoId: Long) {
        repository.toggleFavoriteVideo(videoId)
    }
}
