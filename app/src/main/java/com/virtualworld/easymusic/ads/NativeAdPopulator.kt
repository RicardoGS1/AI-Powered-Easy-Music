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
        bindTextContent(nativeAd, adView)
        bindMediaContent(nativeAd, adView, NativeAdMediaShape.LANDSCAPE_16_9)
    }

    fun populateAlbumItem(nativeAd: NativeAd, adView: NativeAdView) {
        bindAlbumTextContent(nativeAd, adView)
        bindMediaContent(nativeAd, adView, NativeAdMediaShape.SQUARE)
    }

    private fun bindTextContent(nativeAd: NativeAd, adView: NativeAdView) {
        val headlineView = adView.findViewById<TextView>(R.id.ad_headline)
        val bodyView = adView.findViewById<TextView>(R.id.ad_body)
        val callToActionView = adView.findViewById<TextView>(R.id.ad_call_to_action)
        val advertiserView = adView.findViewById<TextView>(R.id.ad_advertiser)

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

    private fun bindMediaContent(
        nativeAd: NativeAd,
        adView: NativeAdView,
        mediaShape: NativeAdMediaShape,
    ) {
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
                bindNativeAdWhenMediaSized(adView, nativeAd, mediaContainer, mediaView, mediaShape)
            }
            nativeAd.icon?.drawable != null -> {
                adView.mediaView = null
                adView.iconView = iconView
                iconView.setImageDrawable(nativeAd.icon!!.drawable)
                iconView.visibility = View.VISIBLE
                mediaView.visibility = View.GONE
                mediaContainer.visibility = View.VISIBLE
                bindNativeAdWhenMediaSized(adView, nativeAd, mediaContainer, iconView, mediaShape)
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
        mediaShape: NativeAdMediaShape,
    ) {
        fun bindWhenReady() {
            if (!container.isAttachedToWindow) return

            val minSizePx = minAdMediaSizePx(container)
            val availableWidth = resolveAvailableWidth(container)
            if (availableWidth == null) {
                container.doOnLayout { bindWhenReady() }
                return
            }

            val width = max(availableWidth, minSizePx)
            val height = mediaHeightForWidth(width, minSizePx, mediaShape)
            applyMediaSize(container, mediaView, height)
            container.requestLayout()
            container.post { adView.setNativeAd(nativeAd) }
        }

        if (container.isLaidOut && resolveAvailableWidth(container) != null) {
            bindWhenReady()
        } else {
            container.doOnLayout { bindWhenReady() }
        }
    }

    private fun mediaHeightForWidth(
        width: Int,
        minSizePx: Int,
        mediaShape: NativeAdMediaShape,
    ): Int = when (mediaShape) {
        NativeAdMediaShape.LANDSCAPE_16_9 -> max(width * 9 / 16, minSizePx)
        NativeAdMediaShape.SQUARE -> max(width, minSizePx)
    }

    private fun resolveAvailableWidth(view: View): Int? {
        var current: View? = view
        while (current != null) {
            current.width.takeIf { it > 0 }?.let { return it }
            current = current.parent as? View
        }
        return null
    }

    private fun applyMediaSize(container: View, mediaView: View, targetHeight: Int) {
        listOf(container, mediaView).forEach { view ->
            if (view.layoutParams.height != targetHeight) {
                view.layoutParams = view.layoutParams.apply {
                    height = targetHeight
                }
            }
        }
    }

    private fun minAdMediaSizePx(view: View): Int =
        view.resources.getDimensionPixelSize(R.dimen.native_ad_media_min_size)
}

private enum class NativeAdMediaShape {
    LANDSCAPE_16_9,
    SQUARE,
}
