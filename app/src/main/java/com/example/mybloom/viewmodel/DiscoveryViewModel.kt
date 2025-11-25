package com.example.mybloom.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybloom.repository.DiscoveryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel pour l'écran Capture
 * Gère l'identification des plantes avec Gemini AI
 */
class DiscoveryViewModel(
    private val repository: DiscoveryRepository
) : ViewModel() {

    private val _captureState = MutableStateFlow(CaptureUiState())
    val captureState: StateFlow<CaptureUiState> = _captureState.asStateFlow()

    /**
     * Identifier une plante avec Gemini AI et sauvegarder dans Room
     */
    fun identifyAndSavePlant(bitmap: Bitmap) {
        viewModelScope.launch {
            _captureState.value = CaptureUiState(isProcessing = true)

            repository.identifyAndSavePlant(bitmap)
                .onSuccess { discovery ->
                    _captureState.value = CaptureUiState(
                        isProcessing = false,
                        savedDiscoveryId = discovery.id
                    )
                }
                .onFailure { exception ->
                    _captureState.value = CaptureUiState(
                        isProcessing = false,
                        error = exception.message ?: "Erreur d'identification"
                    )
                }
        }
    }

    /**
     * Réinitialiser l'état de capture
     */
    fun resetCaptureState() {
        _captureState.value = CaptureUiState()
    }
}

/**
 * État de l'UI pour l'écran Capture
 */
data class CaptureUiState(
    val isProcessing: Boolean = false,
    val savedDiscoveryId: Int? = null,
    val error: String? = null
)
