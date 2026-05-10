package com.virtualworld.easymusic.data.remote.gemini

import android.util.Log
import com.google.gson.Gson
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

@Singleton
class GeminiSongInsightDataSource @Inject constructor(
    private val httpClient: OkHttpClient,
    private val gson: Gson,
    @param:Named("gemini_api_key") private val apiKey: String
) {

    suspend fun fetchInsight(song: Song): SongInsightResult = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext SongInsightResult.Error(
                "Añade GEMINI_API_KEY en local.properties (clave de Google AI Studio) y vuelve a compilar."
            )
        }
        val durationSec = (song.duration / 1000L).coerceAtLeast(0L)
        val prompt = """
            Tienes solo estos metadatos de un archivo de música local (pueden ser imprecisos):
            - Título: ${song.title}
            - Artista: ${song.artist}
            - Álbum: ${song.album}
            - Duración aproximada: ${durationSec}s

            Identifica la canción conocida que mejor encaje con esos metadatos y di el artista, el album y el titulo en el resumen.

            Responde ÚNICAMENTE con un JSON válido (sin markdown) con estas claves exactas:
            {
              "resumen": "string",
              "genero_o_estilo": "string",
              "epoca_o_contexto": "string",
              "dato_curioso": "string",
              "artistas_o_temas_similares": ["string", ...]
            }
            Texto en español. Máximo ~140 palabras en total repartidas entre campos. Si no hay datos, usa cadenas breves tipo "No disponible".
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
                .addQueryParameter("key", apiKey)
                .build()
        } catch (e: Exception) {
            Log.e(TAG, "URL Gemini inválida", e)
            return@withContext SongInsightResult.Error("Error interno al construir la petición.")
        }

        Log.d(TAG, "POST " + url.newBuilder().setQueryParameter("key", "…").build())

        return@withContext try {
            val json = gson.toJson(geminiBody)
            val request = Request.Builder()
                .url(url)
                .post(json.toRequestBody(JSON_MEDIA))
                .header("Content-Type", "application/json; charset=utf-8")
                .build()

            httpClient.newCall(request).execute().use { resp ->
                val raw = resp.body?.string().orEmpty()
                if (!resp.isSuccessful) {
                    Log.e(TAG, "HTTP ${resp.code} ${resp.message} — cuerpo: ${raw.take(800)}")
                    val apiMsg = parseApiErrorMessage(raw)
                    return@use SongInsightResult.Error(
                        apiMsg ?: "HTTP ${resp.code}: ${resp.message}"
                    )
                }
                val parsed = try {
                    gson.fromJson(raw, GeminiGenerateResponse::class.java)
                } catch (e: JsonSyntaxException) {
                    Log.e(TAG, "JSON de respuesta inválido: ${raw.take(500)}", e)
                    return@use SongInsightResult.Error("Respuesta de la API no reconocida.")
                }
                parsed.error?.message?.let { msg ->
                    return@use SongInsightResult.Error(msg)
                }
                val text = parsed.candidates
                    ?.firstOrNull()
                    ?.content
                    ?.parts
                    ?.firstOrNull()
                    ?.text
                    ?.trim()
                if (text.isNullOrEmpty()) {
                    Log.w(TAG, "Sin candidatos: ${raw.take(600)}")
                    return@use SongInsightResult.Error("La API no devolvió texto utilizable.")
                }
                val jsonPayload = text.stripMarkdownJsonFence()
                val insight = try {
                    gson.fromJson(jsonPayload, SongAiInsight::class.java)
                } catch (_: JsonSyntaxException) {
                    return@use SongInsightResult.Error("No se pudo interpretar la respuesta JSON del modelo.")
                }
                SongInsightResult.Success(insight)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fallo de red o ejecución", e)
            SongInsightResult.Error(e.message ?: "Error de red o del servicio.")
        }
    }

    private fun parseApiErrorMessage(raw: String): String? = try {
        gson.fromJson(raw, GeminiGenerateResponse::class.java)?.error?.message
    } catch (_: Exception) {
        null
    }

    companion object {
        private const val TAG = "EasyMusicGemini"

        /** Modelo ligero actual (2.0 flash-lite ya no se ofrece a cuentas nuevas). */
        const val GEMINI_MODEL = "gemini-2.5-flash-lite"

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
