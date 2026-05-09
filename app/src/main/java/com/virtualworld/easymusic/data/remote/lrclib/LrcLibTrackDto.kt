package com.virtualworld.easymusic.data.remote.lrclib

import com.google.gson.annotations.SerializedName

data class LrcLibTrackDto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("trackName") val trackName: String? = null,
    @SerializedName("artistName") val artistName: String? = null,
    @SerializedName("albumName") val albumName: String? = null,
    @SerializedName("duration") val duration: Int? = null,
    @SerializedName("instrumental") val instrumental: Boolean? = null,
    @SerializedName("plainLyrics") val plainLyrics: String? = null,
    @SerializedName("syncedLyrics") val syncedLyrics: String? = null
)
