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
         * Pantalla principal con Bottom Navigation Bar.
         * Contiene 5 tabs: Inicio, Productos, Carrito, Pedidos, Perfil.
         *
         * Solo accesible tras autenticación exitosa.
         */
        composable("home") {
            MainScreen(
                onLogout = {
                    // Navegar al login y limpiar todo el backstack
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
