package com.virtualworld.easymusic.ui.splash

import androidx.lifecycle.ViewModel
import com.virtualworld.easymusic.firebase.RemoteConfigValues
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val remoteConfigValues: RemoteConfigValues,
) : ViewModel() {
    fun isAppOpenEnabled(): Boolean = remoteConfigValues.isAppOpenEnabled()

    fun getAppOpenLoadWaitMs(): Long = remoteConfigValues.getAppOpenLoadWaitMs()
}
