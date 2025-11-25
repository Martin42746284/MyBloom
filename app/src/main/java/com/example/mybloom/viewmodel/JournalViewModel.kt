package com.example.mybloom.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybloom.entities.DiscoveryEntity
import com.example.mybloom.repository.DiscoveryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel pour l'écran Journal qui affiche toutes les découvertes
 * Gère l'état des découvertes et les opérations CRUD
 */
class JournalViewModel(
    private val repository: DiscoveryRepository
) : ViewModel() {

    // État des découvertes de l'utilisateur connecté
    val discoveries: StateFlow<List<DiscoveryEntity>> = repository.getUserDiscoveries()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    // État de l'UI pour le Journal
    private val _uiState = MutableStateFlow(JournalUiState())
    val uiState: StateFlow<JournalUiState> = _uiState.asStateFlow()

    // État de recherche
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Découvertes filtrées par recherche
    private val _filteredDiscoveries = MutableStateFlow<List<DiscoveryEntity>>(emptyList())
    val filteredDiscoveries: StateFlow<List<DiscoveryEntity>> = _filteredDiscoveries.asStateFlow()

    init {
        // Observer les découvertes et appliquer le filtre de recherche
        viewModelScope.launch {
            discoveries.collect { allDiscoveries ->
                applySearchFilter(allDiscoveries)
            }
        }
    }

    /**
     * Supprimer une découverte
     * Supprime à la fois l'image locale et l'entrée de la base de données
     */
    fun deleteDiscovery(discovery: DiscoveryEntity) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // Supprimer via le repository (gère image + DB)
                repository.deleteDiscovery(discovery)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Découverte supprimée"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Erreur lors de la suppression: ${e.message}"
                )
            }
        }
    }

    /**
     * Rechercher des découvertes par nom
     */
    fun searchDiscoveries(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            discoveries.value.let { allDiscoveries ->
                applySearchFilter(allDiscoveries)
            }
        }
    }

    /**
     * Appliquer le filtre de recherche
     */
    private fun applySearchFilter(allDiscoveries: List<DiscoveryEntity>) {
        val query = _searchQuery.value
        _filteredDiscoveries.value = if (query.isBlank()) {
            allDiscoveries
        } else {
            allDiscoveries.filter { discovery ->
                discovery.plantName.contains(query, ignoreCase = true) ||
                        discovery.aiFact.contains(query, ignoreCase = true)
            }
        }
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _filteredDiscoveries.value = discoveries.value
    }

    fun getDiscoveryCount(): Int {
        return discoveries.value.size
    }

    fun resetUiState() {
        _uiState.value = JournalUiState()
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            successMessage = null,
            error = null
        )
    }
}

data class JournalUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)
