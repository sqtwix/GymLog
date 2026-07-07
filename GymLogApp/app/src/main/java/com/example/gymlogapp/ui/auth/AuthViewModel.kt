package com.example.gymlogapp.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymlogapp.data.ServiceLocator
import com.example.gymlogapp.data.network.LoginRequest
import com.example.gymlogapp.data.network.RegisterRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    object Success : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val authManager = ServiceLocator.getAuthManager(application)

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    val apiUrl = MutableStateFlow(authManager.getApiUrl())

    fun updateApiUrl(url: String) {
        apiUrl.value = url
        authManager.saveApiUrl(url)
        ServiceLocator.resetApiService()
    }

    fun login(email: String, password: String, onLoginSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Email and password cannot be empty")
            return
        }

        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                val apiService = ServiceLocator.getApiService(getApplication())
                val response = apiService.login(LoginRequest(email.trim(), password))
                authManager.saveToken(response.token)
                _uiState.value = AuthUiState.Success
                onLoginSuccess()
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.localizedMessage ?: "Failed to login. Please check URL and credentials.")
            }
        }
    }

    fun register(
        username: String,
        email: String,
        password: String,
        gender: String,
        birthDate: String,
        onRegisterSuccess: () -> Unit
    ) {
        if (username.isBlank() || email.isBlank() || password.isBlank() || gender.isBlank() || birthDate.isBlank()) {
            _uiState.value = AuthUiState.Error("All fields are required")
            return
        }

        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                val apiService = ServiceLocator.getApiService(getApplication())
                val response = apiService.register(
                    RegisterRequest(
                        username = username.trim(),
                        email = email.trim(),
                        password = password,
                        gender = gender, // Expected "M" or "F"
                        birthDate = birthDate // Expected "yyyy-MM-dd'T'HH:mm:ss"
                    )
                )
                // If register returns token, auto login, else wait for manual login
                authManager.saveToken(response.token)
                _uiState.value = AuthUiState.Success
                onRegisterSuccess()
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.localizedMessage ?: "Registration failed")
            }
        }
    }

    fun clearError() {
        _uiState.value = AuthUiState.Idle
    }
}
