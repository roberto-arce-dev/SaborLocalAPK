package com.example.miappmodular.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.miappmodular.repository.AuthSaborLocalRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla splash
 * Verifica si hay una sesión activa al iniciar la app
 *
 * **Arquitectura simple:**
 * El ViewModel crea su propio repository directamente.
 */
class SplashViewModel(application: Application) : AndroidViewModel(application) {

    // El repository se crea directamente (sin inyección de dependencias)
    private val repository = AuthSaborLocalRepository()

    private val _navigationState = MutableStateFlow<SplashNavigationState>(SplashNavigationState.Checking)
    val navigationState: StateFlow<SplashNavigationState> = _navigationState.asStateFlow()

    init {
        checkSession()
    }

    /**
     * Verifica si hay una sesión activa
     */
    private fun checkSession() {
        viewModelScope.launch {
            // Pequeño delay para mostrar el splash (opcional)
            delay(1000)

            // Verificar si hay token guardado
            val isLoggedIn = repository.isLoggedIn()

            if (isLoggedIn) {
                // Verificar que el usuario existe en sesión
                val currentUser = repository.getCurrentUser()
                if (currentUser != null) {
                    _navigationState.value = SplashNavigationState.NavigateToHome
                } else {
                    _navigationState.value = SplashNavigationState.NavigateToLogin
                }
            } else {
                _navigationState.value = SplashNavigationState.NavigateToLogin
            }
        }
    }
}

/**
 * Estados de navegación del splash
 */
sealed class SplashNavigationState {
    object Checking : SplashNavigationState()
    object NavigateToHome : SplashNavigationState()
    object NavigateToLogin : SplashNavigationState()
}
