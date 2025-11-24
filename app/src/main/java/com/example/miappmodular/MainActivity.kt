package com.example.miappmodular

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.miappmodular.data.remote.RetrofitClient
import com.example.miappmodular.ui.navigation.AppNavigation
import com.example.miappmodular.ui.theme.MiAppModularTheme

/**
 * Actividad principal y punto de entrada de la aplicación.
 *
 * Esta actividad es el contenedor raíz que aloja toda la UI de Jetpack Compose.
 * No contiene lógica de negocio ni UI específica, solo inicializa:
 * 1. RetrofitClient (para configuración de red)
 * 2. El tema Material3 de la app ([MiAppModularTheme])
 * 3. El grafo de navegación ([AppNavigation])
 *
 * **Arquitectura simple:**
 * ```
 * MainActivity (Activity)
 *   └── RetrofitClient.initialize(context)  ← Inicializa TokenManager
 *   └── MiAppModularTheme (Material3 Theme)
 *       └── Surface (Background container)
 *           └── AppNavigation (NavHost)
 *               ├── LoginScreen → LoginViewModel
 *               ├── RegisterScreen → RegisterViewModel
 *               ├── HomeScreen → ...
 *               └── ProfileScreen → ...
 * ```
 *
 * **Configuración en AndroidManifest.xml:**
 * Esta actividad debe estar configurada como `MAIN` y `LAUNCHER`:
 * ```xml
 * <activity
 *     android:name=".MainActivity"
 *     android:exported="true">
 *     <intent-filter>
 *         <action android:name="android.intent.action.MAIN" />
 *         <category android:name="android.intent.category.LAUNCHER" />
 *     </intent-filter>
 * </activity>
 * ```
 *
 * **Ciclo de vida:**
 * - `onCreate()` se llama una vez al crear la actividad
 * - Inicializa RetrofitClient con el contexto (para TokenManager)
 * - `setContent {}` establece la UI Compose (reemplaza setContentView en XML)
 * - La navegación y estado se manejan mediante Compose Navigation y ViewModels
 *
 * @see RetrofitClient
 * @see MiAppModularTheme
 * @see AppNavigation
 */
class MainActivity : ComponentActivity() {

    /**
     * Punto de entrada del ciclo de vida de la actividad.
     *
     * Inicializa:
     * 1. **RetrofitClient** - Configura TokenManager para gestión segura de tokens
     * 2. **Tema Material3** personalizado
     * 3. **Surface** con color de fondo del tema
     * 4. **Sistema de navegación** de la app
     *
     * **IMPORTANTE:**
     * RetrofitClient.initialize() debe llamarse ANTES de setContent {}
     * porque los ViewModels pueden necesitar acceder al TokenManager.
     *
     * @param savedInstanceState Estado guardado de la actividad (null en primera ejecución).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar RetrofitClient con el contexto de la aplicación
        // Esto configura TokenManager (EncryptedSharedPreferences) para tokens seguros
        RetrofitClient.initialize(this)

        setContent {
            MiAppModularTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}
