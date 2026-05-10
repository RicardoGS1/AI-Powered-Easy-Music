package com.virtualworld.easymusic.domain.model

import com.google.gson.annotations.SerializedName

/**
 * Respuesta JSON esperada del modelo (metadatos locales; el texto es orientativo).
 */
data class SongAiInsight(
    @SerializedName("resumen") val resumen: String? = null,
    @SerializedName("genero_o_estilo") val generoOEstilo: String? = null,
    @SerializedName("epoca_o_contexto") val epocaOContexto: String? = null,
    @SerializedName("dato_curioso") val datoCurioso: String? = null,
    @SerializedName("artistas_o_temas_similares") val artistasOTemasSimilares: List<String>? = null
)

sealed class SongInsightResult {
    data class Success(val insight: SongAiInsight) : SongInsightResult()
    data class Error(val message: String) : SongInsightResult()
}
