package com.virtualworld.easymusic.ui.player

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PlaylistRemove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Fullscreen
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
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextButton
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
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
import androidx.compose.runtime.key
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.virtualworld.easymusic.ui.components.AlbumArtAsyncImage
import com.virtualworld.easymusic.ui.components.EditSongMetadataDialog
import com.virtualworld.easymusic.ui.components.EditVideoTitleDialog
import com.virtualworld.easymusic.ui.components.InlineVideoPlayer
import com.virtualworld.easymusic.ui.components.VideoThumbnailAsyncImage
import com.virtualworld.easymusic.domain.model.LyricsLine
import com.virtualworld.easymusic.domain.model.LyricsResult
import com.virtualworld.easymusic.domain.model.LyricsSearchCandidate
import com.virtualworld.easymusic.domain.model.Song
import com.virtualworld.easymusic.domain.model.SongInsightResult
import com.virtualworld.easymusic.domain.model.Video
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
    onNavigateToVideoLibrary: () -> Unit,
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
        videoPlayer = uiState.videoPlayer,
        onNavigateToLibrary = onNavigateToLibrary,
        onNavigateToLibrarySearch = onNavigateToLibrarySearch,
        onNavigateToVideoLibrary = onNavigateToVideoLibrary,
        onNavigateToEqualizer = onNavigateToEqualizer,
        onNavigateToSettings = onNavigateToSettings,
        onToggleRepeatMode = { viewModel.toggleRepeatMode() },
        onToggleShuffle = { viewModel.toggleShuffle() },
        onSeekTo = { viewModel.seekTo(it) },
        onPrevious = { viewModel.previous() },
        onTogglePlayPause = { viewModel.togglePlayPause() },
        onNext = { viewModel.next() },
        skipRemoveFromQueueConfirmation = uiState.skipRemoveFromQueueConfirmation,
        onExcludeCurrentSong = { viewModel.excludeCurrentSongFromLibrary() },
        onSetSkipRemoveFromQueueConfirmation = { viewModel.setSkipRemoveFromQueueConfirmation(it) },
        onToggleLyricsSheet = { viewModel.toggleLyricsSheet() },
        onDismissLyricsSheet = { viewModel.dismissLyricsSheet() },
        onPickLyricsCandidate = { viewModel.loadLyricsForLrcLibId(it) },
        onToggleInsightSheet = { viewModel.toggleInsightSheet() },
        onDismissInsightSheet = { viewModel.dismissInsightSheet() },
        onToggleFavoriteCurrentSong = { viewModel.toggleFavoriteCurrentSong() },
        onToggleFavoriteCurrentVideo = { viewModel.toggleFavoriteCurrentVideo() },
        onSetVideoFullscreen = { viewModel.setVideoFullscreen(it) },
        onOpenMetadataEditor = { viewModel.openMetadataEditor() },
        onDismissMetadataEditor = { viewModel.dismissMetadataEditor() },
        onMetadataTitleChange = { viewModel.updateMetadataEditorTitle(it) },
        onMetadataArtistChange = { viewModel.updateMetadataEditorArtist(it) },
        onMetadataAlbumChange = { viewModel.updateMetadataEditorAlbum(it) },
        onFetchMetadataFromAi = { viewModel.fetchMetadataFromAi() },
        onSaveMetadata = { viewModel.saveMetadataEdits() },
        onClearMetadataWritePermissionRequest = { viewModel.clearMetadataWritePermissionRequest() },
        onMetadataWritePermissionResult = { viewModel.onMetadataWritePermissionResult(it) },
        onOpenVideoTitleEditor = { viewModel.openVideoTitleEditor() },
        onDismissVideoTitleEditor = { viewModel.dismissVideoTitleEditor() },
        onVideoTitleChange = { viewModel.updateVideoTitleEditorTitle(it) },
        onSaveVideoTitle = { viewModel.saveVideoTitleEdits() },
        onClearVideoTitleWritePermissionRequest = { viewModel.clearVideoTitleWritePermissionRequest() },
        onVideoTitleWritePermissionResult = { viewModel.onVideoTitleWritePermissionResult(it) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerContent(
    uiState: PlayerUiState,
    videoPlayer: Player?,
    onNavigateToLibrary: () -> Unit,
    onNavigateToLibrarySearch: () -> Unit,
    onNavigateToVideoLibrary: () -> Unit,
    onNavigateToEqualizer: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onToggleRepeatMode: () -> Unit,
    onToggleShuffle: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onPrevious: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    skipRemoveFromQueueConfirmation: Boolean = false,
    onExcludeCurrentSong: () -> Unit,
    onSetSkipRemoveFromQueueConfirmation: (Boolean) -> Unit,
    onToggleLyricsSheet: () -> Unit,
    onDismissLyricsSheet: () -> Unit,
    onPickLyricsCandidate: (Long) -> Unit,
    onToggleInsightSheet: () -> Unit,
    onDismissInsightSheet: () -> Unit,
    onToggleFavoriteCurrentSong: () -> Unit,
    onToggleFavoriteCurrentVideo: () -> Unit,
    onSetVideoFullscreen: (Boolean) -> Unit,
    onOpenMetadataEditor: () -> Unit,
    onDismissMetadataEditor: () -> Unit,
    onMetadataTitleChange: (String) -> Unit,
    onMetadataArtistChange: (String) -> Unit,
    onMetadataAlbumChange: (String) -> Unit,
    onFetchMetadataFromAi: () -> Unit,
    onSaveMetadata: () -> Unit,
    onClearMetadataWritePermissionRequest: () -> Unit,
    onMetadataWritePermissionResult: (Boolean) -> Unit,
    onOpenVideoTitleEditor: () -> Unit,
    onDismissVideoTitleEditor: () -> Unit,
    onVideoTitleChange: (String) -> Unit,
    onSaveVideoTitle: () -> Unit,
    onClearVideoTitleWritePermissionRequest: () -> Unit,
    onVideoTitleWritePermissionResult: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val song = uiState.playerState.currentSong
    val activeVideo = uiState.activeVideo
    val isVideoMode = activeVideo != null
    val isCurrentFavorite = song != null && song.id in uiState.favoriteSongIds
    val isCurrentVideoFavorite = activeVideo != null && activeVideo.id in uiState.favoriteVideoIds
    var extraControlsExpanded by remember { mutableStateOf(false) }
    var volumeBarExpanded by remember { mutableStateOf(false) }
    var volumeFraction by remember { mutableFloatStateOf(0f) }
    var showRemoveFromQueueDialog by remember { mutableStateOf(false) }
    var dontShowRemoveDialogAgain by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val audioManager = remember(context) {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val activity = context as? Activity
    val pressBackAgainMessage = stringResource(R.string.press_back_again_to_exit)
    var lastBackPressTime by remember { mutableStateOf(0L) }
    var metadataPermissionDialogOpen by remember { mutableStateOf(false) }
    var videoTitlePermissionDialogOpen by remember { mutableStateOf(false) }

    val metadataWritePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        metadataPermissionDialogOpen = false
        onMetadataWritePermissionResult(result.resultCode == Activity.RESULT_OK)
    }

    val videoTitleWritePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        videoTitlePermissionDialogOpen = false
        onVideoTitleWritePermissionResult(result.resultCode == Activity.RESULT_OK)
    }

    LaunchedEffect(uiState.metadataWritePermissionRequest) {
        val intentSender = uiState.metadataWritePermissionRequest ?: return@LaunchedEffect
        if (metadataPermissionDialogOpen) return@LaunchedEffect
        metadataPermissionDialogOpen = true
        onClearMetadataWritePermissionRequest()
        metadataWritePermissionLauncher.launch(
            IntentSenderRequest.Builder(intentSender).build(),
        )
    }

    LaunchedEffect(uiState.videoTitleWritePermissionRequest) {
        val intentSender = uiState.videoTitleWritePermissionRequest ?: return@LaunchedEffect
        if (videoTitlePermissionDialogOpen) return@LaunchedEffect
        videoTitlePermissionDialogOpen = true
        onClearVideoTitleWritePermissionRequest()
        videoTitleWritePermissionLauncher.launch(
            IntentSenderRequest.Builder(intentSender).build(),
        )
    }

    val swipeQueue = remember(isVideoMode, song?.id, activeVideo?.id, uiState.playbackQueue, uiState.videoQueue) {
        if (isVideoMode && activeVideo != null) {
            val index = uiState.videoQueue.indexOfFirst { it.id == activeVideo.id }
            Triple(
                index > 0,
                index >= 0 && index < uiState.videoQueue.lastIndex,
                index,
            )
        } else if (song != null) {
            val index = uiState.playbackQueue.indexOfFirst { it.id == song.id }
            Triple(
                index > 0,
                index >= 0 && index < uiState.playbackQueue.lastIndex,
                index,
            )
        } else {
            Triple(false, false, -1)
        }
    }
    val hasSwipePrevious = swipeQueue.first
    val hasSwipeNext = swipeQueue.second
    val swipeIndex = swipeQueue.third
    val prevSong = if (!isVideoMode && swipeIndex > 0) {
        uiState.playbackQueue.getOrNull(swipeIndex - 1)
    } else {
        null
    }
    val nextSong = if (!isVideoMode && swipeIndex >= 0) {
        uiState.playbackQueue.getOrNull(swipeIndex + 1)
    } else {
        null
    }
    val prevVideo = if (isVideoMode && swipeIndex > 0) {
        uiState.videoQueue.getOrNull(swipeIndex - 1)
    } else {
        null
    }
    val nextVideo = if (isVideoMode && swipeIndex >= 0) {
        uiState.videoQueue.getOrNull(swipeIndex + 1)
    } else {
        null
    }

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    BackHandler(
        enabled = !drawerState.isOpen &&
            !uiState.videoFullscreen &&
            !uiState.lyricsSheetVisible &&
            !uiState.insightSheetVisible &&
            !uiState.metadataEditorVisible &&
            !uiState.videoTitleEditorVisible &&
            !showRemoveFromQueueDialog,
    ) {
        val now = System.currentTimeMillis()
        if (now - lastBackPressTime <= 2_000L) {
            activity?.finish()
        } else {
            lastBackPressTime = now
            Toast.makeText(context, pressBackAgainMessage, Toast.LENGTH_LONG).show()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = false,
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
                    IconButton(
                        onClick = { scope.launch { drawerState.close() } },
                        modifier = Modifier.align(Alignment.Start)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                            tint = TextWhite
                        )
                    }
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
            modifier = modifier.fillMaxSize()
        ) {
            ImmersivePlayerBackground(
                albumArtUri = song?.albumArtUri,
                isActive = !isVideoMode && song != null,
                isPlaying = uiState.playerState.isPlaying && !isVideoMode,
                modifier = Modifier.fillMaxSize()
            )
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isWideLayout = maxWidth > maxHeight
            val horizontalPadding = if (isWideLayout) 16.dp else 24.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = horizontalPadding)
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(bottom = if (isWideLayout) 8.dp else 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(if (isWideLayout) 8.dp else 48.dp))

            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                PlayerLabeledIconButton(
                    onClick = { scope.launch { drawerState.open() } },
                    icon = Icons.Default.Menu,
                    label = stringResource(R.string.cd_menu),
                    tint = TextWhite,
                    labelColor = TextWhite,
                )
                Text(
                    text = stringResource(R.string.now_playing),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = TextWhite,
                    modifier = Modifier.padding(top = if (isWideLayout) 4.dp else 12.dp)
                )
                PlayerLabeledIconButton(
                    onClick = onNavigateToLibrarySearch,
                    icon = Icons.Default.Search,
                    label = stringResource(R.string.cd_search),
                    tint = TextWhite,
                    labelColor = TextWhite,
                )
            }

            val bottomControls: @Composable () -> Unit = {
                if (!(uiState.videoFullscreen && isVideoMode)) {
                    PlayerBottomControls(
                        isWideLayout = isWideLayout,
                        isVideoMode = isVideoMode,
                        song = song,
                        uiState = uiState,
                        extraControlsExpanded = extraControlsExpanded,
                        volumeBarExpanded = volumeBarExpanded,
                        volumeFraction = volumeFraction,
                        audioManager = audioManager,
                        onNavigateToLibrary = onNavigateToLibrary,
                        onNavigateToVideoLibrary = onNavigateToVideoLibrary,
                        onNavigateToEqualizer = onNavigateToEqualizer,
                        onToggleRepeatMode = onToggleRepeatMode,
                        onToggleShuffle = onToggleShuffle,
                        onToggleLyricsSheet = onToggleLyricsSheet,
                        onToggleInsightSheet = onToggleInsightSheet,
                        onExcludeCurrentSong = onExcludeCurrentSong,
                        skipRemoveFromQueueConfirmation = skipRemoveFromQueueConfirmation,
                        onSetVolumeFraction = { volumeFraction = it },
                        onVolumeBarExpandedChange = { volumeBarExpanded = it },
                        onExtraControlsExpandedChange = { extraControlsExpanded = it },
                        onShowRemoveFromQueueDialog = {
                            dontShowRemoveDialogAgain = false
                            showRemoveFromQueueDialog = true
                        },
                        onSeekTo = onSeekTo,
                        onPrevious = onPrevious,
                        onTogglePlayPause = onTogglePlayPause,
                        onNext = onNext,
                    )
                }
            }

            val playerSwipeToSkip: @Composable (Modifier) -> Unit = { swipeModifier ->
                PlayerSwipeToSkip(
                enabled = !uiState.videoFullscreen && (song != null || activeVideo != null),
                hasPrevious = hasSwipePrevious,
                hasNext = hasSwipeNext,
                onSwipeToPrevious = onPrevious,
                onSwipeToNext = onNext,
                modifier = swipeModifier,
                previousContent = when {
                    prevSong != null -> {
                        { PlayerSongArtworkPreview(song = prevSong, isWideLayout = isWideLayout) }
                    }
                    prevVideo != null -> {
                        { PlayerVideoArtworkPreview(video = prevVideo, isWideLayout = isWideLayout) }
                    }
                    else -> null
                },
                nextContent = when {
                    nextSong != null -> {
                        { PlayerSongArtworkPreview(song = nextSong, isWideLayout = isWideLayout) }
                    }
                    nextVideo != null -> {
                        { PlayerVideoArtworkPreview(video = nextVideo, isWideLayout = isWideLayout) }
                    }
                    else -> null
                },
            ) {
                PlayerMediaArtworkContent(
                    isWideLayout = isWideLayout,
                    isVideoMode = isVideoMode,
                    song = song,
                    activeVideo = activeVideo,
                    videoPlayer = videoPlayer,
                    videoFullscreen = uiState.videoFullscreen,
                    isCurrentFavorite = isCurrentFavorite,
                    isCurrentVideoFavorite = isCurrentVideoFavorite,
                    onToggleFavoriteCurrentSong = onToggleFavoriteCurrentSong,
                    onToggleFavoriteCurrentVideo = onToggleFavoriteCurrentVideo,
                    onSetVideoFullscreen = onSetVideoFullscreen,
                    onOpenMetadataEditor = onOpenMetadataEditor,
                    onOpenVideoTitleEditor = onOpenVideoTitleEditor,
                )
            }
            }

            if (isWideLayout) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    playerSwipeToSkip(
                        Modifier
                            .weight(0.46f)
                            .fillMaxHeight()
                    )
                    Column(
                        modifier = Modifier
                            .weight(0.54f)
                            .fillMaxHeight()
                            .padding(start = 8.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        PlayerTrackInfo(
                            isVideoMode = isVideoMode,
                            song = song,
                            activeVideo = activeVideo,
                            compact = true,
                        )
                        if (!(uiState.videoFullscreen && isVideoMode)) {
                            Spacer(modifier = Modifier.height(12.dp))
                            bottomControls()
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(32.dp))
                playerSwipeToSkip(Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(28.dp))
                PlayerTrackInfo(
                    isVideoMode = isVideoMode,
                    song = song,
                    activeVideo = activeVideo,
                )
                Spacer(modifier = Modifier.weight(1f))
                bottomControls()
            }
            }
        }

        if (uiState.videoFullscreen && isVideoMode) {
            VideoFullscreenOverlay(
                player = videoPlayer,
                videoTitle = activeVideo!!.title,
                position = uiState.currentPosition,
                duration = uiState.videoDuration,
                isPlaying = uiState.videoIsPlaying,
                onExitFullscreen = { onSetVideoFullscreen(false) },
                onTogglePlayPause = onTogglePlayPause,
                onPrevious = onPrevious,
                onNext = onNext,
                onSeekTo = onSeekTo,
                swipeEnabled = hasSwipePrevious || hasSwipeNext,
                hasPrevious = hasSwipePrevious,
                hasNext = hasSwipeNext,
                previousPreview = prevVideo?.let { video ->
                    { PlayerVideoArtworkPreview(video = video, fillMaxSize = true) }
                },
                nextPreview = nextVideo?.let { video ->
                    { PlayerVideoArtworkPreview(video = video, fillMaxSize = true) }
                },
            )
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

        if (uiState.metadataEditorVisible) {
            EditSongMetadataDialog(
                title = uiState.metadataEditorTitle,
                artist = uiState.metadataEditorArtist,
                album = uiState.metadataEditorAlbum,
                aiLoading = uiState.metadataAiLoading,
                saving = uiState.metadataSaving,
                aiEnabled = uiState.aiInsightEnabled,
                errorMessage = uiState.metadataEditorError,
                onTitleChange = onMetadataTitleChange,
                onArtistChange = onMetadataArtistChange,
                onAlbumChange = onMetadataAlbumChange,
                onFetchFromAi = onFetchMetadataFromAi,
                onSave = onSaveMetadata,
                onDismiss = onDismissMetadataEditor,
            )
        }

        if (uiState.videoTitleEditorVisible) {
            EditVideoTitleDialog(
                title = uiState.videoTitleEditorTitle,
                saving = uiState.videoTitleSaving,
                errorMessage = uiState.videoTitleEditorError,
                onTitleChange = onVideoTitleChange,
                onSave = onSaveVideoTitle,
                onDismiss = onDismissVideoTitleEditor,
            )
        }

        if (showRemoveFromQueueDialog) {
            AlertDialog(
                onDismissRequest = { showRemoveFromQueueDialog = false },
                containerColor = DarkSurface,
                titleContentColor = TextWhite,
                textContentColor = TextGray,
                title = {
                    Text(text = stringResource(R.string.remove_from_queue_dialog_title))
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(text = stringResource(R.string.remove_from_queue_dialog_message))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    dontShowRemoveDialogAgain = !dontShowRemoveDialogAgain
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = dontShowRemoveDialogAgain,
                                onCheckedChange = { dontShowRemoveDialogAgain = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Teal400,
                                    uncheckedColor = TextGray,
                                    checkmarkColor = DarkBackground
                                )
                            )
                            Text(
                                text = stringResource(R.string.remove_from_queue_dialog_dont_show_again),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextGray
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (dontShowRemoveDialogAgain) {
                                onSetSkipRemoveFromQueueConfirmation(true)
                            }
                            showRemoveFromQueueDialog = false
                            onExcludeCurrentSong()
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.remove_from_queue_dialog_confirm),
                            color = Teal400
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRemoveFromQueueDialog = false }) {
                        Text(
                            text = stringResource(R.string.remove_from_queue_dialog_cancel),
                            color = TextGray
                        )
                    }
                }
            )
        }
        }
    }
}

