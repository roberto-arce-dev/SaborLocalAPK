package com.example.miappmodular.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.miappmodular.viewmodel.SplashNavigationState
import com.example.miappmodular.viewmodel.SplashViewModel

/**
 * Pantalla Splash inicial
 * Verifica si hay una sesión activa y navega automáticamente
 */
@Composable
fun SplashScreen(
    viewModel: SplashViewModel = viewModel(),
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val navigationState by viewModel.navigationState.collectAsState()

    // Efecto para navegar según el estado
    LaunchedEffect(navigationState) {
        when (navigationState) {
            is SplashNavigationState.NavigateToHome -> {
                onNavigateToHome()
            }
            is SplashNavigationState.NavigateToLogin -> {
                onNavigateToLogin()
            }
            is SplashNavigationState.Checking -> {
                // Esperando...
            }
        }
    }

    // UI del Splash
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo de la app
            Surface(
                modifier = Modifier.size(120.dp),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Filled.LocalDining,
                    contentDescription = null,
                    tint = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Nombre de la app
            Text(
                text = "SaborLocal",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )

            Text(
                text = "Productos frescos de tu región",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                ),
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Loading indicator
            if (navigationState is SplashNavigationState.Checking) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
