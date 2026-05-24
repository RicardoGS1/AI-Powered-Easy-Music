package com.virtualworld.easymusic.data.remote.gemini

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.google.gson.Gson
import com.virtualworld.easymusic.R
import com.google.gson.JsonSyntaxException
import com.virtualworld.easymusic.domain.model.Song
import com.virtualworld.easymusic.domain.model.SongAiInsight
import com.virtualworld.easymusic.domain.model.SongInsightResult
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
import java.util.Locale

@Singleton
class GeminiSongInsightDataSource @Inject constructor(
    private val app: Application,
    private val httpClient: OkHttpClient,
    private val gson: Gson,
    @param:Named("gemini_api_key") private val apiKey: String
) {

    suspend fun fetchInsight(song: Song): SongInsightResult = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext SongInsightResult.Error(
                app.getString(R.string.gemini_key_missing)
            )
        }
        val durationSec = (song.duration / 1000L).coerceAtLeast(0L)
        val appLocale = app.resolveAppLocaleForContent()
        val languageTag = appLocale.toLanguageTag()
        val languageLabelEn = appLocale.getDisplayLanguage(Locale.ENGLISH).ifBlank { languageTag }
        val emptyFieldHint = app.getString(R.string.no_data).trim()
        val prompt = """
            You only have these metadata fields from a local music file (they may be inaccurate):
            - Title: ${song.title}
            - Artist: ${song.artist}
            - Album: ${song.album}
            - Approx. duration: ${durationSec}s

            Identify the best-matching well-known song and mention artist, album, and title in the summary.

            Reply ONLY with valid JSON (no markdown) using exactly these keys:
            {
              "resumen": "string",
              "genero_o_estilo": "string",
              "epoca_o_contexto": "string",
              "dato_curioso": "string",
              "artistas_o_temas_similares": ["string", ...]
            }

            Rules:
            - Keep the JSON property names exactly as shown (do not translate keys).
            - Write every human-readable string VALUE in $languageLabelEn (BCP-47: $languageTag), with natural phrasing for that language.
            - About 140 words total across all string fields.
            - If some information is missing, use a short placeholder like "$emptyFieldHint".
        """.trimIndent()

        val geminiBody = GeminiGenerateRequest(
            contents = listOf(
                GeminiContentBlock(
                    parts = listOf(GeminiTextPart(prompt))
                )
            ),
            generationConfig = GeminiGenerationConfig()
        )

        val url = try {
            HttpUrl.Builder()
                .scheme("https")
                .host("generativelanguage.googleapis.com")
                .addPathSegment("v1beta")
                .addPathSegment("models")
                .addEncodedPathSegment("$GEMINI_MODEL:generateContent")
                .build()
        } catch (e: Exception) {
            Log.e(TAG, "URL Gemini inválida", e)
            return@withContext SongInsightResult.Error(app.getString(R.string.error_building_request))
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
                    Log.e(TAG, "HTTP ${resp.code} ${resp.message} — cuerpo: ${redactSecrets(raw).take(800)}")
                    val apiMsg = parseApiErrorMessage(raw)?.let(::redactSecrets)
                    return@use SongInsightResult.Error(
                        apiMsg ?: "HTTP ${resp.code}: ${resp.message}"
                    )
                }
                val parsed = try {
                    gson.fromJson(raw, GeminiGenerateResponse::class.java)
                } catch (e: JsonSyntaxException) {
                    Log.e(TAG, "JSON de respuesta inválido: ${redactSecrets(raw).take(500)}", e)
                    return@use SongInsightResult.Error(app.getString(R.string.api_response_unrecognized))
                }
                parsed.error?.message?.let { msg ->
                    return@use SongInsightResult.Error(redactSecrets(msg))
                }
                val text = parsed.candidates
                    ?.firstOrNull()
                    ?.content
                    ?.parts
                    ?.firstOrNull()
                    ?.text
                    ?.trim()
                if (text.isNullOrEmpty()) {
                    Log.w(TAG, "Sin candidatos: ${redactSecrets(raw).take(600)}")
                    return@use SongInsightResult.Error(app.getString(R.string.api_no_usable_text))
                }
                val jsonPayload = text.stripMarkdownJsonFence()
                val insight = try {
                    gson.fromJson(jsonPayload, SongAiInsight::class.java)
                } catch (_: JsonSyntaxException) {
                    return@use SongInsightResult.Error(app.getString(R.string.json_parse_error))
                }
                SongInsightResult.Success(insight)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fallo de red o ejecución", e)
            SongInsightResult.Error(app.getString(R.string.network_error))
        }
    }

    /** Evita filtrar la clave en pantalla o en logs si aparece en URLs o textos de error. */
    private fun redactSecrets(text: String): String {
        var sanitized = text
        if (apiKey.isNotBlank()) {
            sanitized = sanitized.replace(apiKey, "[REDACTED]")
        }
        sanitized = API_KEY_IN_URL.replace(sanitized, "key=[REDACTED]")
        sanitized = GOOGLE_API_KEY_PATTERN.replace(sanitized, "[REDACTED]")
        return sanitized
    }

    private fun parseApiErrorMessage(raw: String): String? = try {
        gson.fromJson(raw, GeminiGenerateResponse::class.java)?.error?.message
    } catch (_: Exception) {
        null
    }

    companion object {
        private const val TAG = "EasyMusicGemini"
        private val API_KEY_IN_URL = Regex("(?i)([?&]key=)[^&\\s\"']+")
        private val GOOGLE_API_KEY_PATTERN = Regex("AIza[0-9A-Za-z_-]{20,}")

        /** Modelo ligero actual (2.0 flash-lite ya no se ofrece a cuentas nuevas). */
        const val GEMINI_MODEL = "gemini-2.5-flash-lite"

        private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()
    }
}

private fun Application.resolveAppLocaleForContent(): Locale {
    val applied = AppCompatDelegate.getApplicationLocales()
    if (!applied.isEmpty) {
        return applied[0] ?: Locale.getDefault()
    }
    val locales = resources.configuration.locales
    return if (locales.size() > 0) locales[0] else Locale.getDefault()
}

private fun String.stripMarkdownJsonFence(): String {
    var s = trim()
    if (!s.startsWith("```")) return s
    s = s.removePrefix("```json").removePrefix("```").trim()
    val end = s.lastIndexOf("```")
    if (end >= 0) s = s.substring(0, end).trim()
    return s
}
