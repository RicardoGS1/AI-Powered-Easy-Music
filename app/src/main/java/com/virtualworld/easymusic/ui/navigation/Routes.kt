package com.virtualworld.easymusic.ui.navigation

object Routes {
    const val SPLASH = "splash"
    const val PLAYER = "player"
    const val LIBRARY = "library?openSearch={openSearch}"
    const val EQUALIZER = "equalizer"
    const val COLLECTION = "collection/{kind}/{itemId}"
    const val SETTINGS = "settings"
    const val VIDEO_LIBRARY = "video_library"

    fun library(openSearch: Boolean = false): String = "library?openSearch=$openSearch"

    fun collectionAlbum(albumId: Long): String = "collection/album/$albumId"

    fun collectionArtist(artistId: Long): String = "collection/artist/$artistId"
}
