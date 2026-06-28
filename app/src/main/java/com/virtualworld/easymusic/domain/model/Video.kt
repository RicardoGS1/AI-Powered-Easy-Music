package com.virtualworld.easymusic.domain.model

import android.net.Uri

data class Video(
    val id: Long,
    val title: String,
    val duration: Long,
    val uri: Uri,
    val thumbnailUri: Uri?,
    val width: Int,
    val height: Int,
)
