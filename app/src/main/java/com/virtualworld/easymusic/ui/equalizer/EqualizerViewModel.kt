package com.virtualworld.easymusic.ui.equalizer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.virtualworld.easymusic.playback.EqualizerManager
import com.virtualworld.easymusic.playback.EqualizerState
import com.virtualworld.easymusic.playback.PlaybackController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EqualizerViewModel @Inject constructor(
    private val equalizerManager: EqualizerManager,
    private val playbackController: PlaybackController
) : ViewModel() {

    val state: StateFlow<EqualizerState> = equalizerManager.state

    init {
        viewModelScope.launch {
            playbackController.audioSessionIdFlow.collect { sessionId ->
                if (sessionId != 0) {
                    equalizerManager.initialize(sessionId)
                }
            }
        }
    }

    fun setEnabled(enabled: Boolean) = equalizerManager.setEnabled(enabled)

    fun setBandLevel(band: Int, level: Int) = equalizerManager.setBandLevel(band, level)

    fun selectPreset(presetIndex: Int) = equalizerManager.selectPreset(presetIndex)

    fun setBassBoostEnabled(enabled: Boolean) = equalizerManager.setBassBoostEnabled(enabled)

    fun setBassBoostStrength(strength: Int) = equalizerManager.setBassBoostStrength(strength)
}
