package com.virtualworld.easymusic.ui.player

import android.content.Context
import android.media.AudioManager
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import com.virtualworld.easymusic.ui.components.AlbumArtAsyncImage
import com.virtualworld.easymusic.domain.model.LyricsLine
import com.virtualworld.easymusic.domain.model.LyricsResult
import com.virtualworld.easymusic.domain.model.LyricsSearchCandidate
import com.virtualworld.easymusic.domain.model.Song
import com.virtualworld.easymusic.domain.model.SongInsightResult
import com.virtualworld.easymusic.playback.PlayerState
import com.virtualworld.easymusic.ui.components.formatDuration
import com.virtualworld.easymusic.ui.theme.DarkBackground
import com.virtualworld.easymusic.ui.theme.DarkSurface
import com.virtualworld.easymusic.ui.theme.DarkSurfaceVariant
import com.virtualworld.easymusic.ui.theme.EasyMusicTheme
import com.virtualworld.easymusic.ui.theme.Teal400
import com.virtualworld.easymusic.ui.theme.TextGray
import com.virtualworld.easymusic.ui.theme.TextWhite
import com.virtualworld.easymusic.ui.util.openAppPlayStoreListing
import com.virtualworld.easymusic.ui.util.shareAppPlayStoreLink
import com.virtualworld.easymusic.R
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    onNavigateToLibrary: () -> Unit,
    onNavigateToLibrarySearch: () -> Unit,
    onNavigateToEqualizer: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.refreshAiInsightRemoteFlag()
    }

    PlayerContent(
        uiState = uiState,
        onNavigateToLibrary = onNavigateToLibrary,
        onNavigateToLibrarySearch = onNavigateToLibrarySearch,
        onNavigateToEqualizer = onNavigateToEqualizer,
        onNavigateToSettings = onNavigateToSettings,
        onToggleRepeatMode = { viewModel.toggleRepeatMode() },
        onToggleShuffle = { viewModel.toggleShuffle() },
        onSeekTo = { viewModel.seekTo(it) },
        onPrevious = { viewModel.previous() },
        onTogglePlayPause = { viewModel.togglePlayPause() },
        onNext = { viewModel.next() },
        onExcludeCurrentSong = { viewModel.excludeCurrentSongFromLibrary() },
        onToggleLyricsSheet = { viewModel.toggleLyricsSheet() },
        onDismissLyricsSheet = { viewModel.dismissLyricsSheet() },
        onPickLyricsCandidate = { viewModel.loadLyricsForLrcLibId(it) },
        onToggleInsightSheet = { viewModel.toggleInsightSheet() },
        onDismissInsightSheet = { viewModel.dismissInsightSheet() },
        onToggleFavoriteCurrentSong = { viewModel.toggleFavoriteCurrentSong() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerContent(
    uiState: PlayerUiState,
    onNavigateToLibrary: () -> Unit,
    onNavigateToLibrarySearch: () -> Unit,
    onNavigateToEqualizer: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onToggleRepeatMode: () -> Unit,
    onToggleShuffle: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onPrevious: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onExcludeCurrentSong: () -> Unit,
    onToggleLyricsSheet: () -> Unit,
    onDismissLyricsSheet: () -> Unit,
    onPickLyricsCandidate: (Long) -> Unit,
    onToggleInsightSheet: () -> Unit,
    onDismissInsightSheet: () -> Unit,
    onToggleFavoriteCurrentSong: () -> Unit,
    modifier: Modifier = Modifier
) {
    val song = uiState.playerState.currentSong
    val isCurrentFavorite = song != null && song.id in uiState.favoriteSongIds
    var extraControlsExpanded by remember { mutableStateOf(false) }
    var volumeBarExpanded by remember { mutableStateOf(false) }
    var volumeFraction by remember { mutableFloatStateOf(0f) }
    val context = LocalContext.current
    val audioManager = remember(context) {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .fillMaxHeight()
                    .wrapContentWidth(align = Alignment.Start),
                drawerContainerColor = DarkSurface
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    DrawerMenuAction(
                        icon = Icons.Default.Share,
                        label = stringResource(R.string.drawer_share_app),
                        onClick = {
                            shareAppPlayStoreLink(context)
                            scope.launch { drawerState.close() }
                        }
                    )
                    DrawerMenuAction(
                        icon = Icons.Default.StarRate,
                        label = stringResource(R.string.drawer_rate_app),
                        onClick = {
                            openAppPlayStoreListing(context)
                            scope.launch { drawerState.close() }
                        }
                    )
                    DrawerMenuAction(
                        icon = Icons.Default.Settings,
                        label = stringResource(R.string.drawer_settings),
                        onClick = {
                            onNavigateToSettings()
                            scope.launch { drawerState.close() }
                        }
                    )
                }
            }
        }
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            DarkSurfaceVariant,
                            DarkSurface,
                            DarkBackground
                        )
                    )
                )
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = stringResource(R.string.cd_menu),
                        tint = TextWhite
                    )
                }
                Text(
                    text = stringResource(R.string.now_playing),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = TextWhite
                )
                IconButton(onClick = onNavigateToLibrarySearch) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(R.string.cd_search),
                        tint = TextWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Album art (favorito superpuesto arriba a la derecha)
            if (song != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.78f)
                        .aspectRatio(1f)
                        .shadow(24.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    AlbumArtAsyncImage(
                        albumArtUri = song.albumArtUri,
                        contentDescription = song.album,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = onToggleFavoriteCurrentSong,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.Black.copy(alpha = 0.45f),
                            contentColor = if (isCurrentFavorite) Teal400 else Color.White
                        )
                    ) {
                        Icon(
                            imageVector = if (isCurrentFavorite) {
                                Icons.Filled.Favorite
                            } else {
                                Icons.Outlined.FavoriteBorder
                            },
                            contentDescription = if (isCurrentFavorite) {
                                stringResource(R.string.cd_remove_favorite)
                            } else {
                                stringResource(R.string.cd_add_favorite)
                            }
                        )
                    }
                }
            } else {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.78f)
                        .aspectRatio(1f)
                        .shadow(24.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    color = DarkSurfaceVariant
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.padding(64.dp),
                        tint = Teal400
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Song info
            Text(
                text = song?.title ?: stringResource(R.string.no_song),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = song?.artist ?: stringResource(R.string.select_a_song),
                style = MaterialTheme.typography.bodyLarge,
                color = TextGray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            // Secondary controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            volumeBarExpanded = !volumeBarExpanded
                            if (volumeBarExpanded) {
                                val max =
                                    audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                                        .coerceAtLeast(1)
                                val cur = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                                volumeFraction = cur.toFloat() / max.toFloat()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = stringResource(R.string.cd_volume),
                            tint = if (volumeBarExpanded) Teal400 else TextGray
                        )
                    }
                    IconButton(onClick = onToggleRepeatMode) {
                        Icon(
                            imageVector = when (uiState.playerState.repeatMode) {
                                Player.REPEAT_MODE_ONE -> Icons.Default.RepeatOne
                                else -> Icons.Default.Repeat
                            },
                            contentDescription = stringResource(R.string.cd_repeat),
                            tint = if (uiState.playerState.repeatMode != Player.REPEAT_MODE_OFF)
                                Teal400 else TextGray
                        )
                    }
                    IconButton(onClick = onToggleShuffle) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = stringResource(R.string.cd_shuffle),
                            tint = if (uiState.playerState.shuffleEnabled)
                                Teal400 else TextGray
                        )
                    }
                    IconButton(onClick = onNavigateToLibrary) {
                        Icon(
                            imageVector = Icons.Filled.Folder,
                            contentDescription = stringResource(R.string.cd_library),
                            tint = TextGray
                        )
                    }
                }
                IconButton(onClick = { extraControlsExpanded = !extraControlsExpanded }) {
                    Icon(
                        imageVector = if (extraControlsExpanded) {
                            Icons.Filled.ExpandMore
                        } else {
                            Icons.Filled.ExpandLess
                        },
                        contentDescription = if (extraControlsExpanded) {
                            stringResource(R.string.cd_hide_extra_controls)
                        } else {
                            stringResource(R.string.cd_show_extra_controls)
                        },
                        tint = if (extraControlsExpanded) Teal400 else TextGray
                    )
                }
            }

            AnimatedVisibility(
                visible = volumeBarExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Slider(
                        value = volumeFraction,
                        onValueChange = { v ->
                            volumeFraction = v
                            val max = audioManager
                                .getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                                .coerceAtLeast(1)
                            val index = (v * max).roundToInt().coerceIn(0, max)
                            audioManager.setStreamVolume(
                                AudioManager.STREAM_MUSIC,
                                index,
                                0
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = Teal400,
                            activeTrackColor = Teal400,
                            inactiveTrackColor = DarkSurfaceVariant
                        )
                    )
                }
            }

            AnimatedVisibility(
                visible = extraControlsExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateToEqualizer) {
                        Icon(
                            imageVector = Icons.Filled.Equalizer,
                            contentDescription = stringResource(R.string.cd_equalizer),
                            tint = TextGray
                        )
                    }
                    IconButton(
                        onClick = onExcludeCurrentSong,
                        enabled = song != null
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.cd_remove_from_queue),
                            tint = if (song != null) TextGray else TextGray.copy(alpha = 0.4f)
                        )
                    }
                    IconButton(onClick = onToggleLyricsSheet) {
                        Icon(
                            imageVector = Icons.Filled.Subtitles,
                            contentDescription = "Letra (LRCLIB)",
                            tint = if (uiState.lyricsSheetVisible) Teal400 else TextGray
                        )
                    }
                    if (uiState.aiInsightEnabled) {
                        IconButton(
                            onClick = onToggleInsightSheet,
                            enabled = song != null
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = "IA (Gemini)",
                                tint = when {
                                    song == null -> TextGray.copy(alpha = 0.4f)
                                    uiState.insightSheetVisible -> Teal400
                                    else -> TextGray
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress bar
            val duration = uiState.playerState.duration
            val position = uiState.currentPosition

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatDuration(position),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray
                )
                Text(
                    text = formatDuration(duration),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray
                )
            }

            Slider(
                value = if (duration > 0) position.toFloat() / duration.toFloat() else 0f,
                onValueChange = { fraction ->
                    onSeekTo((fraction * duration).toLong())
                },
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = Teal400,
                    activeTrackColor = Teal400,
                    inactiveTrackColor = DarkSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Main controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPrevious,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = stringResource(R.string.cd_previous),
                        modifier = Modifier.size(36.dp),
                        tint = TextWhite
                    )
                }

                Surface(
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    color = Teal400,
                    shadowElevation = 8.dp,
                    onClick = onTogglePlayPause
                ) {
                    Icon(
                        imageVector = if (uiState.playerState.isPlaying)
                            Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (uiState.playerState.isPlaying) stringResource(R.string.cd_pause) else stringResource(R.string.cd_play),
                        modifier = Modifier.padding(18.dp),
                        tint = Color.White
                    )
                }

                IconButton(
                    onClick = onNext,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = stringResource(R.string.cd_next),
                        modifier = Modifier.size(36.dp),
                        tint = TextWhite
                    )
                }
            }
        }

        if (uiState.lyricsSheetVisible) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = onDismissLyricsSheet,
                sheetState = sheetState,
                containerColor = DarkSurface,
                contentColor = TextWhite
            ) {
                LyricsSheetContent(
                    loading = uiState.lyricsLoading,
                    searchingAlternatives = uiState.lyricsSearchingAlternatives,
                    result = uiState.lyricsResult,
                    positionMs = uiState.currentPosition,
                    onPickLyricsCandidate = onPickLyricsCandidate,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                )
            }
        }

        if (uiState.insightSheetVisible) {
            val insightSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = onDismissInsightSheet,
                sheetState = insightSheetState,
                containerColor = DarkSurface,
                contentColor = TextWhite
            ) {
                SongInsightSheetContent(
                    loading = uiState.insightLoading,
                    result = uiState.insightResult,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                )
            }
        }
        }
    }
}

