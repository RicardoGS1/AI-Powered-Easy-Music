package com.virtualworld.easymusic.ui.navigation

object Routes {
    const val PLAYER = "player"
    const val LIBRARY = "library?openSearch={openSearch}"
    const val EQUALIZER = "equalizer"

    fun library(openSearch: Boolean = false): String = "library?openSearch=$openSearch"
}
