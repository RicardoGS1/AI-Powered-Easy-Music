package com.virtualworld.easymusic.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.virtualworld.easymusic.ui.library.tabs.AlbumsTab
import com.virtualworld.easymusic.ui.library.tabs.ArtistsTab
import com.virtualworld.easymusic.ui.library.tabs.SongsTab
import com.virtualworld.easymusic.ui.theme.DarkBackground
import com.virtualworld.easymusic.ui.theme.DarkSurface
import com.virtualworld.easymusic.ui.theme.Teal400
import com.virtualworld.easymusic.ui.theme.TextGray
import com.virtualworld.easymusic.ui.theme.TextWhite

private val tabs = listOf("Canciones", "Albums", "Artistas")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onNavigateBack: () -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
                    onClick = { viewModel.selectTab(index) },
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
                    songs = uiState.songs,
                    onSongClick = { song ->
                        viewModel.playSong(song)
                        onNavigateBack()
                    }
                )
                1 -> AlbumsTab(
                    albums = uiState.albums,
                    onAlbumClick = { album ->
                        viewModel.playAlbumSongs(album.id)
                        onNavigateBack()
                    }
                )
                2 -> ArtistsTab(
                    artists = uiState.artists,
                    onArtistClick = { }
                )
            }
        }
    }
}
