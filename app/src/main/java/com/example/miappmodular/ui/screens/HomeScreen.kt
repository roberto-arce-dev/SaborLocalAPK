package com.example.miappmodular.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.miappmodular.ui.components.*
import com.example.miappmodular.ui.theme.*

/**
 * Pantalla principal (Dashboard) de la aplicación tras autenticación exitosa.
 *
 * **Funcionalidad:**
 * - Dashboard centralizado con vista general de la app
 * - TopBar personalizado shadcn.io con logo, título y acciones
 * - Tarjetas de estadísticas (Total Usuarios, Activos Hoy)
 * - Grid de módulos/features con badges de versión de implementación
 * - Navegación a perfil de usuario y logout
 * - Navegación a módulos individuales (Map, Camera, etc.)
 *
 * **Arquitectura de UI:**
 * ```
 * HomeScreen
 * ├── TopBar (Surface + Row)
 * │   ├── Logo + Título "Mi App Modular"
 * │   └── Actions: Profile Icon + Logout Icon
 * ├── StatCards (Row con 2 cards)
 * │   ├── Total Usuarios (1,234)
 * │   └── Activos Hoy (89)
 * └── Módulos Grid (LazyVerticalGrid 2 columnas)
 *     ├── Mapa GPS (IL 2.4)
 *     ├── Cámara (IL 2.4)
 *     ├── Base de Datos (IL 2.3)
 *     ├── Configuración (IL 2.3)
 *     ├── Temas (IL 2.1)
 *     └── Notificaciones (IL 2.2)
 * ```
 *
 * **Diseño shadcn.io:**
 * - TopBar: Surface con elevación 1.dp y divider inferior
 * - Background: BackgroundSecondary para contraste
 * - Grid: 2 columnas fijas con spacing 12.dp
 * - Cards: FeatureModuleCard con badge, icono, título, descripción
 * - StatCards: Layout horizontal con icono y valores numéricos
 *
 * **Badges de módulos:**
 * Los badges indican el nivel de implementación (IL = Implementation Level):
 * - IL 2.4: Completamente implementado
 * - IL 2.3: Funcional pero incompleto
 * - IL 2.2: En desarrollo
 * - IL 2.1: Prototipo
 *
 * **Navegación:**
 * - Icono Profile → Navega a ProfileScreen
 * - Icono Logout → Cierra sesión y regresa a LoginScreen (backstack limpiado)
 * - Cards de módulos → Navegan a sus respectivas pantallas
 *
 * **Solo accesible tras autenticación exitosa.** Si el usuario no está
 * autenticado, el AppNavigation debe redirigir automáticamente a LoginScreen.
 *
 * @param onNavigateToProfile Callback para navegar a pantalla de perfil.
 * @param onNavigateToMap Callback para navegar a módulo de mapa GPS.
 * @param onNavigateToCamera Callback para navegar a módulo de cámara.
 * @param onLogout Callback para cerrar sesión y regresar a login.
 *
 * @see FeatureModuleCard
 * @see com.example.miappmodular.ui.navigation.AppNavigation
 * @see ProfileScreen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToMap: () -> Unit = {},
    onNavigateToCamera: () -> Unit = {},
    onNavigateToProductosList: () -> Unit = {},
    onNavigateToCreateProducto: () -> Unit = {},
    onNavigateToCreateProductor: () -> Unit = {},
    onNavigateToProductoresList: () -> Unit = {},
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            // TopBar estilo shadcn.io
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Surface,
                shadowElevation = 1.dp
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Logo/Título
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(40.dp),
                                shape = MaterialTheme.shapes.small,
                                color = Primary
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Apps,
                                    contentDescription = null,
                                    tint = androidx.compose.ui.graphics.Color.White,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Mi App Modular",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Foreground
                                    )
                                )
                                Text(
                                    text = "Dashboard",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = ForegroundMuted
                                    )
                                )
                            }
                        }

                        // Acciones
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = onNavigateToProfile,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = "Perfil",
                                    tint = ForegroundMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(
                                onClick = onLogout,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Logout,
                                    contentDescription = "Cerrar sesión",
                                    tint = ForegroundMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    ShadcnDivider()
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundSecondary)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header con estadísticas


                Spacer(modifier = Modifier.height(20.dp))

                // Título de sección
                Text(
                    text = "Módulos",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Foreground
                    )
                )

                Text(
                    text = "Accede a las diferentes funcionalidades",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = ForegroundMuted
                    ),
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Grid de módulos
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    item {
                        FeatureModuleCard(
                            icon = Icons.Filled.ShoppingCart,
                            title = "Productos",
                            description = "Lista de productos",
                            badge = "IL 2.4",
                            onClick = onNavigateToProductosList
                        )
                    }

                    item {
                        FeatureModuleCard(
                            icon = Icons.Filled.AddCircle,
                            title = "Crear Producto",
                            description = "Nuevo producto",
                            badge = "IL 2.4",
                            onClick = onNavigateToCreateProducto
                        )
                    }

                    item {
                        FeatureModuleCard(
                            icon = Icons.Filled.PersonAdd,
                            title = "Crear Productor",
                            description = "Nuevo productor (ADMIN)",
                            badge = "IL 2.4",
                            onClick = onNavigateToCreateProductor
                        )
                    }

                    item {
                        FeatureModuleCard(
                            icon = Icons.Filled.Group,
                            title = "Ver Productores",
                            description = "Lista de productores",
                            badge = "IL 2.4",
                            onClick = onNavigateToProductoresList
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tarjeta de estadística reutilizable con diseño shadcn.io.
 *
 * Componente presentacional que muestra una métrica clave con su valor
 * numérico y un icono representativo. Usado típicamente en dashboards
 * para mostrar KPIs (Key Performance Indicators) de forma visual.
 *
 * **Diseño:**
 * - Layout horizontal: Texto a la izquierda, icono a la derecha
 * - Título: bodySmall, ForegroundMuted, Medium weight
 * - Valor: headlineMedium, Foreground, Bold (destaca el número)
 * - Icono: Surface con fondo Muted, tamaño 40.dp
 * - Elevación: 1.dp (sombra sutil)
 *
 * **Ejemplo de uso:**
 * ```kotlin
 * StatCard(
 *     title = "Ventas Hoy",
 *     value = "$12,345",
 *     icon = Icons.Filled.AttachMoney,
 *     modifier = Modifier.weight(1f)
 * )
 * ```
 *
 * **Casos de uso comunes:**
 * - Estadísticas de usuarios (totales, activos, nuevos)
 * - Métricas de ventas o ingresos
 * - Contadores de eventos o notificaciones
 * - Cualquier KPI numérico que requiera visualización compacta
 *
 * @param title Etiqueta descriptiva de la métrica (ej: "Total Usuarios").
 * @param value Valor numérico o texto a destacar (ej: "1,234" o "$5.6K").
 * @param icon Icono representativo de la métrica (Material Icons).
 * @param modifier Modificador opcional para layout (ej: .weight() en Row).
 *
 * @see ShadcnCard
 * @see HomeScreen
 */
