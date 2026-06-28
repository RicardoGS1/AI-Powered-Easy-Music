package com.virtualworld.easymusic.domain.usecase

import com.virtualworld.easymusic.domain.repository.VideoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveFavoriteVideoIdsUseCase @Inject constructor(
    private val repository: VideoRepository,
) {
    operator fun invoke(): Flow<Set<Long>> = repository.favoriteVideoIds()
}
