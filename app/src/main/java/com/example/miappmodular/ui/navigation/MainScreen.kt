package com.example.miappmodular.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.miappmodular.ui.components.FloatingNavBar
import com.example.miappmodular.ui.components.NavigationItem
import com.example.miappmodular.ui.screens.*

/**
 * MainScreen con Bottom Navigation Bar
 *
 * Pantalla contenedora que gestiona la navegación principal de la app
 * con un Bottom Navigation Bar estilo Instagram/Facebook/Amazon.
 *
 * **Bottom Navigation Tabs:**
 * - 🏠 Inicio: Feed principal con productos destacados
 * - 🛍️ Productos: Catálogo completo
 * - 🛒 Carrito: Carrito de compras (con badge de cantidad)
 * - 📦 Pedidos: Historial de pedidos
 * - 👤 Perfil: Configuración y logout
 */
@Composable
fun MainScreen(
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    
    // Observamos el estado del carrito para el badge
    val cartItems by com.example.miappmodular.data.local.CarritoManager.items.collectAsState()
    val cartCount = cartItems.sumOf { it.cantidad }

    // Mapeamos los items de navegación a la clase que usa el componente
    val navItems = bottomNavItems.map {
        NavigationItem(
            route = it.route,
            label = it.label,
            icon = it.icon,
            badgeCount = if (it.route == BottomNavRoute.Carrito.route) cartCount else 0
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            // No bottomBar here, we overlay it
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = BottomNavRoute.Home.route,
                modifier = Modifier.padding(paddingValues)
            ) {
                // Tab 1: Inicio (Home Feed)
                composable(BottomNavRoute.Home.route) {
                    HomeScreen(
                        onNavigateToProfile = {
                            navController.navigate(BottomNavRoute.Profile.route)
                        },
                        onNavigateToProductosList = {
                            navController.navigate(BottomNavRoute.Productos.route)
                        },
                        onNavigateToProductoresList = {
                            navController.navigate("productores_list")
                        },
                        onNavigateToCarrito = {
                            navController.navigate(BottomNavRoute.Carrito.route)
                        },
                        onNavigateToPedidos = {
                            navController.navigate(BottomNavRoute.Pedidos.route)
                        },
                        onNavigateToCreateProducto = {
                            navController.navigate("create_producto")
                        },
                        onNavigateToCreateProductor = {
                            navController.navigate("create_productor")
                        },
                        onLogout = onLogout
                    )
                }

                // Tab 2: Productos
                composable(BottomNavRoute.Productos.route) {
                    ProductosListScreen(
                        onNavigateBack = {
                            navController.navigate(BottomNavRoute.Home.route)
                        },
                        onProductClick = { productId ->
                            // TODO: navegar a detalle de producto
                        }
                    )
                }

                // Tab 3: Carrito
                composable(BottomNavRoute.Carrito.route) {
                    CarritoScreen(
                        onNavigateBack = {
                            navController.navigate(BottomNavRoute.Home.route)
                        },
                        onNavigateToCheckout = {
                            navController.navigate("checkout")
                        }
                    )
                }

                // Tab 4: Pedidos
                composable(BottomNavRoute.Pedidos.route) {
                    PedidosScreen(
                        onNavigateBack = {
                            navController.navigate(BottomNavRoute.Home.route)
                        },
                        onPedidoClick = { pedidoId ->
                            navController.navigate("pedido_detalle/$pedidoId")
                        }
                    )
                }

                // Tab 5: Perfil
                composable(BottomNavRoute.Profile.route) {
                    ProfileScreen(
                        onNavigateBack = {
                            navController.navigate(BottomNavRoute.Home.route)
                        },
                        onLogout = onLogout
                    )
                }

                // ===== PANTALLAS SECUNDARIAS (sin bottom nav visible) =====

                composable("productores_list") {
                    ProductoresListScreen(
                        onNavigateBack = {
                            navController.navigateUp()
                        },
                        onProductorClick = { productorId ->
                            navController.navigate("productor_detalle/$productorId")
                        }
                    )
                }

                composable("productor_detalle/{productorId}") { backStackEntry ->
                    val productorId = backStackEntry.arguments?.getString("productorId") ?: ""
                    ProductorDetalleScreen(
                        productorId = productorId,
                        onNavigateBack = {
                            navController.navigateUp()
                        },
                        onNavigateToCarrito = {
                            navController.navigate(BottomNavRoute.Carrito.route)
                        }
                    )
                }

                composable("create_producto") {
                    CreateProductoScreen(
                        onNavigateBack = {
                            navController.navigateUp()
                        },
                        onProductoCreated = {
                            navController.navigate(BottomNavRoute.Productos.route)
                        }
                    )
                }

                composable("create_productor") {
                    CreateProductorScreen(
                        onNavigateBack = {
                            navController.navigateUp()
                        },
                        onProductorCreated = {
                            navController.navigate(BottomNavRoute.Home.route)
                        }
                    )
                }

                composable("checkout") {
                    CheckoutScreen(
                        onNavigateBack = {
                            navController.navigateUp()
                        },
                        onPedidoCreated = { pedidoId ->
                            navController.navigate("pedido_exito") {
                                popUpTo(BottomNavRoute.Home.route) { inclusive = false }
                            }
                        }
                    )
                }

                composable("pedido_exito") {
                    PedidoExitoScreen(
                        onNavigateToHome = {
                            com.example.miappmodular.viewmodel.LastPedidoHolder.clearLastPedido()
                            navController.navigate(BottomNavRoute.Home.route) {
                                popUpTo(BottomNavRoute.Home.route) { inclusive = true }
                            }
                        },
                        onNavigateToPedidos = {
                            com.example.miappmodular.viewmodel.LastPedidoHolder.clearLastPedido()
                            navController.navigate(BottomNavRoute.Pedidos.route) {
                                popUpTo(BottomNavRoute.Home.route) { inclusive = false }
                            }
                        }
                    )
                }

                composable("pedido_detalle/{pedidoId}") { backStackEntry ->
                    val pedidoId = backStackEntry.arguments?.getString("pedidoId") ?: ""
                    PedidoDetalleScreen(
                        pedidoId = pedidoId,
                        onNavigateBack = {
                            navController.navigateUp()
                        }
                    )
                }
            }
        }
        
        // Overlay FloatingNavBar
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        
        // Only show bottom nav on top-level destinations
        val isTopLevelDestination = bottomNavItems.any { it.route == currentDestination?.route }
        
        if (isTopLevelDestination) {
            FloatingNavBar(
                items = navItems,
                currentDestination = currentDestination,
                onItemClick = { item ->
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

/**
 * Rutas del Bottom Navigation
 */
sealed class BottomNavRoute(val route: String, val label: String, val icon: ImageVector) {
    object Home : BottomNavRoute("home_tab", "Inicio", Icons.Filled.Home)
    object Productos : BottomNavRoute("productos_tab", "Productos", Icons.Filled.ShoppingBag)
    object Carrito : BottomNavRoute("carrito_tab", "Carrito", Icons.Filled.ShoppingCart)
    object Pedidos : BottomNavRoute("pedidos_tab", "Pedidos", Icons.Filled.List)
    object Profile : BottomNavRoute("profile_tab", "Perfil", Icons.Filled.Person)
}

/**
 * Lista de items para el Bottom Navigation Bar
 */
private val bottomNavItems = listOf(
    BottomNavRoute.Home,
    BottomNavRoute.Carrito,
    BottomNavRoute.Productos,
    BottomNavRoute.Pedidos,
    BottomNavRoute.Profile
)
