package com.virtualworld.easymusic.ui.equalizer

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.virtualworld.easymusic.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.virtualworld.easymusic.playback.EqualizerManager
import com.virtualworld.easymusic.playback.EqualizerState
import com.virtualworld.easymusic.ui.theme.DarkBackground
import com.virtualworld.easymusic.ui.theme.DarkCard
import com.virtualworld.easymusic.ui.theme.DarkSurface
import com.virtualworld.easymusic.ui.theme.DarkSurfaceVariant
import com.virtualworld.easymusic.ui.theme.EasyMusicTheme
import com.virtualworld.easymusic.ui.theme.Teal200
import com.virtualworld.easymusic.ui.theme.Teal400
import com.virtualworld.easymusic.ui.theme.Teal700
import com.virtualworld.easymusic.ui.theme.TextDarkGray
import com.virtualworld.easymusic.ui.theme.TextGray
import com.virtualworld.easymusic.ui.theme.TextWhite

private const val StylePresetGridColumns = 3

@Composable
fun EqualizerScreen(
    onNavigateBack: () -> Unit,
    viewModel: EqualizerViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    EqualizerContent(
        state = state,
        onNavigateBack = onNavigateBack,
        onEnabledChanged = viewModel::setEnabled,
        onBandLevelChanged = viewModel::setBandLevel,
        onPresetSelected = viewModel::selectPreset,
        onBassBoostEnabledChanged = viewModel::setBassBoostEnabled,
        onBassBoostStrengthChanged = viewModel::setBassBoostStrength
    )
}

@Composable
fun EqualizerContent(
    state: EqualizerState,
    onNavigateBack: () -> Unit,
    onEnabledChanged: (Boolean) -> Unit,
    onBandLevelChanged: (Int, Int) -> Unit,
    onPresetSelected: (Int) -> Unit,
    onBassBoostEnabledChanged: (Boolean) -> Unit,
    onBassBoostStrengthChanged: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkSurfaceVariant, DarkSurface, DarkBackground)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            TopBar(onNavigateBack = onNavigateBack)

            Spacer(modifier = Modifier.height(16.dp))

            EnableSection(
                isEnabled = state.isEnabled,
                onEnabledChanged = onEnabledChanged
            )

            Spacer(modifier = Modifier.height(20.dp))

            StylePresetSelector(
                presets = state.presets,
                currentPreset = state.currentPreset,
                globalEqEnabled = state.isEnabled,
                bandsReady = state.numberOfBands > 0,
                onPresetSelected = onPresetSelected
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (state.numberOfBands > 0) {
                BandSliders(
                    state = state,
                    isEnabled = state.isEnabled,
                    onBandLevelChanged = onBandLevelChanged
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            BassBoostSection(
                isEnabled = state.isEnabled && state.isBassBoostEnabled,
                isBassBoostEnabled = state.isBassBoostEnabled,
                strength = state.bassBoostStrength,
                globalEnabled = state.isEnabled,
                onEnabledChanged = onBassBoostEnabledChanged,
                onStrengthChanged = onBassBoostStrengthChanged
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun TopBar(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.cd_back),
                tint = TextWhite
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Filled.Equalizer,
            contentDescription = null,
            tint = Teal400,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = stringResource(R.string.equalizer_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            color = TextWhite
        )
    }
}

@Composable
private fun EnableSection(
    isEnabled: Boolean,
    onEnabledChanged: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(DarkCard)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = stringResource(R.string.equalizer),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = TextWhite
            )
            Text(
                text = if (isEnabled) stringResource(R.string.enabled) else stringResource(R.string.disabled),
                style = MaterialTheme.typography.bodySmall,
                color = if (isEnabled) Teal400 else TextGray
            )
        }
        Switch(
            checked = isEnabled,
            onCheckedChange = onEnabledChanged,
            colors = SwitchDefaults.colors(
                checkedThumbColor = TextWhite,
                checkedTrackColor = Teal400,
                uncheckedThumbColor = TextGray,
                uncheckedTrackColor = DarkSurfaceVariant
            )
        )
    }
}

@Composable
private fun StylePresetSelector(
    presets: List<String>,
    currentPreset: Int,
    globalEqEnabled: Boolean,
    bandsReady: Boolean,
    onPresetSelected: (Int) -> Unit
) {
    val names = presets.ifEmpty { EqualizerManager.PRESETS.map { it.name } }
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.music_style),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = TextGray,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 0.dp)
        )
        if (!bandsReady) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.eq_play_song_hint),
                style = MaterialTheme.typography.bodySmall,
                color = TextDarkGray,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(DarkCard)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            names.chunked(StylePresetGridColumns).forEachIndexed { rowIndex, row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEachIndexed { colIndex, label ->
                        val index = rowIndex * StylePresetGridColumns + colIndex
                        val selected = index == currentPreset
                        FilterChip(
                            selected = selected,
                            onClick = { onPresetSelected(index) },
                            enabled = true,
                            modifier = Modifier.weight(1f),
                            label = {
                                Text(
                                    text = label,
                                    modifier = Modifier.fillMaxWidth(),
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 2,
                                    textAlign = TextAlign.Center
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = DarkSurfaceVariant,
                                labelColor = TextWhite,
                                selectedContainerColor = Teal400.copy(alpha = 0.35f),
                                selectedLabelColor = Teal200,
                                disabledContainerColor = DarkSurfaceVariant,
                                disabledLabelColor = TextWhite
                            )
                        )
                    }
                    repeat((StylePresetGridColumns - row.size).coerceAtLeast(0)) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        if (currentPreset !in names.indices && globalEqEnabled && bandsReady) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.eq_custom_curve),
                style = MaterialTheme.typography.bodySmall,
                color = TextDarkGray,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}

