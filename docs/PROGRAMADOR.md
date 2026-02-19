# Documentación Técnica - EquipajeApp

## Introducción

Aplicación Android nativa para control de equipaje en micros de larga distancia, cumpliendo Resolución 4/2026 (Argentina).

**Integración:** 4 Stored Procedures (SP) según especificación PDF.

---

## Stack Tecnológico

| Componente | Tecnología | Versión |
|------------|------------|---------|
| Lenguaje | Kotlin | 1.9.x |
| IDE | Android Studio | Hedgehog+ |
| SDK Mínimo | API 24 | Android 7.0 |
| SDK Target | API 34 | Android 14 |
| Arquitectura | Clean Architecture + MVVM | - |
| DI | Hilt | 2.48.1 |
| Networking | Retrofit + OkHttp + Gson | 2.9.0 |
| Persistencia | DataStore Preferences | 1.0.0 |
| Cámara | CameraX + ML Kit | 1.3.1 / 17.2.0 |
| UI | Material Design 3 + ViewBinding | - |

---

## Estructura del Proyecto

```
app/src/main/java/com/transporte/equipajeapp/
│
├── EquipajeApplication.kt              # Application class (@HiltAndroidApp)
│
├── domain/                              # Lógica de negocio (sin dependencias externas)
│   ├── model/
│   │   └── Models.kt                   # Usuario, Boleto, Equipaje, EquipajeListado, Result
│   ├── repository/
│   │   └── Repositories.kt             # Interfaces: AuthRepository, ServicioRepository, EquipajeRepository
│   └── usecase/
│       └── UseCases.kt                 # Login, LeerBoleto, LeerEquipaje, ListaDeEquipajes
│
├── data/                                # Implementaciones
│   ├── model/
│   │   └── ApiModels.kt                # DTOs: EqLoginRequest/Response, EqLeerBoletoRequest/Response, etc.
│   ├── remote/
│   │   ├── ApiService.kt               # Definición de 4 endpoints Retrofit
│   │   └── NetworkClient.kt            # Configuración BASE_URL y timeouts
│   ├── local/
│   │   └── PreferencesManager.kt       # DataStore: sesión + credenciales SP
│   └── repository/
│       ├── AuthRepositoryImpl.kt       # Implementa Eq_Login
│       ├── ServicioRepositoryImpl.kt   # Implementa Eq_LeerBoleto
│       └── EquipajeRepositoryImpl.kt   # Implementa Eq_LeerEquipeje y Eq_ListaDeEquipajes
│
├── ui/                                  # Presentación
│   ├── MainActivity.kt                 # Entry point (routing)
│   ├── login/
│   │   ├── LoginActivity.kt
│   │   └── LoginViewModel.kt
│   ├── dashboard/
│   │   ├── DashboardActivity.kt
│   │   ├── DashboardViewModel.kt
│   │   └── ServicioAdapter.kt
│   ├── scanner/
│   │   ├── QrScannerActivity.kt        # CameraX + ML Kit
│   │   └── QrScannerViewModel.kt
│   └── registro/
│       ├── RegistroEquipajeActivity.kt # Flujo completo: Boleto + Marbete
│       └── RegistroEquipajeViewModel.kt
│
└── di/
    └── AppModule.kt                    # Módulos Hilt
```

---

## Flujo de Datos (4 Stored Procedures)

