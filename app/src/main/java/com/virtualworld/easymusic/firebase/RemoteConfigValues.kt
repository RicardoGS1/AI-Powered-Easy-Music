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
}
