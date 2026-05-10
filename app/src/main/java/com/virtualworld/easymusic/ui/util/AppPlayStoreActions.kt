package com.virtualworld.easymusic.ui.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.ShareCompat
import com.virtualworld.easymusic.R

fun shareAppPlayStoreLink(context: Context) {
    val packageName = context.packageName
    val storeUrl = "https://play.google.com/store/apps/details?id=$packageName"
    val message = context.getString(
        R.string.share_app_text,
        context.getString(R.string.app_name),
        storeUrl
    )
    ShareCompat.IntentBuilder(context)
        .setType("text/plain")
        .setChooserTitle(context.getString(R.string.share_app_chooser_title))
        .setText(message)
        .startChooser()
}

fun openAppPlayStoreListing(context: Context) {
    val packageName = context.packageName
    val marketUri = Uri.parse("market://details?id=$packageName")
    val marketIntent = Intent(Intent.ACTION_VIEW, marketUri)
    try {
        context.startActivity(marketIntent)
    } catch (_: ActivityNotFoundException) {
        val webUri = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
        context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
    }
}
