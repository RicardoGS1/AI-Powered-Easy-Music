package com.virtualworld.easymusic.ui.components

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import coil3.asImage
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.virtualworld.easymusic.R

@Composable
fun AlbumArtAsyncImage(
    albumArtUri: Uri?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val context = LocalContext.current
    val placeholder = R.drawable.ic_album_art_placeholder
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(albumArtUri)
            .placeholder {
                ContextCompat.getDrawable(it.context, placeholder)?.asImage()
            }
            .error {
                ContextCompat.getDrawable(it.context, placeholder)?.asImage()
            }
            .fallback {
                ContextCompat.getDrawable(it.context, placeholder)?.asImage()
            }
            .build(),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
    )
}
