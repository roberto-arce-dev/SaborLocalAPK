package com.example.miappmodular.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Paleta de colores SaborLocal - Inspirada en productos frescos y locales
 *
 * **Filosofía del diseño:**
 * - Verde: Productos frescos, orgánicos, sostenibles
 * - Naranja/Terracota: Calidez, comunidad local, energía
 * - Crema/Beige: Natural, artesanal, acogedor
 * - Marrones tierra: Rústico, auténtico, de la tierra
 *
 * **Psicología del color:**
 * - Verde → Naturaleza, frescura, confianza, salud
 * - Naranja → Energía, entusiasmo, accesibilidad
 * - Crema → Calidez, confort, simplicidad
 * - Tierra → Estabilidad, autenticidad, conexión con la tierra
 */

// ========= Verdes (Frescura y Naturaleza) =========

/** Verde bosque profundo - Color principal de la marca */
val ForestGreen = Color(0xFF2D6A4F)

/** Verde hoja - Para hover states y acentos */
val LeafGreen = Color(0xFF40916C)

/** Verde menta - Para containers y fondos suaves */
val MintGreen = Color(0xFF52B788)

/** Verde claro - Para fondos y superficies */
val LightGreen = Color(0xFFD8F3DC)

/** Verde muy claro - Para fondos secundarios */
val PaleGreen = Color(0xFFF1FAF5)

// ========= Azules Suaves (Acento Secundario) =========

/** Azul suave - Color secundario principal */
val SoftBlue = Color(0xFF4A90E2)

/** Azul cielo - Para hover y acentos */
val SkyBlue = Color(0xFF5BA3F5)

/** Azul claro - Para fondos secundarios */
val LightBlue = Color(0xFFE3F2FD)

// ========= Cremas y Neutros Cálidos =========

/** Crema natural - Fondo principal */
val CreamBackground = Color(0xFFFFFBF5)

/** Crema medio - Para cards y surfaces */
val CreamSurface = Color(0xFFFFF8F0)

/** Beige suave - Para variantes */
val Beige = Color(0xFFF5EFE6)

// ========= Marrones Tierra (Autenticidad) =========

/** Marrón tierra - Para textos y acentos oscuros */
val EarthBrown = Color(0xFF6B4423)

/** Marrón medio - Para textos secundarios */
val MediumBrown = Color(0xFF8B6F47)

/** Marrón claro - Para borders sutiles */
val LightBrown = Color(0xFFD4C4B0)

// ========= Grises Neutros (Balance) =========

/** Gris carbón - Para textos principales */
val CharcoalGray = Color(0xFF2B2D31)

/** Gris medio - Para textos secundarios */
val MediumGray = Color(0xFF6C757D)

/** Gris claro - Para borders */
val LightGray = Color(0xFFE9ECEF)

/** Gris muy claro - Para fondos */
val PaleGray = Color(0xFFF8F9FA)

// ========= Estados Semánticos =========

/** Éxito - Verde menta (producto disponible) */
val Success = MintGreen

/** Éxito claro - Fondo para estados exitosos */
val SuccessLight = LightGreen

/** Error - Rojo tomate (producto agotado, error) */
val Destructive = Color(0xFFDC3545)

/** Error claro - Fondo para errores */
val DestructiveLight = Color(0xFFFFF0F0)

/** Advertencia - Amarillo (stock bajo) */
val Warning = Color(0xFFFFA726)

/** Advertencia claro - Fondo para advertencias */
val WarningLight = Color(0xFFFFF3E0)

/** Info - Azul cielo (información general) */
val Info = Color(0xFF4A90E2)

/** Info claro - Fondo para información */
val InfoLight = Color(0xFFE3F2FD)

// ========= Colores Principales del Tema =========

/** Color primario - Verde bosque (botones principales, links) */
val Primary = ForestGreen

/** Primary hover - Verde hoja (hover en botones) */
val PrimaryHover = LeafGreen

/** Primary light - Para containers primarios */
val PrimaryLight = MintGreen

/** Color de acento - Azul suave (CTAs, destacados) */
val Accent = SoftBlue

/** Accent hover - Azul cielo (hover en accent) */
val AccentHover = SkyBlue

// ========= Backgrounds =========

/** Fondo principal - Blanco casi puro */
val Background = Color(0xFFF8F9FA) // GhostWhite

/** Fondo secundario - Blanco puro */
val BackgroundSecondary = Color(0xFFFFFFFF)

/** Surface - Blanco puro para cards elevados */
val Surface = Color(0xFFFFFFFF)

/** Surface variant - Blanco puro */
val SurfaceVariant = Color(0xFFFFFFFF)

// ========= Borders =========

/** Border principal - Marrón claro sutil */
val Border = LightBrown

/** Border en focus - Verde hoja */
val BorderFocus = LeafGreen

// ========= Textos =========

/** Texto principal - Gris carbón */
val Foreground = CharcoalGray

/** Texto secundario - Gris medio */
val ForegroundMuted = MediumGray

/** Texto sutil - Para hints */
val ForegroundSubtle = LightGray

// ========= Muted (Deshabilitados) =========

/** Fondo muted - Beige */
val Muted = Beige

/** Texto muted - Gris medio */
val MutedForeground = MediumGray

// ========= Focus Ring =========

/** Ring de focus - Verde bosque */
val Ring = ForestGreen

/** Ring offset - Fondo */
val RingOffset = Background

// ========= Colores Específicos de SaborLocal =========

/** Verde para productos orgánicos certificados */
val OrganicGreen = MintGreen

/** Naranja para productos artesanales */
val ArtisanalOrange = Color(0xFFFFA726)

/** Marrón para productos de granja */
val FarmBrown = EarthBrown

/** Verde para "Disponible" */
val AvailableGreen = Success

/** Rojo para "Agotado" */
val OutOfStockRed = Destructive

/** Amarillo para "Stock Bajo" */
val LowStockYellow = Warning
