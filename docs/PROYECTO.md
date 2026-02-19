# Estado del Proyecto - EquipajeApp

## ¿Qué es?

App Android para **control de equipaje en micros de larga distancia**. Permite registrar el equipaje de los pasajeros asociándolo a sus boletos mediante códigos QR/marbetes.

**Cumple con:** Resolución 4/2026 (Gobierno de Argentina, enero 2026) - Uso de sistemas digitales para control de equipaje.

---

## Funcionalidad

El chofer del micro:

1. **Se loguea** con número de interno y contraseña (`Eq_Login`)
2. **Lee el boleto** del pasajero (`Eq_LeerBoleto`)
3. **Escanera el marbete** del equipaje (`Eq_LeerEquipeje`)
4. **Ve la lista** de equipajes registrados (`Eq_ListaDeEquipajes`)

---

## Estado Actual

### ✅ Completado

- [x] Estructura del proyecto Android (Clean Architecture)
- [x] Configuración Gradle con Kotlin DSL
- [x] Dependency Injection con Hilt
- [x] Integración con 4 Stored Procedures según PDF
  - [x] Eq_Login (autenticación)
  - [x] Eq_LeerBoleto (leer boleto)
  - [x] Eq_LeerEquipeje (validar marbete)
  - [x] Eq_ListaDeEquipajes (listar equipajes)
- [x] Pantalla Login
- [x] Pantalla Dashboard con listado de equipajes
- [x] Pantalla Registro de Equipaje (2 pasos: boleto + marbete)
- [x] Escáner QR (CameraX + ML Kit)
- [x] Persistencia de sesión con DataStore
- [x] Material Design 3

### ⏳ Pendiente (requieren datos del backend)

- [ ] Configurar URL real del webservice
- [ ] Testing con backend real
- [ ] Ajustes finos según respuestas del servidor

---

## Arquitectura

```
┌──────────────────────────────────────────────────────────────┐
│                        UI Layer                              │
│  LoginActivity → DashboardActivity → RegistroEquipajeActivity│
└─────────────────────────┬────────────────────────────────────┘
                          │
┌─────────────────────────▼────────────────────────────────────┐
│                     ViewModels                               │
│  (StateFlow: Idle, Loading, Success, Error)                  │
└─────────────────────────┬────────────────────────────────────┘
                          │
┌─────────────────────────▼────────────────────────────────────┐
│                    Use Cases                                 │
│  LoginUseCase | LeerBoletoUseCase | LeerEquipajeUseCase     │
│  ListaDeEquipajesUseCase                                     │
└─────────────────────────┬────────────────────────────────────┘
                          │
┌─────────────────────────▼────────────────────────────────────┐
│                 Repository Interface                         │
│  AuthRepository | ServicioRepository | EquipajeRepository    │
└─────────────────────────┬────────────────────────────────────┘
                          │
┌─────────────────────────▼────────────────────────────────────┐
│                  Data Layer (Impl)                           │
│  Retrofit → 4 Stored Procedures (Eq_Login, Eq_LeerBoleto...) │
└──────────────────────────────────────────────────────────────┘
```

---

## Stack Tecnológico

| Componente | Tecnología |
|------------|------------|
| Lenguaje | Kotlin |
| IDE | Android Studio |
| UI | XML layouts + ViewBinding |
| Red | Retrofit + OkHttp + Gson |
| DI | Hilt |
| Persistencia | DataStore Preferences |
| Cámara | CameraX + ML Kit |
| Arquitectura | MVVM + Clean Architecture |

---

## Integración con Backend

La app se comunica con **4 Stored Procedures** según especificación del PDF:

| SP | Función | Request | Response |
|----|---------|---------|----------|
| Eq_Login | Login | NrOInterno, PasswordUsuario, Usuario, Password | Error, Descr, IdServicio, Servicio |
| Eq_LeerBoleto | Leer boleto | Empresa, Boleto, IdServicio, Usuario, Password | Error, Descr, IdBoleto, Butaca, Pasajero, Documento |
| Eq_LeerEquipeje | Validar marbete | IdServicio, IdBoleto, Marbete, Usuario, Password | Error, Descr |
| Eq_ListaDeEquipajes | Listar equipajes | IdServicio, Usuario, Password | Error, Descr, Lista de equipajes |

**Credenciales fijas:**
- Usuario: `dUDl7aR`
- Password: `dPu8rSHsA*`

---

## Para Continuar

### 1. Configurar URL del Webservice

**Archivo:** `app/src/main/java/com/transporte/equipajeapp/data/remote/NetworkClient.kt`

```kotlin
// Línea 14 - Cambiar por la URL real:
private const val BASE_URL = "http://url-del-webservice/"
```

### 2. Verificar Endpoints

Asegurarse de que el servidor tenga implementados:
- `POST Eq_Login`
- `POST Eq_LeerBoleto`
- `POST Eq_LeerEquipeje`
- `POST Eq_ListaDeEquipajes`

### 3. Probar Flujo

1. Login con interno/password
2. Leer boleto (empresa + número)
3. Validar marbete
4. Ver listado de equipajes

---

## Documentación Relacionada

| Archivo | Contenido |
|---------|-----------|
| `docs/WEBSERVICE_CONFIG.md` | Especificación de los 4 Stored Procedures |
| `docs/PROGRAMADOR.md` | Documentación técnica detallada |
| `data/remote/NetworkClient.kt` | Configuración de URL |
| `data/model/ApiModels.kt` | Modelos de datos |

---

## Notas

- **El proyecto está listo para compilar** (`./gradlew assembleDebug`)
- **Usa Gradle 8.x + JDK 17**
- **APK generado en:** `app/build/outputs/apk/debug/`
- **No requiere base de datos local** (todo va contra el webservice)

---

**Última actualización:** 2026-02-13  
**Versión:** 1.0.0 (integración con Stored Procedures)
