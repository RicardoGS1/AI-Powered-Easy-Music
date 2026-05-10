package com.virtualworld.easymusic.ui.library

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.virtualworld.easymusic.domain.model.Album
import com.virtualworld.easymusic.domain.model.Artist
import com.virtualworld.easymusic.domain.model.Song
import com.virtualworld.easymusic.ui.library.tabs.AlbumsTab
import com.virtualworld.easymusic.ui.library.tabs.ArtistsTab
import com.virtualworld.easymusic.ui.library.tabs.SongsTab
import com.virtualworld.easymusic.ui.theme.DarkBackground
import com.virtualworld.easymusic.ui.theme.DarkSurface
import com.virtualworld.easymusic.ui.theme.EasyMusicTheme
import com.virtualworld.easymusic.ui.theme.Teal400
import com.virtualworld.easymusic.ui.theme.TextGray
import com.virtualworld.easymusic.ui.theme.TextWhite

private val tabs = listOf("Canciones", "Albums", "Artistas")

@Composable
fun LibraryScreen(
    initialFocusSearch: Boolean = false,
    onNavigateBack: () -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LibraryScreenContent(
        uiState = uiState,
        initialFocusSearch = initialFocusSearch,
        onNavigateBack = onNavigateBack,
        onSearchQueryChange = { viewModel.setSearchQuery(it) },
        onTabSelected = { viewModel.selectTab(it) },
        onSongClick = { song ->
            viewModel.playSong(song)
            onNavigateBack()
        },
        onAlbumClick = { album ->
            viewModel.playAlbumSongs(album.id)
            onNavigateBack()
        },
        onArtistClick = { }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibraryScreenContent(
    uiState: LibraryUiState,
    initialFocusSearch: Boolean,
    onNavigateBack: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onTabSelected: (Int) -> Unit,
    onSongClick: (Song) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onArtistClick: (Artist) -> Unit
) {
    val searchFieldFocusRequester = remember { FocusRequester() }

    val filteredSongs = remember(uiState.songs, uiState.searchQuery) {
        LibrarySearchFilters.songs(uiState.songs, uiState.searchQuery)
    }
    val filteredAlbums = remember(uiState.albums, uiState.searchQuery) {
        LibrarySearchFilters.albums(uiState.albums, uiState.searchQuery)
    }
    val filteredArtists = remember(uiState.artists, uiState.searchQuery) {
        LibrarySearchFilters.artists(uiState.artists, uiState.searchQuery)
    }

    LaunchedEffect(initialFocusSearch) {
        if (initialFocusSearch) {
            onTabSelected(0)
            searchFieldFocusRequester.requestFocus()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Biblioteca",
                    color = TextWhite
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = TextWhite
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = DarkSurface
            )
        )

        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .focusRequester(searchFieldFocusRequester),
            placeholder = {
                Text(
                    text = "Buscar canciones, álbumes o artistas",
                    color = TextGray,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = TextGray
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                focusedBorderColor = Teal400,
                unfocusedBorderColor = TextGray,
                cursorColor = Teal400,
                focusedPlaceholderColor = TextGray,
                unfocusedPlaceholderColor = TextGray,
                focusedLeadingIconColor = Teal400,
                unfocusedLeadingIconColor = TextGray
            )
        )

        TabRow(
            selectedTabIndex = uiState.selectedTab,
            containerColor = DarkSurface,
            contentColor = Teal400,
            indicator = { tabPositions ->
                if (uiState.selectedTab < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[uiState.selectedTab]),
                        color = Teal400
                    )
                }
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = uiState.selectedTab == index,
                    onClick = { onTabSelected(index) },
                    text = {
                        Text(
                            text = title,
                            color = if (uiState.selectedTab == index) Teal400 else TextGray
                        )
                    }
                )
            }
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Teal400)
            }
        } else {
            when (uiState.selectedTab) {
                0 -> SongsTab(
                    songs = filteredSongs,
                    onSongClick = onSongClick
                )
                1 -> AlbumsTab(
                    albums = filteredAlbums,
                    onAlbumClick = onAlbumClick
                )
                2 -> ArtistsTab(
                    artists = filteredArtists,
                    onArtistClick = onArtistClick
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LibraryScreenPreview() {
    EasyMusicTheme {
        LibraryScreenContent(
            uiState = LibraryUiState(
                songs = listOf(
                    Song(1L, "Song 1", "Artist 1", "Album 1", 1L, 180000L, Uri.EMPTY, null),
                    Song(2L, "Song 2", "Artist 2", "Album 2", 2L, 210000L, Uri.EMPTY, null)
                ),
                albums = listOf(
                    Album(1L, "Album 1", "Artist 1", null, 10),
                    Album(2L, "Album 2", "Artist 2", null, 12)
                ),
                artists = listOf(
                    Artist(1L, "Artist 1", 10, 1),
                    Artist(2L, "Artist 2", 12, 1)
                ),
                isLoading = false,
                selectedTab = 0
            ),
            initialFocusSearch = false,
            onNavigateBack = {},
            onSearchQueryChange = {},
            onTabSelected = {},
            onSongClick = {},
            onAlbumClick = {},
            onArtistClick = {}
        )
    }
}
