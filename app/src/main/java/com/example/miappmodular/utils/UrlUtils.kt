package com.example.miappmodular.utils

import com.example.miappmodular.model.Producto
import com.example.miappmodular.model.Productor

/**
 * Utilidades para construcción de URLs de imágenes y recursos.
 *
 * **¿Por qué esta clase?**
 * - Los modelos de dominio NO deben conocer detalles de infraestructura (URLs, IPs, puertos)
 * - Centraliza la lógica de construcción de URLs para facilitar cambios (dev → prod)
 * - Permite inyectar base URL de forma consistente en toda la app
 *
 * **Uso en ViewModels o UI:**
 * ```kotlin
 * val imageUrl = UrlUtils.getImageUrl(producto.imagen)
 * val thumbnailUrl = UrlUtils.getImageUrl(productor.imagenThumbnail)
 * ```
 */
object UrlUtils {

    /**
     * Base URL del servidor.
     *
     * **IMPORTANTE:**
     * - Para emulador Android: usa 10.0.2.2 (IP especial que apunta a localhost del host)
     * - Para dispositivo físico: usa la IP de tu PC en la red local (ej: 192.168.1.X)
     * - Para producción: usa el dominio real (ej: https://saborloca-api.onrender.com)
     */
    private const val BASE_URL = "https://saborloca-api.onrender.com"

    /**
     * Construye la URL completa de una imagen a partir de su ruta relativa.
     *
     * @param relativePath Ruta relativa de la imagen (ej: "uploads/productos/imagen.jpg")
     * @return URL completa de la imagen, o null si relativePath es null
     *
     * Ejemplo:
     * ```kotlin
     * val url = UrlUtils.getImageUrl("uploads/productos/tomate.jpg")
     * // Resultado: "https://saborloca-api.onrender.com/uploads/productos/tomate.jpg"
     * ```
     */
    fun getImageUrl(relativePath: String?): String? {
        return relativePath?.let {
            // Si la ruta ya es una URL completa, retornarla tal cual
            if (it.startsWith("http://") || it.startsWith("https://")) {
                it
            } else {
                // Construir URL completa
                "$BASE_URL/${it.trimStart('/')}"
            }
        }
    }

    /**
     * Construye la URL completa para cualquier recurso del servidor.
     *
     * @param endpoint Endpoint o ruta del recurso (ej: "api/productos")
     * @return URL completa
     */
    fun getResourceUrl(endpoint: String): String {
        return "$BASE_URL/${endpoint.trimStart('/')}"
    }
}

/**
 * Extensiones de conveniencia para convertir las rutas de imagen a URLs completas
 * directamente desde los modelos de dominio.
 */
fun Producto.getImagenUrl(): String? = UrlUtils.getImageUrl(imagen)

fun Producto.getThumbnailUrl(): String? = UrlUtils.getImageUrl(imagenThumbnail)

fun Productor.getImagenUrl(): String? = UrlUtils.getImageUrl(imagen)

fun Productor.getThumbnailUrl(): String? = UrlUtils.getImageUrl(imagenThumbnail)
