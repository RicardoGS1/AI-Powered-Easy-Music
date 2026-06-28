package com.virtualworld.easymusic.ui.video

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.virtualworld.easymusic.R
import com.virtualworld.easymusic.ads.LIBRARY_NATIVE_AD_KEY
import com.virtualworld.easymusic.ads.LibraryNativeListAd
import com.virtualworld.easymusic.domain.model.Video
import com.virtualworld.easymusic.ui.components.VideoItem
import com.virtualworld.easymusic.ui.theme.DarkBackground
import com.virtualworld.easymusic.ui.theme.DarkSurface
import com.virtualworld.easymusic.ui.theme.Teal400
import com.virtualworld.easymusic.ui.theme.TextGray
import com.virtualworld.easymusic.ui.theme.TextWhite

@Composable
fun VideoLibraryScreen(
    onNavigateBack: () -> Unit,
    viewModel: VideoLibraryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filteredVideos = remember(
        uiState.videos,
        uiState.favoriteVideoIds,
        uiState.searchQuery,
        uiState.selectedTab,
    ) {
        viewModel.filteredVideos()
    }

    VideoLibraryContent(
        isLoading = uiState.isLoading,
        searchQuery = uiState.searchQuery,
        selectedTab = uiState.selectedTab,
        favoriteVideoIds = uiState.favoriteVideoIds,
        videos = filteredVideos,
        onNavigateBack = onNavigateBack,
        onSearchQueryChange = { viewModel.setSearchQuery(it) },
        onTabSelected = { viewModel.selectTab(it) },
        onToggleFavorite = { viewModel.toggleFavorite(it) },
        onVideoClick = { video ->
            viewModel.selectVideo(video)
            onNavigateBack()
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VideoLibraryContent(
    isLoading: Boolean,
    searchQuery: String,
    selectedTab: Int,
    favoriteVideoIds: Set<Long>,
    videos: List<Video>,
    onNavigateBack: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onTabSelected: (Int) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onVideoClick: (Video) -> Unit,
) {
    var searchExpanded by remember { mutableStateOf(false) }
    val searchFieldFocusRequester = remember { FocusRequester() }

    val tabs = listOf(
        stringResource(R.string.tab_favorites),
        stringResource(R.string.video_tab_all),
    )

    val noVideoFavoritesYetText = stringResource(R.string.no_video_favorites_yet)
    val noVideosFoundText = stringResource(R.string.no_videos_found)
    val emptyMessage = remember(selectedTab, favoriteVideoIds, searchQuery) {
        when {
            selectedTab == VideoLibraryTabs.TAB_FAVORITES &&
                favoriteVideoIds.isEmpty() && searchQuery.isBlank() -> noVideoFavoritesYetText
            else -> noVideosFoundText
        }
    }

    LaunchedEffect(searchExpanded) {
        if (searchExpanded) {
            searchFieldFocusRequester.requestFocus()
        }
    }

    fun collapseSearch() {
        searchExpanded = false
        onSearchQueryChange("")
    }

    BackHandler(enabled = searchExpanded) {
        collapseSearch()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
    ) {
        TopAppBar(
            title = {
                if (searchExpanded) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(searchFieldFocusRequester),
                        placeholder = {
                            Text(
                                text = stringResource(R.string.video_search_hint),
                                color = TextGray,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = TextGray,
                                modifier = Modifier.padding(start = 4.dp),
                            )
                        },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextWhite),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        shape = MaterialTheme.shapes.small,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = Teal400,
                            unfocusedBorderColor = TextGray.copy(alpha = 0.5f),
                            cursorColor = Teal400,
                            focusedPlaceholderColor = TextGray,
                            unfocusedPlaceholderColor = TextGray,
                            focusedLeadingIconColor = Teal400,
                            unfocusedLeadingIconColor = TextGray,
                        ),
                    )
                } else {
                    Text(
                        text = stringResource(R.string.video_library_title),
                        color = TextWhite,
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back),
                        tint = TextWhite,
                    )
                }
            },
            actions = {
                if (searchExpanded) {
                    IconButton(onClick = { collapseSearch() }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.cd_close_search),
                            tint = TextWhite,
                        )
                    }
                } else {
                    IconButton(onClick = { searchExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(R.string.cd_search),
                            tint = TextWhite,
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground),
        )

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = DarkSurface,
            contentColor = Teal400,
            indicator = { tabPositions ->
                if (selectedTab < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Teal400,
                    )
                }
            },
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { onTabSelected(index) },
                    text = {
                        Text(
                            text = title,
                            color = if (selectedTab == index) Teal400 else TextGray,
                        )
                    },
                )
            }
        }

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Teal400)
                }
            }
            videos.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (selectedTab == VideoLibraryTabs.TAB_ALL && searchQuery.isBlank()) {
                            stringResource(R.string.video_library_empty)
                        } else {
                            emptyMessage
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextGray,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            else -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    videos.forEachIndexed { index, video ->
                        item(key = video.id) {
                            VideoItem(
                                video = video,
                                isFavorite = video.id in favoriteVideoIds,
                                onToggleFavorite = { onToggleFavorite(video.id) },
                                onClick = { onVideoClick(video) },
                            )
                        }
                        if (index == 0) {
                            item(key = LIBRARY_NATIVE_AD_KEY) {
                                LibraryNativeListAd()
                            }
                        }
                    }
                }
            }
        }
    }
}
