# EquipajeApp - Documentación Técnica Completa

**Fecha:** 19 de Febrero de 2026  
**Versión:** 1.1.0  
**Estado:** En desarrollo - WebService integrado, pendiente testing con datos reales

---

## 📋 ÍNDICE

1. [Resumen Ejecutivo](#1-resumen-ejecutivo)
2. [Arquitectura del Proyecto](#2-arquitectura-del-proyecto)
3. [Estructura de Carpetas](#3-estructura-de-carpetas)
4. [Configuración Actual](#4-configuración-actual)
5. [WebService SOAP](#5-webservice-soap)
6. [Dependencias Principales](#6-dependencias-principales)
7. [Flujos de la Aplicación](#7-flujos-de-la-aplicación)
8. [Pendientes y TODOs](#8-pendientes-y-todos)
9. [Checklist para Retomar](#9-checklist-para-retomar)
10. [Notas Importantes](#10-notas-importantes)

---

## 1. Resumen Ejecutivo

Aplicación Android para choferes de Delta Transporte que permite:
- Login con número de interno
- Ver lista de servicios asignados
- Registrar equipaje (asociar boleto + marbete)
- Consultar equipaje para control policial (Gendarmería/Tránsito)

**Estado actual:**
- ✅ Conectada al WebService real (SOAP)
- ✅ Compilación exitosa
- ✅ APK generada y subida a GitHub
- ⏳ Pendiente: Testing con datos reales del WebService
- ⏳ Pendiente: Parseo completo de respuestas XML

---

## 2. Arquitectura del Proyecto

### Patrón: MVVM + Clean Architecture + Repository Pattern

```
UI Layer (Activities/Fragments)
    ↓
ViewModel (Lógica de presentación)
    ↓
UseCases (Casos de uso) - OPCIONAL
    ↓
Repository (Abstracción de datos)
    ↓
Data Layer (Remote/Local)
    ↓
WebService SOAP / Local Storage
```

### Componentes principales:

| Capa | Archivos Clave |
|------|---------------|
| **UI** | `LoginActivity.kt`, `DashboardActivity.kt`, `ServicioDetalleActivity.kt` |
| **ViewModel** | `LoginViewModel.kt`, `DashboardViewModel.kt` |
| **Repository** | `AuthRepositoryImpl.kt`, `ServicioRepositoryImpl.kt`, `EquipajeRepositoryImpl.kt` |
| **Remote** | `SoapClient.kt` (OkHttp + XML manual) |
| **Local** | `PreferencesManager.kt` (DataStore) |
| **DI** | `AppModule.kt` (Hilt) |

---

## 3. Estructura de Carpetas

```
EquipajeApp/
├── app/src/main/java/com/transporte/equipajeapp/
│   ├── data/
│   │   ├── local/
│   │   │   └── PreferencesManager.kt      # Almacenamiento local
│   │   ├── remote/
│   │   │   ├── ApiService.kt              # Retrofit (legacy, no usado)
│   │   │   ├── NetworkClient.kt           # Retrofit config (legacy)
│   │   │   └── SoapClient.kt              # **CLIENTE SOAP ACTUAL**
│   │   ├── repository/
│   │   │   ├── AuthRepositoryImpl.kt      # Login
│   │   │   ├── ServicioRepositoryImpl.kt  # Servicios y boletos
│   │   │   ├── EquipajeRepositoryImpl.kt  # Equipajes
│   │   │   └── MockRepositories.kt        # Para testing offline
│   │   └── model/
│   │       └── ApiModels.kt               # Modelos de datos
│   ├── domain/
│   │   ├── model/
│   │   │   ├── Models.kt                  # Entidades del dominio
│   │   │   └── Result.kt                  # Wrapper de resultados
│   │   ├── repository/
│   │   │   └── Repositories.kt            # Interfaces
│   │   └── usecase/
│   │       └── UseCases.kt                # Casos de uso
│   ├── ui/
│   │   ├── login/
│   │   │   ├── LoginActivity.kt
│   │   │   └── LoginViewModel.kt
│   │   ├── dashboard/
│   │   │   ├── DashboardActivity.kt
│   │   │   ├── DashboardViewModel.kt
│   │   │   └── ServicioAdapter.kt
│   │   ├── servicio/
│   │   │   ├── ServicioDetalleActivity.kt
│   │   │   └── ServicioDetalleViewModel.kt
│   │   ├── scanner/
│   │   │   ├── QrScannerActivity.kt
│   │   │   └── QrScannerViewModel.kt
│   │   └── registro/
│   │       ├── RegistroEquipajeActivity.kt
│   │       └── RegistroEquipajeViewModel.kt
│   └── di/
│       └── AppModule.kt                   # Inyección de dependencias
├── app/src/main/res/                       # Layouts, drawables, strings
├── docs/                                   # Documentación
├── movil/                                  # APKs compiladas
└── [archivos Gradle]
```

---

## 4. Configuración Actual

### 4.1 WebService

**URL:** `http://servidordeltapy.dyndns.org/WSDelta_POS/wsdelta_pos.asmx`

**Ubicación en código:**
- Archivo: `app/src/main/java/.../data/remote/SoapClient.kt`
- Línea: `private const val URL = "http://servidordeltapy.dyndns.org/WSDelta_POS/wsdelta_pos.asmx"`

**Credenciales del Sistema (fijas):**
- Usuario: `dUDl7aR`
- Password: `dPu8rSH` (7 caracteres)

**Credenciales del Chofer:**
- NroInterno: [Pendiente - pedir al programador del WS]
- PasswordUsuario: [Pendiente - pedir al programador del WS]

### 4.2 Repositorios Activos

En `AppModule.kt` se configura el uso de repositorios **REALES**:

```kotlin
@Provides
@Singleton
fun provideAuthRepository(
    soapClient: SoapClient,
    preferencesManager: PreferencesManager
): AuthRepository = AuthRepositoryImpl(soapClient, preferencesManager)
```

**NO usar mocks** (salvo para testing offline).

### 4.3 Compilación

**Requisitos:**
- Android Studio Hedgehog o superior
- JDK 17
- Android SDK 34
- Gradle 8.2+

**Comandos:**
```bash
# Clean y build
./gradlew clean assembleDebug

# Solo build
./gradlew assembleDebug

# Instalar en dispositivo
./gradlew installDebug
```

---

## 5. WebService SOAP

### 5.1 Métodos Implementados

| Método | Uso | Estado |
|--------|-----|--------|
| `Eq_Login` | Autenticación del chofer | ✅ Implementado |
| `Eq_LeerBoleto` | Validar boleto y obtener datos pasajero | ✅ Implementado |
| `Eq_LeerEquipaje` | Asociar marbete con boleto | ✅ Implementado |
| `Eq_ListaDeEquipajes` | Consultar equipajes del servicio | ✅ Implementado |

### 5.2 Implementación Técnica

**Cliente:** `SoapClient.kt` usa **OkHttp** + XML manual

**Formato Request SOAP 1.1:**
```xml
POST /WSDelta_POS/wsdelta_pos.asmx HTTP/1.1
Host: servidordeltapy.dyndns.org
Content-Type: text/xml; charset=utf-8
SOAPAction: "Delta/Eq_Login"

<?xml version="1.0" encoding="utf-8"?>
<soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
               xmlns:xsd="http://www.w3.org/2001/XMLSchema" 
               xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <Eq_Login xmlns="Delta">
      <NroInterno>string</NroInterno>
      <PasswordUsuario>string</PasswordUsuario>
      <Usuario>string</Usuario>
      <Password>string</Password>
    </Eq_Login>
  </soap:Body>
</soap:Envelope>
```

**Parseo de Respuestas:**
- Actualmente: XML manual con `XmlPullParser`
- Pendiente: Implementar parseo completo de DataSets

---

## 6. Dependencias Principales

### Core Android
```kotlin
implementation("androidx.core:core-ktx:1.12.0")
implementation("androidx.appcompat:appcompat:1.6.1")
implementation("com.google.android.material:material:1.11.0")
```

### Inyección de Dependencias
```kotlin
implementation("com.google.dagger:hilt-android:2.51.1")
kapt("com.google.dagger:hilt-android-compiler:2.51.1")
```

### Networking (SOAP/HTTP)
```kotlin
// OkHttp para requests SOAP
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

// Retrofit (legacy, no usado actualmente)
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
```

### Cámara y QR
```kotlin
// CameraX
implementation("androidx.camera:camera-core:1.3.1")
implementation("androidx.camera:camera-camera2:1.3.1")
implementation("androidx.camera:camera-lifecycle:1.3.1")
implementation("androidx.camera:camera-view:1.3.1")

// ML Kit para QR
implementation("com.google.mlkit:barcode-scanning:17.2.0")
```

### Almacenamiento
```kotlin
// DataStore (reemplaza a SharedPreferences)
implementation("androidx.datastore:datastore-preferences:1.0.0")

// Room (base de datos local - opcional)
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
kapt("androidx.room:room-compiler:2.6.1")
```

### Programación Asíncrona
```kotlin
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
```

---

## 7. Flujos de la Aplicación

### FLUJO 1: Login y Selección de Servicio

```
Usuario: Ingresa interno (ej: 1001)
    ↓
LoginActivity → LoginViewModel
    ↓
AuthRepositoryImpl.login()
    ↓
SoapClient.login(EqLoginRequest)
    ↓
POST Eq_Login al WebService
    ↓
Response: Lista de servicios del chofer
    ↓
Guardar en PreferencesManager
    ↓
Navegar a DashboardActivity
    ↓
Mostrar lista de servicios
    ↓
Usuario selecciona un servicio
    ↓
Navegar a ServicioDetalleActivity
```

### FLUJO 2: Registrar Equipaje (Subir a Bodega)

```
ServicioDetalleActivity
    ↓
Usuario toca "Escanear Boleto"
    ↓
QrScannerActivity (resultado: qrContent)
    ↓
ServicioRepositoryImpl.leerBoleto()
    ↓
SoapClient.leerBoleto(EqLeerBoletoRequest)
    ↓
POST Eq_LeerBoleto
    ↓
Response: Datos del pasajero o error
    ↓
Si OK: Mostrar datos del pasajero
    ↓
Usuario toca "Escanear Marbete"
    ↓
QrScannerActivity (resultado: marbete)
    ↓
EquipajeRepositoryImpl.leerEquipaje()
    ↓
SoapClient.leerEquipaje(EqLeerEquipajeRequest)
    ↓
POST Eq_LeerEquipaje
    ↓
Response: Error 0 (OK) o error
    ↓
Si OK: Mostrar "Equipaje registrado"
```

### FLUJO 3: Consultar Equipaje (Gendarmería/Tránsito)

```
Micro detenido por control
    ↓
Chofer abre app → Escanea boleto del pasajero
    ↓
Obtiene IdBoleto
    ↓
EquipajeRepositoryImpl.listaDeEquipajes()
    ↓
SoapClient.listaDeEquipajes(EqListaEquipajesRequest)
    ↓
POST Eq_ListaDeEquipajes
    ↓
Response: Lista de todos los equipajes del servicio
    ↓
Filtrar por IdBoleto
    ↓
Mostrar: "Pasajero X tiene marbetes: 0014367, 0014368"
    ↓
Agente busca esos marbetes en bodega
```

---

## 8. Pendientes y TODOs

### 🔴 CRÍTICO (Antes de producción)

1. **Testing con WebService real**
   - Obtener interno real del programador del WS
   - Probar login
   - Verificar que devuelve lista de servicios
   - Probar lectura de boletos
   - Probar asociación de marbetes

2. **Parseo completo de respuestas XML**
   - Actual: XML se devuelve como String sin parsear completamente
   - Pendiente: Implementar parseo robusto de DataSets XML
   - Archivos afectados: `ServicioRepositoryImpl.kt`, `EquipajeRepositoryImpl.kt`

3. **Manejo de errores del WebService**
   - Cuando boleto no pertenece al servicio
   - Cuando marbete ya está usado
   - Cuando servicio no tiene equipajes

### 🟡 IMPORTANTE (Para v1.2)

4. **Mejoras sugeridas por el otro programador:**
   - Refactorizar `SoapClient.kt` con función genérica `executeSoapCall`
   - Implementar serialización XML con `simple-xml`
   - Manejo específico de SOAP Faults
   - Mover URL del WebService a archivo de configuración

5. **UI/UX:**
   - Diseño final con colores de Delta
   - Pantallas de carga (loading)
   - Mensajes de error más amigables
   - Validación de cámara y permisos

6. **Seguridad:**
   - No hardcodear credenciales del sistema
   - Usar Android Keystore para datos sensibles
   - Implementar ProGuard para release

### 🟢 OPCIONAL (Futuro)

7. **Funcionalidades adicionales:**
   - Sincronización offline
   - Historial de equipajes
   - Estadísticas
   - Notificaciones push

8. **Optimizaciones:**
   - Cache de datos
   - Compresión de imágenes QR
   - Lazy loading

---

## 9. Checklist para Retomar el Proyecto

### Si vas a continuar desarrollando:

- [ ] **Abrir proyecto:** File → Open → `C:\laragon\www\android\EquipajeApp` (carpeta raíz, NO app/)
- [ ] **Sincronizar Gradle:** File → Sync Project with Gradle Files
- [ ] **Verificar configuración:** Revisar `SoapClient.kt` tenga URL correcta del WebService
- [ ] **Credenciales:** Confirmar que `SYSTEM_USER` y `SYSTEM_PASSWORD` sean correctos
- [ ] **Compilar:** Build → Rebuild Project
- [ ] **Probar:** Ejecutar en dispositivo/emulador

### Si vas a probar con WebService real:

- [ ] **Obtener datos:** Pedir al programador del WS:
  - Interno de prueba
  - Password del chofer (si aplica)
  - Confirmar servicios cargados
- [ ] **APK:** Instalar `movil/EquipajeApp-v1.1.0-webservice.apk`
- [ ] **Conexión:** Verificar internet en dispositivo
- [ ] **Test:** Login → Seleccionar servicio → Escanear boleto

### Si vas a implementar mejoras:

- [ ] **Leer:** Revisar sección "Pendientes y TODOs" arriba
- [ ] **Priorizar:** Decidir qué es crítico vs opcional
- [ ] **Backup:** Crear rama git antes de cambios grandes
- [ ] **Documentar:** Actualizar este archivo con cambios

---

## 10. Notas Importantes

### Sobre el WebService
- **Tipo:** SOAP 1.1 sobre HTTP
- **Namespace:** `Delta`
- **Respuestas:** DataSets XML (formato complejo .NET)
- **Timeout:** 30 segundos configurado
- **Autenticación:** Doble nivel (sistema + chofer)

### Sobre la Arquitectura
- **Patrón:** MVVM recomendado por Google
- **DI:** Hilt (inyección de dependencias)
- **Async:** Corrutinas de Kotlin
- **Storage:** DataStore (más moderno que SharedPreferences)

### Sobre el Código
- **Idioma:** Kotlin (100%)
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 34 (Android 14)
- **Java:** 17

### Sobre el Escaneo QR
- **Librería:** ML Kit de Google (más moderna que ZXing)
- **Cámara:** CameraX (API actual de Android)
- **Formatos:** Soporta QR, Code 128, Code 39, etc.

### Sobre Git
- **Repositorio:** `https://github.com/sergioecm60/EquipajeApp.git`
- **Rama principal:** `main`
- **APKs:** Carpeta `movil/` (no ignorada por .gitignore)
- **Docs:** Carpeta `docs/` con toda la documentación

---

## 📞 Contacto y Soporte

**Desarrollador:** Sergio Cabrera  
**Email:** sergiomiers@gmail.com  
**WhatsApp:** +54 11 6759-8452  
**Empresa:** SECM Soluciones TI

**WebService:** Delta Transporte  
**Programador WS:** [Nombre del contacto]

---

**Última actualización:** 19/02/2026  
**Próxima revisión:** Después de testing con WebService real