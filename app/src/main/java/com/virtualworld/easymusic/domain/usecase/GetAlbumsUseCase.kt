package com.virtualworld.easymusic.domain.usecase

import com.virtualworld.easymusic.domain.model.Album
import com.virtualworld.easymusic.domain.repository.MusicRepository
import javax.inject.Inject

class GetAlbumsUseCase @Inject constructor(
    private val repository: MusicRepository
) {
    suspend operator fun invoke(): List<Album> = repository.getAlbums()
}
