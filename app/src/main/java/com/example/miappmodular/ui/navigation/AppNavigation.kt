package com.example.miappmodular.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.miappmodular.ui.screens.*

/**
 * Grafo de navegación principal de la aplicación.
 *
 * Define todas las rutas de navegación y las transiciones entre pantallas
 * usando Jetpack Compose Navigation. Coordina el flujo de navegación entre:
 * - Pantallas de autenticación (Login, Register)
 * - Pantallas principales (Home, Profile)
 *
 * **Rutas definidas:**
 * - `"login"` - Pantalla de inicio de sesión (ruta inicial)
 * - `"register"` - Pantalla de registro de nuevos usuarios
 * - `"home"` - Pantalla principal tras autenticación exitosa
 * - `"profile"` - Pantalla de perfil del usuario
 *
 * **Estrategia de backstack:**
 * - Login/Register → Home: Limpia backstack con `popUpTo(...) { inclusive = true }`
 *   para evitar que el botón atrás regrese a login tras autenticarse
 * - Home → Profile: Navegación estándar, puede volver atrás
 * - Logout: Regresa a login y limpia backstack completo
 *
 * **Ejemplo de flujo de navegación:**
 * ```
 * Login → [Usuario se registra] → Register → [Registro exitoso]
 *   ↓ Backstack limpio                              ↓
 * Home → [Ver perfil] → Profile → [Atrás] → Home → [Logout] → Login
 * ```
 *
 * Ejemplo de navegación programática:
 * ```kotlin
 * // Navegar a profile
 * navController.navigate("profile")
 *
 * // Navegar limpiando backstack
 * navController.navigate("home") {
 *     popUpTo("login") { inclusive = true }
 * }
 *
 * // Volver atrás
 * navController.navigateUp()
 * ```
 *
 * @see LoginScreen
 * @see RegisterScreen
 * @see HomeScreen
 * @see ProfileScreen
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        /**
         * Ruta: splash
         *
         * Pantalla inicial que verifica si hay una sesión activa.
         * - Si hay token válido → Navega a home
         * - Si no hay token → Navega a login
         */
        composable("splash") {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        /**
         * Ruta: login
         *
         * Pantalla inicial de la app. Permite:
         * - Iniciar sesión con email/password
         * - Navegar a registro si no tiene cuenta
         * - Login exitoso → Navega a home limpiando backstack
         */
        composable("login") {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate("register")
                },
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        /**
         * Ruta: register
         *
         * Pantalla de registro de nuevos usuarios. Permite:
         * - Crear cuenta con name, email, password
         * - Volver a login con navigateUp()
         * - Registro exitoso → Navega a home limpiando backstack
         */
        composable("register") {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.navigateUp()
                },
                onRegisterSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        /**
         * Ruta: home
         *
         * Pantalla principal de la app (dashboard). Muestra:
         * - Estadísticas de usuarios
         * - Grid de módulos/features
         * - Botones de perfil y logout
         *
         * Solo accesible tras autenticación exitosa.
         */
        composable("home") {
            HomeScreen(
                onNavigateToProfile = {
                    navController.navigate("profile")
                },
                onNavigateToProductosList = {
                    navController.navigate("productos_list")
                },
                onNavigateToCreateProducto = {
                    navController.navigate("create_producto")
                },
                onNavigateToCreateProductor = {
                    navController.navigate("create_productor")
                },
                onNavigateToProductoresList = {
                    navController.navigate("productores_list")
                },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        /**
         * Ruta: profile
         *
         * Pantalla de perfil del usuario. Muestra:
         * - Datos del usuario (nombre, email)
         * - Fecha de registro
         * - Último acceso
         *
         * Navegación estándar, permite volver a home con navigateUp().
         */
        composable("profile") {
            ProfileScreen(
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }

        /**
         * Ruta: productos_list
         *
         * Lista de productos con filtros de búsqueda.
         * Permite buscar por nombre, filtrar por productor y rango de precios.
         */
        composable("productos_list") {
            ProductosListScreen(
                onNavigateBack = {
                    navController.navigateUp()
                },
                onProductClick = { productId ->
                    // TODO: Navegar a detalles del producto
                    // navController.navigate("producto_detail/$productId")
                }
            )
        }

        /**
         * Ruta: create_producto
         *
         * Formulario para crear un nuevo producto.
         * Solo accesible para usuarios con rol PRODUCTOR.
         */
        composable("create_producto") {
            CreateProductoScreen(
                onNavigateBack = {
                    navController.navigateUp()
                },
                onProductoCreated = {
                    // Navegar de vuelta a la lista de productos
                    navController.popBackStack("productos_list", inclusive = false)
                }
            )
        }

        /**
         * Ruta: create_productor
         *
         * Formulario para crear un nuevo productor.
         * Solo accesible para usuarios con rol ADMIN.
         */
        composable("create_productor") {
            CreateProductorScreen(
                onNavigateBack = {
                    navController.navigateUp()
                },
                onProductorCreated = {
                    // Navegar de vuelta al home
                    navController.popBackStack("home", inclusive = false)
                }
            )
        }

        /**
         * Ruta: productores_list
         *
         * Lista de todos los productores registrados.
         * Muestra información de cada productor y permite eliminarlos.
         */
        composable("productores_list") {
            ProductoresListScreen(
                onNavigateBack = {
                    navController.navigateUp()
                },
                onProductorClick = { productorId ->
                    // TODO: Navegar a detalles del productor
                    // navController.navigate("productor_detail/$productorId")
                }
            )
        }
    }
}
