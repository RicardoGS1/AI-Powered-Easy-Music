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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.virtualworld.easymusic.R
import com.virtualworld.easymusic.domain.model.Artist
import com.virtualworld.easymusic.ui.components.ArtistItem
import com.virtualworld.easymusic.ui.theme.DarkSurfaceVariant
import com.virtualworld.easymusic.ui.theme.TextGray

@Composable
fun ArtistsTab(
    artists: List<Artist>,
    onArtistClick: (Artist) -> Unit
) {
    if (artists.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.no_artists_found),
                style = MaterialTheme.typography.bodyLarge,
                color = TextGray
            )
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(artists, key = { it.id }) { artist ->
                ArtistItem(
                    artist = artist,
                    onClick = { onArtistClick(artist) }
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
