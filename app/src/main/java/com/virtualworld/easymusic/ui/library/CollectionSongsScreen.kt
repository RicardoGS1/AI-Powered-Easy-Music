package com.virtualworld.easymusic.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.virtualworld.easymusic.R
import com.virtualworld.easymusic.ui.library.tabs.SongsTab
import com.virtualworld.easymusic.ui.theme.DarkBackground
import com.virtualworld.easymusic.ui.theme.DarkSurface
import com.virtualworld.easymusic.ui.theme.Teal400
import com.virtualworld.easymusic.ui.theme.TextGray
import com.virtualworld.easymusic.ui.theme.TextWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionSongsScreen(
    onNavigateBack: () -> Unit,
    onPlaybackStarted: () -> Unit,
    viewModel: CollectionSongsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = uiState.title,
                        color = TextWhite,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 2
                    )
                    uiState.subtitle?.takeIf { it.isNotBlank() }?.let { sub ->
                        Text(
                            text = sub,
                            color = TextGray,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 2.dp),
                            maxLines = 1
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back),
                        tint = TextWhite
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
        )

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Teal400)
                }
            }

            uiState.isMissing -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    val message = uiState.title.ifEmpty { stringResource(R.string.no_songs_to_show) }
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextGray,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                Button(
                    onClick = {
                        viewModel.playAll()
                        onPlaybackStarted()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Teal400),
                    enabled = uiState.songs.isNotEmpty()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = TextWhite
                        )
                        Text(
                            text = stringResource(R.string.play_all),
                            color = TextWhite,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                SongsTab(
                    songs = uiState.songs,
                    onSongClick = { song ->
                        viewModel.playFrom(song)
                        onPlaybackStarted()
                    },
                    emptyMessage = stringResource(R.string.no_songs),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
                }
            }
        }
    }
}
