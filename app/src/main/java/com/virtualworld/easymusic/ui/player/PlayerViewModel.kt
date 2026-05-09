package com.virtualworld.easymusic.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.virtualworld.easymusic.domain.model.Song
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerUiState(
    val playerState: PlayerState = PlayerState(),
    val currentPosition: Long = 0L,
    val songs: List<Song> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val getSongsUseCase: GetSongsUseCase,
    private val getLastPlayedUseCase: GetLastPlayedUseCase,
    private val saveLastPlayedUseCase: SaveLastPlayedUseCase,
    val playbackController: PlaybackController
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    init {
        playbackController.connect()
        observePlayerState()
        startPositionUpdater()
        loadSongsAndLastPlayed()
    }

    private fun observePlayerState() {
        viewModelScope.launch {
            playbackController.playerState.collectLatest { state ->
                _uiState.update { it.copy(playerState = state) }
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
}
