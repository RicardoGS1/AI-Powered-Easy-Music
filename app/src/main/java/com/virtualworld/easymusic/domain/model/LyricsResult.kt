package com.virtualworld.easymusic.domain.model

data class LyricsLine(val timeMs: Long, val text: String)

sealed interface LyricsResult {
    data class Synced(val lines: List<LyricsLine>) : LyricsResult
    data class PlainOnly(val text: String) : LyricsResult
    data object Instrumental : LyricsResult
    data object NotFound : LyricsResult
    data class Failure(val message: String) : LyricsResult
}
