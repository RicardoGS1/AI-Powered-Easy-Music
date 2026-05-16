package com.virtualworld.easymusic.firebase

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.virtualworld.easymusic.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseBootstrap @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig,
) {
    fun start() {
        val settings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(
                if (BuildConfig.DEBUG) 0 else 43_200,
            )
            .build()
        remoteConfig.setConfigSettingsAsync(settings)

        val defaults = mapOf<String, Any>(
            // Por defecto la IA activa; en consola puedes poner false para apagarla sin actualizar la app.
            RemoteConfigKeys.ENABLE_AI_INSIGHT to true,
        )
        remoteConfig.setDefaultsAsync(defaults)
        remoteConfig.fetchAndActivate()
    }
}
