package com.virtualworld.easymusic.domain.usecase

import com.virtualworld.easymusic.domain.model.PlaybackSession
import com.virtualworld.easymusic.domain.repository.MusicRepository
import javax.inject.Inject

class SavePlaybackSessionUseCase @Inject constructor(
    private val repository: MusicRepository,
) {
    suspend operator fun invoke(session: PlaybackSession) = repository.savePlaybackSession(session)
}
