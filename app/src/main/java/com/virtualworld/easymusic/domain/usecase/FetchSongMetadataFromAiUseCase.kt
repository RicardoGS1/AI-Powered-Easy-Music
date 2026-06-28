package com.virtualworld.easymusic.domain.usecase

import android.app.Application
import com.virtualworld.easymusic.R
import com.virtualworld.easymusic.data.remote.gemini.GeminiSongMetadataDataSource
import com.virtualworld.easymusic.domain.model.Song
import com.virtualworld.easymusic.domain.model.SongMetadataLookupResult
import com.virtualworld.easymusic.firebase.RemoteConfigValues
import javax.inject.Inject

class FetchSongMetadataFromAiUseCase @Inject constructor(
    private val app: Application,
    private val dataSource: GeminiSongMetadataDataSource,
    private val remoteConfigValues: RemoteConfigValues,
) {
    suspend operator fun invoke(song: Song): SongMetadataLookupResult {
        if (!remoteConfigValues.isAiInsightEnabled()) {
            return SongMetadataLookupResult.Error(
                app.getString(R.string.ai_not_available),
            )
        }
        return dataSource.fetchCorrectedMetadata(song)
    }
}
