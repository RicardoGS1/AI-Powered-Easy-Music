package com.virtualworld.easymusic.ui.player

import android.app.Application
import android.content.IntentSender
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.virtualworld.easymusic.domain.model.Song
import com.virtualworld.easymusic.domain.model.Video
import com.virtualworld.easymusic.domain.model.LyricsResult
import com.virtualworld.easymusic.domain.model.SongMetadataEdit
import com.virtualworld.easymusic.domain.model.SongMetadataLookupResult
import com.virtualworld.easymusic.domain.model.SongInsightResult
import com.virtualworld.easymusic.domain.model.UpdateSongMetadataResult
import com.virtualworld.easymusic.domain.model.UpdateVideoTitleResult
import com.virtualworld.easymusic.data.preferences.MusicPreferences
import com.virtualworld.easymusic.domain.usecase.ExcludeSongFromLibraryUseCase
import com.virtualworld.easymusic.domain.usecase.FetchSongInsightUseCase
import com.virtualworld.easymusic.domain.usecase.FetchSongMetadataFromAiUseCase
import com.virtualworld.easymusic.domain.usecase.FetchLyricsUseCase
import com.virtualworld.easymusic.domain.usecase.GetPlaybackSessionUseCase
import com.virtualworld.easymusic.domain.usecase.GetSongsUseCase
import com.virtualworld.easymusic.domain.usecase.ObserveFavoriteSongIdsUseCase
import com.virtualworld.easymusic.domain.usecase.ObserveFavoriteVideoIdsUseCase
import com.virtualworld.easymusic.domain.usecase.SaveLastPlayedUseCase
import com.virtualworld.easymusic.domain.usecase.SavePlaybackSessionUseCase
import com.virtualworld.easymusic.domain.usecase.ToggleFavoriteSongUseCase
import com.virtualworld.easymusic.domain.usecase.ToggleFavoriteVideoUseCase
import com.virtualworld.easymusic.domain.usecase.UpdateSongMetadataUseCase
import com.virtualworld.easymusic.domain.usecase.UpdateVideoTitleUseCase
import com.virtualworld.easymusic.firebase.RemoteConfigValues
import com.virtualworld.easymusic.R
import com.virtualworld.easymusic.playback.PlaybackController
import com.virtualworld.easymusic.playback.PlayerState
import com.virtualworld.easymusic.playback.VideoPlaybackController
import androidx.media3.common.Player
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerUiState(
    val playerState: PlayerState = PlayerState(),
    val currentPosition: Long = 0L,
    val songs: List<Song> = emptyList(),
    val favoriteSongIds: Set<Long> = emptySet(),
    val favoriteVideoIds: Set<Long> = emptySet(),
    val isLoading: Boolean = true,
    val lyricsSheetVisible: Boolean = false,
    val lyricsLoading: Boolean = false,
    val lyricsSearchingAlternatives: Boolean = false,
    val lyricsResult: LyricsResult? = null,
    val insightSheetVisible: Boolean = false,
    val insightLoading: Boolean = false,
    val insightResult: SongInsightResult? = null,
    /** Kill switch vía Firebase Remote Config ([RemoteConfigKeys.ENABLE_AI_INSIGHT]). */
    val aiInsightEnabled: Boolean = true,
    val skipRemoveFromQueueConfirmation: Boolean = false,
    val activeVideo: Video? = null,
    val playbackQueue: List<Song> = emptyList(),
    val videoQueue: List<Video> = emptyList(),
    val videoIsPlaying: Boolean = false,
    val videoDuration: Long = 0L,
    val videoFullscreen: Boolean = false,
    val videoPlayer: Player? = null,
    val metadataEditorVisible: Boolean = false,
    val metadataEditorTitle: String = "",
    val metadataEditorArtist: String = "",
    val metadataEditorAlbum: String = "",
    val metadataAiLoading: Boolean = false,
    val metadataSaving: Boolean = false,
    val metadataEditorError: String? = null,
    val metadataWritePermissionRequest: IntentSender? = null,
    val videoTitleEditorVisible: Boolean = false,
    val videoTitleEditorTitle: String = "",
    val videoTitleSaving: Boolean = false,
    val videoTitleEditorError: String? = null,
    val videoTitleWritePermissionRequest: IntentSender? = null,
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val getSongsUseCase: GetSongsUseCase,
    private val getPlaybackSessionUseCase: GetPlaybackSessionUseCase,
    private val saveLastPlayedUseCase: SaveLastPlayedUseCase,
    private val savePlaybackSessionUseCase: SavePlaybackSessionUseCase,
    private val excludeSongFromLibraryUseCase: ExcludeSongFromLibraryUseCase,
    private val fetchLyricsUseCase: FetchLyricsUseCase,
    private val fetchSongInsightUseCase: FetchSongInsightUseCase,
    private val fetchSongMetadataFromAiUseCase: FetchSongMetadataFromAiUseCase,
    private val updateSongMetadataUseCase: UpdateSongMetadataUseCase,
    private val updateVideoTitleUseCase: UpdateVideoTitleUseCase,
    private val observeFavoriteSongIdsUseCase: ObserveFavoriteSongIdsUseCase,
    private val observeFavoriteVideoIdsUseCase: ObserveFavoriteVideoIdsUseCase,
    private val toggleFavoriteSongUseCase: ToggleFavoriteSongUseCase,
    private val toggleFavoriteVideoUseCase: ToggleFavoriteVideoUseCase,
    private val remoteConfigValues: RemoteConfigValues,
    private val musicPreferences: MusicPreferences,
    private val app: Application,
    val playbackController: PlaybackController,
    private val videoPlaybackController: VideoPlaybackController,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()
    private var lyricsFetchJob: Job? = null
    private var insightFetchJob: Job? = null
    private var metadataAiFetchJob: Job? = null
    private var metadataSaveJob: Job? = null
    private var metadataWriteAccessConfirmed = false
    private var videoTitleSaveJob: Job? = null
    private var videoTitleWriteAccessConfirmed = false
    private var sessionRestored = false
    private var lastPersistedSongId: Long? = null

    init {
        playbackController.connect()
        videoPlaybackController.connect()
        observePlayerState()
        observeVideoState()
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
        viewModelScope.launch {
            observeFavoriteSongIdsUseCase().collect { ids ->
                _uiState.update { it.copy(favoriteSongIds = ids) }
            }
        }
        viewModelScope.launch {
            observeFavoriteVideoIdsUseCase().collect { ids ->
                _uiState.update { it.copy(favoriteVideoIds = ids) }
            }
        }
        viewModelScope.launch {
            musicPreferences.skipRemoveFromQueueConfirmation().collect { skip ->
                _uiState.update { it.copy(skipRemoveFromQueueConfirmation = skip) }
            }
        }
        refreshAiInsightRemoteFlag()
    }

    fun refreshAiInsightRemoteFlag() {
        _uiState.update {
            it.copy(aiInsightEnabled = remoteConfigValues.isAiInsightEnabled())
        }
    }

    private fun observeVideoState() {
        viewModelScope.launch {
            videoPlaybackController.state.collectLatest { state ->
                _uiState.update { current ->
                    val oldId = current.activeVideo?.id
                    val newId = state.currentVideo?.id
                    val videoChanged = oldId != null && oldId != newId
                    if (videoChanged) {
                        videoTitleSaveJob?.cancel()
                        videoTitleWriteAccessConfirmed = false
                    }
                    current.copy(
                        activeVideo = state.currentVideo,
                        videoQueue = videoPlaybackController.getPlaylist(),
                        videoIsPlaying = state.isPlaying,
                        videoDuration = state.duration,
                        videoFullscreen = state.isFullscreen,
                        videoPlayer = if (state.isConnected) videoPlaybackController.getPlayer() else null,
                        videoTitleEditorVisible = if (videoChanged) false else current.videoTitleEditorVisible,
                        videoTitleSaving = if (videoChanged) false else current.videoTitleSaving,
                        videoTitleEditorError = if (videoChanged) null else current.videoTitleEditorError,
                        videoTitleWritePermissionRequest = if (videoChanged) null else current.videoTitleWritePermissionRequest,
                    )
                }
            }
        }
    }

    private fun observePlayerState() {
        viewModelScope.launch {
            playbackController.playerState.collectLatest { state ->
                if (state.isPlaying && _uiState.value.activeVideo != null) {
                    videoPlaybackController.clearVideo()
                }
                _uiState.update { current ->
                    val oldId = current.playerState.currentSong?.id
                    val newId = state.currentSong?.id
                    val songChanged = oldId != null && oldId != newId
                    if (songChanged) {
                        lyricsFetchJob?.cancel()
                        insightFetchJob?.cancel()
                        metadataAiFetchJob?.cancel()
                        metadataSaveJob?.cancel()
                        metadataWriteAccessConfirmed = false
                    }
                    current.copy(
                        playerState = state,
                        playbackQueue = playbackController.getPlaylist(),
                        lyricsSheetVisible = if (songChanged) false else current.lyricsSheetVisible,
                        lyricsLoading = if (songChanged) false else current.lyricsLoading,
                        lyricsSearchingAlternatives = if (songChanged) false else current.lyricsSearchingAlternatives,
                        lyricsResult = if (songChanged) null else current.lyricsResult,
                        insightSheetVisible = if (songChanged) false else current.insightSheetVisible,
                        insightLoading = if (songChanged) false else current.insightLoading,
                        insightResult = if (songChanged) null else current.insightResult,
                        metadataEditorVisible = if (songChanged) false else current.metadataEditorVisible,
                        metadataAiLoading = if (songChanged) false else current.metadataAiLoading,
                        metadataSaving = if (songChanged) false else current.metadataSaving,
                        metadataEditorError = if (songChanged) null else current.metadataEditorError,
                        metadataWritePermissionRequest = if (songChanged) null else current.metadataWritePermissionRequest,
                    )
                }
                state.currentSong?.let { song ->
                    saveLastPlayedUseCase(song.id)
                    if (song.id != lastPersistedSongId) {
                        lastPersistedSongId = song.id
                        persistPlaybackSession()
                    }
                }
            }
        }
    }

    private fun startPositionUpdater() {
        viewModelScope.launch {
            var ticksSinceLastSave = 0
            while (true) {
                delay(500L)
                val activeVideo = _uiState.value.activeVideo
                val position = if (activeVideo != null) {
                    videoPlaybackController.getCurrentPosition()
                } else {
                    playbackController.getCurrentPosition()
                }
                _uiState.update { it.copy(currentPosition = position) }
                if (activeVideo == null && playbackController.getCurrentSession() != null) {
                    ticksSinceLastSave++
                    if (ticksSinceLastSave >= 4) {
                        ticksSinceLastSave = 0
                        persistPlaybackSession()
                    }
                }
            }
        }
    }

    private fun persistPlaybackSession() {
        viewModelScope.launch {
            playbackController.getCurrentSession()?.let { savePlaybackSessionUseCase(it) }
        }
    }

    private fun loadSongsAndLastPlayed() {
        viewModelScope.launch {
            try {
                val songs = getSongsUseCase()
                _uiState.update { it.copy(songs = songs, isLoading = false) }
                restoreSavedPlaybackSession(songs)
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun restoreSavedPlaybackSession(songs: List<Song>) {
        viewModelScope.launch {
            if (sessionRestored) return@launch
            val session = getPlaybackSessionUseCase() ?: return@launch
            playbackController.playerState.first { it.isConnected }
            if (sessionRestored) return@launch
            sessionRestored = true
            playbackController.restoreSession(songs, session)
            _uiState.update { it.copy(currentPosition = session.positionMs) }
        }
    }

    fun togglePlayPause() {
        if (_uiState.value.activeVideo != null) {
            videoPlaybackController.togglePlayPause()
            return
        }
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

    fun next() {
        if (_uiState.value.activeVideo != null) {
            videoPlaybackController.next()
        } else {
            playbackController.next()
        }
    }

    fun previous() {
        if (_uiState.value.activeVideo != null) {
            videoPlaybackController.previous()
        } else {
            playbackController.previous()
        }
    }

    fun seekTo(position: Long) {
        if (_uiState.value.activeVideo != null) {
            videoPlaybackController.seekTo(position)
        } else {
            playbackController.seekTo(position)
        }
    }

    fun toggleShuffle() = playbackController.toggleShuffle()
    fun toggleRepeatMode() = playbackController.toggleRepeatMode()

    fun setVideoFullscreen(fullscreen: Boolean) {
        videoPlaybackController.setFullscreen(fullscreen)
    }

    fun getVideoPlayer() = videoPlaybackController.getPlayer()

    fun excludeCurrentSongFromLibrary() {
        viewModelScope.launch {
            val song = _uiState.value.playerState.currentSong ?: return@launch
            excludeSongFromLibraryUseCase(song.id)
            playbackController.removeCurrentSongFromQueue()
        }
    }

    fun setSkipRemoveFromQueueConfirmation(skip: Boolean) {
        viewModelScope.launch {
            musicPreferences.setSkipRemoveFromQueueConfirmation(skip)
        }
    }

    fun toggleFavoriteCurrentSong() {
        val songId = _uiState.value.playerState.currentSong?.id ?: return
        viewModelScope.launch {
            toggleFavoriteSongUseCase(songId)
        }
    }

    fun toggleFavoriteCurrentVideo() {
        val videoId = _uiState.value.activeVideo?.id ?: return
        viewModelScope.launch {
            toggleFavoriteVideoUseCase(videoId)
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
        insightFetchJob?.cancel()
        _uiState.update {
            it.copy(
                insightSheetVisible = false,
                insightLoading = false,
                insightResult = null,
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

    fun toggleInsightSheet() {
        val snapshot = _uiState.value
        if (snapshot.insightSheetVisible) {
            insightFetchJob?.cancel()
            _uiState.update {
                it.copy(
                    insightSheetVisible = false,
                    insightLoading = false,
                    insightResult = null
                )
            }
            return
        }
        val aiEnabled = remoteConfigValues.isAiInsightEnabled()
        _uiState.update { it.copy(aiInsightEnabled = aiEnabled) }
        if (!aiEnabled) return
        val song = snapshot.playerState.currentSong ?: return
        insightFetchJob?.cancel()
        lyricsFetchJob?.cancel()
        _uiState.update {
            it.copy(
                lyricsSheetVisible = false,
                lyricsLoading = false,
                lyricsSearchingAlternatives = false,
                lyricsResult = null,
                insightSheetVisible = true,
                insightLoading = true,
                insightResult = null
            )
        }
        insightFetchJob = viewModelScope.launch {
            val result = fetchSongInsightUseCase(song)
            if (!isActive) return@launch
            _uiState.update {
                it.copy(insightLoading = false, insightResult = result)
            }
        }
    }

    fun dismissInsightSheet() {
        insightFetchJob?.cancel()
        _uiState.update {
            it.copy(
                insightSheetVisible = false,
                insightLoading = false,
                insightResult = null
            )
        }
    }

    fun openMetadataEditor() {
        val song = _uiState.value.playerState.currentSong ?: return
        metadataAiFetchJob?.cancel()
        metadataSaveJob?.cancel()
        metadataWriteAccessConfirmed = false
        _uiState.update {
            it.copy(
                metadataEditorVisible = true,
                metadataEditorTitle = song.title,
                metadataEditorArtist = song.artist,
                metadataEditorAlbum = song.album,
                metadataAiLoading = false,
                metadataSaving = false,
                metadataEditorError = null,
            )
        }
    }

    fun dismissMetadataEditor() {
        metadataAiFetchJob?.cancel()
        metadataSaveJob?.cancel()
        metadataWriteAccessConfirmed = false
        _uiState.update {
            it.copy(
                metadataEditorVisible = false,
                metadataAiLoading = false,
                metadataSaving = false,
                metadataEditorError = null,
                metadataWritePermissionRequest = null,
            )
        }
    }

    fun clearMetadataWritePermissionRequest() {
        _uiState.update { it.copy(metadataWritePermissionRequest = null) }
    }

    fun onMetadataWritePermissionResult(granted: Boolean) {
        clearMetadataWritePermissionRequest()
        if (granted) {
            metadataWriteAccessConfirmed = true
            saveMetadataEdits()
        } else {
            metadataWriteAccessConfirmed = false
            _uiState.update {
                it.copy(
                    metadataSaving = false,
                    metadataEditorError = app.getString(R.string.metadata_permission_denied),
                )
            }
        }
    }

    fun updateMetadataEditorTitle(value: String) {
        _uiState.update { it.copy(metadataEditorTitle = value, metadataEditorError = null) }
    }

    fun updateMetadataEditorArtist(value: String) {
        _uiState.update { it.copy(metadataEditorArtist = value, metadataEditorError = null) }
    }

    fun updateMetadataEditorAlbum(value: String) {
        _uiState.update { it.copy(metadataEditorAlbum = value, metadataEditorError = null) }
    }

    fun fetchMetadataFromAi() {
        val snapshot = _uiState.value
        val song = snapshot.playerState.currentSong ?: return
        if (!remoteConfigValues.isAiInsightEnabled()) {
            _uiState.update {
                it.copy(aiInsightEnabled = false)
            }
            return
        }
        metadataAiFetchJob?.cancel()
        _uiState.update { it.copy(metadataAiLoading = true, metadataEditorError = null) }
        metadataAiFetchJob = viewModelScope.launch {
            when (val result = fetchSongMetadataFromAiUseCase(song)) {
                is SongMetadataLookupResult.Success -> {
                    _uiState.update {
                        it.copy(
                            metadataAiLoading = false,
                            metadataEditorTitle = result.title,
                            metadataEditorArtist = result.artist,
                            metadataEditorAlbum = result.album,
                        )
                    }
                }
                is SongMetadataLookupResult.NotFound -> {
                    _uiState.update {
                        it.copy(metadataAiLoading = false, metadataEditorError = result.message)
                    }
                }
                is SongMetadataLookupResult.Error -> {
                    _uiState.update {
                        it.copy(metadataAiLoading = false, metadataEditorError = result.message)
                    }
                }
            }
        }
    }

    fun saveMetadataEdits() {
        val snapshot = _uiState.value
        val song = snapshot.playerState.currentSong ?: return
        metadataSaveJob?.cancel()
        _uiState.update { it.copy(metadataSaving = true, metadataEditorError = null) }
        metadataSaveJob = viewModelScope.launch {
            val result = updateSongMetadataUseCase(
                songId = song.id,
                metadata = SongMetadataEdit(
                    title = snapshot.metadataEditorTitle,
                    artist = snapshot.metadataEditorArtist,
                    album = snapshot.metadataEditorAlbum,
                ),
                writeAccessConfirmed = metadataWriteAccessConfirmed,
            )
            if (!isActive) return@launch
            when (result) {
                is UpdateSongMetadataResult.Success -> {
                    metadataWriteAccessConfirmed = false
                    playbackController.updateSongInPlaylist(result.updatedSong)
                    val songs = getSongsUseCase()
                    _uiState.update {
                        it.copy(
                            songs = songs,
                            playbackQueue = playbackController.getPlaylist(),
                            metadataSaving = false,
                            metadataEditorVisible = false,
                            metadataEditorError = null,
                            metadataWritePermissionRequest = null,
                        )
                    }
                }
                is UpdateSongMetadataResult.NeedsWritePermission -> {
                    _uiState.update {
                        it.copy(
                            metadataSaving = false,
                            metadataWritePermissionRequest = result.intentSender,
                        )
                    }
                }
                is UpdateSongMetadataResult.Error -> {
                    metadataWriteAccessConfirmed = false
                    _uiState.update {
                        it.copy(metadataSaving = false, metadataEditorError = result.message)
                    }
                }
            }
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

    fun openVideoTitleEditor() {
        val video = _uiState.value.activeVideo ?: return
        videoTitleSaveJob?.cancel()
        videoTitleWriteAccessConfirmed = false
        _uiState.update {
            it.copy(
                videoTitleEditorVisible = true,
                videoTitleEditorTitle = video.title,
                videoTitleSaving = false,
                videoTitleEditorError = null,
            )
        }
    }

    fun dismissVideoTitleEditor() {
        videoTitleSaveJob?.cancel()
        videoTitleWriteAccessConfirmed = false
        _uiState.update {
            it.copy(
                videoTitleEditorVisible = false,
                videoTitleSaving = false,
                videoTitleEditorError = null,
                videoTitleWritePermissionRequest = null,
            )
        }
    }

    fun clearVideoTitleWritePermissionRequest() {
        _uiState.update { it.copy(videoTitleWritePermissionRequest = null) }
    }

    fun onVideoTitleWritePermissionResult(granted: Boolean) {
        clearVideoTitleWritePermissionRequest()
        if (granted) {
            videoTitleWriteAccessConfirmed = true
            saveVideoTitleEdits()
        } else {
            videoTitleWriteAccessConfirmed = false
            _uiState.update {
                it.copy(
                    videoTitleSaving = false,
                    videoTitleEditorError = app.getString(R.string.metadata_permission_denied),
                )
            }
        }
    }

    fun updateVideoTitleEditorTitle(value: String) {
        _uiState.update { it.copy(videoTitleEditorTitle = value, videoTitleEditorError = null) }
    }

    fun saveVideoTitleEdits() {
        val snapshot = _uiState.value
        val video = snapshot.activeVideo ?: return
        videoTitleSaveJob?.cancel()
        _uiState.update { it.copy(videoTitleSaving = true, videoTitleEditorError = null) }
        videoTitleSaveJob = viewModelScope.launch {
            val result = updateVideoTitleUseCase(
                videoId = video.id,
                title = snapshot.videoTitleEditorTitle,
                writeAccessConfirmed = videoTitleWriteAccessConfirmed,
            )
            if (!isActive) return@launch
            when (result) {
                is UpdateVideoTitleResult.Success -> {
                    videoTitleWriteAccessConfirmed = false
                    videoPlaybackController.updateVideoInPlaylist(result.updatedVideo)
                    _uiState.update {
                        it.copy(
                            videoTitleSaving = false,
                            videoTitleEditorVisible = false,
                            videoTitleEditorError = null,
                            videoTitleWritePermissionRequest = null,
                        )
                    }
                }
                is UpdateVideoTitleResult.NeedsWritePermission -> {
                    _uiState.update {
                        it.copy(
                            videoTitleSaving = false,
                            videoTitleWritePermissionRequest = result.intentSender,
                        )
                    }
                }
                is UpdateVideoTitleResult.Error -> {
                    videoTitleWriteAccessConfirmed = false
                    _uiState.update {
                        it.copy(videoTitleSaving = false, videoTitleEditorError = result.message)
                    }
                }
            }
        }
    }

}
