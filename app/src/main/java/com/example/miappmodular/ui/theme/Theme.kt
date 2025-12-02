package com.example.miappmodular.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Esquema de colores Light - SaborLocal
 *
 * Paleta inspirada en productos frescos, locales y sostenibles:
 * - Verde bosque: Frescura y naturaleza
 * - Naranja terracota: Calidez y comunidad local
 * - Crema natural: Autenticidad y simplicidad
 * - Marrones tierra: Conexión con productores locales
 */
private val LightColorScheme = lightColorScheme(
    // ========= Primary (Verde Bosque) =========
    primary = Primary,                      // Verde bosque #2D6A4F
    onPrimary = Color.White,
    primaryContainer = LightGreen,          // Verde claro #D8F3DC
    onPrimaryContainer = ForestGreen,

    // ========= Secondary (Azul Suave) =========
    secondary = Accent,                     // Azul suave #4A90E2
    onSecondary = Color.White,
    secondaryContainer = LightBlue,         // Azul claro #E3F2FD
    onSecondaryContainer = Color(0xFF1565C0),

    // ========= Tertiary (Verde Menta) =========
    tertiary = MintGreen,                   // Verde menta #52B788
    onTertiary = Color.White,
    tertiaryContainer = PaleGreen,          // Verde pálido #F1FAF5
    onTertiaryContainer = ForestGreen,

    // ========= Error (Rojo Tomate) =========
    error = Destructive,                    // Rojo tomate #DC3545
    onError = Color.White,
    errorContainer = DestructiveLight,      // Rojo claro #FFF0F0
    onErrorContainer = Color(0xFF8B0000),

    // ========= Background (Crema Natural) =========
    background = Color(0xFFF8F9FA),         // Blanco Humo #F8F9FA (casi blanco)
    onBackground = Foreground,              // Gris carbón #2B2D31

    // ========= Surface (Blanco y Beige) =========
    surface = Surface,                      // Blanco puro #FFFFFF
    onSurface = Foreground,                 // Gris carbón
    surfaceVariant = Color(0xFFFFFFFF),     // Blanco puro para variantes también
    onSurfaceVariant = ForegroundMuted,     // Gris medio

    // ========= Outline (Borders) =========
    outline = Border,                       // Marrón claro #D4C4B0
    outlineVariant = Beige,                 // Beige suave

    // ========= Inverse (Para snackbars, etc) =========
    inverseSurface = ForestGreen,           // Verde bosque
    inverseOnSurface = Color.White,
    inversePrimary = MintGreen,             // Verde menta

    // ========= Otros =========
    surfaceTint = MintGreen,
    scrim = Color.Black.copy(alpha = 0.32f)
)

/**
 * Esquema de colores Dark - SaborLocal
 *
 * Modo oscuro con tonos cálidos que mantienen la identidad de marca.
 */
private val DarkColorScheme = darkColorScheme(
    // ========= Primary =========
    primary = MintGreen,                    // Verde menta más brillante
    onPrimary = CharcoalGray,
    primaryContainer = ForestGreen,         // Verde bosque oscuro
    onPrimaryContainer = LightGreen,

    // ========= Secondary =========
    secondary = SkyBlue,                    // Azul cielo más brillante
    onSecondary = CharcoalGray,
    secondaryContainer = Color(0xFF1565C0), // Azul oscuro
    onSecondaryContainer = LightBlue,

    // ========= Tertiary =========
    tertiary = MintGreen,
    onTertiary = CharcoalGray,
    tertiaryContainer = LeafGreen,
    onTertiaryContainer = PaleGreen,

    // ========= Error =========
    error = Destructive,
    onError = Color.White,
    errorContainer = Color(0xFF8B0000),     // Rojo oscuro
    onErrorContainer = DestructiveLight,

    // ========= Background =========
    background = Color(0xFF1A1A1A),         // Casi negro cálido
    onBackground = CreamBackground,

    // ========= Surface =========
    surface = Color(0xFF242424),            // Gris oscuro cálido
    onSurface = CreamBackground,
    surfaceVariant = Color(0xFF3A3A3A),
    onSurfaceVariant = Beige,

    // ========= Outline =========
    outline = MediumBrown,
    outlineVariant = EarthBrown,

    // ========= Inverse =========
    inverseSurface = CreamSurface,
    inverseOnSurface = CharcoalGray,
    inversePrimary = ForestGreen,

    // ========= Otros =========
    surfaceTint = MintGreen,
    scrim = Color.Black.copy(alpha = 0.32f)
)

/**
 * Shapes - Esquinas redondeadas inspiradas en productos naturales
 *
 * Bordes suaves que evocan la forma orgánica de frutas y verduras.
 */
val Shapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),   // Bordes sutiles
    small = RoundedCornerShape(8.dp),        // Cards pequeños
    medium = RoundedCornerShape(12.dp),      // Cards normales
    large = RoundedCornerShape(16.dp),       // Cards grandes, imágenes
    extraLarge = RoundedCornerShape(24.dp)   // Modales, sheets
)

/**
 * Tema principal de SaborLocal
 *
 * **Características del diseño:**
 * - Colores cálidos y naturales que evocan productos frescos
 * - Verde principal para transmitir sostenibilidad y frescura
 * - Naranja/terracota para crear calidez y energía local
 * - Cremas y beiges para fondos acogedores y naturales
 * - Marrones tierra para autenticidad y conexión con productores
 *
 * **Psicología aplicada:**
 * - Verde → Confianza en productos orgánicos y saludables
 * - Naranja → Accesibilidad y entusiasmo por apoyar lo local
 * - Crema → Simplicidad y autenticidad artesanal
 * - Tierra → Estabilidad y conexión directa con la fuente
 *
 * @param darkTheme Si debe usar el tema oscuro
 * @param content Contenido de la app
 */
@Composable
fun MiAppModularTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
