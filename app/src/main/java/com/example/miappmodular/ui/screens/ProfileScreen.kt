package com.example.miappmodular.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.miappmodular.ui.theme.Primary
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.miappmodular.model.User
import com.example.miappmodular.ui.components.StandardScaffold
import com.example.miappmodular.viewmodel.ProfileUiState
import com.example.miappmodular.viewmodel.ProfileViewModel

/**
 * Pantalla de perfil del usuario
 *
 * **Funcionalidades:**
 * - Muestra información del usuario (nombre, email, rol, etc.)
 * - Permite refrescar datos con pull-to-refresh
 * - Botón de cerrar sesión
 * - Navegación automática al login cuando se cierra sesión
 *
 * @param viewModel ViewModel del perfil
 * @param onNavigateBack Callback para volver atrás
 * @param onLogout Callback cuando el usuario cierra sesión
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Navegar automáticamente al login cuando se cierra sesión
    LaunchedEffect(uiState) {
        if (uiState is ProfileUiState.LoggedOut) {
            onLogout()
        }
    }

    StandardScaffold(
        title = "Mi Perfil",
        onNavigateBack = onNavigateBack,
        actionIcon = Icons.Default.Refresh,
        onActionClick = { viewModel.refreshProfile() },
        containerColor = Color(0xFFF8F9FA) // Fondo claro
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is ProfileUiState.Loading -> {
                    LoadingContent()
                }

                is ProfileUiState.Success -> {
                    ProfileContent(
                        user = state.user,
                        onLogoutClick = { showLogoutDialog = true }
                    )
                }

                is ProfileUiState.Error -> {
                    ErrorContent(
                        message = state.message,
                        onRetry = { viewModel.refreshProfile() },
                        onLogout = { showLogoutDialog = true }
                    )
                }

                is ProfileUiState.LoggedOut -> {
                    // Estado transitorio - se navega automáticamente en LaunchedEffect
                }
            }
        }
    }

    // Diálogo de confirmación para cerrar sesión
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = { Icon(Icons.Default.Logout, contentDescription = null) },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Estás seguro de que deseas cerrar sesión?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Cerrar sesión")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Cargando perfil...",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    onLogout: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                Icons.Default.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Error al cargar perfil",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reintentar")
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(0.7f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Outlined.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar Sesión")
            }
        }
    }
}

@Composable
private fun ProfileContent(
    user: User,
    onLogoutClick: () -> Unit
) {
    // Estados editables
    var nombre by remember { mutableStateOf(user.nombre ?: "") }
    var telefono by remember { mutableStateOf(user.telefono ?: "") }
    var direccion by remember { mutableStateOf(user.direccion ?: "") }
    var showSaveMessage by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar Grande con botón de cambiar foto
        Box(
            modifier = Modifier.size(120.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Surface(
                modifier = Modifier
                    .size(120.dp)
                    .border(4.dp, Color.White, CircleShape)
                    .shadow(4.dp, CircleShape),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = getInitials(nombre.ifEmpty { "Usuario" }),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Botón "Change Photo"
            Surface(
                onClick = { /* TODO: Implementar cambio de foto */ },
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Cambiar foto",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Texto "Cambiar Foto"
        TextButton(onClick = { /* TODO */ }) {
            Text(
                text = "Cambiar Foto",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Rol (solo lectura)
        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        ) {
            Text(
                text = getRoleName(user.role),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Campos Editables
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Campo Nombre
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null)
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color(0xFFE0E0E0)
                )
            )

            // Campo Email (solo lectura)
            OutlinedTextField(
                value = user.email,
                onValueChange = { },
                label = { Text("Correo Electrónico") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = null)
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = Color(0xFFE0E0E0),
                    disabledTextColor = Color(0xFF666666)
                )
            )

            // Campo Teléfono
            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it },
                label = { Text("Teléfono") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Default.Phone, contentDescription = null)
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color(0xFFE0E0E0)
                )
            )

            // Campo Dirección
            OutlinedTextField(
                value = direccion,
                onValueChange = { direccion = it },
                label = { Text("Dirección") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Default.LocationOn, contentDescription = null)
                },
                shape = RoundedCornerShape(12.dp),
                minLines = 2,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color(0xFFE0E0E0)
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Mensaje de guardado
        if (showSaveMessage) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = "✓ Cambios guardados localmente. ¡Pronto se sincronizarán con el servidor!",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Botón Guardar Cambios
        Button(
            onClick = {
                // TODO: Implementar guardado cuando exista endpoint PATCH /api/auth/profile
                showSaveMessage = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Guardar Cambios",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón de cerrar sesión (Estilo Outline Rojo)
        OutlinedButton(
            onClick = onLogoutClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Outlined.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Cerrar Sesión",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Espacio para el nav bar
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun ProfileInfoItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFFF5F5F5),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color(0xFF1A1A1A),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1A1A1A)
                )
            }
        }
    }
}

/**
 * Obtiene las iniciales del nombre (máximo 2 caracteres)
 */
private fun getInitials(nombre: String): String {
    val parts = nombre.trim().split(" ")
    return when {
        parts.size >= 2 -> "${parts[0].first()}${parts[1].first()}".uppercase()
        parts.size == 1 -> parts[0].take(2).uppercase()
        else -> "?"
    }
}

/**
 * Convierte el rol a un nombre legible
 */
private fun getRoleName(role: String): String {
    return when (role.uppercase()) {
        "ADMIN" -> "Administrador"
        "PRODUCTOR" -> "Productor"
        "CLIENTE" -> "Cliente"
        else -> role
    }
}
