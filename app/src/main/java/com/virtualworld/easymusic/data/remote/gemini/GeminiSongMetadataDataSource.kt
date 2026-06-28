package com.virtualworld.easymusic.data.remote.gemini

import android.app.Application
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.SerializedName
import com.virtualworld.easymusic.R
import com.virtualworld.easymusic.domain.model.Song
import com.virtualworld.easymusic.domain.model.SongMetadataLookupResult
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

private data class GeminiSongMetadataResponse(
    @SerializedName("found") val found: Boolean? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("artist") val artist: String? = null,
    @SerializedName("album") val album: String? = null,
)

@Singleton
class GeminiSongMetadataDataSource @Inject constructor(
    private val app: Application,
    private val httpClient: OkHttpClient,
    private val gson: Gson,
    @param:Named("gemini_api_key") private val apiKey: String,
) {

    suspend fun fetchCorrectedMetadata(song: Song): SongMetadataLookupResult = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext SongMetadataLookupResult.Error(
                app.getString(R.string.gemini_key_missing),
            )
        }
        val durationSec = (song.duration / 1000L).coerceAtLeast(0L)
        val prompt = """
            You receive metadata from a local music file (it may be wrong or incomplete):
            - Title: ${song.title}
            - Artist: ${song.artist}
            - Album: ${song.album}
            - Approx. duration: ${durationSec}s

            Identify the best-matching well-known commercial recording if you are confident.
            Reply ONLY with valid JSON (no markdown) using exactly these keys:
            {
              "found": true or false,
              "title": "string",
              "artist": "string",
              "album": "string"
            }

            Rules:
            - Set "found" to true only when you are reasonably confident about title, artist, and album.
            - If unsure, set "found" to false and leave title/artist/album as empty strings.
            - Use the official/common spelling for title, artist, and album (Latin script when applicable).
            - Do not invent data; prefer empty strings over guesses when "found" is false.
        """.trimIndent()

        val geminiBody = GeminiGenerateRequest(
            contents = listOf(
                GeminiContentBlock(parts = listOf(GeminiTextPart(prompt))),
            ),
            generationConfig = GeminiGenerationConfig(temperature = 0.2f, maxOutputTokens = 256),
        )

        val url = try {
            HttpUrl.Builder()
                .scheme("https")
                .host("generativelanguage.googleapis.com")
                .addPathSegment("v1beta")
                .addPathSegment("models")
                .addEncodedPathSegment("${GeminiSongInsightDataSource.GEMINI_MODEL}:generateContent")
                .build()
        } catch (e: Exception) {
            Log.e(TAG, "URL Gemini inválida", e)
            return@withContext SongMetadataLookupResult.Error(app.getString(R.string.error_building_request))
        }

        return@withContext try {
            val json = gson.toJson(geminiBody)
            val request = Request.Builder()
                .url(url)
                .post(json.toRequestBody(JSON_MEDIA))
                .header("Content-Type", "application/json; charset=utf-8")
                .header("x-goog-api-key", apiKey)
                .build()

            httpClient.newCall(request).execute().use { resp ->
                val raw = resp.body?.string().orEmpty()
                if (!resp.isSuccessful) {
                    Log.e(TAG, "HTTP ${resp.code} — cuerpo: ${raw.take(800)}")
                    return@use SongMetadataLookupResult.Error(
                        app.getString(R.string.network_error),
                    )
                }
                val parsed = try {
                    gson.fromJson(raw, GeminiGenerateResponse::class.java)
                } catch (e: JsonSyntaxException) {
                    Log.e(TAG, "JSON de respuesta inválido", e)
                    return@use SongMetadataLookupResult.Error(app.getString(R.string.api_response_unrecognized))
                }
                parsed.error?.message?.let { msg ->
                    return@use SongMetadataLookupResult.Error(msg)
                }
                val text = parsed.candidates
                    ?.firstOrNull()
                    ?.content
                    ?.parts
                    ?.firstOrNull()
                    ?.text
                    ?.trim()
                if (text.isNullOrEmpty()) {
                    return@use SongMetadataLookupResult.Error(app.getString(R.string.api_no_usable_text))
                }
                val metadata = try {
                    gson.fromJson(text.stripMarkdownJsonFence(), GeminiSongMetadataResponse::class.java)
                } catch (_: JsonSyntaxException) {
                    return@use SongMetadataLookupResult.Error(app.getString(R.string.json_parse_error))
                }
                if (metadata.found != true) {
                    return@use SongMetadataLookupResult.NotFound(
                        app.getString(R.string.metadata_ai_not_found),
                    )
                }
                val title = metadata.title?.trim().orEmpty()
                val artist = metadata.artist?.trim().orEmpty()
                val album = metadata.album?.trim().orEmpty()
                if (title.isEmpty() || artist.isEmpty() || album.isEmpty()) {
                    return@use SongMetadataLookupResult.NotFound(
                        app.getString(R.string.metadata_ai_not_found),
                    )
                }
                SongMetadataLookupResult.Success(title = title, artist = artist, album = album)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fallo de red o ejecución", e)
            SongMetadataLookupResult.Error(app.getString(R.string.network_error))
        }
    }

    companion object {
        private const val TAG = "EasyMusicGeminiMeta"
        private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()
    }
}

private fun String.stripMarkdownJsonFence(): String {
    var s = trim()
    if (!s.startsWith("```")) return s
    s = s.removePrefix("```json").removePrefix("```").trim()
    val end = s.lastIndexOf("```")
    if (end >= 0) s = s.substring(0, end).trim()
    return s
}
