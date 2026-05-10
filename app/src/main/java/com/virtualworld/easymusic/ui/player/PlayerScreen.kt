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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import coil3.compose.AsyncImage
import com.virtualworld.easymusic.domain.model.LyricsLine
import com.virtualworld.easymusic.domain.model.LyricsResult
import com.virtualworld.easymusic.domain.model.LyricsSearchCandidate
import com.virtualworld.easymusic.domain.model.Song
import com.virtualworld.easymusic.playback.PlayerState
import com.virtualworld.easymusic.ui.components.formatDuration
import com.virtualworld.easymusic.ui.theme.DarkBackground
import com.virtualworld.easymusic.ui.theme.DarkSurface
import com.virtualworld.easymusic.ui.theme.DarkSurfaceVariant
import com.virtualworld.easymusic.ui.theme.EasyMusicTheme
import com.virtualworld.easymusic.ui.theme.Teal400
import com.virtualworld.easymusic.ui.theme.TextGray
import com.virtualworld.easymusic.ui.theme.TextWhite
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    onNavigateToLibrary: () -> Unit,
    onNavigateToLibrarySearch: () -> Unit,
    onNavigateToEqualizer: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PlayerContent(
        uiState = uiState,
        onNavigateToLibrary = onNavigateToLibrary,
        onNavigateToLibrarySearch = onNavigateToLibrarySearch,
        onNavigateToEqualizer = onNavigateToEqualizer,
        onToggleRepeatMode = { viewModel.toggleRepeatMode() },
        onToggleShuffle = { viewModel.toggleShuffle() },
        onSeekTo = { viewModel.seekTo(it) },
        onPrevious = { viewModel.previous() },
        onTogglePlayPause = { viewModel.togglePlayPause() },
        onNext = { viewModel.next() },
        onExcludeCurrentSong = { viewModel.excludeCurrentSongFromLibrary() },
        onToggleLyricsSheet = { viewModel.toggleLyricsSheet() },
        onDismissLyricsSheet = { viewModel.dismissLyricsSheet() },
        onPickLyricsCandidate = { viewModel.loadLyricsForLrcLibId(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerContent(
    uiState: PlayerUiState,
    onNavigateToLibrary: () -> Unit,
    onNavigateToLibrarySearch: () -> Unit,
    onNavigateToEqualizer: () -> Unit,
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
    modifier: Modifier = Modifier
) {
    val song = uiState.playerState.currentSong
    var extraControlsExpanded by remember { mutableStateOf(false) }
    var volumeBarExpanded by remember { mutableStateOf(false) }
    var volumeFraction by remember { mutableFloatStateOf(0f) }
    val context = LocalContext.current
    val audioManager = remember(context) {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

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
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = TextWhite
                    )
                }
                Text(
                    text = "NOW PLAYING",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = TextWhite
                )
                IconButton(onClick = onNavigateToLibrarySearch) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = TextWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Album art
            if (song != null) {
                AsyncImage(
                    model = song.albumArtUri,
                    contentDescription = song.album,
                    modifier = Modifier
                        .fillMaxWidth(0.78f)
                        .aspectRatio(1f)
                        .shadow(24.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
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
                text = song?.title ?: "Sin cancion",
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
                text = song?.artist ?: "Selecciona una cancion",
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
                            contentDescription = "Volumen",
                            tint = if (volumeBarExpanded) Teal400 else TextGray
                        )
                    }
                    IconButton(onClick = onToggleRepeatMode) {
                        Icon(
                            imageVector = when (uiState.playerState.repeatMode) {
                                Player.REPEAT_MODE_ONE -> Icons.Default.RepeatOne
                                else -> Icons.Default.Repeat
                            },
                            contentDescription = "Repetir",
                            tint = if (uiState.playerState.repeatMode != Player.REPEAT_MODE_OFF)
                                Teal400 else TextGray
                        )
                    }
                    IconButton(onClick = onToggleShuffle) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Aleatorio",
                            tint = if (uiState.playerState.shuffleEnabled)
                                Teal400 else TextGray
                        )
                    }
                    IconButton(onClick = onNavigateToLibrary) {
                        Icon(
                            imageVector = Icons.Filled.Folder,
                            contentDescription = "Biblioteca",
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
                            "Ocultar controles extra"
                        } else {
                            "Mostrar controles extra"
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
                            contentDescription = "Ecualizador",
                            tint = TextGray
                        )
                    }
                    IconButton(
                        onClick = onExcludeCurrentSong,
                        enabled = song != null
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Quitar de la cola y de la biblioteca",
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
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = "IA",
                            tint = TextGray
                        )
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
                        contentDescription = "Anterior",
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
                        contentDescription = if (uiState.playerState.isPlaying) "Pausar" else "Reproducir",
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
                        contentDescription = "Siguiente",
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
            text = "Letra",
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
                                text = "No se encontró una letra con coincidencia exacta (título, artista, álbum y duración). Buscando otras coincidencias en LRCLIB…",
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
                    text = "Esta pista figura como instrumental en LRCLIB.",
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
                    text = "No se encontró letra para esta canción. Comprueba título, artista, álbum y duración en los metadatos.",
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
            text = "Varias coincidencias en LRCLIB. Elige la pista correcta:",
            style = MaterialTheme.typography.bodyMedium,
            color = TextGray,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )
        if (candidates.isEmpty()) {
            Text(
                text = "No hay opciones para mostrar.",
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
                            text = candidate.trackName.ifBlank { "Sin título" },
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
            onToggleRepeatMode = {},
            onToggleShuffle = {},
            onSeekTo = {},
            onPrevious = {},
            onTogglePlayPause = {},
            onNext = {},
            onExcludeCurrentSong = {},
            onToggleLyricsSheet = {},
            onDismissLyricsSheet = {},
            onPickLyricsCandidate = {}
        )
    }
}
