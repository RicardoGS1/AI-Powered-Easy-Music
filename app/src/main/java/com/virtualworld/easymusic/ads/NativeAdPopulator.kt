package com.virtualworld.easymusic.ads

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.virtualworld.easymusic.R

object NativeAdPopulator {

    fun populateListItem(nativeAd: NativeAd, adView: NativeAdView) {
        val headlineView = adView.findViewById<TextView>(R.id.ad_headline)
        val bodyView = adView.findViewById<TextView>(R.id.ad_body)
        val callToActionView = adView.findViewById<TextView>(R.id.ad_call_to_action)
        val iconView = adView.findViewById<ImageView>(R.id.ad_app_icon)
        val advertiserView = adView.findViewById<TextView>(R.id.ad_advertiser)

        adView.headlineView = headlineView
        adView.bodyView = bodyView
        adView.callToActionView = callToActionView
        adView.iconView = iconView
        adView.advertiserView = advertiserView

        headlineView.text = nativeAd.headline

        if (nativeAd.body.isNullOrBlank()) {
            bodyView.visibility = View.GONE
        } else {
            bodyView.text = nativeAd.body
            bodyView.visibility = View.VISIBLE
        }

        if (nativeAd.advertiser.isNullOrBlank()) {
            advertiserView.visibility = View.GONE
        } else {
            advertiserView.text = nativeAd.advertiser
            advertiserView.visibility = View.VISIBLE
        }

        if (nativeAd.callToAction.isNullOrBlank()) {
            callToActionView.visibility = View.GONE
        } else {
            callToActionView.text = nativeAd.callToAction
            callToActionView.visibility = View.VISIBLE
        }

        val iconDrawable = nativeAd.icon?.drawable
        if (iconDrawable != null) {
            iconView.setImageDrawable(iconDrawable)
            iconView.visibility = View.VISIBLE
        } else {
            iconView.visibility = View.GONE
        }

        adView.setNativeAd(nativeAd)
    }

    fun populateAlbumItem(nativeAd: NativeAd, adView: NativeAdView) {
        val headlineView = adView.findViewById<TextView>(R.id.ad_headline)
        val bodyView = adView.findViewById<TextView>(R.id.ad_body)
        val callToActionView = adView.findViewById<TextView>(R.id.ad_call_to_action)
        val advertiserView = adView.findViewById<TextView>(R.id.ad_advertiser)
        val mediaView = adView.findViewById<MediaView>(R.id.ad_media)
        val iconView = adView.findViewById<ImageView>(R.id.ad_app_icon)

        adView.headlineView = headlineView
        adView.bodyView = bodyView
        adView.callToActionView = callToActionView
        adView.advertiserView = advertiserView

        headlineView.text = nativeAd.headline

        if (nativeAd.body.isNullOrBlank()) {
            bodyView.visibility = View.GONE
        } else {
            bodyView.text = nativeAd.body
            bodyView.visibility = View.VISIBLE
        }

        if (nativeAd.advertiser.isNullOrBlank()) {
            advertiserView.visibility = View.GONE
        } else {
            advertiserView.text = nativeAd.advertiser
            advertiserView.visibility = View.VISIBLE
        }

        if (nativeAd.callToAction.isNullOrBlank()) {
            callToActionView.visibility = View.GONE
        } else {
            callToActionView.text = nativeAd.callToAction
            callToActionView.visibility = View.VISIBLE
        }

        nativeAd.mediaContent?.let { mediaContent ->
            adView.mediaView = mediaView
            adView.iconView = null
            mediaView.mediaContent = mediaContent
            mediaView.visibility = View.VISIBLE
            iconView.visibility = View.GONE
            setSquareMediaHeight(mediaView)
        } ?: nativeAd.icon?.drawable?.let { drawable ->
            adView.mediaView = null
            adView.iconView = iconView
            iconView.setImageDrawable(drawable)
            iconView.visibility = View.VISIBLE
            mediaView.visibility = View.GONE
            setSquareMediaHeight(iconView)
        } ?: run {
            mediaView.visibility = View.GONE
            iconView.visibility = View.GONE
        }

        adView.setNativeAd(nativeAd)
    }

    private fun setSquareMediaHeight(view: View) {
        view.post {
            val width = view.width
            if (width > 0) {
                view.layoutParams = view.layoutParams.apply {
                    height = width
                }
            }
        }
    }
}
