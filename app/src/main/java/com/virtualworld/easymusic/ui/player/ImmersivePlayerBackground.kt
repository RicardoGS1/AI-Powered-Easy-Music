package com.virtualworld.easymusic.ui.player

import android.net.Uri
import android.os.Build
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.virtualworld.easymusic.ui.components.AlbumArtAsyncImage
import com.virtualworld.easymusic.ui.theme.DarkBackground
import com.virtualworld.easymusic.ui.theme.DarkSurface
import com.virtualworld.easymusic.ui.theme.DarkSurfaceVariant
import com.virtualworld.easymusic.ui.theme.EasyMusicTheme
import com.virtualworld.easymusic.ui.theme.Teal700

@Composable
fun ImmersivePlayerBackground(
    albumArtUri: Uri?,
    isActive: Boolean,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "immersive_bg")
    val breathe by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5_500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe_scale"
    )
    val drift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift"
    )
    val scale = if (isPlaying) breathe else 1f
    val offsetX = if (isActive) (drift - 0.5f) * 24f else 0f
    val offsetY = if (isActive) (0.5f - drift) * 18f else 0f

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkSurfaceVariant, DarkSurface, DarkBackground)
                )
            )
    ) {
        if (isActive && albumArtUri != null) {
            Crossfade(
                targetState = albumArtUri,
                modifier = Modifier.fillMaxSize(),
                label = "album_art_bg"
            ) { uri ->
                AlbumArtAsyncImage(
                    albumArtUri = uri,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(scale * 1.25f)
                        .graphicsLayer {
                            translationX = offsetX
                            translationY = offsetY
                            alpha = 0.9f
                        }
                        .then(
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                Modifier.blur(72.dp)
                            } else {
                                Modifier
                            }
                        ),
                    contentScale = ContentScale.Crop
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                DarkBackground.copy(alpha = 0.55f),
                                Color.Transparent,
                                DarkBackground.copy(alpha = 0.45f),
                                DarkBackground.copy(alpha = 0.88f)
                            )
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.Transparent,
                                Teal700.copy(alpha = 0.12f),
                                DarkBackground.copy(alpha = 0.65f)
                            ),
                            radius = 1_100f
                        )
                    )
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1B2A)
@Composable
private fun ImmersivePlayerBackgroundPreview() {
    EasyMusicTheme {
        ImmersivePlayerBackground(
            albumArtUri = Uri.EMPTY,
            isActive = true,
            isPlaying = true,
            modifier = Modifier.fillMaxSize()
        )
    }
}
