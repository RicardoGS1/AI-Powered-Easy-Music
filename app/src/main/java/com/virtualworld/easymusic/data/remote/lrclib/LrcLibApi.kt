package com.virtualworld.easymusic.data.remote.lrclib

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface LrcLibApi {

    @GET("api/get-cached")
    suspend fun getLyricsCached(
        @Query("track_name") trackName: String,
        @Query("artist_name") artistName: String,
        @Query("album_name") albumName: String,
        @Query("duration") durationSeconds: Int
    ): Response<LrcLibTrackDto>

    @GET("api/get")
    suspend fun getLyrics(
        @Query("track_name") trackName: String,
        @Query("artist_name") artistName: String,
        @Query("album_name") albumName: String,
        @Query("duration") durationSeconds: Int
    ): Response<LrcLibTrackDto>
}
