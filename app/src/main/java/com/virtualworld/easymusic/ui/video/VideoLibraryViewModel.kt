package com.virtualworld.easymusic.ui.video

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.virtualworld.easymusic.domain.model.Video
import com.virtualworld.easymusic.domain.usecase.GetVideosUseCase
import com.virtualworld.easymusic.domain.usecase.ObserveFavoriteVideoIdsUseCase
import com.virtualworld.easymusic.domain.usecase.ToggleFavoriteVideoUseCase
import com.virtualworld.easymusic.playback.VideoPlaybackController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

object VideoLibraryTabs {
    const val TAB_FAVORITES = 0
    const val TAB_ALL = 1
}

data class VideoLibraryUiState(
    val videos: List<Video> = emptyList(),
    val favoriteVideoIds: Set<Long> = emptySet(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val selectedTab: Int = VideoLibraryTabs.TAB_ALL,
)

@HiltViewModel
class VideoLibraryViewModel @Inject constructor(
    private val getVideosUseCase: GetVideosUseCase,
    private val observeFavoriteVideoIdsUseCase: ObserveFavoriteVideoIdsUseCase,
    private val toggleFavoriteVideoUseCase: ToggleFavoriteVideoUseCase,
    private val videoPlaybackController: VideoPlaybackController,
) : ViewModel() {

    private val _uiState = MutableStateFlow(VideoLibraryUiState())
    val uiState: StateFlow<VideoLibraryUiState> = _uiState.asStateFlow()

    init {
        videoPlaybackController.connect()
        loadVideos()
        viewModelScope.launch {
            observeFavoriteVideoIdsUseCase().collect { ids ->
                _uiState.update { it.copy(favoriteVideoIds = ids) }
            }
        }
    }

    private fun loadVideos() {
        viewModelScope.launch {
            try {
                val videos = getVideosUseCase()
                _uiState.update { it.copy(videos = videos, isLoading = false) }
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun selectTab(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun toggleFavorite(videoId: Long) {
        viewModelScope.launch {
            toggleFavoriteVideoUseCase(videoId)
        }
    }

    fun filteredVideos(): List<Video> {
        val state = _uiState.value
        val source = when (state.selectedTab) {
            VideoLibraryTabs.TAB_FAVORITES -> state.videos
                .filter { it.id in state.favoriteVideoIds }
                .sortedBy { it.title.lowercase() }
            else -> state.videos
        }
        val query = state.searchQuery.trim().lowercase()
        if (query.isEmpty()) return source
        return source.filter { it.title.lowercase().contains(query) }
    }

    fun selectVideo(video: Video) {
        val videos = filteredVideos()
        val index = videos.indexOfFirst { it.id == video.id }
        videoPlaybackController.selectVideo(
            video = video,
            videos = videos,
            startIndex = index.coerceAtLeast(0),
        )
    }
}
