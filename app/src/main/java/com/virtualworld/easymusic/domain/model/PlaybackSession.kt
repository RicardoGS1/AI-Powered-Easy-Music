package com.virtualworld.easymusic.domain.model

import androidx.media3.common.Player

data class PlaybackSession(
    val queueSongIds: List<Long>,
    val currentIndex: Int,
    val positionMs: Long,
    val shuffleEnabled: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
)
