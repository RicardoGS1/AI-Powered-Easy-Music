package com.virtualworld.easymusic.data.datasource

import android.app.Application
import android.content.ContentResolver
import android.content.ContentUris
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
                val resolvedTitle = when {
                    !title.isNullOrBlank() -> title
                    !displayName.isNullOrBlank() -> displayName.substringBeforeLast('.')
                    else -> app.getString(R.string.unknown)
                }

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
}
