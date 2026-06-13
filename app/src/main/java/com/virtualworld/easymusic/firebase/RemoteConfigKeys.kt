package com.virtualworld.easymusic.firebase

/**
 * Claves definidas en Firebase Remote Config (consola).
 * Deben coincidir con los parámetros creados en Firebase.
 */
object RemoteConfigKeys {
    /** Si es false, se oculta la IA (Gemini) y no se llama a la API. */
    const val ENABLE_AI_INSIGHT = "enable_ai_insight"

    /** Si es false, no se carga ni muestra el anuncio App Open al abrir la app. */
    const val ENABLE_APP_OPEN = "enable_app_open"

    /** Tiempo máximo de espera (ms) para cargar el App Open antes de navegar a la pantalla principal. */
    const val APP_OPEN_LOAD_WAIT_MS = "app_open_load_wait_ms"
}
