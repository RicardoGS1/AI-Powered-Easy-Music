package com.virtualworld.easymusic.firebase

/**
 * Claves definidas en Firebase Remote Config (consola).
 * Deben coincidir con los parámetros creados en Firebase.
 */
object RemoteConfigKeys {
    /** Si es false, se oculta la IA (Gemini) y no se llama a la API. */
    const val ENABLE_AI_INSIGHT = "enable_ai_insight"
}
