package com.virtualworld.easymusic.domain.usecase

import com.virtualworld.easymusic.domain.model.Song
import com.virtualworld.easymusic.domain.repository.MusicRepository
import javax.inject.Inject

class GetSongsUseCase @Inject constructor(
    private val repository: MusicRepository
) {
    suspend operator fun invoke(): List<Song> = repository.getSongs()
}
