package com.virtualworld.easymusic.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
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
}
