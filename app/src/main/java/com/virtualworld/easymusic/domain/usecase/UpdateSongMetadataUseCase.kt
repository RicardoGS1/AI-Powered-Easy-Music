package com.virtualworld.easymusic.domain.usecase

import com.virtualworld.easymusic.domain.model.SongMetadataEdit
import com.virtualworld.easymusic.domain.model.UpdateSongMetadataResult
import com.virtualworld.easymusic.domain.repository.MusicRepository
import javax.inject.Inject

class UpdateSongMetadataUseCase @Inject constructor(
    private val musicRepository: MusicRepository,
) {
    suspend operator fun invoke(
        songId: Long,
        metadata: SongMetadataEdit,
        writeAccessConfirmed: Boolean = false,
    ): UpdateSongMetadataResult {
        return musicRepository.updateSongMetadata(songId, metadata, writeAccessConfirmed)
    }
}
