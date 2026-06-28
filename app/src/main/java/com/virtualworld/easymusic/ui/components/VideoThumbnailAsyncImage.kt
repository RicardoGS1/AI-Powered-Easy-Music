package com.virtualworld.easymusic.ui.components

import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.virtualworld.easymusic.R
import com.virtualworld.easymusic.ui.theme.DarkSurfaceVariant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun VideoThumbnailAsyncImage(
    videoId: Long,
    videoUri: Uri,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val context = LocalContext.current
    var thumbnail by remember(videoId) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(videoId, videoUri) {
        thumbnail = withContext(Dispatchers.IO) {
            loadVideoThumbnail(context.contentResolver, videoId, videoUri)
        }
    }

    if (thumbnail != null) {
        Image(
            bitmap = thumbnail!!.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
        )
    } else {
        Image(
            painter = painterResource(R.drawable.ic_album_art_placeholder),
            contentDescription = contentDescription,
            modifier = modifier.background(DarkSurfaceVariant),
            contentScale = contentScale,
        )
    }
}

private fun loadVideoThumbnail(
    contentResolver: ContentResolver,
    videoId: Long,
    videoUri: Uri,
): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentResolver.loadThumbnail(videoUri, Size(512, 288), null)
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Video.Thumbnails.getThumbnail(
                contentResolver,
                videoId,
                MediaStore.Video.Thumbnails.MINI_KIND,
                null,
            )
        }
    } catch (_: Exception) {
        try {
            @Suppress("DEPRECATION")
            MediaStore.Video.Thumbnails.getThumbnail(
                contentResolver,
                videoId,
                MediaStore.Video.Thumbnails.MICRO_KIND,
                null,
            )
        } catch (_: Exception) {
            null
        }
    }
}
