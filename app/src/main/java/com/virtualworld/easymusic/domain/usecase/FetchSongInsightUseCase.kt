package com.virtualworld.easymusic.domain.usecase

import android.app.Application
import com.virtualworld.easymusic.R
import com.virtualworld.easymusic.data.remote.gemini.GeminiSongInsightDataSource
import com.virtualworld.easymusic.domain.model.Song
import com.virtualworld.easymusic.domain.model.SongInsightResult
import com.virtualworld.easymusic.firebase.RemoteConfigValues
import javax.inject.Inject

class FetchSongInsightUseCase @Inject constructor(
    private val app: Application,
    private val dataSource: GeminiSongInsightDataSource,
    private val remoteConfigValues: RemoteConfigValues,
) {
    suspend operator fun invoke(song: Song): SongInsightResult {
        if (!remoteConfigValues.isAiInsightEnabled()) {
            return SongInsightResult.Error(
                app.getString(R.string.ai_not_available),
            )
        }
        return dataSource.fetchInsight(song)
    }
}
