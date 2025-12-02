package com.example.miappmodular.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Barra superior simple y consistente para toda la app
 *
 * **Diseño:**
 * - Fondo blanco (sin TopAppBar de Material que puede aplicar colores propios)
 * - IconButton de navegación a la izquierda
 * - Título centrado
 * - Acción opcional a la derecha
 *
 * **Ventaja:**
 * Este componente garantiza que todas las pantallas tengan la misma cabecera
 * y el mismo color blanco, evitando inconsistencias con la Status Bar.
 *
 * @param title Título de la pantalla
 * @param onNavigateBack Callback para el botón de atrás
 * @param actionIcon Ícono opcional para la acción derecha
 * @param onActionClick Callback opcional para la acción derecha
 * @param actionContent Contenido composable personalizado para la derecha (alternativa a actionIcon)
 */
@Composable
fun SimpleTopBar(
    title: String,
    onNavigateBack: () -> Unit,
    actionIcon: ImageVector? = null,
    onActionClick: (() -> Unit)? = null,
    actionContent: (@Composable () -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
    ) {
        // Botón de navegación (izquierda)
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
        }

        // Título (centro)
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Center)
        )

        // Acción opcional (derecha)
        Box(modifier = Modifier.align(Alignment.CenterEnd)) {
            when {
                actionContent != null -> actionContent()
                actionIcon != null && onActionClick != null -> {
                    IconButton(onClick = onActionClick) {
                        Icon(actionIcon, contentDescription = null)
                    }
                }
            }
        }
    }
}
