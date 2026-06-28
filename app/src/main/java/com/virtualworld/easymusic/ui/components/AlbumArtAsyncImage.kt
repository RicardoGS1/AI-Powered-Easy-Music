package com.virtualworld.easymusic.ui.components

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
    onEditMetadataClick: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val placeholder = R.drawable.ic_album_art_placeholder
    Box(modifier = modifier) {
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
            modifier = Modifier.fillMaxSize(),
            contentScale = contentScale,
        )
        onEditMetadataClick?.let { onClick ->
            IconButton(
                onClick = onClick,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(6.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Black.copy(alpha = 0.45f),
                    contentColor = Color.White,
                ),
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.cd_edit_metadata),
                    tint = Color.White,
                )
            }
        }
    }
}