@Composable
private fun BandSliders(
    state: EqualizerState,
    isEnabled: Boolean,
    onBandLevelChanged: (Int, Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(DarkCard)
            .padding(vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "+${state.maxLevel / 100} dB",
                style = MaterialTheme.typography.labelSmall,
                color = TextDarkGray
            )
            Text(
                text = "0 dB",
                style = MaterialTheme.typography.labelSmall,
                color = TextDarkGray
            )
            Text(
                text = "${state.minLevel / 100} dB",
                style = MaterialTheme.typography.labelSmall,
                color = TextDarkGray
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            state.bandLevels.forEachIndexed { index, level ->
                BandSlider(
                    level = level,
                    minLevel = state.minLevel,
                    maxLevel = state.maxLevel,
                    frequency = state.centerFrequencies.getOrElse(index) { 0 },
                    isEnabled = isEnabled,
                    onLevelChanged = { onBandLevelChanged(index, it) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun BandSlider(
    level: Int,
    minLevel: Int,
    maxLevel: Int,
    frequency: Int,
    isEnabled: Boolean,
    onLevelChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeColor by animateColorAsState(
        targetValue = if (isEnabled) Teal400 else TextDarkGray,
        label = "bandColor"
    )
    val range = minLevel.toFloat()..maxLevel.toFloat()

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "${level / 100}",
            style = MaterialTheme.typography.labelSmall,
            color = if (isEnabled) Teal200 else TextDarkGray,
            textAlign = TextAlign.Center,
            fontSize = 10.sp
        )

        Slider(
            value = level.toFloat(),
            onValueChange = { onLevelChanged(it.toInt()) },
            valueRange = range,
            enabled = isEnabled,
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .rotate(270f)
                .width(160.dp),
            colors = SliderDefaults.colors(
                thumbColor = activeColor,
                activeTrackColor = activeColor,
                inactiveTrackColor = DarkSurfaceVariant,
                disabledThumbColor = TextDarkGray,
                disabledActiveTrackColor = TextDarkGray.copy(alpha = 0.5f),
                disabledInactiveTrackColor = DarkSurfaceVariant.copy(alpha = 0.5f)
            )
        )

        Text(
            text = formatFrequency(frequency),
            style = MaterialTheme.typography.labelSmall,
            color = if (isEnabled) TextGray else TextDarkGray,
            textAlign = TextAlign.Center,
            fontSize = 10.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun BassBoostSection(
    isEnabled: Boolean,
    isBassBoostEnabled: Boolean,
    strength: Int,
    globalEnabled: Boolean,
    onEnabledChanged: (Boolean) -> Unit,
    onStrengthChanged: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(DarkCard)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(R.string.bass_boost),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TextWhite
                )
                Text(
                    text = "${(strength / 10)}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isEnabled) Teal400 else TextGray
                )
            }
            Switch(
                checked = isBassBoostEnabled,
                onCheckedChange = onEnabledChanged,
                enabled = globalEnabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = TextWhite,
                    checkedTrackColor = Teal700,
                    uncheckedThumbColor = TextGray,
                    uncheckedTrackColor = DarkSurfaceVariant
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Slider(
            value = strength.toFloat(),
            onValueChange = { onStrengthChanged(it.toInt()) },
            valueRange = 0f..1000f,
            enabled = isEnabled,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = Teal400,
                activeTrackColor = Teal400,
                inactiveTrackColor = DarkSurfaceVariant,
                disabledThumbColor = TextDarkGray,
                disabledActiveTrackColor = TextDarkGray.copy(alpha = 0.5f),
                disabledInactiveTrackColor = DarkSurfaceVariant.copy(alpha = 0.5f)
            )
        )
    }
}

private fun formatFrequency(milliHz: Int): String {
    val hz = milliHz / 1000
    return if (hz >= 1000) "${hz / 1000}k" else "${hz}"
}

@Preview(showBackground = true)
@Composable
fun EqualizerScreenPreview() {
    EasyMusicTheme {
        EqualizerContent(
            state = EqualizerState(
                isEnabled = true,
                numberOfBands = 5,
                bandLevels = listOf(0, 500, 1000, 500, 0),
                minLevel = -1500,
                maxLevel = 1500,
                centerFrequencies = listOf(60000, 230000, 910000, 3600000, 14000000),
                presets = listOf("Normal", "Pop", "Rock", "Jazz", "Classical"),
                currentPreset = 1,
                bassBoostStrength = 500,
                isBassBoostEnabled = true
            ),
            onNavigateBack = {},
            onEnabledChanged = {},
            onBandLevelChanged = { _, _ -> },
            onPresetSelected = {},
            onBassBoostEnabledChanged = {},
            onBassBoostStrengthChanged = {}
        )
    }
}
