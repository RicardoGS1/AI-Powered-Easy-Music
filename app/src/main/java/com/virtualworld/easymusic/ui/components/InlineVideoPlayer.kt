package com.virtualworld.easymusic.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.ContentFrame
import androidx.media3.ui.compose.SURFACE_TYPE_SURFACE_VIEW
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW

@UnstableApi
@Composable
fun InlineVideoPlayer(
    player: Player?,
    modifier: Modifier = Modifier,
    useTextureView: Boolean = false,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val videoScalingMode = when (contentScale) {
        ContentScale.Crop,
        ContentScale.FillBounds,
        ContentScale.FillHeight,
        ContentScale.FillWidth -> C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
        else -> C.VIDEO_SCALING_MODE_SCALE_TO_FIT
    }

    DisposableEffect(player, videoScalingMode) {
        (player as? ExoPlayer)?.videoScalingMode = videoScalingMode
        onDispose { }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        ContentFrame(
            player = player,
            modifier = Modifier.fillMaxSize(),
            surfaceType = if (useTextureView) {
                SURFACE_TYPE_TEXTURE_VIEW
            } else {
                SURFACE_TYPE_SURFACE_VIEW
            },
            contentScale = contentScale,
        )
    }
}
