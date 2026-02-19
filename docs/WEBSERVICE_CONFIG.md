# Documentación de Webservice - EquipajeApp

## Resumen

Esta aplicación Android se integra con 4 Stored Procedures (SP) según la especificación del PDF recibido:

1. **Eq_Login** - Autenticación
2. **Eq_LeerBoleto** - Leer datos de boleto  
3. **Eq_LeerEquipeje** - Validar marbete
4. **Eq_ListaDeEquipajes** - Listar equipajes del servicio

---

## URL del Webservice

**Archivo a configurar:**
```
app/src/main/java/com/transporte/equipajeapp/data/remote/NetworkClient.kt
```

**Línea 14:**
```kotlin
private const val BASE_URL = "http://url-del-webservice-externo/"
```

---

## Stored Procedures Implementados

### 1) Eq_Login - Autenticación

**Endpoint:** `POST Eq_Login`

**Request:**
```json
{
    "NrOInterno": "144       ",
    "PasswordUsuario": "pepito  ",
    "Usuario": "dUDl7aR  ",
    "Password": "dPu8rSHsA*"
}
```

**Response:**
```json
{
    "Error": 0,
    "Descr": "OK",
    "IdServicio": 61544,
    "Servicio": "EPA ASU-ENC 31/01/2026 17:30"
}
```

**Notas:**
- `Error = 0` = Éxito
- `Error <> 0` = Error, mostrar `Descr`
- `Usuario` y `Password` son fijos según especificación

---

### 2) Eq_LeerBoleto - Leer datos de boleto

**Endpoint:** `POST Eq_LeerBoleto`

**Request:**
```json
{
    "Empresa": "EPA",
    "Boleto": 100010720003193,
    "IdServicio": 61544,
    "Usuario": "dUDl7aR  ",
    "Password": "dPu8rSHsA*"
}
```

**Response:**
```json
{
    "Error": 0,
    "Descr": "OK",
    "IdBoleto": 304127,
    "Butaca": 14,
    "Pasajero": "BARRIOS, ARTURO MOISES",
    "Documento": "C - 3595119"
}
```

**Uso:** Validar boleto antes de registrar equipaje

---

### 3) Eq_LeerEquipeje - Validar marbete

**Endpoint:** `POST Eq_LeerEquipeje`

**Request:**
```json
{
    "IdServicio": 61544,
    "IdBoleto": 304127,
    "Marbete": "MARBETE000KKK002",
    "Usuario": "dUDl7aR  ",
    "Password": "dPu8rSHsA*"
}
```

**Response:**
```json
{
    "Error": 0,
    "Descr": "OK"
}
```

**Uso:** Verificar que el marbete es válido para el boleto/servicio

---

### 4) Eq_ListaDeEquipajes - Listar equipajes

**Endpoint:** `POST Eq_ListaDeEquipajes`

**Request:**
```json
{
    "IdServicio": 61544,
    "Usuario": "dUDl7aR  ",
    "Password": "dPu8rSHsA*"
}
```

**Response:**
```json
{
    "Error": 0,
    "Descr": "OK",
    "Equipajes": [
        {
            "HD_IdBoleto": 304191,
            "Texto": "15 -39503917- BARRIOS, ARTURO MOISES 0014367,0014368 0014369"
        }
    ]
}
```

**Formato del campo Texto:**
```
cantidad -documento- pasajero marbete1,marbete2 marbete3

Ejemplo:
15 -39503917- BARRIOS, ARTURO MOISES 0014367,0014368 0014369
```

---

## Flujo de Datos

```
┌─────────────────────────────────────────────────────────────┐
│  LOGIN                                                      │
│  Eq_Login → Obtiene IdServicio y guarda credenciales       │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────┐
│  LEER BOLETO                                               │
│  Eq_LeerBoleto → Valida boleto, obtiene datos pasajero     │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────┐
│  VALIDAR MARBETE                                           │
│  Eq_LeerEquipeje → Verifica marbete es válido              │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────┐
│  LISTAR EQUIPAJES (opcional)                               │
│  Eq_ListaDeEquipajes → Muestra equipajes registrados       │
└─────────────────────────────────────────────────────────────┘
```

---

## Credenciales Fijas

| Campo | Valor | Tipo |
|-------|-------|------|
| Usuario | `dUDl7aR` | CHAR(7) |
| Password | `dPu8rSHsA*` | CHAR(7) |

Estas credenciales se guardan automáticamente tras login exitoso y se usan en todos los SPs.

---

## Manejo de Errores

Todos los endpoints devuelven:

```json
{
    "Error": 0,
    "Descr": "OK / Mensaje de Error"
}
```

- **Error = 0**: Operación exitosa
- **Error <> 0**: Error, mostrar mensaje en `Descr`

---

## Archivos Clave para Integración

| Archivo | Propósito |
|---------|-----------|
| `data/remote/NetworkClient.kt` | Configurar BASE_URL |
| `data/remote/ApiService.kt` | Definición de los 4 endpoints |
| `data/model/ApiModels.kt` | Modelos request/response |
| `data/repository/AuthRepositoryImpl.kt` | Implementa Eq_Login |
| `data/repository/ServicioRepositoryImpl.kt` | Implementa Eq_LeerBoleto |
| `data/repository/EquipajeRepositoryImpl.kt` | Implementa Eq_LeerEquipeje y Eq_ListaDeEquipajes |

---

## Próximos Pasos

1. ✅ App Android lista con los 4 SPs implementados
2. ⏳ Configurar URL real del webservice en `NetworkClient.kt`
3. ⏳ Verificar que los SPs estén implementados en el servidor
4. ⏳ Testing del flujo completo

---

**Última actualización:** 2026-02-13  
**Versión:** 1.0 (integración con Stored Procedures)
