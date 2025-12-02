package com.example.miappmodular.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.miappmodular.data.local.CarritoManager
import com.example.miappmodular.model.Producto
import com.example.miappmodular.ui.components.StandardScaffold
import com.example.miappmodular.viewmodel.ProductorDetalleUiState
import com.example.miappmodular.viewmodel.ProductorDetalleViewModel

/**
 * Pantalla que muestra el catálogo de productos de un productor específico
 *
 * **Flujo del cliente:**
 * Cliente selecciona un productor → Ve sus productos → Agrega al carrito
 *
 * @param productorId ID del productor
 * @param onNavigateBack Callback para volver atrás
 * @param onNavigateToCarrito Callback para ir al carrito
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductorDetalleScreen(
    productorId: String,
    viewModel: ProductorDetalleViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToCarrito: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val productorNombre by viewModel.productorNombre.collectAsState()
    val itemsEnCarrito by CarritoManager.items.collectAsState()

    // Cargar productos al iniciar
    LaunchedEffect(productorId) {
        viewModel.loadProductos(productorId)
    }

    StandardScaffold(
        title = productorNombre.ifEmpty { "Productos" },
        onNavigateBack = onNavigateBack,
        actionContent = {
            // Badge del carrito
            BadgedBox(
                badge = {
                    if (itemsEnCarrito.isNotEmpty()) {
                        Badge {
                            Text("${CarritoManager.getCantidadTotal()}")
                        }
                    }
                }
            ) {
                IconButton(onClick = onNavigateToCarrito) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = "Ver carrito")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is ProductorDetalleUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is ProductorDetalleUiState.Empty -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                Icons.Default.ShoppingBag,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Este productor aún no tiene productos",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                is ProductorDetalleUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.productos) { producto ->
                            ProductoCardConCarrito(
                                producto = producto,
                                onAgregarAlCarrito = { cantidad ->
                                    viewModel.agregarAlCarrito(producto, cantidad)
                                },
                                cantidadEnCarrito = viewModel.getCantidadEnCarrito(producto.id)
                            )
                        }
                    }
                }

                is ProductorDetalleUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                Icons.Default.ErrorOutline,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Error",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.loadProductos(productorId) }) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Reintentar")
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Card de producto con controles para agregar al carrito
 */
@Composable
fun ProductoCardConCarrito(
    producto: Producto,
    onAgregarAlCarrito: (Int) -> Unit,
    cantidadEnCarrito: Int
) {
    var cantidad by remember { mutableStateOf(1) }
    var showDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Imagen del producto
            AsyncImage(
                model = producto.imagenThumbnail ?: producto.imagen,
                contentDescription = producto.nombre,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Información del producto
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                // Nombre
                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Descripción
                Text(
                    text = producto.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Precio
                Text(
                    text = producto.getPrecioFormateado(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Stock
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = if (producto.stock > 0)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (producto.stock > 0) "Stock: ${producto.stock}" else "Sin stock",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (producto.stock > 0)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onErrorContainer
                        )
                    }

                    // Mostrar si ya está en el carrito
                    if (cantidadEnCarrito > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.ShoppingCart,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$cantidadEnCarrito",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Botón agregar al carrito
                Button(
                    onClick = { showDialog = true },
                    enabled = producto.stock > 0,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.AddShoppingCart, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Agregar al carrito")
                }
            }
        }
    }

    // Diálogo para seleccionar cantidad
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            icon = {
                Icon(Icons.Default.AddShoppingCart, contentDescription = null)
            },
            title = {
                Text("Agregar ${producto.nombre}")
            },
            text = {
                Column {
                    Text("Selecciona la cantidad:")
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Botón -
                        IconButton(
                            onClick = { if (cantidad > 1) cantidad-- }
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Disminuir")
                        }

                        // Cantidad
                        Text(
                            text = "$cantidad",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )

                        // Botón +
                        IconButton(
                            onClick = { if (cantidad < producto.stock) cantidad++ }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Aumentar")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Stock disponible: ${producto.stock}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Subtotal: ${"$%.2f".format(producto.precio * cantidad)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAgregarAlCarrito(cantidad)
                        showDialog = false
                        cantidad = 1
                    }
                ) {
                    Text("Agregar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
