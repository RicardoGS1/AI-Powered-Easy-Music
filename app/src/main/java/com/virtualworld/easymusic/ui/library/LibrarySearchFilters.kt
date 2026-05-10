package com.virtualworld.easymusic.ui.library

import com.virtualworld.easymusic.domain.model.Album
import com.virtualworld.easymusic.domain.model.Artist
import com.virtualworld.easymusic.domain.model.Song

internal object LibrarySearchFilters {

    fun songs(songs: List<Song>, query: String): List<Song> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return songs
        return songs.filter { song ->
            song.title.lowercase().contains(q) ||
                song.artist.lowercase().contains(q) ||
                song.album.lowercase().contains(q)
        }
    }

    fun albums(albums: List<Album>, query: String): List<Album> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return albums
        return albums.filter { album ->
            album.name.lowercase().contains(q) ||
                album.artist.lowercase().contains(q)
        }
    }

    fun artists(artists: List<Artist>, query: String): List<Artist> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return artists
        return artists.filter { artist ->
            artist.name.lowercase().contains(q)
        }
    }
}
