package com.virtualworld.easymusic.playback

import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MusicPlaybackService : MediaSessionService() {

    @Inject
    lateinit var equalizerManager: EqualizerManager

    private var player: ExoPlayer? = null
    private var mediaSession: MediaSession? = null

    companion object {
        @Volatile
        var audioSessionId: Int = 0
    }

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true
            )
            .setHandleAudioBecomingNoisy(true)
            .build()

        // Sin esto, muchos dispositivos usan «audio offload» (DSP): el Equalizer del sistema no afecta la salida.
        player!!.trackSelectionParameters = player!!.trackSelectionParameters
            .buildUpon()
            .setAudioOffloadPreferences(
                TrackSelectionParameters.AudioOffloadPreferences.Builder()
                    .setAudioOffloadMode(
                        TrackSelectionParameters.AudioOffloadPreferences.AUDIO_OFFLOAD_MODE_DISABLED
                    )
                    .build()
            )
            .build()

        player!!.addListener(
            object : Player.Listener {
                override fun onAudioSessionIdChanged(audioSessionId: Int) {
                    publishSessionAndInitEqualizer(audioSessionId)
                }
            }
        )

        publishSessionAndInitEqualizer(player!!.audioSessionId)

        mediaSession = MediaSession.Builder(this, player!!).build()
    }

    /**
     * El ecualizador debe crearse con la sesión del mismo [ExoPlayer] que reproduce (Media3 / issue #1125).
     * El [MediaController] puede no ser fiable para este dato en todos los firmwares.
     */
    private fun publishSessionAndInitEqualizer(sessionId: Int) {
        if (sessionId == C.AUDIO_SESSION_ID_UNSET || sessionId == 0) return
        MusicPlaybackService.audioSessionId = sessionId
        equalizerManager.initialize(sessionId)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        if (::equalizerManager.isInitialized) {
            equalizerManager.release()
        }
        mediaSession?.run {
            player?.release()
            release()
        }
        mediaSession = null
        player = null
        super.onDestroy()
    }
}