@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    ShadcnCard(
        modifier = modifier,
        elevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = ForegroundMuted,
                        fontWeight = FontWeight.Medium
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Foreground
                    )
                )
            }

            Surface(
                modifier = Modifier.size(40.dp),
                shape = MaterialTheme.shapes.small,
                color = Muted
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ForegroundMuted,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                )
            }
        }
    }
}

/**
 * Tarjeta clickable para módulos/features de la aplicación con diseño shadcn.io.
 *
 * Componente reutilizable que representa un módulo o feature de la app en el
 * dashboard principal. Diseñado para grids LazyVerticalGrid con aspecto ratio 1:1.
 *
 * **Diseño:**
 * - Layout vertical con 3 secciones:
 *   1. Header: Icono (48.dp, fondo Muted) + Badge en esquina superior derecha
 *   2. Body: Título (titleMedium, SemiBold) + Descripción (bodySmall, Muted)
 *   3. Footer: "Abrir" + ArrowForward icon (color Primary)
 * - AspectRatio 1:1 (cuadrado perfecto)
 * - Padding interno: 16.dp
 * - Elevación: 1.dp
 * - Clickable con efecto ripple
 *
 * **Badges:**
 * Indican el nivel de implementación del módulo:
 * - IL 2.4: Completamente funcional y testeado
 * - IL 2.3: Funcional con features pendientes
 * - IL 2.2: En desarrollo activo
 * - IL 2.1: Prototipo o demo
 *
 * **Ejemplo de uso:**
 * ```kotlin
 * LazyVerticalGrid(columns = GridCells.Fixed(2)) {
 *     item {
 *         FeatureModuleCard(
 *             icon = Icons.Filled.Map,
 *             title = "Mapa GPS",
 *             description = "Ubicación y navegación",
 *             badge = "IL 2.4",
 *             onClick = { navController.navigate("map") }
 *         )
 *     }
 * }
 * ```
 *
 * **Variaciones de estado:**
 * - Normal: Fondo blanco, borde sutil
 * - Hover/Press: Ripple effect con color Primary
 * - Disabled: Se puede implementar con enabled parameter en futuras versiones
 *
 * @param icon Icono Material que representa el módulo visualmente.
 * @param title Nombre del módulo (ej: "Mapa GPS", "Cámara").
 * @param description Descripción breve de la funcionalidad (1-3 palabras).
 * @param badge Etiqueta de versión/nivel de implementación (ej: "IL 2.4").
 * @param onClick Callback ejecutado al hacer click en el card.
 *
 * @see ShadcnCard
 * @see ShadcnBadge
 * @see HomeScreen
 */
@Composable
fun FeatureModuleCard(
    icon: ImageVector,
    title: String,
    description: String,
    badge: String,
    onClick: () -> Unit
) {
    ShadcnCard(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        onClick = onClick,
        elevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = Muted
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                        )
                    }

                    ShadcnBadge(
                        text = badge,
                        variant = BadgeVariant.Default
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Foreground
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = ForegroundMuted
                    )
                )
            }

            // Footer del card
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Abrir",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Primary,
                        fontWeight = FontWeight.Medium
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}