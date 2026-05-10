package com.virtualworld.easymusic.domain.repository

import com.virtualworld.easymusic.domain.model.Album
import com.virtualworld.easymusic.domain.model.Artist
import com.virtualworld.easymusic.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    suspend fun getSongs(): List<Song>
    suspend fun getAlbums(): List<Album>
    suspend fun getArtists(): List<Artist>
    suspend fun getSongsByAlbum(albumId: Long): List<Song>
    fun getLastPlayedSongId(): Flow<Long?>
    suspend fun saveLastPlayedSongId(songId: Long)
    suspend fun excludeSongFromLibrary(songId: Long)
    fun excludedSongIds(): Flow<Set<Long>>
    suspend fun toggleFavoriteSong(songId: Long)
    fun favoriteSongIds(): Flow<Set<Long>>
}
