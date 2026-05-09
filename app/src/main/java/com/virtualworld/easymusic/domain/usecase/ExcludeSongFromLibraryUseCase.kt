package com.virtualworld.easymusic.domain.usecase

import com.virtualworld.easymusic.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ExcludeSongFromLibraryUseCase @Inject constructor(
    private val repository: MusicRepository
) {
    suspend operator fun invoke(songId: Long) {
        repository.excludeSongFromLibrary(songId)
    }

    fun observeExcludedIds(): Flow<Set<Long>> = repository.excludedSongIds()
}
