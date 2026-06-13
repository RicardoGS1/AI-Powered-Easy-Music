package com.virtualworld.easymusic

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.virtualworld.easymusic.ads.AppOpenAdManager
import com.virtualworld.easymusic.di.FirebaseBootstrapEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class EasyMusicApp :
    Application(),
    Application.ActivityLifecycleCallbacks,
    DefaultLifecycleObserver {

    lateinit var appOpenAdManager: AppOpenAdManager
        private set

    private var currentActivity: Activity? = null
    private var isMobileAdsInitialized = false
    private var isColdStartCompleted = false
    private val adManagerReadyListeners = mutableListOf<(AppOpenAdManager) -> Unit>()

    override fun onCreate() {
        super<Application>.onCreate()
        EntryPointAccessors.fromApplication(this, FirebaseBootstrapEntryPoint::class.java)
            .firebaseBootstrap()
            .start()

        if (BuildConfig.DEBUG) {
            MobileAds.setRequestConfiguration(
                RequestConfiguration.Builder()
                    .setTestDeviceIds(listOf(AdRequest.DEVICE_ID_EMULATOR))
                    .build(),
            )
        }

        registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        appOpenAdManager = AppOpenAdManager(this)
        val remoteConfigValues = EntryPointAccessors.fromApplication(
            this,
            FirebaseBootstrapEntryPoint::class.java,
        ).remoteConfigValues()
        MobileAds.initialize(this) {
            isMobileAdsInitialized = true
            if (remoteConfigValues.isAppOpenEnabled()) {
                appOpenAdManager.loadAd()
            }
            synchronized(adManagerReadyListeners) {
                adManagerReadyListeners.forEach { it(appOpenAdManager) }
                adManagerReadyListeners.clear()
            }
        }
    }

    fun markColdStartCompleted() {
        isColdStartCompleted = true
    }

    fun runWhenAppOpenAdManagerReady(listener: (AppOpenAdManager) -> Unit) {
        if (::appOpenAdManager.isInitialized && isMobileAdsInitialized) {
            listener(appOpenAdManager)
        } else {
            synchronized(adManagerReadyListeners) {
                adManagerReadyListeners.add(listener)
            }
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        if (!isColdStartCompleted) return
        val remoteConfigValues = EntryPointAccessors.fromApplication(
            this,
            FirebaseBootstrapEntryPoint::class.java,
        ).remoteConfigValues()
        if (!remoteConfigValues.isAppOpenEnabled()) return
        currentActivity?.let { activity ->
            appOpenAdManager.showAdIfAvailable(activity)
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit

    override fun onActivityStarted(activity: Activity) {
        if (!appOpenAdManager.isShowingAd) {
            currentActivity = activity
        }
    }

    override fun onActivityResumed(activity: Activity) = Unit

    override fun onActivityPaused(activity: Activity) = Unit

    override fun onActivityStopped(activity: Activity) = Unit

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

    override fun onActivityDestroyed(activity: Activity) = Unit
}
