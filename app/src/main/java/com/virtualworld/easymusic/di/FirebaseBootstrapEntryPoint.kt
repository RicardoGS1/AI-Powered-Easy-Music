package com.virtualworld.easymusic.di

import com.virtualworld.easymusic.firebase.FirebaseBootstrap
import com.virtualworld.easymusic.firebase.RemoteConfigValues
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface FirebaseBootstrapEntryPoint {
    fun firebaseBootstrap(): FirebaseBootstrap

    fun remoteConfigValues(): RemoteConfigValues
}
