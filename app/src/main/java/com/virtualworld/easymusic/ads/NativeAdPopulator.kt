package com.virtualworld.easymusic.ads

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.doOnLayout
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.virtualworld.easymusic.R
import kotlin.math.max

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
        bindAlbumTextContent(nativeAd, adView)
        bindAlbumMediaContent(nativeAd, adView)
    }

    private fun bindAlbumTextContent(nativeAd: NativeAd, adView: NativeAdView) {
        val headlineView = adView.findViewById<TextView>(R.id.ad_headline)
        val bodyView = adView.findViewById<TextView>(R.id.ad_body)
        val callToActionView = adView.findViewById<TextView>(R.id.ad_call_to_action)
        val advertiserView = adView.findViewById<TextView>(R.id.ad_advertiser)

        adView.headlineView = headlineView
        adView.bodyView = bodyView
        adView.callToActionView = callToActionView
        adView.advertiserView = advertiserView

        headlineView.text = nativeAd.headline

        val subtitle = nativeAd.body?.takeIf { it.isNotBlank() }
            ?: nativeAd.advertiser?.takeIf { it.isNotBlank() }
            .orEmpty()
        bodyView.text = subtitle

        advertiserView.text = nativeAd.advertiser.orEmpty()
        callToActionView.text = nativeAd.callToAction.orEmpty()
    }

    private fun bindAlbumMediaContent(nativeAd: NativeAd, adView: NativeAdView) {
        val mediaView = adView.findViewById<MediaView>(R.id.ad_media)
        val iconView = adView.findViewById<ImageView>(R.id.ad_app_icon)
        val mediaContainer = adView.findViewById<View>(R.id.ad_media_container)

        when {
            nativeAd.mediaContent != null -> {
                adView.mediaView = mediaView
                adView.iconView = null
                mediaView.mediaContent = nativeAd.mediaContent
                mediaView.visibility = View.VISIBLE
                iconView.visibility = View.GONE
                mediaContainer.visibility = View.VISIBLE
                bindNativeAdWhenMediaSized(adView, nativeAd, mediaContainer, mediaView)
            }
            nativeAd.icon?.drawable != null -> {
                adView.mediaView = null
                adView.iconView = iconView
                iconView.setImageDrawable(nativeAd.icon!!.drawable)
                iconView.visibility = View.VISIBLE
                mediaView.visibility = View.GONE
                mediaContainer.visibility = View.VISIBLE
                bindNativeAdWhenMediaSized(adView, nativeAd, mediaContainer, iconView)
            }
            else -> {
                adView.mediaView = null
                adView.iconView = null
                mediaView.visibility = View.GONE
                iconView.visibility = View.GONE
                mediaContainer.visibility = View.GONE
                adView.setNativeAd(nativeAd)
            }
        }
    }

    private fun bindNativeAdWhenMediaSized(
        adView: NativeAdView,
        nativeAd: NativeAd,
        container: View,
        mediaView: View,
    ) {
        fun bindWhenReady() {
            if (!container.isAttachedToWindow) return

            val minSizePx = minAdMediaSizePx(container)
            val availableWidth = resolveAvailableWidth(container)
            if (availableWidth == null) {
                container.doOnLayout { bindWhenReady() }
                return
            }

            val size = max(availableWidth, minSizePx)
            applySquareMediaSize(container, mediaView, size)
            container.requestLayout()
            container.post { adView.setNativeAd(nativeAd) }
        }

        if (container.isLaidOut && resolveAvailableWidth(container) != null) {
            bindWhenReady()
        } else {
            container.doOnLayout { bindWhenReady() }
        }
    }

    private fun resolveAvailableWidth(view: View): Int? {
        var current: View? = view
        while (current != null) {
            current.width.takeIf { it > 0 }?.let { return it }
            current = current.parent as? View
        }
        return null
    }

    private fun applySquareMediaSize(container: View, mediaView: View, size: Int) {
        listOf(container, mediaView).forEach { view ->
            if (view.layoutParams.height != size) {
                view.layoutParams = view.layoutParams.apply { height = size }
            }
        }
    }

    private fun minAdMediaSizePx(view: View): Int =
        (120 * view.resources.displayMetrics.density).toInt()
}
