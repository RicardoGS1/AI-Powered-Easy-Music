package com.virtualworld.easymusic.ads

import android.util.Log
import android.view.LayoutInflater
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.virtualworld.easymusic.BuildConfig
import com.virtualworld.easymusic.R
import com.virtualworld.easymusic.ui.theme.DarkCard

const val LIBRARY_NATIVE_AD_KEY = "library_native_ad"

@Composable
fun LibraryNativeListAd(
    modifier: Modifier = Modifier,
) {
    val nativeAd = rememberLoadedNativeAd()
    nativeAd?.let { ad ->
        AndroidView(
            modifier = modifier.fillMaxWidth(),
            factory = { context ->
                LayoutInflater.from(context)
                    .inflate(R.layout.native_ad_list_item, null, false) as NativeAdView
            },
            onReset = { adView ->
                adView.setTag(R.id.native_ad_bound, null)
            },
            onRelease = { adView ->
                adView.setTag(R.id.native_ad_bound, null)
            },
            update = { adView ->
                if (adView.getTag(R.id.native_ad_bound) != ad) {
                    NativeAdPopulator.populateListItem(ad, adView)
                    adView.setTag(R.id.native_ad_bound, ad)
                }
            },
        )
    }
}

@Composable
fun LibraryNativeAlbumAd(
    modifier: Modifier = Modifier,
) {
    val nativeAd = rememberLoadedNativeAd()
    nativeAd?.let { ad ->
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
        ) {
            AndroidView(
                modifier = Modifier.fillMaxWidth(),
                factory = { context ->
                    LayoutInflater.from(context)
                        .inflate(R.layout.native_ad_album_item, null, false) as NativeAdView
                },
                onReset = { adView ->
                    adView.setTag(R.id.native_ad_bound, null)
                },
                onRelease = { adView ->
                    adView.setTag(R.id.native_ad_bound, null)
                },
                update = { adView ->
                    if (adView.getTag(R.id.native_ad_bound) != ad) {
                        NativeAdPopulator.populateAlbumItem(ad, adView)
                        adView.setTag(R.id.native_ad_bound, ad)
                    }
                },
            )
        }
    }
}

@Composable
private fun rememberLoadedNativeAd(): NativeAd? {
    val context = LocalContext.current
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }

    DisposableEffect(Unit) {
        val adLoader = AdLoader.Builder(context, BuildConfig.NATIVE_AD_UNIT_ID)
            .forNativeAd { ad ->
                nativeAd?.destroy()
                nativeAd = ad
            }
            .withAdListener(
                object : AdListener() {
                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Log.d(TAG, "Native ad failed to load: ${loadAdError.message}")
                    }
                },
            )
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                    .setMediaAspectRatio(NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_ANY)
                    .build(),
            )
            .build()

        adLoader.loadAd(AdRequest.Builder().build())

        onDispose {
            nativeAd?.destroy()
            nativeAd = null
        }
    }

    return nativeAd
}

private const val TAG = "NativeAdComposable"
