package com.virtualworld.easymusic.playback

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

data class EqualizerState(
    val isEnabled: Boolean = false,
    val numberOfBands: Int = 0,
    val bandLevels: List<Int> = emptyList(),
    val minLevel: Int = 0,
    val maxLevel: Int = 0,
    val centerFrequencies: List<Int> = emptyList(),
    val presets: List<String> = emptyList(),
    val currentPreset: Int = -1,
    val bassBoostStrength: Int = 0,
    val isBassBoostEnabled: Boolean = false
)

@Singleton
class EqualizerManager @Inject constructor() {

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var currentSessionId: Int = -1

    private val _state = MutableStateFlow(EqualizerState())
    val state: StateFlow<EqualizerState> = _state.asStateFlow()

    fun initialize(audioSessionId: Int) {
        if (audioSessionId == 0) return
        if (audioSessionId == currentSessionId && equalizer != null) return

        val previousState = _state.value
        release()
        currentSessionId = audioSessionId

        try {
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = previousState.isEnabled

                val bands = numberOfBands.toInt()
                val range = bandLevelRange
                val minLvl = range[0].toInt()
                val maxLvl = range[1].toInt()

                val frequencies = (0 until bands).map { getCenterFreq(it.toShort()) }
                val presetNames = (0 until numberOfPresets.toInt()).map {
                    getPresetName(it.toShort())
                }

                val levels = if (previousState.bandLevels.size == bands &&
                    previousState.currentPreset == -1
                ) {
                    previousState.bandLevels.forEachIndexed { i, level ->
                        setBandLevel(i.toShort(), level.toShort())
                    }
                    previousState.bandLevels
                } else if (previousState.currentPreset >= 0 &&
                    previousState.currentPreset < numberOfPresets
                ) {
                    usePreset(previousState.currentPreset.toShort())
                    (0 until bands).map { getBandLevel(it.toShort()).toInt() }
                } else {
                    (0 until bands).map { getBandLevel(it.toShort()).toInt() }
                }

                _state.update { s ->
                    s.copy(
                        numberOfBands = bands,
                        bandLevels = levels,
                        minLevel = minLvl,
                        maxLevel = maxLvl,
                        centerFrequencies = frequencies,
                        presets = presetNames,
                        currentPreset = previousState.currentPreset
                    )
                }
            }

            bassBoost = BassBoost(0, audioSessionId).apply {
                enabled = previousState.isBassBoostEnabled && previousState.isEnabled
                if (strengthSupported) {
                    setStrength(previousState.bassBoostStrength.toShort())
                }
            }
        } catch (e: Exception) {
            Log.e("EqualizerManager", "Failed to initialize audio effects for session $audioSessionId", e)
        }
    }

    fun setEnabled(enabled: Boolean) {
        equalizer?.enabled = enabled
        bassBoost?.enabled = if (enabled) _state.value.isBassBoostEnabled else false
        _state.update { it.copy(isEnabled = enabled) }
    }

    fun setBandLevel(band: Int, level: Int) {
        equalizer?.setBandLevel(band.toShort(), level.toShort())
        _state.update { s ->
            s.copy(
                bandLevels = s.bandLevels.toMutableList().apply { this[band] = level },
                currentPreset = -1
            )
        }
    }

    fun selectPreset(presetIndex: Int) {
        equalizer?.usePreset(presetIndex.toShort())
        val bands = equalizer?.numberOfBands?.toInt() ?: return
        val levels = (0 until bands).map {
            equalizer?.getBandLevel(it.toShort())?.toInt() ?: 0
        }
        _state.update { it.copy(currentPreset = presetIndex, bandLevels = levels) }
    }

    fun setBassBoostEnabled(enabled: Boolean) {
        bassBoost?.enabled = enabled && _state.value.isEnabled
        _state.update { it.copy(isBassBoostEnabled = enabled) }
    }

    fun setBassBoostStrength(strength: Int) {
        val clamped = strength.coerceIn(0, 1000)
        if (bassBoost?.strengthSupported == true) {
            bassBoost?.setStrength(clamped.toShort())
        }
        _state.update { it.copy(bassBoostStrength = clamped) }
    }

    fun release() {
        equalizer?.release()
        bassBoost?.release()
        equalizer = null
        bassBoost = null
        currentSessionId = -1
    }
}
