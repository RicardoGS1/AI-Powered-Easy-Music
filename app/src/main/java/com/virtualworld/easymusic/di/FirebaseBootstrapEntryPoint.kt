package com.virtualworld.easymusic.di

import com.virtualworld.easymusic.firebase.FirebaseBootstrap
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface FirebaseBootstrapEntryPoint {
    fun firebaseBootstrap(): FirebaseBootstrap
}
