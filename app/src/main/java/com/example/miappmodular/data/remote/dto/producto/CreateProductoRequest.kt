package com.example.miappmodular.data.remote.dto.producto

/**
 * Request para crear un nuevo Producto
 */
data class CreateProductoRequest(
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val unidad: String,
    val stock: Int,
    val productor: String  // ID del productor
)
