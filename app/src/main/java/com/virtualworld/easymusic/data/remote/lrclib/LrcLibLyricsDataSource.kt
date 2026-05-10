package com.virtualworld.easymusic.data.remote.lrclib

import android.app.Application
import com.virtualworld.easymusic.R
import com.virtualworld.easymusic.domain.model.LyricsResult
import com.virtualworld.easymusic.domain.model.LyricsSearchCandidate
import com.virtualworld.easymusic.domain.model.Song
import java.io.IOException
import kotlin.math.abs
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LrcLibLyricsDataSource @Inject constructor(
    private val app: Application,
    private val api: LrcLibApi
) {

    suspend fun fetchLyricsExactOnly(song: Song): LyricsResult {
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
            LyricsResult.Failure(e.message ?: app.getString(R.string.no_connection))
        }
    }

    suspend fun fetchLyricsViaSearch(song: Song): LyricsResult {
        val durationSec = (song.duration / 1000L).toInt().coerceAtLeast(1)
        val trackName = song.title.ifBlank { "Unknown" }
        val artistName = song.artist.ifBlank { "Unknown" }
        val albumName = song.album.ifBlank { "Unknown" }
        return try {
            searchFallback(durationSec, trackName, artistName, albumName)
        } catch (e: IOException) {
            LyricsResult.Failure(e.message ?: app.getString(R.string.no_connection))
        }
    }

    suspend fun fetchLyricsById(id: Long): LyricsResult {
        return try {
            val response = api.getLyricsById(id)
            when {
                response.isSuccessful -> {
                    val body = response.body() ?: return LyricsResult.NotFound
                    mapDtoToResult(body)
                }
                response.code() == 404 -> LyricsResult.NotFound
                else -> LyricsResult.Failure(httpMessage(response))
            }
        } catch (e: IOException) {
            LyricsResult.Failure(e.message ?: app.getString(R.string.no_connection))
        }
    }

    private suspend fun searchFallback(
        durationSec: Int,
        trackName: String,
        artistName: String,
        albumName: String
    ): LyricsResult {
        var list = searchTracks(trackName = trackName, artistName = artistName)
        if (list.isEmpty() && albumName.isNotBlank() && albumName != "Unknown") {
            list = searchTracks(trackName = trackName, artistName = artistName, albumName = albumName)
        }
        if (list.isEmpty()) {
            val q = listOf(artistName, trackName).filter { it.isNotBlank() && it != "Unknown" }
                .joinToString(" ")
                .trim()
            if (q.isNotEmpty()) {
                list = searchTracks(q = q)
            }
        }

        val withId = list.mapNotNull { dto ->
            val id = dto.id ?: return@mapNotNull null
            dto to id
        }
        if (withId.isEmpty()) return LyricsResult.NotFound

        val sorted = withId
            .distinctBy { it.second }
            .sortedBy { (dto, _) ->
                abs((dto.duration ?: 0) - durationSec)
            }
            .map { it.first }

        return when (sorted.size) {
            1 -> resolveSingleSearchHit(sorted.first())
            else -> LyricsResult.MultipleCandidates(
                sorted.map { dto ->
                    LyricsSearchCandidate(
                        id = dto.id!!,
                        trackName = dto.trackName.orEmpty(),
                        artistName = dto.artistName.orEmpty(),
                        albumName = dto.albumName.orEmpty(),
                        durationSeconds = dto.duration ?: 0
                    )
                }
            )
        }
    }

    private suspend fun searchTracks(
        q: String? = null,
        trackName: String? = null,
        artistName: String? = null,
        albumName: String? = null
    ): List<LrcLibTrackDto> {
        val response = api.search(q = q, trackName = trackName, artistName = artistName, albumName = albumName)
        if (!response.isSuccessful) return emptyList()
        return response.body().orEmpty()
    }

    private suspend fun resolveSingleSearchHit(dto: LrcLibTrackDto): LyricsResult {
        val fromDto = mapDtoToResult(dto)
        if (fromDto !is LyricsResult.NotFound) return fromDto
        val id = dto.id ?: return LyricsResult.NotFound
        return fetchLyricsById(id)
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
        return app.getString(R.string.http_error, response.code())
    }
}
