package com.virtualworld.easymusic.ui.player

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.virtualworld.easymusic.R
import com.virtualworld.easymusic.ui.components.InlineVideoPlayer
import com.virtualworld.easymusic.ui.components.formatDuration
import com.virtualworld.easymusic.ui.theme.DarkSurfaceVariant
import com.virtualworld.easymusic.ui.theme.Teal400
import com.virtualworld.easymusic.ui.theme.TextGray
import com.virtualworld.easymusic.ui.theme.TextWhite
import kotlinx.coroutines.delay

private const val CONTROLS_AUTO_HIDE_MS = 4_000L

@OptIn(UnstableApi::class)
@Composable
fun VideoFullscreenOverlay(
    player: Player?,
    videoTitle: String,
    position: Long,
    duration: Long,
    isPlaying: Boolean,
    onExitFullscreen: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeekTo: (Long) -> Unit,
    swipeEnabled: Boolean = false,
    hasPrevious: Boolean = false,
    hasNext: Boolean = false,
    previousPreview: (@Composable () -> Unit)? = null,
    nextPreview: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    var controlsVisible by remember { mutableStateOf(true) }
    var hideToken by remember { mutableIntStateOf(0) }

    fun revealControls() {
        controlsVisible = true
        hideToken++
    }

    LaunchedEffect(controlsVisible, isPlaying, hideToken) {
        if (controlsVisible && isPlaying) {
            delay(CONTROLS_AUTO_HIDE_MS)
            controlsVisible = false
        }
    }

    BackHandler(onBack = onExitFullscreen)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        key("fullscreen-player") {
            PlayerSwipeToSkip(
                enabled = swipeEnabled,
                hasPrevious = hasPrevious,
                hasNext = hasNext,
                onSwipeToPrevious = onPrevious,
                onSwipeToNext = onNext,
                modifier = Modifier.fillMaxSize(),
                previousContent = previousPreview,
                nextContent = nextPreview,
            ) {
                InlineVideoPlayer(
                    player = player,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                    useTextureView = true,
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = { controlsVisible = !controlsVisible },
                ),
        )

        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.72f),
                                    Color.Transparent,
                                ),
                            ),
                        )
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Spacer(modifier = Modifier.size(48.dp))
                        Text(
                            text = videoTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                        )
                        IconButton(
                            onClick = {
                                revealControls()
                                onExitFullscreen()
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FullscreenExit,
                                contentDescription = stringResource(R.string.cd_exit_fullscreen),
                                tint = TextWhite,
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.78f),
                                ),
                            ),
                        )
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = formatDuration(position),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextGray,
                        )
                        Text(
                            text = formatDuration(duration),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextGray,
                        )
                    }

                    Slider(
                        value = if (duration > 0) position.toFloat() / duration.toFloat() else 0f,
                        onValueChange = { fraction ->
                            revealControls()
                            onSeekTo((fraction * duration).toLong())
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = Teal400,
                            activeTrackColor = Teal400,
                            inactiveTrackColor = DarkSurfaceVariant,
                        ),
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(
                            onClick = {
                                revealControls()
                                onPrevious()
                            },
                            modifier = Modifier.size(52.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SkipPrevious,
                                contentDescription = stringResource(R.string.cd_previous),
                                modifier = Modifier.size(36.dp),
                                tint = TextWhite,
                            )
                        }

                        Surface(
                            modifier = Modifier.size(68.dp),
                            shape = CircleShape,
                            color = Teal400,
                            shadowElevation = 8.dp,
                            onClick = {
                                revealControls()
                                onTogglePlayPause()
                            },
                        ) {
                            Icon(
                                imageVector = if (isPlaying) {
                                    Icons.Filled.Pause
                                } else {
                                    Icons.Filled.PlayArrow
                                },
                                contentDescription = if (isPlaying) {
                                    stringResource(R.string.cd_pause)
                                } else {
                                    stringResource(R.string.cd_play)
                                },
                                modifier = Modifier.padding(16.dp),
                                tint = Color.White,
                            )
                        }

                        IconButton(
                            onClick = {
                                revealControls()
                                onNext()
                            },
                            modifier = Modifier.size(52.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SkipNext,
                                contentDescription = stringResource(R.string.cd_next),
                                modifier = Modifier.size(36.dp),
                                tint = TextWhite,
                            )
                        }
                    }
                }
            }
        }
    }
}
