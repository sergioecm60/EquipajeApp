# EquipajeApp - Control de Equipaje

Aplicación Android para control de equipaje en servicios de transporte utilizando WebService SOAP.

## Descripción

Aplicación móvil para choferes que permite:
- Login con número de interno
- Ver servicio asignado
- Escanear boletos de pasajeros
- Validar marbetes de equipaje
- Listar equipajes registrados

## WebService

**URL:** `http://servidordeltapy.dyndns.org/WSDelta_POS/wsdelta_pos.asmx`

**Métodos disponibles:**
- `Eq_Login` - Autenticación
- `Eq_LeerBoleto` - Leer datos de boleto
- `Eq_LeerEquipaje` - Validar marbete
- `Eq_ListaDeEquipajes` - Listar equipajes

## Requisitos

- Android 7.0+ (API 24)
- Conexión a Internet
- Cámara para escaneo de QR

## Configuración

La app ya está configurada para conectar con el WebService Delta. No requiere configuración adicional.

## Estructura del Proyecto

```
EquipajeApp/
├── app/                        # Código fuente Android
│   ├── src/main/java/...       # Código Kotlin
│   ├── src/main/res/           # Layouts y recursos
│   └── build.gradle.kts        # Dependencias del módulo
├── docs/                       # Documentación
│   ├── WEBSERVICE_DELTA_CONFIG.md  # Configuración SOAP
│   ├── PROGRAMADOR.md          # Guía para desarrolladores
│   ├── PROYECTO.md             # Especificación del proyecto
│   └── WEBSERVICE_CONFIG.md    # Configuración legacy (referencia)
├── build.gradle.kts            # Configuración Gradle raíz
└── README.md                   # Este archivo
```

## Documentación

Ver carpeta `docs/` para:
- `WEBSERVICE_DELTA_CONFIG.md` - Configuración del WebService
- `PROGRAMADOR.md` - Guía para desarrolladores
- `PROYECTO.md` - Especificación del proyecto

## Compilación

```bash
Build → Clean Project
Build → Assemble Project
```

## Credenciales de Prueba

- **Interno:** 1001, 1002, 1003
- **Contraseña:** (cualquiera o vacía)

## Autor

Desarrollado para Delta Transporte.

## Licencia

Privada - Uso exclusivo para Delta Transporte.