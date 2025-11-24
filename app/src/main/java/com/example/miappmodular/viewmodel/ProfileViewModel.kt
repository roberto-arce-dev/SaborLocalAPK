package com.example.miappmodular.viewmodel

/**
 * ARCHIVO TEMPORALMENTE DESHABILITADO
 *
 * Este ProfileViewModel usaba UserRepository con Room (base de datos local)
 * que fue eliminado cuando migramos al backend de SaborLocal.
 *
 * TODO: Implementar nuevo ProfileViewModel que use AuthSaborLocalRepository
 * y el modelo User de SaborLocalModels.kt.
 */

/*
import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.miappmodular.AppDependencies
import com.example.miappmodular.data.local.entity.User
import com.example.miappmodular.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

data class ProfileUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val error: String? = null,
    val formattedCreatedAt: String = "",
    val avatarUri: Uri? = null
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    // Código comentado - ver AuthSaborLocalRepository para nueva implementación
}
*/
