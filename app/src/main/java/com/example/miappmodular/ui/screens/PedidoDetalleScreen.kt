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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.miappmodular.model.EstadoPedido
import com.example.miappmodular.model.Pedido
import com.example.miappmodular.model.PedidoItem
import com.example.miappmodular.ui.components.StandardScaffold
import com.example.miappmodular.viewmodel.PedidoDetalleUiState
import com.example.miappmodular.viewmodel.PedidoDetalleViewModel

/**
 * Pantalla que muestra el detalle completo de un pedido
 *
 * **Información mostrada:**
 * - Estado del pedido con indicador visual
 * - Lista de productos con cantidades y precios
 * - Total del pedido
 * - Fecha del pedido
 * - Información del cliente
 *
 * @param pedidoId ID del pedido
 * @param viewModel ViewModel del pedido
 * @param onNavigateBack Callback para volver atrás
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidoDetalleScreen(
    pedidoId: String,
    viewModel: PedidoDetalleViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Cargar pedido al iniciar
    LaunchedEffect(pedidoId) {
        viewModel.loadPedido(pedidoId)
    }

    StandardScaffold(
        title = "Detalle del Pedido",
        onNavigateBack = onNavigateBack
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is PedidoDetalleUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is PedidoDetalleUiState.Success -> {
                    PedidoDetalleContent(pedido = state.pedido)
                }

                is PedidoDetalleUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
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
                        Button(onClick = { viewModel.loadPedido(pedidoId) }) {
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

/**
 * Contenido del detalle del pedido
 */
@Composable
fun PedidoDetalleContent(pedido: Pedido) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Encabezado: ID y estado
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE8F5E9)  // Verde menta suave
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Pedido #${pedido.id.takeLast(8)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20)  // Verde oscuro
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Fecha: ${pedido.fecha}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF2E7D32)  // Verde medio
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    EstadoBadge(estado = pedido.estado)
                }
            }
        }

        // Indicador de progreso
        item {
            EstadoProgressIndicator(estado = pedido.estado)
        }

        // Sección: Productos
        item {
            Text(
                text = "Productos (${pedido.items.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        // Lista de items
        items(pedido.items) { item ->
            PedidoItemCard(item = item)
        }

        // Total
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF3E0)  // Naranja crema suave
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total del pedido:",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE65100)  // Naranja oscuro
                    )
                    Text(
                        text = pedido.getTotalFormateado(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE76F51)  // Naranja terracota
                    )
                }
            }
        }
    }
}

/**
 * Card de un item del pedido
 */
@Composable
fun PedidoItemCard(item: PedidoItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen del producto
            AsyncImage(
                model = item.producto.imagenThumbnail ?: item.producto.imagen,
                contentDescription = item.producto.nombre,
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Información
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.producto.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Por: ${item.producto.productor.nombre}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${"$%.2f".format(item.precio)} × ${item.cantidad}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Subtotal: ${item.getSubtotalFormateado()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Indicador visual del progreso del pedido
 */
@Composable
fun EstadoProgressIndicator(estado: EstadoPedido) {
    val steps = listOf(
        EstadoPedido.PENDIENTE,
        EstadoPedido.EN_PREPARACION,
        EstadoPedido.EN_CAMINO,
        EstadoPedido.ENTREGADO
    )

    val currentStepIndex = steps.indexOf(estado).takeIf { it >= 0 } ?: 0

    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            steps.forEachIndexed { index, step ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Círculo indicador
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (index <= currentStepIndex)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (index < currentStepIndex) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (index <= currentStepIndex)
                                        MaterialTheme.colorScheme.onPrimary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Texto del estado
                    Text(
                        text = step.displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (index == currentStepIndex) FontWeight.Bold else FontWeight.Normal,
                        color = if (index <= currentStepIndex)
                            MaterialTheme.colorScheme.onSurface
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Línea conectora (excepto en el último)
                if (index < steps.size - 1) {
                    Box(
                        modifier = Modifier
                            .padding(start = 15.dp)
                            .width(2.dp)
                            .height(24.dp)
                            .background(
                                if (index < currentStepIndex)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            )
                    )
                }
            }
        }
    }
}
