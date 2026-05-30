package com.virtualworld.easymusic.domain.usecase

import com.virtualworld.easymusic.domain.model.PlaybackSession
import com.virtualworld.easymusic.domain.repository.MusicRepository
import javax.inject.Inject

class GetPlaybackSessionUseCase @Inject constructor(
    private val repository: MusicRepository,
) {
    suspend operator fun invoke(): PlaybackSession? = repository.getPlaybackSession()
}
