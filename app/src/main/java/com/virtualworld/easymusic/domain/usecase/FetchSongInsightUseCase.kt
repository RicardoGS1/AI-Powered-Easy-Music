package com.virtualworld.easymusic.domain.usecase

import com.virtualworld.easymusic.data.remote.gemini.GeminiSongInsightDataSource
import com.virtualworld.easymusic.domain.model.Song
import com.virtualworld.easymusic.domain.model.SongInsightResult
import javax.inject.Inject

class FetchSongInsightUseCase @Inject constructor(
    private val dataSource: GeminiSongInsightDataSource
) {
    suspend operator fun invoke(song: Song): SongInsightResult = dataSource.fetchInsight(song)
}
