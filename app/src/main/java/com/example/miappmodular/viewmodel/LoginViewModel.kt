package com.example.miappmodular.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.miappmodular.model.User
import com.example.miappmodular.repository.AuthSaborLocalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de Login
 *
 * Maneja el estado de la UI y las operaciones de autenticación.
 * Usa StateFlow para exponer el estado reactivamente a la UI.
 */
class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthSaborLocalRepository(application)

    // Estado de la UI
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // Campos del formulario
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    /**
     * Actualiza el email del formulario
     */
    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
    }

    /**
     * Actualiza la contraseña del formulario
     */
    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }

    /**
     * Verifica si hay una sesión activa
     */
    fun isLoggedIn(): Boolean {
        return repository.isLoggedIn()
    }

    /**
     * Obtiene el usuario actual de la sesión
     */
    fun getCurrentUser(): User? {
        return repository.getCurrentUser()
    }

    /**
     * Realiza el login con email y password
     */
    fun login() {
        // Validar campos
        if (_email.value.isBlank() || _password.value.isBlank()) {
            _uiState.value = LoginUiState.Error("Por favor completa todos los campos")
            return
        }

        // Validar formato de email
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(_email.value).matches()) {
            _uiState.value = LoginUiState.Error("Email inválido")
            return
        }

        _uiState.value = LoginUiState.Loading

        viewModelScope.launch {
            val result = repository.login(_email.value, _password.value)

            _uiState.value = if (result.isSuccess) {
                LoginUiState.Success(result.getOrNull()!!)
            } else {
                LoginUiState.Error(result.exceptionOrNull()?.message ?: "Error desconocido")
            }
        }
    }

    /**
     * Resetea el estado a Idle
     */
    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }

    /**
     * Limpia los campos del formulario
     */
    fun clearForm() {
        _email.value = ""
        _password.value = ""
    }
}

/**
 * Estados posibles de la UI de Login
 */
sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val user: User) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}
