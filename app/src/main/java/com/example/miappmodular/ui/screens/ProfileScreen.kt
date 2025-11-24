package com.example.miappmodular.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

/**
 * ARCHIVO TEMPORALMENTE DESHABILITADO
 *
 * Este ProfileScreen usaba ProfileViewModel que dependía de UserRepository con Room.
 * Room fue eliminado cuando migramos al backend de SaborLocal.
 *
 * TODO: Implementar nuevo ProfileScreen que use AuthSaborLocalRepository
 * y el endpoint GET /api/auth/profile del backend de SaborLocal.
 */

@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit
) {
    Text("Perfil en construcción - Migración a SaborLocal backend")
}

/*
// Código original comentado - implementar con AuthSaborLocalRepository

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.miappmodular.ui.components.*
import com.example.miappmodular.ui.theme.*
import com.example.miappmodular.viewmodel.ProfileViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    // Código comentado - implementar con nuevo ProfileViewModel
}
*/
