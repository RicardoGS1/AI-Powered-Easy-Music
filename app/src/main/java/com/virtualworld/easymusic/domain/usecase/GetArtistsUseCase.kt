package com.virtualworld.easymusic.domain.usecase

import com.virtualworld.easymusic.domain.model.Artist
import com.virtualworld.easymusic.domain.repository.MusicRepository
import javax.inject.Inject

class GetArtistsUseCase @Inject constructor(
    private val repository: MusicRepository
) {
    suspend operator fun invoke(): List<Artist> = repository.getArtists()
}
