package com.virtualworld.easymusic.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.media3.common.Player
import com.virtualworld.easymusic.domain.model.PlaybackSession
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "music_prefs")

@Singleton
class MusicPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private companion object {
        val LAST_PLAYED_SONG_ID = longPreferencesKey("last_played_song_id")
        val EXCLUDED_SONG_IDS = stringSetPreferencesKey("excluded_song_ids")
        val FAVORITE_SONG_IDS = stringSetPreferencesKey("favorite_song_ids")
        val FAVORITE_VIDEO_IDS = stringSetPreferencesKey("favorite_video_ids")
        val SKIP_REMOVE_FROM_QUEUE_CONFIRMATION =
            booleanPreferencesKey("skip_remove_from_queue_confirmation")
        val PLAYBACK_QUEUE_IDS = stringPreferencesKey("playback_queue_ids")
        val PLAYBACK_CURRENT_INDEX = intPreferencesKey("playback_current_index")
        val PLAYBACK_POSITION_MS = longPreferencesKey("playback_position_ms")
        val PLAYBACK_SHUFFLE_ENABLED = booleanPreferencesKey("playback_shuffle_enabled")
        val PLAYBACK_REPEAT_MODE = intPreferencesKey("playback_repeat_mode")
    }

    private fun parseQueueIds(raw: String?): List<Long> {
        if (raw.isNullOrBlank()) return emptyList()
        return raw.split(',').mapNotNull { it.toLongOrNull() }
    }

    private fun encodeQueueIds(ids: List<Long>): String = ids.joinToString(",")

    suspend fun getPlaybackSession(): PlaybackSession? {
        val prefs = context.dataStore.data.first()
        val queueIds = parseQueueIds(prefs[PLAYBACK_QUEUE_IDS])
        if (queueIds.isEmpty()) return null
        return PlaybackSession(
            queueSongIds = queueIds,
            currentIndex = prefs[PLAYBACK_CURRENT_INDEX] ?: 0,
            positionMs = prefs[PLAYBACK_POSITION_MS] ?: 0L,
            shuffleEnabled = prefs[PLAYBACK_SHUFFLE_ENABLED] ?: false,
            repeatMode = prefs[PLAYBACK_REPEAT_MODE] ?: Player.REPEAT_MODE_OFF,
        )
    }

    suspend fun savePlaybackSession(session: PlaybackSession) {
        if (session.queueSongIds.isEmpty()) {
            clearPlaybackSession()
            return
        }
        context.dataStore.edit { prefs ->
            prefs[PLAYBACK_QUEUE_IDS] = encodeQueueIds(session.queueSongIds)
            prefs[PLAYBACK_CURRENT_INDEX] = session.currentIndex.coerceAtLeast(0)
            prefs[PLAYBACK_POSITION_MS] = session.positionMs.coerceAtLeast(0L)
            prefs[PLAYBACK_SHUFFLE_ENABLED] = session.shuffleEnabled
            prefs[PLAYBACK_REPEAT_MODE] = session.repeatMode
            prefs[LAST_PLAYED_SONG_ID] =
                session.queueSongIds.getOrNull(session.currentIndex.coerceIn(0, session.queueSongIds.lastIndex))
                    ?: session.queueSongIds.first()
        }
    }

    suspend fun clearPlaybackSession() {
        context.dataStore.edit { prefs ->
            prefs.remove(PLAYBACK_QUEUE_IDS)
            prefs.remove(PLAYBACK_CURRENT_INDEX)
            prefs.remove(PLAYBACK_POSITION_MS)
            prefs.remove(PLAYBACK_SHUFFLE_ENABLED)
            prefs.remove(PLAYBACK_REPEAT_MODE)
        }
    }

    fun skipRemoveFromQueueConfirmation(): Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[SKIP_REMOVE_FROM_QUEUE_CONFIRMATION] ?: false
        }

    suspend fun setSkipRemoveFromQueueConfirmation(skip: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[SKIP_REMOVE_FROM_QUEUE_CONFIRMATION] = skip
        }
    }

    fun getLastPlayedSongId(): Flow<Long?> =
        context.dataStore.data.map { prefs ->
            prefs[LAST_PLAYED_SONG_ID]
        }

    suspend fun saveLastPlayedSongId(songId: Long) {
        context.dataStore.edit { prefs ->
            prefs[LAST_PLAYED_SONG_ID] = songId
        }
    }

    suspend fun addExcludedSongId(songId: Long) {
        context.dataStore.edit { prefs ->
            val current = prefs[EXCLUDED_SONG_IDS] ?: emptySet()
            prefs[EXCLUDED_SONG_IDS] = current + songId.toString()
        }
    }

    suspend fun getExcludedSongIds(): Set<Long> {
        val strings = context.dataStore.data.first()[EXCLUDED_SONG_IDS] ?: emptySet()
        return strings.mapNotNull { it.toLongOrNull() }.toSet()
    }

    fun excludedSongIds(): Flow<Set<Long>> =
        context.dataStore.data.map { prefs ->
            prefs[EXCLUDED_SONG_IDS]?.mapNotNull { it.toLongOrNull() }?.toSet() ?: emptySet()
        }

    fun favoriteSongIds(): Flow<Set<Long>> =
        context.dataStore.data.map { prefs ->
            prefs[FAVORITE_SONG_IDS]?.mapNotNull { it.toLongOrNull() }?.toSet() ?: emptySet()
        }

    suspend fun toggleFavoriteSongId(songId: Long) {
        context.dataStore.edit { prefs ->
            val current = prefs[FAVORITE_SONG_IDS] ?: emptySet()
            val key = songId.toString()
            prefs[FAVORITE_SONG_IDS] = if (key in current) current - key else current + key
        }
    }

    suspend fun removeFavoriteSongId(songId: Long) {
        context.dataStore.edit { prefs ->
            val current = prefs[FAVORITE_SONG_IDS] ?: emptySet()
            prefs[FAVORITE_SONG_IDS] = current - songId.toString()
        }
    }

    fun favoriteVideoIds(): Flow<Set<Long>> =
        context.dataStore.data.map { prefs ->
            prefs[FAVORITE_VIDEO_IDS]?.mapNotNull { it.toLongOrNull() }?.toSet() ?: emptySet()
        }

    suspend fun toggleFavoriteVideoId(videoId: Long) {
        context.dataStore.edit { prefs ->
            val current = prefs[FAVORITE_VIDEO_IDS] ?: emptySet()
            val id = videoId.toString()
            prefs[FAVORITE_VIDEO_IDS] = if (id in current) current - id else current + id
        }
    }
}
