package com.virtualworld.easymusic.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.virtualworld.easymusic.domain.model.Album
import com.virtualworld.easymusic.domain.model.Artist
import com.virtualworld.easymusic.domain.model.Song
import com.virtualworld.easymusic.domain.usecase.GetAlbumsUseCase
import com.virtualworld.easymusic.domain.usecase.GetArtistsUseCase
import com.virtualworld.easymusic.domain.usecase.GetSongsUseCase
import com.virtualworld.easymusic.playback.PlaybackController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryUiState(
    val songs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val isLoading: Boolean = true,
    val selectedTab: Int = 0
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val getSongsUseCase: GetSongsUseCase,
    private val getAlbumsUseCase: GetAlbumsUseCase,
    private val getArtistsUseCase: GetArtistsUseCase,
    private val playbackController: PlaybackController
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        loadLibrary()
    }

    private fun loadLibrary() {
        viewModelScope.launch {
            try {
                val songs = getSongsUseCase()
                val albums = getAlbumsUseCase()
                val artists = getArtistsUseCase()
                _uiState.update {
                    it.copy(
                        songs = songs,
                        albums = albums,
                        artists = artists,
                        isLoading = false
                    )
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun playSong(song: Song) {
        val songs = _uiState.value.songs
        val index = songs.indexOfFirst { it.id == song.id }
        playbackController.playSongs(songs, index.coerceAtLeast(0))
    }

    fun playAlbumSongs(albumId: Long) {
        viewModelScope.launch {
            val albumSongs = _uiState.value.songs.filter { it.albumId == albumId }
            if (albumSongs.isNotEmpty()) {
                playbackController.playSongs(albumSongs, 0)
            }
        }
    }
}
