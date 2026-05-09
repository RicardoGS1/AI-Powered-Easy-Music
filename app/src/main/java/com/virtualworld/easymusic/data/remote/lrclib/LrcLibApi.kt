package com.virtualworld.easymusic.data.remote.lrclib

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
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

    @GET("api/get/{id}")
    suspend fun getLyricsById(
        @Path("id") id: Long
    ): Response<LrcLibTrackDto>

    @GET("api/search")
    suspend fun search(
        @Query("q") q: String? = null,
        @Query("track_name") trackName: String? = null,
        @Query("artist_name") artistName: String? = null,
        @Query("album_name") albumName: String? = null
    ): Response<List<LrcLibTrackDto>>
}
