package com.virtualworld.easymusic.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
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
}
