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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.miappmodular.ui.components.StandardScaffold
import com.example.miappmodular.viewmodel.CreateProductoUiState
import com.example.miappmodular.viewmodel.CreateProductoViewModel

/**
 * Pantalla para crear un nuevo producto
 * Solo accesible para usuarios con rol PRODUCTOR
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProductoScreen(
    viewModel: CreateProductoViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onProductoCreated: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val nombre by viewModel.nombre.collectAsState()
    val descripcion by viewModel.descripcion.collectAsState()
    val precio by viewModel.precio.collectAsState()
    val unidad by viewModel.unidad.collectAsState()
    val stock by viewModel.stock.collectAsState()
    val productorId by viewModel.productorId.collectAsState()
    val focusManager = LocalFocusManager.current

    // Manejar éxito de creación
    LaunchedEffect(uiState) {
        if (uiState is CreateProductoUiState.Success) {
            onProductoCreated()
        }
    }

    StandardScaffold(
        title = "Crear Producto",
        onNavigateBack = onNavigateBack
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Nuevo Producto",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Completa los datos del producto para agregar al catálogo",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Campo de nombre
            OutlinedTextField(
                value = nombre,
                onValueChange = viewModel::onNombreChange,
                label = { Text("Nombre del producto *") },
                leadingIcon = {
                    Icon(Icons.Default.ShoppingBag, contentDescription = "Nombre")
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
                enabled = uiState !is CreateProductoUiState.Loading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de descripción
            OutlinedTextField(
                value = descripcion,
                onValueChange = viewModel::onDescripcionChange,
                label = { Text("Descripción *") },
                leadingIcon = {
                    Icon(Icons.Default.Description, contentDescription = "Descripción")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                maxLines = 4,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is CreateProductoUiState.Loading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campos de precio y unidad en fila
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = precio,
                    onValueChange = viewModel::onPrecioChange,
                    label = { Text("Precio *") },
                    leadingIcon = {
                        Icon(Icons.Default.AttachMoney, contentDescription = "Precio")
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Right) }
                    ),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    enabled = uiState !is CreateProductoUiState.Loading
                )

                OutlinedTextField(
                    value = unidad,
                    onValueChange = viewModel::onUnidadChange,
                    label = { Text("Unidad *") },
                    placeholder = { Text("kg, unidad, etc.") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    enabled = uiState !is CreateProductoUiState.Loading
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de stock
            OutlinedTextField(
                value = stock,
                onValueChange = viewModel::onStockChange,
                label = { Text("Stock disponible *") },
                leadingIcon = {
                    Icon(Icons.Default.Inventory, contentDescription = "Stock")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is CreateProductoUiState.Loading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de ID del productor
            // TODO: Esto debería obtenerse automáticamente del usuario logueado
            OutlinedTextField(
                value = productorId,
                onValueChange = viewModel::onProductorIdChange,
                label = { Text("ID del Productor *") },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = "Productor")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        viewModel.createProducto()
                    }
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is CreateProductoUiState.Loading,
                supportingText = {
                    Text("Este campo se obtendrá automáticamente del usuario logueado")
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botón de crear
            Button(
                onClick = { viewModel.createProducto() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = uiState !is CreateProductoUiState.Loading
            ) {
                if (uiState is CreateProductoUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Crear Producto")
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
            if (uiState is CreateProductoUiState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = (uiState as CreateProductoUiState.Error).message,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}
