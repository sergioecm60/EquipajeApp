# Resumen del Proyecto EquipajeApp

## Estado Actual

**La app Android está desarrollada y lista**, pero no puede probarse porque requiere el **webservice real** que aún no está disponible.

---

## Lo que se hizo

### App Android (completa)
- Pantalla Login
- Dashboard con listados de equipajes
- Registro de equipaje (2 pasos: boleto + marbete)
- Escáner QR con CameraX + ML Kit
- Integración con 4 Stored Procedures (pendiente de webservice)

### API PHP existente (para desarrollo local)
- `api/index.php` - API REST con acciones simples
- Base de datos: `equipaje_test` (tablas: choferes, servicios, boletos, ribetes, equipajes)

### Configuración de versiones (aplicada)
- Gradle: 8.7
- Android Gradle Plugin: 8.5.0
- Kotlin: 1.9.22
- Hilt: 2.51.1

---

## Cómo revertir para usar el webservice

### 1. Cambiar la URL del webservice

**Archivo:** `app/src/main/java/com/transporte/equipajeapp/data/remote/NetworkClient.kt`

```kotlin
// Línea 14 - Cambiar por la URL del webservice:
private const val BASE_URL = "https://tu-servidor.com/"
```

### 2. Asegurar que el webservice tenga los 4 Stored Procedures

| SP | Función |
|----|---------|
| `Eq_Login` | Login del chofer |
| `Eq_LeerBoleto` | Leer datos del boleto |
| `Eq_LeerEquipeje` | Validar marbete |
| `Eq_ListaDeEquipajes` | Listar equipajes del servicio |

### 3. Sincronizar en Android Studio

1. Presionar botón "Sync Project with Gradle Files" (icono elefante)
2. Compilar con `./gradlew assembleDebug` o Run (Play verde)

---

## Si el webservice usa la API actual (acciones simples)

Si el servidor SECM usa la API existente (`api/index.php`), hay que cambiar los endpoints en la app:

**Archivos a modificar:**
- `data/remote/ApiService.kt` - Cambiar endpoints de SPs a acciones simples
- `data/model/ApiModels.kt` - Ajustar modelos de request/response
- Repositories en `data/repository/` - Adaptar llamadas

**Endpoints actuales de la API:**
```
POST ?action=login
GET ?action=servicios_cercanos&interno=1001
GET ?action=verificar_boleto&codigo=QR_XXX
GET ?action=verificar_ribete&codigo=QR_XXX
POST ?action=registrar_equipaje
GET ?action=verificar_equipaje&codigo_ribete=QR_XXX
GET ?action=equipajes_servicio&servicio_id=XXX
```

---

## Para probar ahora (opciones)

### Opción A: Usar API local existente
1. Mantener Laragon corriendo con la API en `api/index.php`
2. Cambiar `BASE_URL` en `NetworkClient.kt` a tu IP local
3. Ambos dispositivos en misma red WiFi

### Opción B: Crear endpoints de SPs en la API
1. Agregar los 4 Stored Procedures a `api/index.php`
2. Mantener la app Android como está

---

**Fecha:** 2026-02-18
