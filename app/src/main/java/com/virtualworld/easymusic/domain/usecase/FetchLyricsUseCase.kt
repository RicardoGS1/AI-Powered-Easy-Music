package com.virtualworld.easymusic.domain.usecase

import com.virtualworld.easymusic.data.remote.lrclib.LrcLibLyricsDataSource
import com.virtualworld.easymusic.domain.model.LyricsResult
import com.virtualworld.easymusic.domain.model.Song
import javax.inject.Inject

class FetchLyricsUseCase @Inject constructor(
    private val dataSource: LrcLibLyricsDataSource
) {
    suspend fun fetchExact(song: Song): LyricsResult = dataSource.fetchLyricsExactOnly(song)

    suspend fun searchAlternatives(song: Song): LyricsResult = dataSource.fetchLyricsViaSearch(song)

    suspend fun byLrcLibId(id: Long): LyricsResult = dataSource.fetchLyricsById(id)
}
