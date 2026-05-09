package com.virtualworld.easymusic.data.repository

import com.virtualworld.easymusic.data.datasource.MediaStoreDataSource
import com.virtualworld.easymusic.data.preferences.MusicPreferences
import com.virtualworld.easymusic.domain.model.Album
import com.virtualworld.easymusic.domain.model.Artist
import com.virtualworld.easymusic.domain.model.Song
import com.virtualworld.easymusic.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepositoryImpl @Inject constructor(
    private val mediaStoreDataSource: MediaStoreDataSource,
    private val musicPreferences: MusicPreferences
) : MusicRepository {

    private var cachedSongs: List<Song>? = null

    override suspend fun getSongs(): List<Song> {
        return cachedSongs ?: mediaStoreDataSource.querySongs().also { cachedSongs = it }
    }

    override suspend fun getAlbums(): List<Album> {
        return mediaStoreDataSource.queryAlbums()
    }

    override suspend fun getArtists(): List<Artist> {
        return mediaStoreDataSource.queryArtists()
    }

    override suspend fun getSongsByAlbum(albumId: Long): List<Song> {
        return mediaStoreDataSource.querySongsByAlbum(albumId)
    }

    override fun getLastPlayedSongId(): Flow<Long?> {
        return musicPreferences.getLastPlayedSongId()
    }

    override suspend fun saveLastPlayedSongId(songId: Long) {
        musicPreferences.saveLastPlayedSongId(songId)
    }
}
