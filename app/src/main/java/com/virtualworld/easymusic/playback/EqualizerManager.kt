package com.virtualworld.easymusic.playback

import android.app.Application
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.util.Log
import com.virtualworld.easymusic.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

data class EqualizerState(
    /** Si es false, el API Equalizer no aplica la curva al audio (aunque muevas bandas). */
    val isEnabled: Boolean = true,
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

data class EqPreset(val name: String, val levels: List<Int>)

@Singleton
class EqualizerManager @Inject constructor(private val app: Application) {

    companion object {
        // Niveles en millibels para 5 bandas: ~60Hz, ~230Hz, ~910Hz, ~3.6kHz, ~14kHz
        val PRESETS = listOf(
            EqPreset("Plano", listOf(0, 0, 0, 0, 0)),
            EqPreset("Rock", listOf(500, 300, -100, 300, 500)),
            EqPreset("Pop", listOf(-100, 200, 500, 200, -100)),
            EqPreset("Jazz", listOf(400, 200, -200, 100, 300)),
            EqPreset("Clásica", listOf(500, 300, -100, 200, 400)),
            EqPreset("Dance", listOf(600, 400, 100, -100, 300)),
            EqPreset("Hip Hop", listOf(600, 400, 0, 100, -100)),
            EqPreset("Electrónica", listOf(500, 200, 0, 200, 500)),
            EqPreset("R&B", listOf(500, 300, -100, 200, 200)),
            EqPreset("Heavy Metal", listOf(400, 100, -300, 100, 300)),
            EqPreset("Acústica", listOf(400, 100, 100, 200, 300)),
            EqPreset("Latino", listOf(400, 100, 0, 100, 500)),
            EqPreset("Vocal", listOf(-200, 0, 400, 400, 100)),
            EqPreset("Graves", listOf(700, 500, 200, 0, 0)),
            EqPreset("Agudos", listOf(0, 0, 200, 500, 700))
        )

        private val PRESET_NAMES: List<String> = PRESETS.map { it.name }
    }

    fun getLocalizedPresetNames(): List<String> = listOf(
        app.getString(R.string.preset_flat),
        app.getString(R.string.preset_rock),
        app.getString(R.string.preset_pop),
        app.getString(R.string.preset_jazz),
        app.getString(R.string.preset_classical),
        app.getString(R.string.preset_dance),
        app.getString(R.string.preset_hip_hop),
        app.getString(R.string.preset_electronic),
        app.getString(R.string.preset_rnb),
        app.getString(R.string.preset_heavy_metal),
        app.getString(R.string.preset_acoustic),
        app.getString(R.string.preset_latin),
        app.getString(R.string.preset_vocal),
        app.getString(R.string.preset_bass),
        app.getString(R.string.preset_treble)
    )

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var currentSessionId: Int = -1

    /** Los nombres de estilo no dependen del hardware: la UI puede mostrarlos antes de que exista sesión de audio. */
    private val _state = MutableStateFlow(EqualizerState(presets = getLocalizedPresetNames()))
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

                val levels = if (previousState.bandLevels.size == bands &&
                    previousState.currentPreset == -1
                ) {
                    val min = bandLevelRange[0].toInt()
                    val max = bandLevelRange[1].toInt()
                    previousState.bandLevels.forEachIndexed { i, level ->
                        val c = level.coerceIn(min, max)
                        setBandLevel(i.toShort(), c.toShort())
                    }
                    previousState.bandLevels
                } else if (previousState.currentPreset >= 0 &&
                    previousState.currentPreset < PRESETS.size
                ) {
                    applyPresetLevels(bands, PRESETS[previousState.currentPreset])
                } else {
                    val min = bandLevelRange[0].toInt()
                    val max = bandLevelRange[1].toInt()
                    (0 until bands).map { getBandLevel(it.toShort()).toInt().coerceIn(min, max) }
                }

                _state.update { s ->
                    s.copy(
                        numberOfBands = bands,
                        bandLevels = levels,
                        minLevel = minLvl,
                        maxLevel = maxLvl,
                        centerFrequencies = frequencies,
                        presets = getLocalizedPresetNames(),
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
            _state.update { it.copy(presets = getLocalizedPresetNames()) }
        }
    }

    private fun Equalizer.applyPresetLevels(bands: Int, preset: EqPreset): List<Int> {
        val adapted = adaptPresetToBands(preset.levels, bands)
        val min = bandLevelRange[0].toInt()
        val max = bandLevelRange[1].toInt()
        val clampedLevels = adapted.map { it.coerceIn(min, max) }
        clampedLevels.forEachIndexed { i, clamped ->
            setBandLevel(i.toShort(), clamped.toShort())
        }
        return clampedLevels
    }

    private fun adaptPresetToBands(presetLevels: List<Int>, targetBands: Int): List<Int> {
        if (presetLevels.size == targetBands) return presetLevels
        if (targetBands <= 0) return emptyList()

        return List(targetBands) { i ->
            val srcPos = i.toFloat() * (presetLevels.size - 1) / (targetBands - 1).coerceAtLeast(1)
            val low = srcPos.toInt().coerceIn(0, presetLevels.size - 1)
            val high = (low + 1).coerceAtMost(presetLevels.size - 1)
            val frac = srcPos - low
            (presetLevels[low] * (1 - frac) + presetLevels[high] * frac).toInt()
        }
    }

    fun setEnabled(enabled: Boolean) {
        equalizer?.enabled = enabled
        bassBoost?.enabled = if (enabled) _state.value.isBassBoostEnabled else false
        _state.update { it.copy(isEnabled = enabled) }
    }

    fun setBandLevel(band: Int, level: Int) {
        val eq = equalizer ?: return
        val min = eq.bandLevelRange[0].toInt()
        val max = eq.bandLevelRange[1].toInt()
        val clamped = level.coerceIn(min, max)
        eq.setBandLevel(band.toShort(), clamped.toShort())
        _state.update { s ->
            s.copy(
                bandLevels = s.bandLevels.toMutableList().apply { this[band] = clamped },
                currentPreset = -1
            )
        }
    }

    fun selectPreset(presetIndex: Int) {
        val preset = PRESETS.getOrNull(presetIndex) ?: return
        val eq = equalizer ?: run {
            _state.update { it.copy(currentPreset = presetIndex) }
            return
        }
        val bands = eq.numberOfBands.toInt()
        val levels = eq.applyPresetLevels(bands, preset)
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
