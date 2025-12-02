package com.example.miappmodular.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy

data class NavigationItem(
    val route: String,
    val icon: ImageVector,
    val label: String,
    val badgeCount: Int = 0
)

@Composable
fun FloatingNavBar(
    items: List<NavigationItem>,
    currentDestination: NavDestination?,
    onItemClick: (NavigationItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(bottom = 16.dp, start = 16.dp, end = 16.dp) // Margen inferior y lateral
            .shadow(elevation = 12.dp, shape = RoundedCornerShape(50)) // Sombra más pronunciada
            .clip(RoundedCornerShape(50)) // Forma de píldora
            .background(Color.White) // Fondo blanco puro
            .height(80.dp) // Altura un poco mayor
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            items.forEachIndexed { index, item ->
                val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                
                // Si es el ítem del medio (índice 2 en una lista de 5), lo destacamos
                val isMiddleItem = index == 2 && items.size == 5

                if (isMiddleItem) {
                    FloatingNavMiddleItem(
                        item = item,
                        onClick = { onItemClick(item) }
                    )
                } else {
                    FloatingNavItem(
                        item = item,
                        selected = selected,
                        onClick = { onItemClick(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun FloatingNavItem(
    item: NavigationItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(contentAlignment = Alignment.TopEnd) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(48.dp) // Tamaño táctil un poco más grande
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = if (selected) MaterialTheme.colorScheme.primary else Color(0xFF9E9E9E),
                modifier = Modifier.size(26.dp)
            )
        }

        if (item.badgeCount > 0) {
            Badge(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
                modifier = Modifier
                    .padding(top = 4.dp, end = 4.dp)
                    .size(16.dp) // Pequeño y discreto
            ) {
                // Si es mayor a 99, mostramos 99+
                val badgeText = if (item.badgeCount > 99) "99+" else item.badgeCount.toString()
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp // Fuente muy pequeña
                )
            }
        }
    }
}

@Composable
fun FloatingNavMiddleItem(
    item: NavigationItem,
    onClick: () -> Unit
) {
    // Botón central destacado (estilo FAB plano integrado)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(64.dp) // Más grande
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondary) // Naranja Terracota
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = Color.White,
            modifier = Modifier.size(32.dp)
        )
    }
}
