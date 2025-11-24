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
 * ViewModel para la pantalla de Registro
 *
 * Maneja el estado de la UI y las operaciones de registro.
 * Solo permite registro de CLIENTES (auto-registro).
 */
class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthSaborLocalRepository(application)

    // Estado de la UI
    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    // Campos del formulario
    private val _nombre = MutableStateFlow("")
    val nombre: StateFlow<String> = _nombre.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    private val _telefono = MutableStateFlow("")
    val telefono: StateFlow<String> = _telefono.asStateFlow()

    private val _direccion = MutableStateFlow("")
    val direccion: StateFlow<String> = _direccion.asStateFlow()

    /**
     * Actualiza el nombre del formulario
     */
    fun onNombreChange(newNombre: String) {
        _nombre.value = newNombre
    }

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
     * Actualiza la confirmación de contraseña
     */
    fun onConfirmPasswordChange(newConfirmPassword: String) {
        _confirmPassword.value = newConfirmPassword
    }

    /**
     * Actualiza el teléfono del formulario
     */
    fun onTelefonoChange(newTelefono: String) {
        _telefono.value = newTelefono
    }

    /**
     * Actualiza la dirección del formulario
     */
    fun onDireccionChange(newDireccion: String) {
        _direccion.value = newDireccion
    }

    /**
     * Realiza el registro de un nuevo CLIENTE
     */
    fun register() {
        // Validar campos obligatorios
        if (_nombre.value.isBlank() || _email.value.isBlank() || _password.value.isBlank()) {
            _uiState.value = RegisterUiState.Error("Por favor completa todos los campos obligatorios")
            return
        }

        // Validar formato de email
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(_email.value).matches()) {
            _uiState.value = RegisterUiState.Error("Email inválido")
            return
        }

        // Validar longitud mínima de contraseña
        if (_password.value.length < 6) {
            _uiState.value = RegisterUiState.Error("La contraseña debe tener al menos 6 caracteres")
            return
        }

        // Validar que las contraseñas coincidan
        if (_password.value != _confirmPassword.value) {
            _uiState.value = RegisterUiState.Error("Las contraseñas no coinciden")
            return
        }

        _uiState.value = RegisterUiState.Loading

        viewModelScope.launch {
            val result = repository.register(
                nombre = _nombre.value,
                email = _email.value,
                password = _password.value,
                telefono = _telefono.value.ifBlank { null },
                direccion = _direccion.value.ifBlank { null }
            )

            _uiState.value = if (result.isSuccess) {
                RegisterUiState.Success(result.getOrNull()!!)
            } else {
                RegisterUiState.Error(result.exceptionOrNull()?.message ?: "Error desconocido")
            }
        }
    }

    /**
     * Resetea el estado a Idle
     */
    fun resetState() {
        _uiState.value = RegisterUiState.Idle
    }

    /**
     * Limpia los campos del formulario
     */
    fun clearForm() {
        _nombre.value = ""
        _email.value = ""
        _password.value = ""
        _confirmPassword.value = ""
        _telefono.value = ""
        _direccion.value = ""
    }
}

/**
 * Estados posibles de la UI de Registro
 */
sealed class RegisterUiState {
    object Idle : RegisterUiState()
    object Loading : RegisterUiState()
    data class Success(val user: User) : RegisterUiState()
    data class Error(val message: String) : RegisterUiState()
}
