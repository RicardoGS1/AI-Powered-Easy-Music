package com.virtualworld.easymusic.data.datasource

import android.app.Application
import android.app.RecoverableSecurityException
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.virtualworld.easymusic.R
import com.virtualworld.easymusic.domain.model.Video
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaStoreVideoDataSource @Inject constructor(
    private val contentResolver: ContentResolver,
    private val app: Application,
) {

    suspend fun queryVideos(): List<Video> = withContext(Dispatchers.IO) {
        val videos = mutableListOf<Video>()

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT,
        )

        val selection = "${MediaStore.Video.Media.DURATION} > 0"
        val sortOrder = "${MediaStore.Video.Media.DISPLAY_NAME} ASC"

        contentResolver.query(collection, projection, selection, null, sortOrder)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)
            val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val contentUri = ContentUris.withAppendedId(collection, id)
                val thumbnailUri = ContentUris.withAppendedId(
                    MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI,
                    id,
                )
                val displayName = cursor.getString(displayNameColumn)
                val title = cursor.getString(titleColumn)
                val resolvedTitle = resolveVideoTitle(displayName, title)

                videos.add(
                    Video(
                        id = id,
                        title = resolvedTitle,
                        duration = cursor.getLong(durationColumn),
                        uri = contentUri,
                        thumbnailUri = thumbnailUri,
                        width = cursor.getInt(widthColumn),
                        height = cursor.getInt(heightColumn),
                    ),
                )
            }
        }

        videos
    }

    suspend fun queryVideoById(videoId: Long): Video? = withContext(Dispatchers.IO) {
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }
        val uri = ContentUris.withAppendedId(collection, videoId)
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT,
        )
        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (!cursor.moveToFirst()) return@withContext null
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)
            val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)
            val id = cursor.getLong(idColumn)
            val thumbnailUri = ContentUris.withAppendedId(
                MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI,
                id,
            )
            val displayName = cursor.getString(displayNameColumn)
            val title = cursor.getString(titleColumn)
            val resolvedTitle = resolveVideoTitle(displayName, title)
            Video(
                id = id,
                title = resolvedTitle,
                duration = cursor.getLong(durationColumn),
                uri = uri,
                thumbnailUri = thumbnailUri,
                width = cursor.getInt(widthColumn),
                height = cursor.getInt(heightColumn),
            )
        }
    }

    suspend fun updateVideoTitle(
        videoId: Long,
        title: String,
        writeAccessConfirmed: Boolean = false,
    ): SongMetadataUpdateAttempt = withContext(Dispatchers.IO) {
        val uri = videoContentUri(videoId)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !writeAccessConfirmed) {
            return@withContext SongMetadataUpdateAttempt.PermissionRequired(
                MediaStore.createWriteRequest(contentResolver, listOf(uri)).intentSender,
            )
        }

        try {
            syncVideoTitleInMediaStore(uri, title)
            SongMetadataUpdateAttempt.Updated
        } catch (e: RecoverableSecurityException) {
            if (writeAccessConfirmed) {
                SongMetadataUpdateAttempt.Failed
            } else {
                SongMetadataUpdateAttempt.PermissionRequired(
                    e.userAction.actionIntent.intentSender,
                )
            }
        } catch (_: SecurityException) {
            if (writeAccessConfirmed) {
                SongMetadataUpdateAttempt.Failed
            } else {
                permissionRequiredFor(uri)
            }
        }
    }

    private fun videoContentUri(videoId: Long): Uri {
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }
        return ContentUris.withAppendedId(collection, videoId)
    }

    private fun permissionRequiredFor(uri: Uri): SongMetadataUpdateAttempt {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            SongMetadataUpdateAttempt.PermissionRequired(
                MediaStore.createWriteRequest(contentResolver, listOf(uri)).intentSender,
            )
        } else {
            SongMetadataUpdateAttempt.Failed
        }
    }

    private fun syncVideoTitleInMediaStore(uri: Uri, title: String) {
        val trimmedTitle = title.trim()
        val currentDisplayName = queryDisplayName(uri)
        val newDisplayName = titleToDisplayName(trimmedTitle, currentDisplayName)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val pending = ContentValues().apply {
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
            contentResolver.update(uri, pending, null, null)
        }

        val values = ContentValues().apply {
            put(MediaStore.Video.Media.TITLE, trimmedTitle)
            put(MediaStore.Video.Media.DISPLAY_NAME, newDisplayName)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.IS_PENDING, 0)
            }
        }
        contentResolver.update(uri, values, null, null)
        contentResolver.notifyChange(uri, null)
    }

    private fun queryDisplayName(uri: Uri): String? {
        contentResolver.query(
            uri,
            arrayOf(MediaStore.Video.Media.DISPLAY_NAME),
            null,
            null,
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val column = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                return cursor.getString(column)
            }
        }
        return null
    }

    private fun resolveVideoTitle(displayName: String?, title: String?): String {
        return when {
            !displayName.isNullOrBlank() -> displayName.substringBeforeLast('.')
            !title.isNullOrBlank() -> title
            else -> app.getString(R.string.unknown)
        }
    }

    private fun titleToDisplayName(title: String, currentDisplayName: String?): String {
        val extension = currentDisplayName
            ?.substringAfterLast('.', missingDelimiterValue = "")
            ?.takeIf { currentDisplayName.contains('.') && it.isNotEmpty() }
        return if (extension != null) "$title.$extension" else title
    }
}
