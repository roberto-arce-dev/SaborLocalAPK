package com.example.miappmodular.utils

/**
 * Utilidad centralizada de validación para formularios de usuario.
 *
 * Este object proporciona funciones puras de validación para campos comunes
 * como email, contraseña y nombre. Todas las funciones retornan `null` si
 * la validación es exitosa, o un mensaje de error descriptivo si falla.
 *
 * **Ventajas de centralizar validaciones:**
 * - Consistencia en reglas de validación en toda la app
 * - Reutilización en múltiples pantallas (Login, Register, Profile)
 * - Facilita testing unitario
 * - Mensajes de error uniformes para mejor UX
 * - Fácil modificación de reglas desde un solo lugar
 *
 * **Patrón de uso:**
 * ```kotlin
 * val emailError = ValidationUtils.validateEmail(email)
 * if (emailError != null) {
 *     // Mostrar error en UI
 *     _uiState.value = _uiState.value.copy(emailError = emailError)
 * } else {
 *     // Email válido, continuar
 * }
 * ```
 *
 * Ejemplo de validación antes de submit:
 * ```kotlin
 * fun onRegisterClick() {
 *     val nameError = ValidationUtils.isValidName(name)
 *     val emailError = ValidationUtils.validateEmail(email)
 *     val passwordError = ValidationUtils.validatePassword(password)
 *
 *     if (nameError == null && emailError == null && passwordError == null) {
 *         // Todos los campos son válidos, proceder con el registro
 *         viewModelScope.launch {
 *             userRepository.registerUser(name, email, password)
 *         }
 *     } else {
 *         // Mostrar errores en UI
 *         _uiState.value = RegisterUiState(
 *             nameError = nameError,
 *             emailError = emailError,
 *             passwordError = passwordError
 *         )
 *     }
 * }
 * ```
 *
 * @see com.example.miappmodular.viewmodel.LoginViewModel
 * @see com.example.miappmodular.viewmodel.RegisterViewModel
 */
object ValidationUtils {

    /**
     * Valida el formato de una dirección de correo electrónico.
     *
     * Usa el patrón `android.util.Patterns.EMAIL_ADDRESS` de Android que implementa
     * una regex robusta compatible con RFC 5322 (formato estándar de emails).
     *
     * **Reglas de validación:**
     * 1. No debe estar vacío (salvo que `allowEmpty = true`)
     * 2. Debe cumplir con el formato estándar de email (usuario@dominio.com)
     * 3. Acepta emails con subdominios (usuario@mail.empresa.com)
     * 4. Acepta TLDs válidos (.com, .org, .co.uk, etc.)
     *
     * **Casos válidos:**
     * - usuario@example.com
     * - nombre.apellido@empresa.co.uk
     * - test+filter@gmail.com
     *
     * **Casos inválidos:**
     * - "" (vacío, salvo allowEmpty = true)
     * - "usuario" (falta @dominio)
     * - "usuario@" (falta dominio)
     * - "@dominio.com" (falta usuario)
     * - "usuario @dominio.com" (espacios no permitidos)
     *
     * Ejemplo de uso:
     * ```kotlin
     * // Validación estricta (no permite vacío)
     * val error1 = ValidationUtils.validateEmail("usuario@example.com")
     * // error1 = null (válido)
     *
     * val error2 = ValidationUtils.validateEmail("invalido")
     * // error2 = "Formato de email inválido"
     *
     * // Validación opcional (permite vacío)
     * val error3 = ValidationUtils.validateEmail("", allowEmpty = true)
     * // error3 = null (válido porque allowEmpty = true)
     * ```
     *
     * @param email Dirección de email a validar.
     * @param allowEmpty Si es `true`, retorna `null` para strings vacíos.
     *                   Útil para campos opcionales en formularios.
     * @return `null` si el email es válido, o mensaje de error descriptivo.
     *
     * @see android.util.Patterns.EMAIL_ADDRESS
     */
    fun validateEmail(email: String, allowEmpty: Boolean = false): String? {
        return when {
            email.isBlank() -> if (allowEmpty) null else "El email es obligatorio"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                "Formato de email inválido"
            else -> null
        }
    }

