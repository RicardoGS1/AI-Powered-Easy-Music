package com.virtualworld.easymusic.domain.usecase

import com.virtualworld.easymusic.domain.model.UpdateVideoTitleResult
import com.virtualworld.easymusic.domain.repository.VideoRepository
import javax.inject.Inject

class UpdateVideoTitleUseCase @Inject constructor(
    private val videoRepository: VideoRepository,
) {
    suspend operator fun invoke(
        videoId: Long,
        title: String,
        writeAccessConfirmed: Boolean = false,
    ): UpdateVideoTitleResult {
        return videoRepository.updateVideoTitle(videoId, title, writeAccessConfirmed)
    }
}