@Composable
private fun DrawerMenuAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextGray
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = TextWhite
        )
    }
}

@Composable
private fun SongInsightSheetContent(
    loading: Boolean,
    result: SongInsightResult?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(
            text = stringResource(R.string.song_data),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Teal400,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.gemini_local_metadata),
            style = MaterialTheme.typography.labelSmall,
            color = TextGray,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp, bottom = 12.dp),
            textAlign = TextAlign.Center
        )

        when {
            loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Teal400)
                }
            }
            result is SongInsightResult.Error -> {
                Text(
                    text = result.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextWhite,
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 16.dp)
                )
            }
            result is SongInsightResult.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 480.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    InsightFieldBlock(label = stringResource(R.string.insight_summary), text = result.insight.resumen)
                    InsightFieldBlock(label = stringResource(R.string.insight_genre), text = result.insight.generoOEstilo)
                    InsightFieldBlock(label = stringResource(R.string.insight_era), text = result.insight.epocaOContexto)
                    InsightFieldBlock(label = stringResource(R.string.insight_fun_fact), text = result.insight.datoCurioso)
                    val similares = result.insight.artistasOTemasSimilares.orEmpty()
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                    if (similares.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.insight_similar_artists),
                            style = MaterialTheme.typography.titleSmall,
                            color = Teal400,
                            fontWeight = FontWeight.SemiBold
                        )
                        similares.forEach { line ->
                            Text(
                                text = "· $line",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextGray
                            )
                        }
                    }
                }
            }
            else -> {
                Text(
                    text = stringResource(R.string.no_data),
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextGray
                )
            }
        }
    }
}

