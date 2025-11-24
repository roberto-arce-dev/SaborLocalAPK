package com.example.miappmodular.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.miappmodular.viewmodel.CreateProductorUiState
import com.example.miappmodular.viewmodel.CreateProductorViewModel

/**
 * Pantalla para crear un nuevo productor
 * Solo accesible para usuarios con rol ADMIN
 *
 * IMPORTANTE: Esta pantalla usa el endpoint POST /api/auth/create-productor
 * que requiere autenticación de ADMIN
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProductorScreen(
    viewModel: CreateProductorViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onProductorCreated: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val nombre by viewModel.nombre.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val ubicacion by viewModel.ubicacion.collectAsState()
    val telefono by viewModel.telefono.collectAsState()

    // Errores de validación
    val nombreError by viewModel.nombreError.collectAsState()
    val emailError by viewModel.emailError.collectAsState()
    val passwordError by viewModel.passwordError.collectAsState()
    val ubicacionError by viewModel.ubicacionError.collectAsState()
    val telefonoError by viewModel.telefonoError.collectAsState()

    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    var passwordVisible by remember { mutableStateOf(false) }
    var ubicacionExpanded by remember { mutableStateOf(false) }

    // Scroll automático al mostrar mensaje de éxito
    LaunchedEffect(uiState) {
        if (uiState is CreateProductorUiState.Success) {
            kotlinx.coroutines.delay(100) // Pequeño delay para que se renderice primero
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    // Lista de regiones de Chile para autocompletado
    val regionesChile = listOf(
        "Arica y Parinacota",
        "Tarapacá",
        "Antofagasta",
        "Atacama",
        "Coquimbo",
        "Valparaíso",
        "Metropolitana de Santiago",
        "O'Higgins",
        "Maule",
        "Ñuble",
        "Biobío",
        "La Araucanía",
        "Los Ríos",
        "Los Lagos",
        "Aysén",
        "Magallanes y la Antártica Chilena"
    )

    // Filtrar regiones según el texto ingresado
    val regionesFiltradas = remember(ubicacion) {
        if (ubicacion.isBlank()) {
            regionesChile
        } else {
            regionesChile.filter { it.contains(ubicacion, ignoreCase = true) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Productor") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Icono de admin
            Surface(
                modifier = Modifier.size(80.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Nuevo Productor",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Solo administradores pueden crear productores",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Campo de nombre
            OutlinedTextField(
                value = nombre,
                onValueChange = viewModel::onNombreChange,
                label = { Text("Nombre del productor *") },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = "Nombre")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is CreateProductorUiState.Loading,
                isError = nombreError != null,
                supportingText = nombreError?.let { { Text(it) } }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de email
            OutlinedTextField(
                value = email,
                onValueChange = viewModel::onEmailChange,
                label = { Text("Email *") },
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = "Email")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is CreateProductorUiState.Loading,
                isError = emailError != null,
                supportingText = emailError?.let { { Text(it) } }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de contraseña
            OutlinedTextField(
                value = password,
                onValueChange = viewModel::onPasswordChange,
                label = { Text("Contraseña *") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = "Contraseña")
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Ocultar" else "Mostrar"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is CreateProductorUiState.Loading,
                isError = passwordError != null,
                supportingText = passwordError?.let { { Text(it) } }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de ubicación con autocompletado
            ExposedDropdownMenuBox(
                expanded = ubicacionExpanded && regionesFiltradas.isNotEmpty(),
                onExpandedChange = { ubicacionExpanded = !ubicacionExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = ubicacion,
                    onValueChange = {
                        viewModel.onUbicacionChange(it)
                        ubicacionExpanded = true
                    },
                    label = { Text("Ubicación *") },
                    leadingIcon = {
                        Icon(Icons.Default.LocationOn, contentDescription = "Ubicación")
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = ubicacionExpanded)
                    },
                    placeholder = { Text("Selecciona o escribe una región") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            ubicacionExpanded = false
                            focusManager.moveFocus(FocusDirection.Down)
                        }
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    enabled = uiState !is CreateProductorUiState.Loading,
                    isError = ubicacionError != null,
                    supportingText = ubicacionError?.let { { Text(it) } }
                )

                ExposedDropdownMenu(
                    expanded = ubicacionExpanded && regionesFiltradas.isNotEmpty(),
                    onDismissRequest = { ubicacionExpanded = false }
                ) {
                    regionesFiltradas.forEach { region ->
                        DropdownMenuItem(
                            text = { Text(region) },
                            onClick = {
                                viewModel.onUbicacionChange(region)
                                ubicacionExpanded = false
                                focusManager.moveFocus(FocusDirection.Down)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Place, contentDescription = null)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de teléfono
            OutlinedTextField(
                value = telefono,
                onValueChange = viewModel::onTelefonoChange,
                label = { Text("Teléfono *") },
                leadingIcon = {
                    Icon(Icons.Default.Phone, contentDescription = "Teléfono")
                },
                placeholder = { Text("+56912345678") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        viewModel.createProductor()
                    }
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is CreateProductorUiState.Loading,
                isError = telefonoError != null,
                supportingText = telefonoError?.let { { Text(it) } }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botón de crear
            Button(
                onClick = { viewModel.createProductor() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = uiState !is CreateProductorUiState.Loading
            ) {
                if (uiState is CreateProductorUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.PersonAdd, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Crear Productor")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Texto de campos obligatorios
            Text(
                text = "* Campos obligatorios",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Nota importante
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "El productor creado podrá iniciar sesión y gestionar sus productos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            // Mostrar error si existe
            if (uiState is CreateProductorUiState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = (uiState as CreateProductorUiState.Error).message,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // Mostrar mensaje de éxito con animación
            AnimatedVisibility(
                visible = uiState is CreateProductorUiState.Success,
                enter = slideInVertically(
                    initialOffsetY = { it / 2 }
                ) + fadeIn(),
                exit = slideOutVertically(
                    targetOffsetY = { it / 2 }
                ) + fadeOut()
            ) {
                val successState = uiState as? CreateProductorUiState.Success
                if (successState != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "¡Productor creado exitosamente!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Usuario: ${successState.user.nombre}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Email: ${successState.user.email}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    viewModel.clearForm()
                                    viewModel.resetState()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Crear otro productor")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = onProductorCreated,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Volver al inicio")
                            }
                        }
                    }
                }
            }
        }
    }
}