```
┌─────────────────────────────────────────────────────────────────────┐
│ 1. Eq_Login                                                         │
│    POST Eq_Login                                                    │
│    { NrOInterno, PasswordUsuario, Usuario, Password }              │
│    → { Error, Descr, IdServicio, Servicio }                        │
│    → Guarda IdServicio y credenciales en DataStore                 │
└──────────────────────────────────┬──────────────────────────────────┘
                                   │
┌──────────────────────────────────▼──────────────────────────────────┐
│ 2. Eq_LeerBoleto                                                    │
│    POST Eq_LeerBoleto                                               │
│    { Empresa, Boleto, IdServicio, Usuario, Password }              │
│    → { Error, Descr, IdBoleto, Butaca, Pasajero, Documento }       │
│    → Muestra datos del pasajero                                     │
└──────────────────────────────────┬──────────────────────────────────┘
                                   │
┌──────────────────────────────────▼──────────────────────────────────┐
│ 3. Eq_LeerEquipeje                                                  │
│    POST Eq_LeerEquipeje                                             │
│    { IdServicio, IdBoleto, Marbete, Usuario, Password }            │
│    → { Error, Descr }                                              │
│    → Valida que el marbete puede usarse                             │
└──────────────────────────────────┬──────────────────────────────────┘
                                   │
┌──────────────────────────────────▼──────────────────────────────────┐
│ 4. Eq_ListaDeEquipajes (opcional)                                   │
│    POST Eq_ListaDeEquipajes                                         │
│    { IdServicio, Usuario, Password }                               │
│    → { Error, Descr, Equipajes[] }                                 │
│    → Parsea campo Texto: "cantidad -documento- pasajero marbetes"   │
└─────────────────────────────────────────────────────────────────────┘
```

---

## Modelos de Datos Principales

### Domain Models (domain/model/Models.kt)

```kotlin
data class Usuario(
    val id: Int,              // IdServicio del login
    val interno: String,      // NrOInterno
    val nombre: String,       // Servicio (ej: "EPA ASU-ENC 31/01/2026 17:30")
    val empresa: String
)

data class Boleto(
    val id: Int,              // IdBoleto
    val numero: String,       // Número del boleto
    val pasajero: String,     // Nombre del pasajero
    val dni: String,          // Documento
    val origen: String,
    val destino: String,
    val fecha: String,
    val servicioId: Int
)

data class EquipajeListado(
    val hdIdBoleto: Int,      // HD_IdBoleto
    val cantidad: Int,        // Parseado del campo Texto
    val documento: String,    // Parseado del campo Texto
    val pasajero: String,     // Parseado del campo Texto
    val marbetes: List<String> // Parseado del campo Texto
)
```

### API Models (data/model/ApiModels.kt)

**Request/Response para cada SP:**

```kotlin
// Eq_Login
data class EqLoginRequest(
    val nroInterno: String,        // CHAR(10)
    val passwordUsuario: String,   // CHAR(7)
    val usuario: String,           // CHAR(7) - FIJO: "dUDl7aR"
    val password: String           // CHAR(7) - FIJO: "dPu8rSHsA*"
)

data class EqLoginResponse(
    val error: Int,                // 0 = OK
    val descr: String?,            // OK / Mensaje de error
    val idServicio: Int?,          // ID del servicio actual
    val servicio: String?          // Descripción del servicio
)

// Eq_LeerBoleto
data class EqLeerBoletoRequest(
    val empresa: String,           // CHAR(3) - Ej: "EPA"
    val boleto: Long,              // BIGINT
    val idServicio: Int,
    val usuario: String,
    val password: String
)

data class EqLeerBoletoResponse(
    val error: Int,
    val descr: String?,
    val idBoleto: Int?,
    val butaca: Int?,
    val pasajero: String?,         // VARCHAR(30)
    val documento: String?         // CHAR(20)
)

// Eq_LeerEquipeje
data class EqLeerEquipajeRequest(
    val idServicio: Int,
    val idBoleto: Int,
    val marbete: String,           // VARCHAR(15)
    val usuario: String,
    val password: String
)

data class EqLeerEquipajeResponse(
    val error: Int,
    val descr: String?
)

// Eq_ListaDeEquipajes
data class EqListaEquipajesRequest(
    val idServicio: Int,
    val usuario: String,
    val password: String
)

data class EqListaEquipajesItem(
    val hdIdBoleto: Int,
    val texto: String?             // VARCHAR(130)
)

data class EqListaEquipajesResponse(
    val error: Int,
    val descr: String?,
    val equipajes: List<EqListaEquipajesItem>?
)
```

---

## Configuración de Red

**Archivo:** `data/remote/NetworkClient.kt`

```kotlin
private const val BASE_URL = "http://url-del-webservice/"
private const val TIMEOUT_SECONDS = 30L

// Headers automáticos:
// Content-Type: application/json
// Accept: application/json
```

