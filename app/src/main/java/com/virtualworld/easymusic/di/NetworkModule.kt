package com.virtualworld.easymusic.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.virtualworld.easymusic.data.remote.lrclib.LrcLibApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val LRCLIB_BASE_URL = "https://lrclib.net/"

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().create()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header(
                    "User-Agent",
                    "EasyMusic/1.0 (https://lrclib.net) Android"
                )
                .build()
            chain.proceed(request)
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideLrcLibApi(client: OkHttpClient, gson: Gson): LrcLibApi =
        Retrofit.Builder()
            .baseUrl(LRCLIB_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(LrcLibApi::class.java)
}
