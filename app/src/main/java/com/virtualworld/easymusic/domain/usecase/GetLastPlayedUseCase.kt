package com.virtualworld.easymusic.domain.usecase

import com.virtualworld.easymusic.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLastPlayedUseCase @Inject constructor(
    private val repository: MusicRepository
) {
    operator fun invoke(): Flow<Long?> = repository.getLastPlayedSongId()
}
