package com.virtualworld.easymusic.domain.usecase

import com.virtualworld.easymusic.data.remote.lrclib.LrcLibLyricsDataSource
import com.virtualworld.easymusic.domain.model.LyricsResult
import com.virtualworld.easymusic.domain.model.Song
import javax.inject.Inject

class FetchLyricsUseCase @Inject constructor(
    private val dataSource: LrcLibLyricsDataSource
) {
    suspend operator fun invoke(song: Song): LyricsResult = dataSource.fetchLyrics(song)
}
