package com.virtualworld.easymusic.domain.model

data class LyricsSearchCandidate(
    val id: Long,
    val trackName: String,
    val artistName: String,
    val albumName: String,
    val durationSeconds: Int
)
