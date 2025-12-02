package com.example.miappmodular.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Layout estándar de la aplicación con SimpleTopBar integrado
 *
 * **Ventajas arquitectónicas:**
 * - **DRY**: Elimina repetición de Scaffold + SimpleTopBar en cada pantalla
 * - **Consistencia**: Garantiza que todas las pantallas usen el mismo layout
 * - **Mantenibilidad**: Cambios globales en un solo lugar
 * - **Separation of Concerns**: Las pantallas se enfocan en contenido, no en estructura
 *
 * **Uso típico:**
 * ```kotlin
 * StandardScaffold(
 *     title = "Mis Pedidos",
 *     onNavigateBack = onNavigateBack,
 *     actionIcon = Icons.Default.Refresh,
 *     onActionClick = { viewModel.refresh() }
 * ) { paddingValues ->
 *     // Contenido de la pantalla
 *     LazyColumn(modifier = Modifier.padding(paddingValues)) {
 *         // ...
 *     }
 * }
 * ```
 *
 * **Para acciones complejas:**
 * ```kotlin
 * StandardScaffold(
 *     title = "Productos",
 *     onNavigateBack = onNavigateBack,
 *     actionContent = {
 *         Row {
 *             IconButton(onClick = { }) { Icon(Icons.Default.Filter, null) }
 *             IconButton(onClick = { }) { Icon(Icons.Default.Refresh, null) }
 *         }
 *     }
 * ) { paddingValues ->
 *     // Contenido
 * }
 * ```
 *
 * @param title Título de la pantalla mostrado en el centro
 * @param onNavigateBack Callback para el botón de navegación atrás
 * @param actionIcon Ícono opcional para acción simple a la derecha
 * @param onActionClick Callback para acción simple (requiere actionIcon)
 * @param actionContent Contenido composable personalizado para la derecha (alternativa a actionIcon)
 * @param containerColor Color de fondo del Scaffold (por defecto: blanco)
 * @param content Contenido de la pantalla que recibe PaddingValues del Scaffold
 */
@Composable
fun StandardScaffold(
    title: String,
    onNavigateBack: () -> Unit,
    actionIcon: ImageVector? = null,
    onActionClick: (() -> Unit)? = null,
    actionContent: (@Composable () -> Unit)? = null,
    containerColor: Color = Color.White,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            SimpleTopBar(
                title = title,
                onNavigateBack = onNavigateBack,
                actionIcon = actionIcon,
                onActionClick = onActionClick,
                actionContent = actionContent
            )
        },
        containerColor = containerColor
    ) { paddingValues ->
        content(paddingValues)
    }
}