    /**
     * Valida que una contraseña cumpla con los requisitos de seguridad.
     *
     * **Reglas de validación (todas obligatorias):**
     * 1. **No vacía:** La contraseña no puede estar en blanco
     * 2. **Longitud mínima:** Al menos 8 caracteres
     * 3. **Mayúsculas:** Debe contener al menos una letra mayúscula (A-Z)
     * 4. **Minúsculas:** Debe contener al menos una letra minúscula (a-z)
     * 5. **Números:** Debe contener al menos un dígito (0-9)
     *
     * **Nota sobre seguridad:**
     * Estos requisitos proporcionan un nivel básico de seguridad. Para apps
     * de producción, considerar:
     * - Aumentar longitud mínima a 12+ caracteres
     * - Requerir caracteres especiales (!@#$%^&*)
     * - Validar contra lista de contraseñas comunes (Have I Been Pwned API)
     * - Implementar medidor de fortaleza visual
     *
     * **Ejemplos de contraseñas:**
     * - ✅ Válida: "Password123" (8 chars, mayús, minús, número)
     * - ✅ Válida: "MiClave2024!" (12 chars, todos los requisitos)
     * - ❌ Inválida: "password" (sin mayúsculas ni números)
     * - ❌ Inválida: "Pass123" (solo 7 caracteres)
     * - ❌ Inválida: "PASSWORD123" (sin minúsculas)
     *
     * Ejemplo de uso:
     * ```kotlin
     * val password = "MiPassword123"
     * val error = ValidationUtils.validatePassword(password)
     *
     * if (error != null) {
     *     showError(error) // Muestra el primer error encontrado
     * } else {
     *     // Contraseña válida, proceder
     *     hashAndStorePassword(password)
     * }
     * ```
     *
     * Ejemplo de validación en tiempo real:
     * ```kotlin
     * fun onPasswordChange(newPassword: String) {
     *     _password.value = newPassword
     *     _passwordError.value = ValidationUtils.validatePassword(newPassword)
     * }
     * ```
     *
     * @param password Contraseña a validar.
     * @return `null` si la contraseña es válida, o el primer error encontrado.
     *         Los errores se retornan en orden de prioridad (vacío > longitud > complejidad).
     *
     * @see validateEmail
     * @see isValidName
     */
    fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "La contraseña es obligatoria"
            password.length < 8 -> "Mínimo 8 caracteres"
            !password.any { it.isUpperCase() } -> "Debe contener mayúsculas"
            !password.any { it.isLowerCase() } -> "Debe contener minúsculas"
            !password.any { it.isDigit() } -> "Debe contener números"
            else -> null
        }
    }

    /**
     * Valida que un nombre de usuario cumpla con los requisitos básicos.
     *
     * **Reglas de validación:**
     * 1. **No vacío:** El nombre no puede estar en blanco
     * 2. **Longitud mínima:** Al menos 3 caracteres
     *
     * **Nota sobre validación de nombres:**
     * Esta es una validación básica. Para producción, considerar:
     * - Permitir caracteres unicode (nombres internacionales: José, François, 李明)
     * - Validar longitud máxima razonable (ej: 50 caracteres)
     * - Permitir espacios y guiones (María José, Jean-Pierre)
     * - Evitar validación de "formato" estricto (nombres varían culturalmente)
     *
     * **Ejemplos:**
     * - ✅ Válido: "Ana"
     * - ✅ Válido: "Juan Pérez"
     * - ✅ Válido: "José María"
     * - ❌ Inválido: "" (vacío)
     * - ❌ Inválido: "Al" (solo 2 caracteres)
     *
     * Ejemplo de uso:
     * ```kotlin
     * val name = "Juan"
     * val error = ValidationUtils.isValidName(name)
     *
     * if (error == null) {
     *     // Nombre válido
     *     saveUserName(name)
     * } else {
     *     // Mostrar error en UI
     *     _nameError.value = error
     * }
     * ```
     *
     * @param name Nombre del usuario a validar.
     * @return `null` si el nombre es válido, o mensaje de error descriptivo.
     *
     * @see validateEmail
     * @see validatePassword
     */
    fun isValidName(name: String): String? {
        return when {
            name.isEmpty() -> "El nombre es requerido"
            name.length < 3 -> "Mínimo 3 caracteres"
            else -> null
        }
    }

    /**
     * Valida el formato de un número de teléfono.
     *
     * **Reglas de validación:**
     * 1. **No vacío:** El teléfono no puede estar en blanco
     * 2. **Solo números y símbolos permitidos:** +, -, espacios, paréntesis
     * 3. **Longitud mínima:** Al menos 8 dígitos (sin contar símbolos)
     * 4. **Longitud máxima:** Máximo 15 dígitos (estándar internacional)
     *
     * **Formatos aceptados (ejemplos para Chile):**
     * - ✅ "+56912345678" (formato internacional)
     * - ✅ "912345678" (formato nacional móvil)
     * - ✅ "+56 9 1234 5678" (con espacios)
     * - ✅ "+56-9-1234-5678" (con guiones)
     * - ✅ "(56) 912345678" (con paréntesis)
     * - ✅ "223456789" (fijo Santiago, 9 dígitos)
     *
     * **Formatos inválidos:**
     * - ❌ "" (vacío)
     * - ❌ "123" (muy corto)
     * - ❌ "abcd1234" (contiene letras)
     * - ❌ "12345678901234567" (muy largo, más de 15 dígitos)
     *
     * **Nota sobre formatos internacionales:**
     * Esta validación es flexible para soportar diferentes formatos de países.
     * Para validación estricta de un país específico, considerar usar
     * librerías especializadas como libphonenumber.
     *
     * Ejemplo de uso:
     * ```kotlin
     * val phone = "+56912345678"
     * val error = ValidationUtils.validatePhone(phone)
     *
     * if (error == null) {
     *     // Teléfono válido
     *     savePhone(phone)
     * } else {
     *     // Mostrar error
     *     _phoneError.value = error
     * }
     * ```
     *
     * @param phone Número de teléfono a validar.
     * @return `null` si el teléfono es válido, o mensaje de error descriptivo.
     *
     * @see validateEmail
     * @see isValidName
     */
    fun validatePhone(phone: String): String? {
        return when {
            phone.isBlank() -> "El teléfono es obligatorio"
            else -> {
                // Extraer solo los dígitos para validar longitud
                val digitsOnly = phone.filter { it.isDigit() }
                when {
                    digitsOnly.length < 8 -> "El teléfono debe tener al menos 8 dígitos"
                    digitsOnly.length > 15 -> "El teléfono no puede tener más de 15 dígitos"
                    // Verificar que solo contenga dígitos y símbolos permitidos
                    !phone.all { it.isDigit() || it in setOf('+', '-', ' ', '(', ')') } ->
                        "El teléfono solo puede contener números y símbolos (+, -, espacios, paréntesis)"
                    else -> null
                }
            }
        }
    }
}
