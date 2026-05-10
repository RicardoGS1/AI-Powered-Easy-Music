package com.virtualworld.easymusic.ui.library.tabs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.virtualworld.easymusic.domain.model.Song
import com.virtualworld.easymusic.ui.components.SongItem
import com.virtualworld.easymusic.ui.theme.DarkSurfaceVariant
import com.virtualworld.easymusic.ui.theme.TextGray

@Composable
fun SongsTab(
    songs: List<Song>,
    onSongClick: (Song) -> Unit,
    emptyMessage: String = "No se encontraron canciones",
    modifier: Modifier = Modifier.fillMaxSize()
) {
    if (songs.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emptyMessage,
                style = MaterialTheme.typography.bodyLarge,
                color = TextGray
            )
        }
    } else {
        LazyColumn(modifier = modifier) {
            items(songs, key = { it.id }) { song ->
                SongItem(
                    song = song,
                    onClick = { onSongClick(song) }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(start = 80.dp),
                    color = DarkSurfaceVariant,
                    thickness = 0.5.dp
                )
            }
        }
    }
}
