package com.virtualworld.easymusic

import android.app.Application
import com.virtualworld.easymusic.di.FirebaseBootstrapEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class EasyMusicApp : Application() {

    override fun onCreate() {
        super.onCreate()
        EntryPointAccessors.fromApplication(this, FirebaseBootstrapEntryPoint::class.java)
            .firebaseBootstrap()
            .start()
    }
}
