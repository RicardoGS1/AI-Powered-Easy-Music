package com.virtualworld.easymusic.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.virtualworld.easymusic.domain.model.Song
import com.virtualworld.easymusic.domain.model.LyricsResult
import com.virtualworld.easymusic.domain.usecase.ExcludeSongFromLibraryUseCase
import com.virtualworld.easymusic.domain.usecase.FetchLyricsUseCase
import com.virtualworld.easymusic.domain.usecase.GetLastPlayedUseCase
import com.virtualworld.easymusic.domain.usecase.GetSongsUseCase
import com.virtualworld.easymusic.domain.usecase.SaveLastPlayedUseCase
import com.virtualworld.easymusic.playback.PlaybackController
import com.virtualworld.easymusic.playback.PlayerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerUiState(
    val playerState: PlayerState = PlayerState(),
    val currentPosition: Long = 0L,
    val songs: List<Song> = emptyList(),
    val isLoading: Boolean = true,
    val lyricsSheetVisible: Boolean = false,
    val lyricsLoading: Boolean = false,
    val lyricsSearchingAlternatives: Boolean = false,
    val lyricsResult: LyricsResult? = null
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val getSongsUseCase: GetSongsUseCase,
    private val getLastPlayedUseCase: GetLastPlayedUseCase,
    private val saveLastPlayedUseCase: SaveLastPlayedUseCase,
    private val excludeSongFromLibraryUseCase: ExcludeSongFromLibraryUseCase,
    private val fetchLyricsUseCase: FetchLyricsUseCase,
    val playbackController: PlaybackController
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()
    private var lyricsFetchJob: Job? = null

    init {
        playbackController.connect()
        observePlayerState()
        startPositionUpdater()
        loadSongsAndLastPlayed()
        viewModelScope.launch {
            excludeSongFromLibraryUseCase.observeExcludedIds().collect {
                try {
                    val songs = getSongsUseCase()
                    _uiState.update { state -> state.copy(songs = songs) }
                } catch (_: Exception) {
                }
            }
        }
    }

    private fun observePlayerState() {
        viewModelScope.launch {
            playbackController.playerState.collectLatest { state ->
                _uiState.update { current ->
                    val oldId = current.playerState.currentSong?.id
                    val newId = state.currentSong?.id
                    val songChanged = oldId != null && oldId != newId
                    current.copy(
                        playerState = state,
                        lyricsSheetVisible = if (songChanged) false else current.lyricsSheetVisible,
                        lyricsLoading = if (songChanged) false else current.lyricsLoading,
                        lyricsSearchingAlternatives = if (songChanged) false else current.lyricsSearchingAlternatives,
                        lyricsResult = if (songChanged) null else current.lyricsResult
                    )
                }
                state.currentSong?.let { song ->
                    saveLastPlayedUseCase(song.id)
                }
            }
        }
    }

    private fun startPositionUpdater() {
        viewModelScope.launch {
            while (true) {
                delay(500L)
                val position = playbackController.getCurrentPosition()
                _uiState.update { it.copy(currentPosition = position) }
            }
        }
    }

    private fun loadSongsAndLastPlayed() {
        viewModelScope.launch {
            try {
                val songs = getSongsUseCase()
                _uiState.update { it.copy(songs = songs, isLoading = false) }

                getLastPlayedUseCase().collectLatest { lastPlayedId ->
                    if (lastPlayedId != null && _uiState.value.playerState.currentSong == null) {
                        val lastSong = songs.find { it.id == lastPlayedId }
                        if (lastSong != null) {
                            _uiState.update {
                                it.copy(
                                    playerState = it.playerState.copy(currentSong = lastSong)
                                )
                            }
                        }
                    }
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun togglePlayPause() {
        val state = _uiState.value
        if (state.playerState.currentSong != null && state.songs.isNotEmpty()) {
            if (!state.playerState.isConnected) return
            if (state.playerState.isPlaying || state.playerState.duration > 0) {
                playbackController.togglePlayPause()
            } else {
                val index = state.songs.indexOfFirst { it.id == state.playerState.currentSong.id }
                playbackController.playSongs(state.songs, index.coerceAtLeast(0))
            }
        } else if (state.songs.isNotEmpty()) {
            playbackController.playSongs(state.songs, 0)
        }
    }

    fun next() = playbackController.next()
    fun previous() = playbackController.previous()
    fun seekTo(position: Long) = playbackController.seekTo(position)
    fun toggleShuffle() = playbackController.toggleShuffle()
    fun toggleRepeatMode() = playbackController.toggleRepeatMode()

    fun excludeCurrentSongFromLibrary() {
        viewModelScope.launch {
            val song = _uiState.value.playerState.currentSong ?: return@launch
            excludeSongFromLibraryUseCase(song.id)
            playbackController.removeCurrentSongFromQueue()
        }
    }

    fun toggleLyricsSheet() {
        val snapshot = _uiState.value
        if (snapshot.lyricsSheetVisible) {
            lyricsFetchJob?.cancel()
            _uiState.update {
                it.copy(
                    lyricsSheetVisible = false,
                    lyricsLoading = false,
                    lyricsSearchingAlternatives = false,
                    lyricsResult = null
                )
            }
            return
        }
        val song = snapshot.playerState.currentSong ?: return
        lyricsFetchJob?.cancel()
        _uiState.update {
            it.copy(
                lyricsSheetVisible = true,
                lyricsLoading = true,
                lyricsSearchingAlternatives = false,
                lyricsResult = null
            )
        }
        lyricsFetchJob = viewModelScope.launch {
            val exact = fetchLyricsUseCase.fetchExact(song)
            if (!isActive) return@launch
            if (exact is LyricsResult.NotFound) {
                _uiState.update { it.copy(lyricsSearchingAlternatives = true) }
                val searchResult = fetchLyricsUseCase.searchAlternatives(song)
                if (!isActive) return@launch
                _uiState.update {
                    it.copy(
                        lyricsLoading = false,
                        lyricsSearchingAlternatives = false,
                        lyricsResult = searchResult
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        lyricsLoading = false,
                        lyricsSearchingAlternatives = false,
                        lyricsResult = exact
                    )
                }
            }
        }
    }

    fun dismissLyricsSheet() {
        lyricsFetchJob?.cancel()
        _uiState.update {
            it.copy(
                lyricsSheetVisible = false,
                lyricsLoading = false,
                lyricsSearchingAlternatives = false,
                lyricsResult = null
            )
        }
    }

    fun loadLyricsForLrcLibId(lrcLibId: Long) {
        lyricsFetchJob?.cancel()
        _uiState.update {
            it.copy(lyricsLoading = true, lyricsSearchingAlternatives = false)
        }
        lyricsFetchJob = viewModelScope.launch {
            val result = fetchLyricsUseCase.byLrcLibId(lrcLibId)
            if (!isActive) return@launch
            _uiState.update {
                it.copy(lyricsLoading = false, lyricsResult = result)
            }
        }
    }
}
