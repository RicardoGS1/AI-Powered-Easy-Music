package com.virtualworld.easymusic.data.datasource

import android.app.Application
import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.virtualworld.easymusic.domain.model.SongMetadataEdit
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.TagOptionSingleton

@Singleton
class AudioMetadataTagWriter @Inject constructor(
    private val contentResolver: ContentResolver,
    private val app: Application,
) {

    fun writeTags(uri: Uri, metadata: SongMetadataEdit): Boolean {
        val extension = resolveAudioExtension(uri) ?: run {
            Log.e(TAG, "No se pudo determinar la extensión del archivo: $uri")
            return false
        }

        TagOptionSingleton.getInstance().isAndroid = true
        val temp = File.createTempFile("easymusic_tag_", ".$extension", app.cacheDir)
        return try {
            contentResolver.openInputStream(uri)?.use { input ->
                temp.outputStream().use { output -> input.copyTo(output) }
            } ?: return false

            val audioFile = AudioFileIO.read(temp)
            val tag = audioFile.tagOrCreateAndSetDefault
            tag.setField(FieldKey.TITLE, metadata.title.trim())
            tag.setField(FieldKey.ARTIST, metadata.artist.trim())
            tag.setField(FieldKey.ALBUM, metadata.album.trim())
            audioFile.commit()

            contentResolver.openOutputStream(uri, "w")?.use { output ->
                temp.inputStream().use { input -> input.copyTo(output) }
            } ?: return false

            true
        } catch (e: Exception) {
            Log.e(TAG, "No se pudieron escribir etiquetas en el archivo (.$extension)", e)
            false
        } finally {
            temp.delete()
        }
    }

    private fun resolveAudioExtension(uri: Uri): String? {
        extensionFromDisplayName(uri.lastPathSegment)?.let { return it }

        contentResolver.query(
            uri,
            arrayOf(
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.MIME_TYPE,
            ),
            null,
            null,
            null,
        )?.use { cursor ->
            if (!cursor.moveToFirst()) return@use
            val displayNameIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)
            val mimeTypeIndex = cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE)
            if (displayNameIndex >= 0) {
                extensionFromDisplayName(cursor.getString(displayNameIndex))?.let { return it }
            }
            if (mimeTypeIndex >= 0) {
                extensionFromMimeType(cursor.getString(mimeTypeIndex))?.let { return it }
            }
        }

        return extensionFromMimeType(contentResolver.getType(uri))
    }

    private fun extensionFromDisplayName(name: String?): String? {
        if (name.isNullOrBlank()) return null
        val extension = name.substringAfterLast('.', "").lowercase()
        if (extension.isBlank() || extension == name.lowercase()) return null
        return extension
    }

    private fun extensionFromMimeType(mimeType: String?): String? {
        return when (mimeType?.lowercase()) {
            "audio/mpeg", "audio/mp3", "audio/mpeg3", "audio/x-mpeg-3" -> "mp3"
            "audio/mp4", "audio/m4a", "audio/x-m4a" -> "m4a"
            "audio/flac", "audio/x-flac" -> "flac"
            "audio/ogg", "application/ogg", "audio/x-ogg" -> "ogg"
            "audio/wav", "audio/x-wav", "audio/wave" -> "wav"
            "audio/aac", "audio/x-aac" -> "aac"
            "audio/webm" -> "webm"
            "audio/3gpp", "audio/amr" -> "3gp"
            else -> mimeType
                ?.substringAfterLast('/')
                ?.takeIf { it.isNotBlank() && !it.contains('.') }
        }
    }

    companion object {
        private const val TAG = "AudioMetadataTagWriter"
    }
}
