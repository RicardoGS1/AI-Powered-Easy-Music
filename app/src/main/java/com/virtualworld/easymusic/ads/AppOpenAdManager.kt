package com.virtualworld.easymusic.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.virtualworld.easymusic.BuildConfig
import java.util.Date
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AppOpenAdManager(context: Context) {

    private val appContext = context.applicationContext
    private val mainScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    var isShowingAd = false
        private set

    private var loadTime: Long = 0
    private var pendingOnShowAdCompleteListener: OnShowAdCompleteListener? = null

    fun loadAd() {
        if (isLoadingAd || isAdAvailable()) {
            return
        }

        isLoadingAd = true
        AppOpenAd.load(
            appContext,
            BuildConfig.APP_OPEN_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    Log.d(TAG, "App open ad loaded.")
                    appOpenAd = ad
                    isLoadingAd = false
                    loadTime = Date().time
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.d(TAG, "App open ad failed to load: ${loadAdError.message}")
                    isLoadingAd = false
                }
            },
        )
    }

    fun showStartupAdIfAvailable(
        activity: Activity,
        enabled: Boolean,
        waitMs: Long,
        onShowAdCompleteListener: OnShowAdCompleteListener,
    ) {
        mainScope.launch {
            if (!enabled) {
                Log.d(TAG, "App open ad disabled by remote config.")
                onShowAdCompleteListener.onShowAdComplete()
                return@launch
            }

            if (!isLoadingAd && !isAdAvailable()) {
                loadAd()
            }

            val deadline = System.currentTimeMillis() + waitMs
            while (!isAdAvailable() && System.currentTimeMillis() < deadline) {
                delay(POLL_INTERVAL_MS)
            }

            if (isAdAvailable()) {
                showAdIfAvailable(activity, onShowAdCompleteListener)
            } else {
                Log.d(TAG, "App open ad not available after waiting ${waitMs}ms.")
                onShowAdCompleteListener.onShowAdComplete()
            }
        }
    }

    fun showAdIfAvailable(
        activity: Activity,
        onShowAdCompleteListener: OnShowAdCompleteListener = OnShowAdCompleteListener {},
    ) {
        if (isShowingAd) {
            Log.d(TAG, "The app open ad is already showing; queuing completion listener.")
            pendingOnShowAdCompleteListener = mergeListeners(
                pendingOnShowAdCompleteListener,
                onShowAdCompleteListener,
            )
            return
        }

        if (!isAdAvailable()) {
            Log.d(TAG, "The app open ad is not ready yet.")
            onShowAdCompleteListener.onShowAdComplete()
            loadAd()
            return
        }

        val listener = onShowAdCompleteListener
        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Ad dismissed fullscreen content.")
                appOpenAd = null
                isShowingAd = false
                notifyShowAdComplete(listener)
                loadAd()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.d(TAG, "Ad failed to show: ${adError.message}")
                appOpenAd = null
                isShowingAd = false
                notifyShowAdComplete(listener)
                loadAd()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Ad showed fullscreen content.")
            }
        }

        isShowingAd = true
        appOpenAd?.show(activity)
    }

    private fun notifyShowAdComplete(primary: OnShowAdCompleteListener) {
        primary.onShowAdComplete()
        pendingOnShowAdCompleteListener?.let { pending ->
            pendingOnShowAdCompleteListener = null
            pending.onShowAdComplete()
        }
    }

    private fun mergeListeners(
        existing: OnShowAdCompleteListener?,
        incoming: OnShowAdCompleteListener,
    ): OnShowAdCompleteListener {
        if (existing == null) return incoming
        return OnShowAdCompleteListener {
            existing.onShowAdComplete()
            incoming.onShowAdComplete()
        }
    }

    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
        val dateDifference = Date().time - loadTime
        val numMilliSecondsPerHour = 3_600_000L
        return dateDifference < numMilliSecondsPerHour * numHours
    }

    private fun isAdAvailable(): Boolean {
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)
    }

    fun interface OnShowAdCompleteListener {
        fun onShowAdComplete()
    }

    companion object {
        private const val TAG = "AppOpenAdManager"
        private const val POLL_INTERVAL_MS = 200L
    }
}
