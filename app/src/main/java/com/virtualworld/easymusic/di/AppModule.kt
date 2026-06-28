package com.virtualworld.easymusic.di

import android.content.ContentResolver
import android.content.Context
import com.virtualworld.easymusic.data.repository.MusicRepositoryImpl
import com.virtualworld.easymusic.data.repository.VideoRepositoryImpl
import com.virtualworld.easymusic.domain.repository.MusicRepository
import com.virtualworld.easymusic.domain.repository.VideoRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindMusicRepository(impl: MusicRepositoryImpl): MusicRepository

    @Binds
    @Singleton
    abstract fun bindVideoRepository(impl: VideoRepositoryImpl): VideoRepository

    companion object {
        @Provides
        @Singleton
        fun provideContentResolver(@ApplicationContext context: Context): ContentResolver {
            return context.contentResolver
        }
    }
}
