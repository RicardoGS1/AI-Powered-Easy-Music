package com.virtualworld.easymusic.domain.repository

import com.virtualworld.easymusic.domain.model.Album
import com.virtualworld.easymusic.domain.model.Artist
import com.virtualworld.easymusic.domain.model.PlaybackSession
import com.virtualworld.easymusic.domain.model.Song
import com.virtualworld.easymusic.domain.model.SongMetadataEdit
import com.virtualworld.easymusic.domain.model.UpdateSongMetadataResult
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    suspend fun getSongs(): List<Song>
    suspend fun getAlbums(): List<Album>
    suspend fun getArtists(): List<Artist>
    suspend fun getSongsByAlbum(albumId: Long): List<Song>
    fun getLastPlayedSongId(): Flow<Long?>
    suspend fun saveLastPlayedSongId(songId: Long)
    suspend fun getPlaybackSession(): PlaybackSession?
    suspend fun savePlaybackSession(session: PlaybackSession)
    suspend fun excludeSongFromLibrary(songId: Long)
    fun excludedSongIds(): Flow<Set<Long>>
    suspend fun toggleFavoriteSong(songId: Long)
    fun favoriteSongIds(): Flow<Set<Long>>
    suspend fun updateSongMetadata(
        songId: Long,
        metadata: SongMetadataEdit,
        writeAccessConfirmed: Boolean = false,
    ): UpdateSongMetadataResult
    fun invalidateSongsCache()
}
