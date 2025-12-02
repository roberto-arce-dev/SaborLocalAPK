package com.example.miappmodular.ui.screens

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.miappmodular.ui.components.StandardScaffold
import com.example.miappmodular.viewmodel.RegisterUiState
import com.example.miappmodular.viewmodel.RegisterViewModel

/**
 * Pantalla de Registro
 *
 * Permite a nuevos usuarios registrarse como CLIENTES.
 * Solo CLIENTES pueden auto-registrarse.
 * Los PRODUCTORES deben ser creados por el ADMIN.
 *
 * @param viewModel ViewModel de Registro
 * @param onRegisterSuccess Callback cuando el registro es exitoso
 * @param onNavigateToLogin Callback para navegar a login
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = viewModel(),
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val nombre by viewModel.nombre.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val confirmPassword by viewModel.confirmPassword.collectAsState()
    val role by viewModel.role.collectAsState()
    val telefono by viewModel.telefono.collectAsState()
    val direccion by viewModel.direccion.collectAsState()
    val nombreNegocio by viewModel.nombreNegocio.collectAsState()
    val descripcion by viewModel.descripcion.collectAsState()
    val focusManager = LocalFocusManager.current

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var expandedRoleMenu by remember { mutableStateOf(false) }

    // Manejar éxito del registro
    LaunchedEffect(uiState) {
        if (uiState is RegisterUiState.Success) {
            onRegisterSuccess()
        }
    }

    StandardScaffold(
        title = "Crear Cuenta",
        onNavigateBack = onNavigateToLogin,
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = if (role == "CLIENTE") {
                    "Regístrate como cliente y empieza a comprar productos frescos"
                } else {
                    "Regístrate como productor y ofrece tus productos"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Selector de Rol (obligatorio)
            ExposedDropdownMenuBox(
                expanded = expandedRoleMenu,
                onExpandedChange = { expandedRoleMenu = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = when (role) {
                        "CLIENTE" -> "Cliente"
                        "PRODUCTOR" -> "Productor"
                        else -> role
                    },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo de cuenta *") },
                    leadingIcon = {
                        Icon(Icons.Default.Badge, contentDescription = "Rol")
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRoleMenu)
                    },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    enabled = uiState !is RegisterUiState.Loading
                )

                ExposedDropdownMenu(
                    expanded = expandedRoleMenu,
                    onDismissRequest = { expandedRoleMenu = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = "Cliente",
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Compra productos frescos",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        onClick = {
                            viewModel.onRoleChange("CLIENTE")
                            expandedRoleMenu = false
                        },
                        leadingIcon = {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null)
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = "Productor",
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Vende tus productos",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        onClick = {
                            viewModel.onRoleChange("PRODUCTOR")
                            expandedRoleMenu = false
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Storefront, contentDescription = null)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de Nombre (obligatorio)
            OutlinedTextField(
                value = nombre,
                onValueChange = viewModel::onNombreChange,
                label = { Text("Nombre completo *") },
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
                enabled = uiState !is RegisterUiState.Loading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de Email (obligatorio)
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
                enabled = uiState !is RegisterUiState.Loading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de Contraseña (obligatorio)
            OutlinedTextField(
                value = password,
                onValueChange = viewModel::onPasswordChange,
                label = { Text("Contraseña * (mínimo 6 caracteres)") },
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
                enabled = uiState !is RegisterUiState.Loading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de Confirmar Contraseña (obligatorio)
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChange,
                label = { Text("Confirmar contraseña *") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = "Confirmar")
                },
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (confirmPasswordVisible) "Ocultar" else "Mostrar"
                        )
                    }
                },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is RegisterUiState.Loading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de Teléfono (opcional)
            OutlinedTextField(
                value = telefono,
                onValueChange = viewModel::onTelefonoChange,
                label = { Text("Teléfono (opcional)") },
                leadingIcon = {
                    Icon(Icons.Default.Phone, contentDescription = "Teléfono")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is RegisterUiState.Loading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de Dirección (opcional)
            OutlinedTextField(
                value = direccion,
                onValueChange = viewModel::onDireccionChange,
                label = { Text("Dirección (opcional)") },
                leadingIcon = {
                    Icon(Icons.Default.Home, contentDescription = "Dirección")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = if (role == "PRODUCTOR") ImeAction.Next else ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (role == "CLIENTE") {
                            focusManager.clearFocus()
                            viewModel.register()
                        } else {
                            focusManager.moveFocus(FocusDirection.Down)
                        }
                    },
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is RegisterUiState.Loading
            )

            // Campos específicos para PRODUCTOR
            if (role == "PRODUCTOR") {
                Spacer(modifier = Modifier.height(16.dp))

                // Título de sección para campos de productor
                Text(
                    text = "Información del Negocio",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Campo de Nombre del Negocio (obligatorio para PRODUCTOR)
                OutlinedTextField(
                    value = nombreNegocio,
                    onValueChange = viewModel::onNombreNegocioChange,
                    label = { Text("Nombre del negocio *") },
                    placeholder = { Text("Ej: Frutas del Valle") },
                    leadingIcon = {
                        Icon(Icons.Default.Storefront, contentDescription = "Negocio")
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
                    enabled = uiState !is RegisterUiState.Loading
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Campo de Descripción del Negocio (opcional para PRODUCTOR)
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = viewModel::onDescripcionChange,
                    label = { Text("Descripción del negocio (opcional)") },
                    placeholder = { Text("Ej: Producimos frutas orgánicas de calidad") },
                    leadingIcon = {
                        Icon(Icons.Default.Description, contentDescription = "Descripción")
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            viewModel.register()
                        }
                    ),
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState !is RegisterUiState.Loading
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botón de Registro
            Button(
                onClick = { viewModel.register() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = uiState !is RegisterUiState.Loading
            ) {
                if (uiState is RegisterUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Crear Cuenta")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Texto de campos obligatorios
            Text(
                text = "* Campos obligatorios",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Mostrar error si existe
            if (uiState is RegisterUiState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = (uiState as RegisterUiState.Error).message,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}
