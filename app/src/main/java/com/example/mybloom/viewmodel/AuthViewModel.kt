package com.example.mybloom.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybloom.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    /**
     * Sign in with email and password
     */
    fun signIn(email: String, password: String, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            _error.value = "Email and password are required."
            return
        }

        _loading.value = true
        _error.value = null

        viewModelScope.launch {
            val result = authRepository.signIn(email, password)
            _loading.value = false

            result.onSuccess {
                onSuccess()
            }.onFailure { exception ->
                _error.value = exception.message ?: "Authentication error"
            }
        }
    }

    /**
     * Sign up with email and password
     * Validation (password confirmation) should be done in the UI layer
     */
    fun signUp(email: String, password: String, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            _error.value = "Email and password are required."
            return
        }

        if (password.length < 6) {
            _error.value = "Password must be at least 6 characters."
            return
        }

        _loading.value = true
        _error.value = null

        viewModelScope.launch {
            val result = authRepository.signUp(email, password)
            _loading.value = false

            result.onSuccess {
                onSuccess()
            }.onFailure { exception ->
                _error.value = exception.message ?: "Account creation error"
            }
        }
    }

    /**
     * Sign in with Google using Credential Manager
     */
    fun signInWithGoogle(context: Context, onSuccess: () -> Unit) {  // ✅ Pas de webClientId
        _loading.value = true
        _error.value = null

        viewModelScope.launch {
            val result = authRepository.signInWithGoogle(context)
            _loading.value = false

            result.onSuccess { onSuccess() }
                .onFailure { _error.value = it.message ?: "Google Sign-In failed" }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }
}
