package com.virtualworld.easymusic.domain.usecase

import com.virtualworld.easymusic.domain.repository.MusicRepository
import javax.inject.Inject

class ToggleFavoriteSongUseCase @Inject constructor(
    private val repository: MusicRepository
) {
    suspend operator fun invoke(songId: Long) {
        repository.toggleFavoriteSong(songId)
    }
}
