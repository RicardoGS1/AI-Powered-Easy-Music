package com.virtualworld.easymusic.domain.usecase

import com.virtualworld.easymusic.domain.model.Video
import com.virtualworld.easymusic.domain.repository.VideoRepository
import javax.inject.Inject

class GetVideosUseCase @Inject constructor(
    private val repository: VideoRepository,
) {
    suspend operator fun invoke(): List<Video> = repository.getVideos()
}
