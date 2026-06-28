package com.virtualworld.easymusic.domain.model

import android.content.IntentSender

sealed class UpdateVideoTitleResult {
    data class Success(val updatedVideo: Video) : UpdateVideoTitleResult()
    data class NeedsWritePermission(val intentSender: IntentSender) : UpdateVideoTitleResult()
    data class Error(val message: String) : UpdateVideoTitleResult()
}
