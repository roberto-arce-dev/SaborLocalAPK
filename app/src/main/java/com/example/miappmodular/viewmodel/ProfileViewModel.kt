package com.example.miappmodular.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.miappmodular.model.ApiResult
import com.example.miappmodular.model.User
import com.example.miappmodular.repository.AuthSaborLocalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para el perfil del usuario autenticado.
 *
 * Usa AuthSaborLocalRepository para obtener/actualizar los datos del usuario
 * y gestionar el cierre de sesión.
 */
class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthSaborLocalRepository()

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        refreshProfile()
    }

    /**
     * Obtiene el perfil desde el backend. Si no hay sesión activa, navega al login.
     */
    fun refreshProfile() {
        if (!repository.isLoggedIn()) {
            _uiState.value = ProfileUiState.LoggedOut
            return
        }

        // Mostrar datos locales rápido si existen
        repository.getCurrentUser()?.let {
            _uiState.value = ProfileUiState.Success(it)
        } ?: run {
            _uiState.value = ProfileUiState.Loading
        }

        viewModelScope.launch {
            when (val result = repository.getProfile()) {
                is ApiResult.Success -> _uiState.value = ProfileUiState.Success(result.data)
                is ApiResult.Error -> _uiState.value = ProfileUiState.Error(result.message)
            }
        }
    }

    /**
     * Cierra la sesión y notifica a la UI para navegar al login.
     */
    fun logout() {
        repository.logout()
        _uiState.value = ProfileUiState.LoggedOut
    }
}

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val user: User) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
    object LoggedOut : ProfileUiState()
}
