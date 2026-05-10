package com.virtualworld.easymusic.domain.usecase

import com.virtualworld.easymusic.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveFavoriteSongIdsUseCase @Inject constructor(
    private val repository: MusicRepository
) {
    operator fun invoke(): Flow<Set<Long>> = repository.favoriteSongIds()
}
