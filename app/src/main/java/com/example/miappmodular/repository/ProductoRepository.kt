package com.example.miappmodular.repository

import android.util.Log
import com.example.miappmodular.data.mapper.toDomain
import com.example.miappmodular.data.mapper.toProductoDomainList
import com.example.miappmodular.data.remote.RetrofitClient
import com.example.miappmodular.data.remote.dto.producto.CreateProductoRequest
import com.example.miappmodular.data.remote.dto.producto.UpdateProductoRequest
import com.example.miappmodular.model.Producto
import com.example.miappmodular.model.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

/**
 * Repositorio para gestionar operaciones relacionadas con Productos
 *
 * Este repositorio actúa como capa de abstracción entre el API y el ViewModel,
 * encapsulando la lógica de red y transformación de datos.
 *
 * **Arquitectura simple:**
 * Accede directamente a RetrofitClient para obtener el API service.
 *
 * Responsabilidades:
 * - Realizar llamadas al API usando Retrofit
 * - Convertir DTOs a modelos de dominio usando Mappers
 * - Manejar errores y retornar Results
 * - Ejecutar operaciones en el thread correcto (IO para red)
 */
class ProductoRepository {

    // Acceso directo al API service desde RetrofitClient
    private val apiService = RetrofitClient.saborLocalProductoApiService

    /**
     * Obtiene la lista de todos los productos con sus productores
     *
     * @return Result con la lista de productos o error
     */
    suspend fun getProductos(): Result<List<Producto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getProductos()

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    val productos = body.data.toProductoDomainList()
                    Result.Success(productos)
                } else {
                    Result.Error("No se pudieron obtener los productos")
                }
            } else {
                Result.Error("Error HTTP ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("ProductoRepository", "Error al obtener productos", e)
            Result.Error("Error de red: ${e.message}", e)
        }
    }

    /**
     * Obtiene un producto específico por ID
     *
     * @param id ID del producto
     * @return Result con el producto o error
     */
    suspend fun getProducto(id: String): Result<Producto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getProducto(id)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    val producto = body.data.toDomain()
                    if (producto != null) {
                        Result.Success(producto)
                    } else {
                        Result.Error("No se pudo convertir el producto")
                    }
                } else {
                    Result.Error("Producto no encontrado")
                }
            } else {
                Result.Error("Error HTTP ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("ProductoRepository", "Error al obtener producto", e)
            Result.Error("Error de red: ${e.message}", e)
        }
    }

    /**
     * Crea un nuevo producto
     *
     * @param nombre Nombre del producto
     * @param descripcion Descripción del producto
     * @param precio Precio del producto
     * @param unidad Unidad de medida (kg, litro, unidad, etc.)
     * @param stock Cantidad en stock
     * @param productorId ID del productor
     * @return Result con el producto creado o error
     */
    suspend fun createProducto(
        nombre: String,
        descripcion: String,
        precio: Double,
        unidad: String,
        stock: Int,
        productorId: String
    ): Result<Producto> = withContext(Dispatchers.IO) {
        try {
            val request = CreateProductoRequest(
                nombre = nombre,
                descripcion = descripcion,
                precio = precio,
                unidad = unidad,
                stock = stock,
                productor = productorId
            )

            val response = apiService.createProducto(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    val producto = body.data.toDomain()
                    if (producto != null) {
                        Result.Success(producto)
                    } else {
                        Result.Error("No se pudo convertir el producto creado")
                    }
                } else {
                    Result.Error("No se pudo crear el producto")
                }
            } else {
                Result.Error("Error HTTP ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("ProductoRepository", "Error al crear producto", e)
            Result.Error("Error de red: ${e.message}", e)
        }
    }

    /**
     * Actualiza un producto existente
     *
     * @param id ID del producto
     * @param nombre Nuevo nombre (opcional)
     * @param descripcion Nueva descripción (opcional)
     * @param precio Nuevo precio (opcional)
     * @param stock Nuevo stock (opcional)
     * @return Result con el producto actualizado o error
     */
    suspend fun updateProducto(
        id: String,
        nombre: String? = null,
        descripcion: String? = null,
        precio: Double? = null,
        stock: Int? = null
    ): Result<Producto> = withContext(Dispatchers.IO) {
        try {
            val request = UpdateProductoRequest(
                nombre = nombre,
                descripcion = descripcion,
                precio = precio,
                stock = stock
            )

            val response = apiService.updateProducto(id, request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    val producto = body.data.toDomain()
                    if (producto != null) {
                        Result.Success(producto)
                    } else {
                        Result.Error("No se pudo convertir el producto actualizado")
                    }
                } else {
                    Result.Error("No se pudo actualizar el producto")
                }
            } else {
                Result.Error("Error HTTP ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("ProductoRepository", "Error al actualizar producto", e)
            Result.Error("Error de red: ${e.message}", e)
        }
    }

    /**
     * Elimina un producto
     *
     * @param id ID del producto
     * @return Result con éxito o error
     */
    suspend fun deleteProducto(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteProducto(id)

            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                Result.Error("Error HTTP ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("ProductoRepository", "Error al eliminar producto", e)
            Result.Error("Error de red: ${e.message}", e)
        }
    }

    /**
     * Sube una imagen para un producto
     *
     * @param id ID del producto
     * @param imageFile Archivo de imagen
     * @return Result con el producto actualizado o error
     */
    suspend fun uploadImage(id: String, imageFile: File): Result<Producto> = withContext(Dispatchers.IO) {
        try {
            val requestBody = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("file", imageFile.name, requestBody)

            val response = apiService.uploadProductoImage(id, part)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    val producto = body.data.toDomain()
                    if (producto != null) {
                        Result.Success(producto)
                    } else {
                        Result.Error("No se pudo convertir el producto")
                    }
                } else {
                    Result.Error("No se pudo subir la imagen")
                }
            } else {
                Result.Error("Error HTTP ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("ProductoRepository", "Error al subir imagen", e)
            Result.Error("Error al subir imagen: ${e.message}", e)
        }
    }
}
