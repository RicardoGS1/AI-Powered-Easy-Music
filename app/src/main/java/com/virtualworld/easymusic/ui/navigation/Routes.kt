package com.virtualworld.easymusic.ui.navigation

object Routes {
    const val PLAYER = "player"
    const val LIBRARY = "library?openSearch={openSearch}"
    const val EQUALIZER = "equalizer"
    const val COLLECTION = "collection/{kind}/{itemId}"
    const val SETTINGS = "settings"

    fun library(openSearch: Boolean = false): String = "library?openSearch=$openSearch"

    fun collectionAlbum(albumId: Long): String = "collection/album/$albumId"

    fun collectionArtist(artistId: Long): String = "collection/artist/$artistId"
}
