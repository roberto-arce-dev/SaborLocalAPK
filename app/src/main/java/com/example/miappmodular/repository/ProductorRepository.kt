package com.example.miappmodular.repository

import android.util.Log
import com.example.miappmodular.data.mapper.toDomain
import com.example.miappmodular.data.mapper.toProductorDomainList
import com.example.miappmodular.data.remote.RetrofitClient
import com.example.miappmodular.data.remote.dto.productor.CreateProductorRequest
import com.example.miappmodular.model.Productor
import com.example.miappmodular.model.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

/**
 * Repositorio para gestionar operaciones relacionadas con Productores
 *
 * **Arquitectura simple:**
 * Accede directamente a RetrofitClient para obtener el API service.
 */
class ProductorRepository {

    // Acceso directo al API service desde RetrofitClient
    private val apiService = RetrofitClient.saborLocalProductorApiService

    /**
     * Obtiene la lista de todos los productores
     */
    suspend fun getProductores(): Result<List<Productor>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getProductores()

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    val productores = body.data.toProductorDomainList()
                    Result.Success(productores)
                } else {
                    Result.Error("No se pudieron obtener los productores")
                }
            } else {
                Result.Error("Error HTTP ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("ProductorRepository", "Error al obtener productores", e)
            Result.Error("Error de red: ${e.message}", e)
        }
    }

    /**
     * Obtiene un productor específico por ID
     */
    suspend fun getProductor(id: String): Result<Productor> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getProductor(id)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    val productor = body.data.toDomain()
                    Result.Success(productor)
                } else {
                    Result.Error("Productor no encontrado")
                }
            } else {
                Result.Error("Error HTTP ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("ProductorRepository", "Error al obtener productor", e)
            Result.Error("Error de red: ${e.message}", e)
        }
    }

    /**
     * Crea un nuevo productor
     */
    suspend fun createProductor(
        nombre: String,
        ubicacion: String,
        telefono: String,
        email: String
    ): Result<Productor> = withContext(Dispatchers.IO) {
        try {
            val request = CreateProductorRequest(
                nombre = nombre,
                ubicacion = ubicacion,
                telefono = telefono,
                email = email
            )

            val response = apiService.createProductor(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    val productor = body.data.toDomain()
                    Result.Success(productor)
                } else {
                    Result.Error("No se pudo crear el productor")
                }
            } else {
                Result.Error("Error HTTP ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("ProductorRepository", "Error al crear productor", e)
            Result.Error("Error de red: ${e.message}", e)
        }
    }

    /**
     * Actualiza un productor existente
     */
    suspend fun updateProductor(
        id: String,
        nombre: String? = null,
        ubicacion: String? = null,
        telefono: String? = null,
        email: String? = null
    ): Result<Productor> = withContext(Dispatchers.IO) {
        try {
            val updates = mutableMapOf<String, Any>()
            nombre?.let { updates["nombre"] = it }
            ubicacion?.let { updates["ubicacion"] = it }
            telefono?.let { updates["telefono"] = it }
            email?.let { updates["email"] = it }

            val response = apiService.updateProductor(id, updates)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    val productor = body.data.toDomain()
                    Result.Success(productor)
                } else {
                    Result.Error("No se pudo actualizar el productor")
                }
            } else {
                Result.Error("Error HTTP ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("ProductorRepository", "Error al actualizar productor", e)
            Result.Error("Error de red: ${e.message}", e)
        }
    }

    /**
     * Elimina un productor
     */
    suspend fun deleteProductor(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteProductor(id)

            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                Result.Error("Error HTTP ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("ProductorRepository", "Error al eliminar productor", e)
            Result.Error("Error de red: ${e.message}", e)
        }
    }

    /**
     * Sube una imagen para un productor
     */
    suspend fun uploadImage(id: String, imageFile: File): Result<Productor> = withContext(Dispatchers.IO) {
        try {
            val requestBody = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("file", imageFile.name, requestBody)

            val response = apiService.uploadProductorImage(id, part)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    val productor = body.data.toDomain()
                    Result.Success(productor)
                } else {
                    Result.Error("No se pudo subir la imagen")
                }
            } else {
                Result.Error("Error HTTP ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("ProductorRepository", "Error al subir imagen", e)
            Result.Error("Error al subir imagen: ${e.message}", e)
        }
    }
}
