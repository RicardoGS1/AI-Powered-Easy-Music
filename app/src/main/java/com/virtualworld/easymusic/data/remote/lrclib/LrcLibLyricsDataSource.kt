package com.virtualworld.easymusic.data.remote.lrclib

import com.virtualworld.easymusic.domain.model.LyricsResult
import com.virtualworld.easymusic.domain.model.Song
import java.io.IOException
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LrcLibLyricsDataSource @Inject constructor(
    private val api: LrcLibApi
) {

    suspend fun fetchLyrics(song: Song): LyricsResult {
        val durationSec = (song.duration / 1000L).toInt().coerceAtLeast(1)
        val trackName = song.title.ifBlank { "Unknown" }
        val artistName = song.artist.ifBlank { "Unknown" }
        val albumName = song.album.ifBlank { "Unknown" }

        return try {
            val cached = api.getLyricsCached(trackName, artistName, albumName, durationSec)
            when {
                cached.isSuccessful -> {
                    val body = cached.body() ?: return LyricsResult.NotFound
                    mapDtoToResult(body)
                }
                cached.code() == 404 -> {
                    val remote = api.getLyrics(trackName, artistName, albumName, durationSec)
                    when {
                        remote.isSuccessful -> {
                            val body = remote.body() ?: return LyricsResult.NotFound
                            mapDtoToResult(body)
                        }
                        remote.code() == 404 -> LyricsResult.NotFound
                        else -> LyricsResult.Failure(httpMessage(remote))
                    }
                }
                else -> LyricsResult.Failure(httpMessage(cached))
            }
        } catch (e: IOException) {
            LyricsResult.Failure(e.message ?: "Sin conexión")
        }
    }

    private fun mapDtoToResult(dto: LrcLibTrackDto): LyricsResult {
        if (dto.instrumental == true) return LyricsResult.Instrumental
        val synced = dto.syncedLyrics?.trim().orEmpty()
        if (synced.isNotEmpty()) {
            val lines = LrcParser.parse(synced)
            if (lines.isNotEmpty()) return LyricsResult.Synced(lines)
        }
        val plain = dto.plainLyrics?.trim().orEmpty()
        if (plain.isNotEmpty()) return LyricsResult.PlainOnly(plain)
        return LyricsResult.NotFound
    }

    private fun httpMessage(response: Response<*>): String {
        val msg = response.message().trim()
        if (msg.isNotEmpty()) return msg
        return "Error HTTP ${response.code()}"
    }
}
