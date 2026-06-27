package com.virtualworld.easymusic.ui.splash

import android.app.Activity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.virtualworld.easymusic.EasyMusicApp
import com.virtualworld.easymusic.R
import com.virtualworld.easymusic.ui.navigation.Routes
import com.virtualworld.easymusic.ui.theme.EasyMusicTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import kotlin.math.max

private const val POLL_INTERVAL_MS = 200L
private const val AD_SHOW_SAFETY_BUFFER_MS = 30_000L
private const val DISABLED_SPLASH_DELAY_MS = 600L

@Composable
fun SplashScreen(
    navController: NavHostController,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val app = context.applicationContext as? EasyMusicApp

    var targetProgress by remember { mutableFloatStateOf(0f) }
    var navigated by remember { mutableStateOf(false) }

    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 400),
        label = "splash_progress",
    )

    fun navigateToPlayer() {
        if (navigated) return
        navigated = true
        navController.navigate(Routes.PLAYER) {
            popUpTo(Routes.SPLASH) { inclusive = true }
        }
        app?.markColdStartCompleted()
    }

    LaunchedEffect(activity, app) {
        if (activity == null || app == null) {
            targetProgress = 1f
            delay(DISABLED_SPLASH_DELAY_MS)
            navigateToPlayer()
            return@LaunchedEffect
        }

        targetProgress = 0.1f

        suspendCancellableCoroutine { continuation ->
            app.consentManager.gatherConsent(activity) {
                if (continuation.isActive) {
                    continuation.resume(Unit)
                }
            }
        }

        if (app.consentManager.canRequestAds()) {
            suspendCancellableCoroutine { continuation ->
                app.initializeMobileAdsIfNeeded {
                    if (continuation.isActive) {
                        continuation.resume(Unit)
                    }
                }
            }
        }

        val appOpenEnabled = viewModel.isAppOpenEnabled() && app.consentManager.canRequestAds()
        val loadWaitMs = viewModel.getAppOpenLoadWaitMs()
        val maxWaitMs = if (appOpenEnabled) {
            loadWaitMs + AD_SHOW_SAFETY_BUFFER_MS
        } else {
            DISABLED_SPLASH_DELAY_MS
        }

        targetProgress = 0.2f
        var managerReady = false

        val progressJob = launch {
            var elapsed = 0L
            while (!navigated && elapsed < maxWaitMs) {
                delay(POLL_INTERVAL_MS)
                elapsed += POLL_INTERVAL_MS
                val t = (elapsed.toFloat() / maxWaitMs).coerceIn(0f, 1f)
                val next = when {
                    !appOpenEnabled -> 0.2f + t * 0.8f
                    managerReady -> (0.5f + t * 0.45f).coerceAtMost(0.95f)
                    else -> 0.2f + t * 0.25f
                }
                targetProgress = max(targetProgress, next)
            }
        }

        if (!appOpenEnabled) {
            delay(DISABLED_SPLASH_DELAY_MS)
        } else {
            withTimeoutOrNull(maxWaitMs) {
                suspendCancellableCoroutine { continuation ->
                    app.runWhenAppOpenAdManagerReady { manager ->
                        managerReady = true
                        manager.showStartupAdIfAvailable(
                            activity = activity,
                            enabled = appOpenEnabled,
                            waitMs = loadWaitMs,
                        ) {
                            if (continuation.isActive) {
                                continuation.resume(Unit)
                            }
                        }
                    }
                }
            }
        }

        progressJob.cancel()
        targetProgress = max(targetProgress, 1f)
        delay(200)
        navigateToPlayer()
    }

    SplashScreenContent(progress = animatedProgress)
}

@Composable
fun SplashScreenContent(progress: Float) {
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background),
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .align(Alignment.Center)
                .clip(CircleShape)
                .background(Color(0xFF0E346F)),
        ) {
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                contentDescription = stringResource(R.string.app_name),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = 1.5f
                        scaleY = 1.5f
                    },
            )
        }

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 24.dp, end = 24.dp, bottom = 48.dp)
                .fillMaxWidth(0.6f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = colorScheme.primary,
            trackColor = colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    EasyMusicTheme {
        SplashScreenContent(progress = 0.5f)
    }
}
