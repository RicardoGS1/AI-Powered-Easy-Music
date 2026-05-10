package com.virtualworld.easymusic.domain.usecase

import com.virtualworld.easymusic.domain.model.Song
import com.virtualworld.easymusic.domain.repository.MusicRepository
import javax.inject.Inject

class GetSongsByAlbumUseCase @Inject constructor(
    private val repository: MusicRepository
) {
    suspend operator fun invoke(albumId: Long): List<Song> = repository.getSongsByAlbum(albumId)
}
