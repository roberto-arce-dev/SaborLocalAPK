package com.example.miappmodular.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.miappmodular.model.User
import com.example.miappmodular.repository.AuthSaborLocalRepository
import com.example.miappmodular.utils.ValidationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para crear productores
 * Solo ADMIN puede crear productores
 *
 * **Arquitectura simple:**
 * El ViewModel crea su propio repository directamente.
 */
class CreateProductorViewModel(application: Application) : AndroidViewModel(application) {

    // El repository se crea directamente (sin inyección de dependencias)
    private val repository = AuthSaborLocalRepository()

    // Estado de la UI
    private val _uiState = MutableStateFlow<CreateProductorUiState>(CreateProductorUiState.Idle)
    val uiState: StateFlow<CreateProductorUiState> = _uiState.asStateFlow()

    // Campos del formulario
    private val _nombre = MutableStateFlow("")
    val nombre: StateFlow<String> = _nombre.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _ubicacion = MutableStateFlow("")
    val ubicacion: StateFlow<String> = _ubicacion.asStateFlow()

    private val _telefono = MutableStateFlow("")
    val telefono: StateFlow<String> = _telefono.asStateFlow()

    // Errores de validación
    private val _nombreError = MutableStateFlow<String?>(null)
    val nombreError: StateFlow<String?> = _nombreError.asStateFlow()

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError.asStateFlow()

    private val _ubicacionError = MutableStateFlow<String?>(null)
    val ubicacionError: StateFlow<String?> = _ubicacionError.asStateFlow()

    private val _telefonoError = MutableStateFlow<String?>(null)
    val telefonoError: StateFlow<String?> = _telefonoError.asStateFlow()

    /**
     * Actualiza el nombre y valida
     */
    fun onNombreChange(newNombre: String) {
        _nombre.value = newNombre
        _nombreError.value = ValidationUtils.isValidName(newNombre)
    }

    /**
     * Actualiza el email y valida
     */
    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
        _emailError.value = ValidationUtils.validateEmail(newEmail)
    }

    /**
     * Actualiza la contraseña y valida
     */
    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
        _passwordError.value = ValidationUtils.validatePassword(newPassword)
    }

    /**
     * Actualiza la ubicación y valida
     */
    fun onUbicacionChange(newUbicacion: String) {
        _ubicacion.value = newUbicacion
        _ubicacionError.value = if (newUbicacion.isBlank()) "La ubicación es obligatoria" else null
    }

    /**
     * Actualiza el teléfono y valida
     */
    fun onTelefonoChange(newTelefono: String) {
        _telefono.value = newTelefono
        _telefonoError.value = ValidationUtils.validatePhone(newTelefono)
    }

    /**
     * Crea un nuevo productor
     * Esta operación requiere token de ADMIN
     */
    fun createProductor() {
        // Validar todos los campos usando ValidationUtils
        val nombreValidation = ValidationUtils.isValidName(_nombre.value)
        val emailValidation = ValidationUtils.validateEmail(_email.value)
        val passwordValidation = ValidationUtils.validatePassword(_password.value)
        val ubicacionValidation = if (_ubicacion.value.isBlank()) "La ubicación es obligatoria" else null
        val telefonoValidation = ValidationUtils.validatePhone(_telefono.value)

        // Actualizar errores
        _nombreError.value = nombreValidation
        _emailError.value = emailValidation
        _passwordError.value = passwordValidation
        _ubicacionError.value = ubicacionValidation
        _telefonoError.value = telefonoValidation

        // Si hay algún error, no proceder
        if (nombreValidation != null || emailValidation != null || passwordValidation != null ||
            ubicacionValidation != null || telefonoValidation != null) {
            _uiState.value = CreateProductorUiState.Error("Por favor corrige los errores del formulario")
            return
        }

        _uiState.value = CreateProductorUiState.Loading

        viewModelScope.launch {
            val result = repository.createProductorUser(
                nombre = _nombre.value,
                email = _email.value,
                password = _password.value,
                ubicacion = _ubicacion.value,
                telefono = _telefono.value
            )

            _uiState.value = if (result.isSuccess) {
                CreateProductorUiState.Success(result.getOrNull()!!)
            } else {
                CreateProductorUiState.Error(result.exceptionOrNull()?.message ?: "Error desconocido")
            }
        }
    }

    /**
     * Resetea el estado a Idle
     */
    fun resetState() {
        _uiState.value = CreateProductorUiState.Idle
    }

    /**
     * Limpia el formulario
     */
    fun clearForm() {
        _nombre.value = ""
        _email.value = ""
        _password.value = ""
        _ubicacion.value = ""
        _telefono.value = ""
    }
}

/**
 * Estados posibles de la UI de creación de productores
 */
sealed class CreateProductorUiState {
    object Idle : CreateProductorUiState()
    object Loading : CreateProductorUiState()
    data class Success(val user: User) : CreateProductorUiState()
    data class Error(val message: String) : CreateProductorUiState()
}
