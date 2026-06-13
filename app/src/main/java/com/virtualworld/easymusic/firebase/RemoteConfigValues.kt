package com.virtualworld.easymusic.firebase

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteConfigValues @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig,
) {
    fun isAiInsightEnabled(): Boolean =
        remoteConfig.getBoolean(RemoteConfigKeys.ENABLE_AI_INSIGHT)

    fun isAppOpenEnabled(): Boolean =
        remoteConfig.getBoolean(RemoteConfigKeys.ENABLE_APP_OPEN)

    fun getAppOpenLoadWaitMs(): Long =
        remoteConfig.getLong(RemoteConfigKeys.APP_OPEN_LOAD_WAIT_MS)
            .coerceIn(MIN_APP_OPEN_WAIT_MS, MAX_APP_OPEN_WAIT_MS)

    companion object {
        const val DEFAULT_APP_OPEN_LOAD_WAIT_MS = 10_000L
        private const val MIN_APP_OPEN_WAIT_MS = 1_000L
        private const val MAX_APP_OPEN_WAIT_MS = 30_000L
    }
}