@Composable
private fun InsightFieldBlock(label: String, text: String?) {
    val value = text?.trim().orEmpty()
    if (value.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = Teal400,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = TextWhite
        )
    }
}

@Composable
private fun LyricsSheetContent(
    loading: Boolean,
    searchingAlternatives: Boolean,
    result: LyricsResult?,
    positionMs: Long,
    onPickLyricsCandidate: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(
            text = stringResource(R.string.lyrics),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Teal400,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Text(
            text = "LRCLIB",
            style = MaterialTheme.typography.labelSmall,
            color = TextGray,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp, bottom = 12.dp),
            textAlign = TextAlign.Center
        )

        when {
            loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp)
                    ) {
                        if (searchingAlternatives) {
                            Text(
                                text = stringResource(R.string.lyrics_no_exact_match),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextGray,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                        CircularProgressIndicator(color = Teal400)
                    }
                }
            }

            result is LyricsResult.MultipleCandidates -> {
                LyricsCandidatePicker(
                    candidates = result.candidates,
                    onPick = onPickLyricsCandidate
                )
            }

            result is LyricsResult.Synced -> {
                SyncedLyricsList(
                    lines = result.lines,
                    positionMs = positionMs
                )
            }

            result is LyricsResult.PlainOnly -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 480.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = result.text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextWhite,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            result is LyricsResult.Instrumental -> {
                Text(
                    text = stringResource(R.string.lyrics_instrumental),
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                )
            }

            result is LyricsResult.NotFound -> {
                Text(
                    text = stringResource(R.string.lyrics_not_found),
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                )
            }

            result is LyricsResult.Failure -> {
                Text(
                    text = result.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                )
            }

            else -> {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun LyricsCandidatePicker(
    candidates: List<LyricsSearchCandidate>,
    onPick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.lyrics_multiple_matches),
            style = MaterialTheme.typography.bodyMedium,
            color = TextGray,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )
        if (candidates.isEmpty()) {
            Text(
                text = stringResource(R.string.lyrics_no_options),
                style = MaterialTheme.typography.bodyLarge,
                color = TextGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            return
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 480.dp)
        ) {
            items(
                items = candidates,
                key = { it.id }
            ) { candidate ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onPick(candidate.id) },
                    color = DarkSurfaceVariant,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                        Text(
                            text = candidate.trackName.ifBlank { stringResource(R.string.no_title) },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = TextWhite,
                            maxLines = 2
                        )
                        Text(
                            text = listOf(candidate.artistName, candidate.albumName)
                                .filter { it.isNotBlank() }
                                .joinToString(" · "),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextGray,
                            maxLines = 2,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Text(
                            text = formatDuration(candidate.durationSeconds * 1000L),
                            style = MaterialTheme.typography.labelMedium,
                            color = Teal400,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SyncedLyricsList(
    lines: List<LyricsLine>,
    positionMs: Long
) {
    val listState = rememberLazyListState()
    val activeIndex = remember(lines, positionMs) { lyricsLineIndexAt(lines, positionMs) }
    LaunchedEffect(activeIndex) {
        if (activeIndex >= 0 && lines.isNotEmpty()) {
            listState.animateScrollToItem(activeIndex.coerceIn(0, lines.lastIndex))
        }
    }
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 480.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        itemsIndexed(
            items = lines,
            key = { index, line -> "${line.timeMs}_$index" }
        ) { index, line ->
            val highlight = index == activeIndex
            Text(
                text = line.text,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                textAlign = TextAlign.Center,
                color = if (highlight) Teal400 else TextGray,
                fontWeight = if (highlight) FontWeight.SemiBold else FontWeight.Normal,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

private fun lyricsLineIndexAt(lines: List<LyricsLine>, positionMs: Long): Int {
    if (lines.isEmpty()) return -1
    var idx = -1
    for (i in lines.indices) {
        if (lines[i].timeMs <= positionMs) idx = i else break
    }
    return idx
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun PlayerScreenPreview() {
    EasyMusicTheme {
        PlayerContent(
            uiState = PlayerUiState(
                playerState = PlayerState(
                    isPlaying = true,
                    currentSong = Song(
                        id = 1L,
                        title = "Sample Song",
                        artist = "Sample Artist",
                        album = "Sample Album",
                        albumId = 1L,
                        duration = 180000L,
                        uri = Uri.EMPTY,
                        albumArtUri = null
                    ),
                    duration = 180000L,
                    shuffleEnabled = true,
                    repeatMode = Player.REPEAT_MODE_ONE
                ),
                currentPosition = 60000L,
                isLoading = false
            ),
            onNavigateToLibrary = {},
            onNavigateToLibrarySearch = {},
            onNavigateToEqualizer = {},
            onNavigateToSettings = {},
            onToggleRepeatMode = {},
            onToggleShuffle = {},
            onSeekTo = {},
            onPrevious = {},
            onTogglePlayPause = {},
            onNext = {},
            onExcludeCurrentSong = {},
            onToggleLyricsSheet = {},
            onDismissLyricsSheet = {},
            onPickLyricsCandidate = {},
            onToggleInsightSheet = {},
            onDismissInsightSheet = {},
            onToggleFavoriteCurrentSong = {}
        )
    }
}
