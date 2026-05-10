package com.virtualworld.easymusic.ui.library

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.virtualworld.easymusic.domain.model.Song
import com.virtualworld.easymusic.domain.usecase.GetAlbumsUseCase
import com.virtualworld.easymusic.domain.usecase.GetArtistsUseCase
import com.virtualworld.easymusic.domain.usecase.GetSongsByAlbumUseCase
import com.virtualworld.easymusic.domain.usecase.GetSongsUseCase
import com.virtualworld.easymusic.playback.PlaybackController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class CollectionKind {
    Album,
    Artist;

    companion object {
        fun fromRoute(value: String?): CollectionKind? = when (value?.lowercase()) {
            "album" -> Album
            "artist" -> Artist
            else -> null
        }
    }
}

data class CollectionSongsUiState(
    val title: String = "",
    val subtitle: String? = null,
    val songs: List<Song> = emptyList(),
    val isLoading: Boolean = true,
    val isMissing: Boolean = false
)

@HiltViewModel
class CollectionSongsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getSongsByAlbumUseCase: GetSongsByAlbumUseCase,
    private val getAlbumsUseCase: GetAlbumsUseCase,
    private val getArtistsUseCase: GetArtistsUseCase,
    private val getSongsUseCase: GetSongsUseCase,
    private val playbackController: PlaybackController
) : ViewModel() {

    private val kind = CollectionKind.fromRoute(savedStateHandle.get<String>(ARG_KIND))
    private val itemId: Long = savedStateHandle.get<Long>(ARG_ITEM_ID) ?: -1L

    private val _uiState = MutableStateFlow(CollectionSongsUiState())
    val uiState: StateFlow<CollectionSongsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        if (kind == null || itemId < 0) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isMissing = true,
                    title = "No encontrado"
                )
            }
            return
        }
        try {
            when (kind) {
                CollectionKind.Album -> loadAlbum()
                CollectionKind.Artist -> loadArtist()
            }
        } catch (_: Exception) {
            _uiState.update {
                it.copy(isLoading = false, isMissing = true, title = "Error al cargar")
            }
        }
    }

    private suspend fun loadAlbum() {
        val songs = getSongsByAlbumUseCase(itemId)
        val meta = getAlbumsUseCase().find { it.id == itemId }
        _uiState.update {
            it.copy(
                isLoading = false,
                isMissing = songs.isEmpty(),
                title = meta?.name ?: songs.firstOrNull()?.album ?: "Álbum",
                subtitle = meta?.artist ?: songs.firstOrNull()?.artist,
                songs = songs
            )
        }
    }

    private suspend fun loadArtist() {
        val artist = getArtistsUseCase().find { it.id == itemId }
        if (artist == null) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isMissing = true,
                    title = "Artista no encontrado"
                )
            }
            return
        }
        val songs = getSongsUseCase()
            .filter { it.artist == artist.name }
            .sortedWith(compareBy({ it.album.lowercase() }, { it.title.lowercase() }))
        _uiState.update {
            it.copy(
                isLoading = false,
                isMissing = songs.isEmpty(),
                title = artist.name,
                subtitle = when (songs.size) {
                    1 -> "1 canción"
                    else -> "${songs.size} canciones"
                },
                songs = songs
            )
        }
    }

    fun playAll() {
        val songs = _uiState.value.songs
        if (songs.isNotEmpty()) {
            playbackController.playSongs(songs, 0)
        }
    }

    fun playFrom(song: Song) {
        val songs = _uiState.value.songs
        if (songs.isEmpty()) return
        val index = songs.indexOfFirst { it.id == song.id }
        playbackController.playSongs(songs, index.coerceAtLeast(0))
    }

    companion object {
        const val ARG_KIND = "kind"
        const val ARG_ITEM_ID = "itemId"
    }
}
