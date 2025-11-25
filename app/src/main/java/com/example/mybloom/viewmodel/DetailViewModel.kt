package com.example.mybloom.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybloom.entities.DiscoveryEntity
import com.example.mybloom.repository.DiscoveryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel pour l'écran de détail d'une découverte
 */
class DetailViewModel(
    private val repository: DiscoveryRepository
) : ViewModel() {

    private val _detailState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val detailState: StateFlow<DetailUiState> = _detailState.asStateFlow()

    /**
     * Charger une découverte par son ID
     */
    fun loadDiscovery(discoveryId: Int) {
        viewModelScope.launch {
            _detailState.value = DetailUiState.Loading

            try {
                val discovery = repository.getDiscoveryById(discoveryId)
                if (discovery != null) {
                    _detailState.value = DetailUiState.Success(discovery)
                } else {
                    _detailState.value = DetailUiState.Error("Découverte introuvable")
                }
            } catch (e: Exception) {
                _detailState.value = DetailUiState.Error(e.message ?: "Erreur de chargement")
            }
        }
    }

    /**
     * Supprimer la découverte actuelle
     */
    fun deleteDiscovery(discovery: DiscoveryEntity, onDeleted: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.deleteDiscovery(discovery)
                onDeleted()
            } catch (e: Exception) {
                _detailState.value = DetailUiState.Error("Erreur de suppression: ${e.message}")
            }
        }
    }
}

/**
 * État de l'UI pour l'écran de détail
 */
sealed class DetailUiState {
    object Loading : DetailUiState()
    data class Success(val discovery: DiscoveryEntity) : DetailUiState()
    data class Error(val message: String) : DetailUiState()
}
