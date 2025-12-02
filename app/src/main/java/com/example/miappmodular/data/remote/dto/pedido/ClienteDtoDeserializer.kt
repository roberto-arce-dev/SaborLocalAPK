package com.example.miappmodular.data.remote.dto.pedido

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

/**
 * Deserializador personalizado para ClienteDto
 *
 * **Problema:**
 * El backend retorna `cliente` de dos formas diferentes según el endpoint:
 * - Lista de pedidos: `"cliente": "69263fd8a744f4759b9937a9"` (String)
 * - Detalle de pedido: `"cliente": { "_id": "...", "nombre": "..." }` (Object)
 *
 * **Solución:**
 * Este deserializador detecta automáticamente el tipo y convierte ambos a ClienteDto:
 * - Si es String → ClienteDto con solo el ID
 * - Si es Object → ClienteDto completo
 *
 * **Ventajas:**
 * - Un solo DTO para ambos casos
 * - Transparente para el resto del código
 * - No requiere cambios en Repositories o ViewModels
 */
class ClienteDtoDeserializer : JsonDeserializer<ClienteDto> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): ClienteDto {
        return when {
            // Caso 1: Es un String (solo ID)
            json.isJsonPrimitive && json.asJsonPrimitive.isString -> {
                ClienteDto(
                    id = json.asString,
                    nombre = null,
                    email = null,
                    telefono = null,
                    direccion = null
                )
            }

            // Caso 2: Es un Object (cliente completo con populate)
            json.isJsonObject -> {
                val obj = json.asJsonObject
                ClienteDto(
                    id = obj.get("_id")?.asString ?: "",
                    nombre = obj.get("nombre")?.asString,
                    email = obj.get("email")?.asString,
                    telefono = obj.get("telefono")?.asString,
                    direccion = obj.get("direccion")?.asString
                )
            }

            // Caso 3: Null o tipo inesperado
            else -> ClienteDto(
                id = "",
                nombre = null,
                email = null,
                telefono = null,
                direccion = null
            )
        }
    }
}
