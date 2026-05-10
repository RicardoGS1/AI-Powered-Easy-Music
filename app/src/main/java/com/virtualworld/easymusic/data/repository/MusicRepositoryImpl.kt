package com.virtualworld.easymusic.data.repository

import com.virtualworld.easymusic.data.datasource.MediaStoreDataSource
import com.virtualworld.easymusic.data.preferences.MusicPreferences
import com.virtualworld.easymusic.domain.model.Album
import com.virtualworld.easymusic.domain.model.Artist
import com.virtualworld.easymusic.domain.model.Song
import com.virtualworld.easymusic.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepositoryImpl @Inject constructor(
    private val mediaStoreDataSource: MediaStoreDataSource,
    private val musicPreferences: MusicPreferences
) : MusicRepository {

    private var cachedSongs: List<Song>? = null

    override suspend fun getSongs(): List<Song> {
        val all = cachedSongs ?: mediaStoreDataSource.querySongs().also { cachedSongs = it }
        val excluded = musicPreferences.getExcludedSongIds()
        return all.filter { it.id !in excluded }
    }

    override suspend fun getAlbums(): List<Album> {
        val songs = getSongs()
        return songs
            .groupBy { it.albumId }
            .map { (_, albumSongs) ->
                val first = albumSongs.first()
                Album(
                    id = first.albumId,
                    name = first.album,
                    artist = first.artist,
                    albumArtUri = first.albumArtUri,
                    songCount = albumSongs.size
                )
            }
            .sortedBy { it.name.lowercase() }
    }

    override suspend fun getArtists(): List<Artist> {
        val songs = getSongs()
        return songs
            .groupBy { it.artist }
            .map { (name, trackList) ->
                val distinctAlbums = trackList.map { it.albumId }.distinct().size
                Artist(
                    id = stableArtistId(name),
                    name = name,
                    songCount = trackList.size,
                    albumCount = distinctAlbums
                )
            }
            .sortedBy { it.name.lowercase() }
    }

    override suspend fun getSongsByAlbum(albumId: Long): List<Song> {
        val excluded = musicPreferences.getExcludedSongIds()
        return mediaStoreDataSource.querySongsByAlbum(albumId).filter { it.id !in excluded }
    }

    override fun getLastPlayedSongId(): Flow<Long?> {
        return musicPreferences.getLastPlayedSongId()
    }

    override suspend fun saveLastPlayedSongId(songId: Long) {
        musicPreferences.saveLastPlayedSongId(songId)
    }

    override suspend fun excludeSongFromLibrary(songId: Long) {
        musicPreferences.addExcludedSongId(songId)
        musicPreferences.removeFavoriteSongId(songId)
    }

    override fun excludedSongIds(): Flow<Set<Long>> = musicPreferences.excludedSongIds()

    override suspend fun toggleFavoriteSong(songId: Long) {
        musicPreferences.toggleFavoriteSongId(songId)
    }

    override fun favoriteSongIds(): Flow<Set<Long>> = musicPreferences.favoriteSongIds()

    private fun stableArtistId(name: String): Long {
        return UUID.nameUUIDFromBytes(name.toByteArray(Charsets.UTF_8)).mostSignificantBits and Long.MAX_VALUE
    }
}