**Endpoints definidos en ApiService.kt:**

```kotlin
@POST("Eq_Login")
suspend fun login(@Body request: EqLoginRequest): Response<EqLoginResponse>

@POST("Eq_LeerBoleto")
suspend fun leerBoleto(@Body request: EqLeerBoletoRequest): Response<EqLeerBoletoResponse>

@POST("Eq_LeerEquipeje")
suspend fun leerEquipaje(@Body request: EqLeerEquipajeRequest): Response<EqLeerEquipajeResponse>

@POST("Eq_ListaDeEquipajes")
suspend fun listaDeEquipajes(@Body request: EqListaEquipajesRequest): Response<EqListaEquipajesResponse>
```

---

## Persistencia de Sesión

**Archivo:** `data/local/PreferencesManager.kt`

Guarda:
- Datos del usuario logueado (id, interno, nombre, empresa)
- Credenciales para SPs (usuario: "dUDl7aR", password: "dPu8rSHsA*", idServicio)

```kotlin
// Al hacer login exitoso:
preferencesManager.saveCredentials(
    usuario = "dUDl7aR",
    password = "dPu8rSHsA*",
    idServicio = response.idServicio
)
```

---

## Manejo de Errores

Todos los SPs devuelven:

```kotlin
if (response.error == 0) {
    // Éxito - procesar datos
} else {
    // Error - mostrar response.descr
}
```

**En UI (ViewModels):**

```kotlin
sealed class Result<out T> {
    data class Success(val data: T) : Result<T>()
    data class Error(val message: String) : Result<T>()
}
```

---

## Parseo del Campo Texto (Eq_ListaDeEquipajes)

**Formato:** `"15 -39503917- BARRIOS, ARTURO MOISES 0014367,0014368 0014369"`

**Implementación:** `EquipajeListadoInfo.parse(texto)`

```kotlin
data class EquipajeListadoInfo(
    val cantidad: Int,           // 15
    val documento: String,       // 39503917
    val pasajero: String,        // BARRIOS, ARTURO MOISES
    val marbetes: List<String>   // [0014367, 0014368, 0014369]
)
```

---

## Testing

```bash
# Compilar debug APK
./gradlew assembleDebug

# Verificar lint
./gradlew lint

# Instalar en dispositivo
./gradlew installDebug
```

**APK generado en:** `app/build/outputs/apk/debug/app-debug.apk`

---

## Configuración para Producción

### 1. Cambiar URL del Webservice

```kotlin
// NetworkClient.kt línea 14
private const val BASE_URL = "https://tu-servidor-produccion.com/"
```

### 2. HTTPS (obligatorio en producción)

En `AndroidManifest.xml` ya está permitido HTTP para desarrollo:
```xml
android:networkSecurityConfig="@xml/network_security_config"
```

Para producción HTTPS, no se necesita cambio si el certificado es válido.

### 3. ProGuard (ofuscación)

En `build.gradle.kts`:
```kotlin
buildTypes {
    release {
        isMinifyEnabled = true
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }
}
```

---

## Errores Comunes

| Error | Solución |
|-------|----------|
| "Cannot locate tasks" | Cambiar configuración de ejecución de "testReleaseUnitTest" a "app" |
| "Network error" | Verificar URL en NetworkClient.kt |
| "Error 500" | Verificar que SPs existan en el servidor |
| "Parse error" | Verificar formato JSON de respuesta |
| "Camera permission" | Verificar permisos en AndroidManifest.xml |

---

## Archivos Clave

| Ruta | Descripción |
|------|-------------|
| `docs/WEBSERVICE_CONFIG.md` | Especificación de los 4 SPs |
| `docs/PROYECTO.md` | Estado del proyecto |
| `data/remote/NetworkClient.kt` | URL del webservice |
| `data/remote/ApiService.kt` | Definición de endpoints |
| `data/model/ApiModels.kt` | Modelos de request/response |
| `domain/model/Models.kt` | Modelos de dominio |

---

**Última actualización:** 2026-02-13  
**Versión:** 1.0.0 (integración con Stored Procedures)
