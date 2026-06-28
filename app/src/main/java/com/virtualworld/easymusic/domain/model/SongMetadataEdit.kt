package com.virtualworld.easymusic.domain.model

import android.content.IntentSender

data class SongMetadataEdit(
    val title: String,
    val artist: String,
    val album: String,
)

sealed class SongMetadataLookupResult {
    data class Success(
        val title: String,
        val artist: String,
        val album: String,
    ) : SongMetadataLookupResult()

    data class NotFound(val message: String) : SongMetadataLookupResult()
    data class Error(val message: String) : SongMetadataLookupResult()
}

sealed class UpdateSongMetadataResult {
    data class Success(val updatedSong: Song) : UpdateSongMetadataResult()
    data class NeedsWritePermission(val intentSender: IntentSender) : UpdateSongMetadataResult()
    data class Error(val message: String) : UpdateSongMetadataResult()
}
