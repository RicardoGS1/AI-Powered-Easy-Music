package com.virtualworld.easymusic.playback

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.virtualworld.easymusic.domain.model.Video
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

data class VideoPlaybackState(
    val currentVideo: Video? = null,
    val isPlaying: Boolean = false,
    val position: Long = 0L,
    val duration: Long = 0L,
    val isFullscreen: Boolean = false,
    val isConnected: Boolean = false,
)

@Singleton
class VideoPlaybackController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val playbackController: PlaybackController,
) {

    private val _state = MutableStateFlow(VideoPlaybackState())
    val state: StateFlow<VideoPlaybackState> = _state.asStateFlow()

    private var mediaController: MediaController? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var currentPlaylist: List<Video> = emptyList()
    private var pendingSelection: Pair<List<Video>, Int>? = null

    fun getPlaylist(): List<Video> = currentPlaylist

    fun connect() {
        if (mediaController != null) return

        val sessionToken = SessionToken(
            context,
            ComponentName(context, VideoPlaybackService::class.java),
        )
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            mediaController = controllerFuture?.let {
                if (it.isDone && !it.isCancelled) it.get() else null
            }
            mediaController?.let { controller ->
                _state.update { it.copy(isConnected = true) }
                setupPlayerListener(controller)
                pendingSelection?.let { (videos, index) ->
                    pendingSelection = null
                    startPlayback(videos, index)
                }
            }
        }, MoreExecutors.directExecutor())
    }

    private fun setupPlayerListener(controller: MediaController) {
        controller.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _state.update { it.copy(isPlaying = isPlaying) }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val index = controller.currentMediaItemIndex
                val video = currentPlaylist.getOrNull(index) ?: return
                _state.update {
                    it.copy(
                        currentVideo = video,
                        duration = controller.duration.coerceAtLeast(0L),
                    )
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    _state.update {
                        it.copy(duration = controller.duration.coerceAtLeast(0L))
                    }
                }
            }
        })
    }

    fun getPlayer(): Player? = mediaController

    fun selectVideo(video: Video, videos: List<Video>, startIndex: Int = 0) {
        if (videos.isEmpty()) return
        connect()
        playbackController.pause()
        currentPlaylist = videos
        val safeIndex = startIndex.coerceIn(0, videos.lastIndex)
        val current = videos[safeIndex]
        if (mediaController != null) {
            startPlayback(videos, safeIndex)
        } else {
            pendingSelection = videos to safeIndex
        }
        _state.update {
            it.copy(
                currentVideo = current,
                isPlaying = true,
                position = 0L,
                duration = mediaController?.duration?.coerceAtLeast(0L) ?: 0L,
                isFullscreen = false,
            )
        }
    }

    private fun startPlayback(videos: List<Video>, startIndex: Int) {
        val mediaItems = videos.map { item ->
            MediaItem.Builder()
                .setUri(item.uri)
                .setMediaId(item.id.toString())
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(item.title)
                        .setArtworkUri(item.thumbnailUri)
                        .build(),
                )
                .build()
        }
        mediaController?.run {
            setMediaItems(mediaItems, startIndex, 0L)
            prepare()
            play()
        }
    }

    fun togglePlayPause() {
        mediaController?.let {
            if (it.isPlaying) it.pause() else it.play()
        }
    }

    fun play() {
        mediaController?.play()
    }

    fun pause() {
        mediaController?.pause()
    }

    fun seekTo(position: Long) {
        mediaController?.seekTo(position.coerceAtLeast(0L))
        _state.update { it.copy(position = position.coerceAtLeast(0L)) }
    }

    fun next() {
        mediaController?.seekToNextMediaItem()
    }

    fun previous() {
        mediaController?.seekToPreviousMediaItem()
    }

    fun setFullscreen(fullscreen: Boolean) {
        (mediaController as? ExoPlayer)?.let { player ->
            player.videoScalingMode = if (fullscreen) {
                C.VIDEO_SCALING_MODE_SCALE_TO_FIT
            } else {
                C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
            }
            if (!fullscreen) {
                player.clearVideoSurface()
            }
        }
        _state.update { it.copy(isFullscreen = fullscreen) }
    }

    fun getCurrentPosition(): Long {
        return mediaController?.currentPosition?.coerceAtLeast(0L) ?: 0L
    }

    fun clearVideo() {
        mediaController?.run {
            stop()
            clearMediaItems()
        }
        currentPlaylist = emptyList()
        _state.update { it.copy(currentVideo = null, isPlaying = false, position = 0L, duration = 0L) }
    }

    fun updateVideoInPlaylist(updatedVideo: Video) {
        currentPlaylist = currentPlaylist.map { video ->
            if (video.id == updatedVideo.id) updatedVideo else video
        }
        mediaController?.let { controller ->
            for (index in 0 until controller.mediaItemCount) {
                val item = controller.getMediaItemAt(index)
                if (item.mediaId == updatedVideo.id.toString()) {
                    val newItem = item.buildUpon()
                        .setMediaMetadata(
                            MediaMetadata.Builder()
                                .setTitle(updatedVideo.title)
                                .setArtworkUri(updatedVideo.thumbnailUri)
                                .build(),
                        )
                        .build()
                    controller.replaceMediaItem(index, newItem)
                    break
                }
            }
        }
        if (_state.value.currentVideo?.id == updatedVideo.id) {
            _state.update { it.copy(currentVideo = updatedVideo) }
        }
    }

    fun disconnect() {
        controllerFuture?.let { MediaController.releaseFuture(it) }
        mediaController = null
        controllerFuture = null
        currentPlaylist = emptyList()
        _state.update { VideoPlaybackState() }
    }

    /** Detiene la reproducción y el servicio; usar solo al cerrar la app, no en segundo plano. */
    fun stopPlaybackAndRelease() {
        mediaController?.run {
            stop()
            clearMediaItems()
        }
        disconnect()
        context.stopService(Intent(context, VideoPlaybackService::class.java))
    }
}
