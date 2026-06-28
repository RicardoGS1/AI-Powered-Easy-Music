package com.virtualworld.easymusic.playback

import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
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
)

@Singleton
class VideoPlaybackController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val playbackController: PlaybackController,
) {

    private val _state = MutableStateFlow(VideoPlaybackState())
    val state: StateFlow<VideoPlaybackState> = _state.asStateFlow()

    private var exoPlayer: ExoPlayer? = null
    private var currentPlaylist: List<Video> = emptyList()

    fun getPlaylist(): List<Video> = currentPlaylist

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _state.update { it.copy(isPlaying = isPlaying) }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val index = exoPlayer?.currentMediaItemIndex ?: return
            val video = currentPlaylist.getOrNull(index) ?: return
            _state.update {
                it.copy(
                    currentVideo = video,
                    duration = exoPlayer?.duration?.coerceAtLeast(0L) ?: 0L,
                )
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_READY) {
                _state.update {
                    it.copy(duration = exoPlayer?.duration?.coerceAtLeast(0L) ?: 0L)
                }
            }
        }
    }

    private fun ensurePlayer(): ExoPlayer {
        return exoPlayer ?: ExoPlayer.Builder(context).build().also { player ->
            player.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
            player.addListener(playerListener)
            exoPlayer = player
        }
    }

    fun getPlayer(): ExoPlayer? = exoPlayer

    fun selectVideo(video: Video, videos: List<Video>, startIndex: Int = 0) {
        if (videos.isEmpty()) return
        playbackController.pause()
        currentPlaylist = videos
        val safeIndex = startIndex.coerceIn(0, videos.lastIndex)
        val player = ensurePlayer()
        val mediaItems = videos.map { item ->
            MediaItem.Builder()
                .setUri(item.uri)
                .setMediaId(item.id.toString())
                .build()
        }
        player.setMediaItems(mediaItems, safeIndex, 0L)
        player.prepare()
        player.play()
        val current = videos[safeIndex]
        _state.update {
            it.copy(
                currentVideo = current,
                isPlaying = true,
                position = 0L,
                duration = player.duration.coerceAtLeast(0L),
                isFullscreen = false,
            )
        }
    }

    fun togglePlayPause() {
        exoPlayer?.let { player ->
            if (player.isPlaying) player.pause() else player.play()
        }
    }

    fun play() {
        exoPlayer?.play()
    }

    fun pause() {
        exoPlayer?.pause()
    }

    fun seekTo(position: Long) {
        exoPlayer?.seekTo(position.coerceAtLeast(0L))
        _state.update { it.copy(position = position.coerceAtLeast(0L)) }
    }

    fun next() {
        exoPlayer?.seekToNextMediaItem()
    }

    fun previous() {
        exoPlayer?.seekToPreviousMediaItem()
    }

    fun setFullscreen(fullscreen: Boolean) {
        exoPlayer?.let { player ->
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
        return exoPlayer?.currentPosition?.coerceAtLeast(0L) ?: 0L
    }

    fun clearVideo() {
        exoPlayer?.run {
            stop()
            clearMediaItems()
        }
        currentPlaylist = emptyList()
        _state.update { VideoPlaybackState() }
    }

    fun release() {
        exoPlayer?.run {
            removeListener(playerListener)
            release()
        }
        exoPlayer = null
        currentPlaylist = emptyList()
        _state.update { VideoPlaybackState() }
    }
}
