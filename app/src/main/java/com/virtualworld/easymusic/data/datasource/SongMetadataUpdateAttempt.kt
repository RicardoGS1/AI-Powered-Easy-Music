package com.virtualworld.easymusic.data.datasource

import android.content.IntentSender

sealed class SongMetadataUpdateAttempt {
    data object Updated : SongMetadataUpdateAttempt()
    data class PermissionRequired(val intentSender: IntentSender) : SongMetadataUpdateAttempt()
    data object Failed : SongMetadataUpdateAttempt()
}
