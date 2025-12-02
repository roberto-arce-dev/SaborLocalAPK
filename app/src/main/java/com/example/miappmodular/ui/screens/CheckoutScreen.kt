package com.example.miappmodular.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
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
import com.example.miappmodular.viewmodel.CheckoutUiState
import com.example.miappmodular.viewmodel.CheckoutViewModel

/**
 * Pantalla de checkout (confirmación de pedido)
 * Rediseñada con estilo pastel y tarjetas limpias.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    viewModel: CheckoutViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onPedidoCreated: (pedidoId: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val items by viewModel.items.collectAsState()

    // Estados para los campos opcionales
    var direccionEntrega by remember { mutableStateOf("") }
    var notasEntrega by remember { mutableStateOf("") }

    // Colores del tema
    val pastelGreen = MaterialTheme.colorScheme.primaryContainer
    val darkGreen = MaterialTheme.colorScheme.primary
    val pastelPurple = MaterialTheme.colorScheme.tertiaryContainer
    val pastelOrange = MaterialTheme.colorScheme.secondaryContainer
    val orangeText = MaterialTheme.colorScheme.secondary

    // Navegar automáticamente cuando el pedido se crea exitosamente
    LaunchedEffect(uiState) {
        if (uiState is CheckoutUiState.Success) {
            val pedido = (uiState as CheckoutUiState.Success).pedido
            onPedidoCreated(pedido.id)
        }
    }

    Scaffold(
        topBar = {
            // Header personalizado simple
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp, start = 8.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.Black
                    )
                }
                Text(
                    text = "Confirmar Pedido",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black,
                    fontWeight = FontWeight.Normal
                )
            }
        },
        bottomBar = {
            if (items.isNotEmpty() && uiState !is CheckoutUiState.Loading) {
                Surface(
                    color = pastelGreen.copy(alpha = 0.3f), // Fondo muy suave para el bottom bar
                    tonalElevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Total y Botón
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Total a pagar:",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${items.size} productos",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.LightGray
                                )
                            }
                            Text(
                                text = viewModel.getTotalFormateado(),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                viewModel.crearPedido(
                                    direccionEntrega = direccionEntrega.ifBlank { null },
                                    notasEntrega = notasEntrega.ifBlank { null }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Confirmar Pedido",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        },
        containerColor = Color.White // Fondo blanco limpio
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is CheckoutUiState.Idle -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 1. Alert Card (Verde)
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = pastelGreen),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = darkGreen,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            text = "Revisa tu pedido",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = darkGreen
                                        )
                                        Text(
                                            text = "Verifica que todo esté correcto antes de confirmar",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = darkGreen.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }

                        // 2. Lista de Productos (Morado)
                        items(items) { item ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = pastelPurple),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Placeholder blanco para la imagen
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color.White,
                                        modifier = Modifier.size(60.dp)
                                    ) {
                                        AsyncImage(
                                            model = item.producto.imagenThumbnail ?: item.producto.imagen,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.width(16.dp))
                                    
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.producto.nombre,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                        Text(
                                            text = "Por: ${item.producto.productor.getDisplayName()}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                        Text(
                                            text = "${"$%.2f".format(item.producto.precio)} × ${item.cantidad}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray.copy(alpha = 0.6f)
                                        )
                                    }
                                    
                                    Text(
                                        text = item.getSubtotalFormateado(),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        // 3. Información de entrega (Naranja)
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = pastelOrange),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = "Información de entrega (opcional)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    // Input Dirección
                                    OutlinedTextField(
                                        value = direccionEntrega,
                                        onValueChange = { direccionEntrega = it },
                                        placeholder = { Text("Dirección de entrega") },
                                        leadingIcon = {
                                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent,
                                            focusedBorderColor = orangeText.copy(alpha = 0.5f),
                                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    // Input Notas
                                    OutlinedTextField(
                                        value = notasEntrega,
                                        onValueChange = { notasEntrega = it },
                                        placeholder = { Text("Notas para la entrega") },
                                        leadingIcon = {
                                            Icon(Icons.Default.Edit, contentDescription = null, tint = Color.Gray)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent,
                                            focusedBorderColor = orangeText.copy(alpha = 0.5f),
                                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                            }
                        }
                        
                        // Espacio extra para scroll
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }

                is CheckoutUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                is CheckoutUiState.Success -> {
                    // Se maneja en el LaunchedEffect
                }

                is CheckoutUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Error: ${state.message}", color = Color.Red)
                            Button(onClick = { viewModel.crearPedido(direccionEntrega, notasEntrega) }) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
            }
        }
    }
}
