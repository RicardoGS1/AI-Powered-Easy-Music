package com.virtualworld.easymusic.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.virtualworld.easymusic.domain.model.Album
import com.virtualworld.easymusic.domain.model.Artist
import com.virtualworld.easymusic.domain.model.Song
import com.virtualworld.easymusic.domain.usecase.ExcludeSongFromLibraryUseCase
import com.virtualworld.easymusic.domain.usecase.GetAlbumsUseCase
import com.virtualworld.easymusic.domain.usecase.GetArtistsUseCase
import com.virtualworld.easymusic.domain.usecase.GetSongsUseCase
import com.virtualworld.easymusic.domain.usecase.ObserveFavoriteSongIdsUseCase
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
    val favoriteSongIds: Set<Long> = emptySet(),
    val isLoading: Boolean = true,
    val selectedTab: Int = 0,
    val searchQuery: String = ""
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val getSongsUseCase: GetSongsUseCase,
    private val getAlbumsUseCase: GetAlbumsUseCase,
    private val getArtistsUseCase: GetArtistsUseCase,
    private val excludeSongFromLibraryUseCase: ExcludeSongFromLibraryUseCase,
    private val observeFavoriteSongIdsUseCase: ObserveFavoriteSongIdsUseCase,
    private val playbackController: PlaybackController
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            excludeSongFromLibraryUseCase.observeExcludedIds().collect {
                loadLibrary()
            }
        }
        viewModelScope.launch {
            observeFavoriteSongIdsUseCase().collect { ids ->
                _uiState.update { it.copy(favoriteSongIds = ids) }
            }
        }
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

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun playSong(song: Song) {
        val songs = songsQueueForCurrentTab()
        val index = songs.indexOfFirst { it.id == song.id }
        playbackController.playSongs(songs, index.coerceAtLeast(0))
    }

    private fun songsQueueForCurrentTab(): List<Song> {
        val state = _uiState.value
        val baseSongs = if (state.selectedTab == TAB_FAVORITES) {
            state.songs
                .filter { it.id in state.favoriteSongIds }
                .sortedBy { it.title.lowercase() }
        } else {
            state.songs
        }
        return LibrarySearchFilters.songs(baseSongs, state.searchQuery)
    }

    companion object {
        const val TAB_FAVORITES = 0
        const val TAB_SONGS = 1
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