private fun Modifier.playerArtworkSize(isWideLayout: Boolean): Modifier = if (isWideLayout) {
    fillMaxHeight(0.92f).aspectRatio(1f, matchHeightConstraintsFirst = true)
} else {
    fillMaxWidth(0.78f).aspectRatio(1f)
}

@Composable
private fun PlayerMediaArtworkContent(
    isWideLayout: Boolean,
    isVideoMode: Boolean,
    song: Song?,
    activeVideo: Video?,
    videoPlayer: Player?,
    videoFullscreen: Boolean,
    isCurrentFavorite: Boolean,
    isCurrentVideoFavorite: Boolean,
    onToggleFavoriteCurrentSong: () -> Unit,
    onToggleFavoriteCurrentVideo: () -> Unit,
    onSetVideoFullscreen: (Boolean) -> Unit,
    onOpenMetadataEditor: () -> Unit,
    onOpenVideoTitleEditor: () -> Unit,
) {
    val containerModifier = if (isWideLayout) {
        Modifier.fillMaxHeight()
    } else {
        Modifier.fillMaxWidth()
    }
    when {
        isVideoMode -> {
            Box(
                modifier = containerModifier,
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .playerArtworkSize(isWideLayout)
                        .shadow(24.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    if (videoFullscreen) {
                        AlbumArtAsyncImage(
                            albumArtUri = activeVideo!!.thumbnailUri ?: activeVideo.uri,
                            contentDescription = activeVideo.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            onEditMetadataClick = onOpenVideoTitleEditor,
                        )
                    } else {
                        key(activeVideo!!.id) {
                            InlineVideoPlayer(
                                player = videoPlayer,
                                modifier = Modifier.fillMaxSize(),
                                useTextureView = true,
                            )
                        }
                    }
                    if (!videoFullscreen) {
                        IconButton(
                            onClick = onOpenVideoTitleEditor,
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
                        IconButton(
                            onClick = onToggleFavoriteCurrentVideo,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(6.dp),
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = Color.Black.copy(alpha = 0.45f),
                                contentColor = if (isCurrentVideoFavorite) Teal400 else Color.White,
                            ),
                        ) {
                            Icon(
                                imageVector = if (isCurrentVideoFavorite) {
                                    Icons.Filled.Favorite
                                } else {
                                    Icons.Outlined.FavoriteBorder
                                },
                                contentDescription = if (isCurrentVideoFavorite) {
                                    stringResource(R.string.cd_remove_favorite)
                                } else {
                                    stringResource(R.string.cd_add_favorite)
                                },
                                tint = if (isCurrentVideoFavorite) Teal400 else Color.White,
                            )
                        }
                        IconButton(
                            onClick = { onSetVideoFullscreen(true) },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(6.dp),
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = Color.Black.copy(alpha = 0.45f),
                                contentColor = Color.White,
                            ),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Fullscreen,
                                contentDescription = stringResource(R.string.cd_fullscreen),
                                tint = Color.White,
                            )
                        }
                    }
                }
            }
        }
        song != null -> {
            Box(
                modifier = containerModifier,
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .playerArtworkSize(isWideLayout)
                        .shadow(24.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    AlbumArtAsyncImage(
                        albumArtUri = song.albumArtUri,
                        contentDescription = song.album,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        onEditMetadataClick = onOpenMetadataEditor,
                    )
                    IconButton(
                        onClick = onToggleFavoriteCurrentSong,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.Black.copy(alpha = 0.45f),
                            contentColor = if (isCurrentFavorite) Teal400 else Color.White,
                        ),
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
                            },
                            tint = if (isCurrentFavorite) Teal400 else Color.White,
                        )
                    }
                }
            }
        }
        else -> {
            Box(
                modifier = containerModifier,
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    modifier = Modifier
                        .playerArtworkSize(isWideLayout)
                        .shadow(24.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    color = DarkSurfaceVariant
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.padding(if (isWideLayout) 32.dp else 64.dp),
                        tint = Teal400
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerTrackInfo(
    isVideoMode: Boolean,
    song: Song?,
    activeVideo: Video?,
    compact: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = when {
                isVideoMode -> activeVideo!!.title
                song != null -> song.title
                else -> stringResource(R.string.no_song)
            },
            style = if (compact) {
                MaterialTheme.typography.titleLarge
            } else {
                MaterialTheme.typography.headlineMedium
            },
            fontWeight = FontWeight.Bold,
            color = TextWhite,
            maxLines = if (compact) 2 else 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = when {
                isVideoMode -> stringResource(R.string.video_local)
                song != null -> song.artist
                else -> stringResource(R.string.select_a_song)
            },
            style = MaterialTheme.typography.bodyLarge,
            color = TextGray,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PlayerBottomControls(
    isWideLayout: Boolean,
    isVideoMode: Boolean,
    song: Song?,
    uiState: PlayerUiState,
    extraControlsExpanded: Boolean,
    volumeBarExpanded: Boolean,
    volumeFraction: Float,
    audioManager: AudioManager,
    onNavigateToLibrary: () -> Unit,
    onNavigateToVideoLibrary: () -> Unit,
    onNavigateToEqualizer: () -> Unit,
    onToggleRepeatMode: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleLyricsSheet: () -> Unit,
    onToggleInsightSheet: () -> Unit,
    onExcludeCurrentSong: () -> Unit,
    skipRemoveFromQueueConfirmation: Boolean,
    onSetVolumeFraction: (Float) -> Unit,
    onVolumeBarExpandedChange: (Boolean) -> Unit,
    onExtraControlsExpandedChange: (Boolean) -> Unit,
    onShowRemoveFromQueueDialog: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onPrevious: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val duration = if (isVideoMode) uiState.videoDuration else uiState.playerState.duration
    val position = uiState.currentPosition
    val isPlaying = if (isVideoMode) uiState.videoIsPlaying else uiState.playerState.isPlaying
    val secondaryLabelMaxWidth = if (isWideLayout) 56.dp else 72.dp

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Top
            ) {
                PlayerLabeledIconButton(
                    onClick = {
                        val expanded = !volumeBarExpanded
                        onVolumeBarExpandedChange(expanded)
                        if (expanded) {
                            val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                                .coerceAtLeast(1)
                            val cur = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                            onSetVolumeFraction(cur.toFloat() / max.toFloat())
                        }
                    },
                    icon = Icons.Default.VolumeUp,
                    label = stringResource(R.string.cd_volume),
                    tint = if (volumeBarExpanded) Teal400 else TextGray,
                    labelMaxWidth = secondaryLabelMaxWidth,
                )
                PlayerLabeledIconButton(
                    onClick = onToggleShuffle,
                    icon = Icons.Default.Shuffle,
                    label = stringResource(R.string.cd_shuffle),
                    tint = if (uiState.playerState.shuffleEnabled) Teal400 else TextGray,
                    labelMaxWidth = secondaryLabelMaxWidth,
                )
                PlayerLabeledIconButton(
                    onClick = onNavigateToLibrary,
                    icon = Icons.Filled.LibraryMusic,
                    label = stringResource(R.string.cd_library),
                    labelMaxWidth = secondaryLabelMaxWidth,
                )
                PlayerLabeledIconButton(
                    onClick = onNavigateToVideoLibrary,
                    icon = Icons.Filled.VideoLibrary,
                    label = stringResource(R.string.cd_video_library),
                    tint = if (isVideoMode) Teal400 else TextGray,
                    labelMaxWidth = secondaryLabelMaxWidth,
                )
            }
            PlayerLabeledIconButton(
                onClick = { onExtraControlsExpandedChange(!extraControlsExpanded) },
                icon = if (extraControlsExpanded) {
                    Icons.Filled.ExpandMore
                } else {
                    Icons.Filled.ExpandLess
                },
                label = if (extraControlsExpanded) {
                    stringResource(R.string.player_label_less)
                } else {
                    stringResource(R.string.player_label_more)
                },
                contentDescription = if (extraControlsExpanded) {
                    stringResource(R.string.cd_hide_extra_controls)
                } else {
                    stringResource(R.string.cd_show_extra_controls)
                },
                tint = if (extraControlsExpanded) Teal400 else TextGray,
                labelMaxWidth = secondaryLabelMaxWidth,
            )
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
                        onSetVolumeFraction(v)
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
                verticalAlignment = Alignment.Top
            ) {
                PlayerLabeledIconButton(
                    onClick = onToggleRepeatMode,
                    icon = when (uiState.playerState.repeatMode) {
                        Player.REPEAT_MODE_ONE -> Icons.Default.RepeatOne
                        else -> Icons.Default.Repeat
                    },
                    label = stringResource(R.string.cd_repeat),
                    tint = if (uiState.playerState.repeatMode != Player.REPEAT_MODE_OFF) {
                        Teal400
                    } else {
                        TextGray
                    },
                    labelMaxWidth = secondaryLabelMaxWidth,
                )
                PlayerLabeledIconButton(
                    onClick = onNavigateToEqualizer,
                    enabled = !isVideoMode,
                    icon = Icons.Filled.Equalizer,
                    label = stringResource(R.string.cd_equalizer),
                    tint = if (isVideoMode) TextGray.copy(alpha = 0.4f) else TextGray,
                    labelMaxWidth = secondaryLabelMaxWidth,
                )
                PlayerLabeledIconButton(
                    onClick = {
                        if (song != null) {
                            if (skipRemoveFromQueueConfirmation) {
                                onExcludeCurrentSong()
                            } else {
                                onShowRemoveFromQueueDialog()
                            }
                        }
                    },
                    enabled = song != null && !isVideoMode,
                    icon = Icons.Filled.PlaylistRemove,
                    label = stringResource(R.string.remove_from_queue_dialog_confirm),
                    contentDescription = stringResource(R.string.cd_remove_from_queue),
                    tint = when {
                        isVideoMode -> TextGray.copy(alpha = 0.4f)
                        song != null -> TextGray
                        else -> TextGray.copy(alpha = 0.4f)
                    },
                    labelMaxWidth = secondaryLabelMaxWidth,
                )
                PlayerLabeledIconButton(
                    onClick = onToggleLyricsSheet,
                    enabled = !isVideoMode,
                    icon = Icons.Filled.Subtitles,
                    label = stringResource(R.string.lyrics),
                    tint = when {
                        isVideoMode -> TextGray.copy(alpha = 0.4f)
                        uiState.lyricsSheetVisible -> Teal400
                        else -> TextGray
                    },
                    labelMaxWidth = secondaryLabelMaxWidth,
                )
                if (uiState.aiInsightEnabled) {
                    PlayerLabeledIconButton(
                        onClick = onToggleInsightSheet,
                        enabled = song != null && !isVideoMode,
                        icon = Icons.Filled.AutoAwesome,
                        label = stringResource(R.string.song_data),
                        tint = when {
                            isVideoMode -> TextGray.copy(alpha = 0.4f)
                            song == null -> TextGray.copy(alpha = 0.4f)
                            uiState.insightSheetVisible -> Teal400
                            else -> TextGray
                        },
                        labelMaxWidth = secondaryLabelMaxWidth,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(if (isWideLayout) 8.dp else 16.dp))

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

        Spacer(modifier = Modifier.height(if (isWideLayout) 4.dp else 8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlayerLabeledIconButton(
                onClick = onPrevious,
                icon = Icons.Default.SkipPrevious,
                label = stringResource(R.string.cd_previous),
                tint = TextWhite,
                labelColor = TextWhite,
                showLabel = false,
                iconModifier = Modifier.size(if (isWideLayout) 32.dp else 36.dp),
                iconButtonModifier = Modifier.size(if (isWideLayout) 48.dp else 56.dp),
            )

            PlayerLabeledPlayButton(
                isPlaying = isPlaying,
                onClick = onTogglePlayPause,
                showLabel = false,
                buttonSize = if (isWideLayout) 64.dp else 72.dp,
                iconPadding = if (isWideLayout) 16.dp else 18.dp,
            )

            PlayerLabeledIconButton(
                onClick = onNext,
                icon = Icons.Default.SkipNext,
                label = stringResource(R.string.cd_next),
                tint = TextWhite,
                labelColor = TextWhite,
                showLabel = false,
                iconModifier = Modifier.size(if (isWideLayout) 32.dp else 36.dp),
                iconButtonModifier = Modifier.size(if (isWideLayout) 48.dp else 56.dp),
            )
        }
    }
}

@Composable
private fun PlayerLabeledIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = TextGray,
    labelColor: Color = tint,
    contentDescription: String = label,
    iconModifier: Modifier = Modifier,
    iconButtonModifier: Modifier = Modifier,
    iconButtonColors: IconButtonColors? = null,
    labelMaxWidth: Dp = 56.dp,
    showLabel: Boolean = true,
) {
    val iconButton: @Composable () -> Unit = {
        if (iconButtonColors != null) {
            IconButton(
                onClick = onClick,
                enabled = enabled,
                modifier = iconButtonModifier,
                colors = iconButtonColors,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = tint,
                    modifier = iconModifier,
                )
            }
        } else {
            IconButton(
                onClick = onClick,
                enabled = enabled,
                modifier = iconButtonModifier,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = tint,
                    modifier = iconModifier,
                )
            }
        }
    }

    if (showLabel) {
        Column(
            modifier = modifier.widthIn(max = labelMaxWidth),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            iconButton()
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (enabled) labelColor else labelColor.copy(alpha = 0.4f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                fontSize = 10.sp,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    } else {
        Box(modifier = modifier) {
            iconButton()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerLabeledPlayButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    buttonSize: Dp = 72.dp,
    iconPadding: Dp = 18.dp,
    showLabel: Boolean = true,
) {
    val label = if (isPlaying) {
        stringResource(R.string.cd_pause)
    } else {
        stringResource(R.string.cd_play)
    }
    val playButton: @Composable () -> Unit = {
        Surface(
            modifier = Modifier.size(buttonSize),
            shape = CircleShape,
            color = Teal400,
            shadowElevation = 8.dp,
            onClick = onClick,
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = label,
                modifier = Modifier.padding(iconPadding),
                tint = Color.White,
            )
        }
    }

    if (showLabel) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            playButton()
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                fontSize = 10.sp,
            )
        }
    } else {
        Box(modifier = modifier) {
            playButton()
        }
    }
}

@Composable
private fun PlayerSongArtworkPreview(
    song: Song,
    isWideLayout: Boolean = false,
) {
    val containerModifier = if (isWideLayout) {
        Modifier.fillMaxHeight()
    } else {
        Modifier.fillMaxWidth()
    }
    Box(
        modifier = containerModifier,
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .playerArtworkSize(isWideLayout)
                .shadow(24.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp)),
        ) {
            AlbumArtAsyncImage(
                albumArtUri = song.albumArtUri,
                contentDescription = song.album,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

@Composable
private fun PlayerVideoArtworkPreview(
    video: Video,
    fillMaxSize: Boolean = false,
    isWideLayout: Boolean = false,
) {
    if (fillMaxSize) {
        VideoThumbnailAsyncImage(
            videoId = video.id,
            videoUri = video.uri,
            contentDescription = video.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        return
    }
    val containerModifier = if (isWideLayout) {
        Modifier.fillMaxHeight()
    } else {
        Modifier.fillMaxWidth()
    }
    Box(
        modifier = containerModifier,
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .playerArtworkSize(isWideLayout)
                .shadow(24.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp)),
        ) {
            VideoThumbnailAsyncImage(
                videoId = video.id,
                videoUri = video.uri,
                contentDescription = video.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
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
            videoPlayer = null,
            onNavigateToLibrary = {},
            onNavigateToLibrarySearch = {},
            onNavigateToVideoLibrary = {},
            onNavigateToEqualizer = {},
            onNavigateToSettings = {},
            onToggleRepeatMode = {},
            onToggleShuffle = {},
            onSeekTo = {},
            onPrevious = {},
            onTogglePlayPause = {},
            onNext = {},
            onExcludeCurrentSong = {},
            onSetSkipRemoveFromQueueConfirmation = {},
            onToggleLyricsSheet = {},
            onDismissLyricsSheet = {},
            onPickLyricsCandidate = {},
            onToggleInsightSheet = {},
            onDismissInsightSheet = {},
            onToggleFavoriteCurrentSong = {},
            onToggleFavoriteCurrentVideo = {},
            onSetVideoFullscreen = {},
            onOpenMetadataEditor = {},
            onDismissMetadataEditor = {},
            onMetadataTitleChange = {},
            onMetadataArtistChange = {},
            onMetadataAlbumChange = {},
            onFetchMetadataFromAi = {},
            onSaveMetadata = {},
            onClearMetadataWritePermissionRequest = {},
            onMetadataWritePermissionResult = {},
            onOpenVideoTitleEditor = {},
            onDismissVideoTitleEditor = {},
            onVideoTitleChange = {},
            onSaveVideoTitle = {},
            onClearVideoTitleWritePermissionRequest = {},
            onVideoTitleWritePermissionResult = {},
        )
    }
}
