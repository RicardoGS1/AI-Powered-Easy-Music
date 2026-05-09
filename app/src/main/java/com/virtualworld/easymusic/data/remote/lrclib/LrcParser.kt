package com.virtualworld.easymusic.data.remote.lrclib

import com.virtualworld.easymusic.domain.model.LyricsLine

object LrcParser {

    private val linePattern = Regex("""^\[(\d{1,3}):(\d{2})(?:\.(\d{1,3}))?]\s*(.*)$""")

    fun parse(syncedLyrics: String): List<LyricsLine> {
        val out = mutableListOf<LyricsLine>()
        syncedLyrics.lineSequence().forEach { raw ->
            val line = raw.trimEnd()
            val match = linePattern.matchEntire(line) ?: return@forEach
            val minutes = match.groupValues[1].toLongOrNull() ?: return@forEach
            val seconds = match.groupValues[2].toLongOrNull() ?: return@forEach
            val fraction = match.groupValues.getOrNull(3).orEmpty()
            val fractionMs = fractionToMillis(fraction)
            val timeMs = minutes * 60_000L + seconds * 1000L + fractionMs
            val text = match.groupValues[4].trim()
            if (text.isNotEmpty()) {
                out.add(LyricsLine(timeMs = timeMs, text = text))
            }
        }
        return out.sortedBy { it.timeMs }
    }

    private fun fractionToMillis(fraction: String): Long {
        if (fraction.isEmpty()) return 0L
        return when (fraction.length) {
            1 -> fraction.toLongOrNull()?.times(100L) ?: 0L
            2 -> fraction.toLongOrNull()?.times(10L) ?: 0L
            else -> fraction.toLongOrNull()?.coerceIn(0L, 999L) ?: 0L
        }
    }
}
