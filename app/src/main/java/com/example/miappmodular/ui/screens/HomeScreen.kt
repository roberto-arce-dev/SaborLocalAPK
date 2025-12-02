package com.example.miappmodular.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.miappmodular.model.Producto
import com.example.miappmodular.ui.components.*
import com.example.miappmodular.ui.theme.*
import com.example.miappmodular.utils.getImagenUrl
import com.example.miappmodular.utils.getThumbnailUrl
import com.example.miappmodular.viewmodel.HomeUiState
import com.example.miappmodular.viewmodel.HomeViewModel

/**
 * HomeScreen rediseñado estilo Instagram/Facebook/Amazon
 *
 * **Nueva Arquitectura:**
 * - Bottom Navigation para acceso rápido (5 tabs principales)
 * - Feed moderno con scroll vertical
 * - Secciones horizontales (productores, productos)
 * - Buscador prominente en el header
 *
 * **Estructura:**
 * ```
 * HomeScreen
 * ├── TopBar Simplificado
 * │   ├── Logo "SaborLocal"
 * │   └── Buscador
 * └── LazyColumn (Feed)
 *     ├── Welcome Header
 *     ├── Quick Actions (4 categorías)
 *     ├── Productores Destacados (carrusel horizontal)
 *     ├── Productos Recientes (carrusel horizontal)
 *     └── Todas las Categorías (grid 2 columnas)
 * ```
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onNavigateToProfile: () -> Unit = {},
    onNavigateToMap: () -> Unit = {},
    onNavigateToCamera: () -> Unit = {},
    onNavigateToProductosList: () -> Unit = {},
    onNavigateToCreateProducto: () -> Unit = {},
    onNavigateToCreateProductor: () -> Unit = {},
    onNavigateToProductoresList: () -> Unit = {},
    onNavigateToCarrito: () -> Unit = {},
    onNavigateToPedidos: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    
    // Estado de datos
    val recentProductsState by viewModel.recentProducts.collectAsState()
    val featuredProductoresState by viewModel.featuredProductores.collectAsState()

    // Estado para el detalle de producto (Bottom Sheet)
    var selectedProduct by remember { mutableStateOf<Producto?>(null) }

    val context = androidx.compose.ui.platform.LocalContext.current

    // Bottom Sheet de detalle
    if (selectedProduct != null) {
        ProductDetailSheet(
            producto = selectedProduct!!,
            onDismissRequest = { selectedProduct = null },
            onAddToCart = { producto ->
                com.example.miappmodular.data.local.CarritoManager.addItem(producto)
                android.widget.Toast.makeText(
                    context,
                    "Producto agregado al carrito",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                selectedProduct = null
            }
        )
    }

    Scaffold(
        topBar = {
            // TopBar Minimalista con Buscador
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Logo y título
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Agriculture,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SaborLocal",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Notificaciones (futuro)
                        IconButton(onClick = { /* TODO: Notifications */ }) {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = "Notificaciones",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Buscador
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Buscar productos, productores...") },
                        leadingIcon = {
                            Icon(Icons.Filled.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "Limpiar")
                                }
                            }
                        },
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 100.dp) // Espacio para Bottom Nav
        ) {
            // ========= WELCOME HEADER =========
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = "Hola! 👋",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "¿Qué quieres comprar hoy?",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ========= QUICK ACTIONS =========
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        QuickActionChip(
                            icon = Icons.Filled.Group,
                            label = "Productores",
                            onClick = onNavigateToProductoresList
                        )
                    }
                    item {
                        QuickActionChip(
                            icon = Icons.Filled.ShoppingCart,
                            label = "Productos",
                            onClick = onNavigateToProductosList
                        )
                    }
                    item {
                        QuickActionChip(
                            icon = Icons.Filled.LocalOffer,
                            label = "Ofertas",
                            onClick = { /* TODO */ }
                        )
                    }
                    item {
                        QuickActionChip(
                            icon = Icons.Filled.Category,
                            label = "Categorías",
                            onClick = { /* TODO */ }
                        )
                    }
                }
            }

            // ========= PRODUCTORES DESTACADOS =========
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SectionHeader(
                        title = "Productores Destacados",
                        actionText = "Ver todos",
                        onActionClick = onNavigateToProductoresList
                    )

                    when (featuredProductoresState) {
                        is HomeUiState.Loading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                        is HomeUiState.Error -> {
                            Text(
                                text = "No se pudieron cargar los productores",
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        is HomeUiState.Success -> {
                            val productores = (featuredProductoresState as HomeUiState.Success<List<com.example.miappmodular.model.Productor>>).data

                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(productores.size) { index ->
                                    val productor = productores[index]
                                    ProductorCard(
                                        productor = productor,
                                        onClick = onNavigateToProductoresList
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ========= PRODUCTOS RECIENTES =========
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    SectionHeader(
                        title = "Productos Frescos",
                        actionText = "Ver todos",
                        onActionClick = onNavigateToProductosList
                    )

                    when (recentProductsState) {
                        is HomeUiState.Loading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                        is HomeUiState.Error -> {
                            Text(
                                text = "No se pudieron cargar los productos",
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        is HomeUiState.Success -> {
                            val productos = (recentProductsState as HomeUiState.Success<List<Producto>>).data
                            
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(productos.size) { index ->
                                    val producto = productos[index]
                                    HomeProductoCard(
                                        producto = producto,
                                        onClick = { selectedProduct = producto }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ========= CATEGORÍAS =========
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Explorar Categorías",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Grid 2x2 de categorías
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CategoryCard(
                            icon = Icons.Filled.Eco,
                            title = "Frutas",
                            count = "120 productos",
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToProductosList
                        )
                        CategoryCard(
                            icon = Icons.Filled.Restaurant,
                            title = "Verduras",
                            count = "85 productos",
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToProductosList
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CategoryCard(
                            icon = Icons.Filled.Fastfood,
                            title = "Lácteos",
                            count = "45 productos",
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToProductosList
                        )
                        CategoryCard(
                            icon = Icons.Filled.Cake,
                            title = "Panadería",
                            count = "60 productos",
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToProductosList
                        )
                    }
                }
            }

            // Espaciado final
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// ==================== COMPONENTES NUEVOS ====================

@Composable
fun SectionHeader(
    title: String,
    actionText: String,
    onActionClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        TextButton(onClick = onActionClick) {
            Text(
                text = actionText,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
            Icon(
                imageVector = Icons.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun QuickActionChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.height(40.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ProductorCard(
    productor: com.example.miappmodular.model.Productor,
    onClick: () -> Unit
) {
    ShadcnCard(
        onClick = onClick,
        modifier = Modifier
            .width(160.dp)
            .height(200.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Avatar del productor con imagen real o placeholder
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                if (productor.imagen != null) {
                    AsyncImage(
                        model = productor.getThumbnailUrl() ?: productor.getImagenUrl(),
                        contentDescription = productor.nombre,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Column {
                Text(
                    text = productor.getDisplayName(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = productor.ubicacion ?: "Sin ubicación",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = productor.telefono ?: "Sin teléfono",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun HomeProductoCard(
    producto: Producto,
    onClick: () -> Unit
) {
    ShadcnCard(
        onClick = onClick,
        modifier = Modifier
            .width(140.dp)
            .height(200.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Imagen del producto
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (producto.imagen != null) {
                    AsyncImage(
                        model = producto.getThumbnailUrl() ?: producto.getImagenUrl(),
                        contentDescription = producto.nombre,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.ShoppingBag,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = producto.getPrecioFormateado(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = producto.productor.getDisplayName(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun CategoryCard(
    icon: ImageVector,
    title: String,
    count: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    ShadcnCard(
        onClick = onClick,
        modifier = modifier.height(120.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = count,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
