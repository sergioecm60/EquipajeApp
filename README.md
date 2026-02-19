# EquipajeApp - Control de Equipaje

Aplicación Android para control de equipaje en servicios de transporte utilizando WebService SOAP.

<p align="center">
  <img src="docs/logo-secm.png" alt="SECM Soluciones TI" width="200"/>
</p>

## 📱 Descripción

Aplicación móvil para choferes que permite:
- 🔐 Login con número de interno
- 🚌 Ver servicios asignados
- 📄 Escanear boletos de pasajeros
- 🏷️ Validar marbetes de equipaje
- 🔍 Consultar equipajes registrados

## 🌐 WebService

**URL:** `http://servidordeltapy.dyndns.org/WSDelta_POS/wsdelta_pos.asmx`

**Métodos disponibles:**
- `Eq_Login` - Autenticación del chofer
- `Eq_LeerBoleto` - Leer datos de boleto
- `Eq_LeerEquipaje` - Asociar marbete con boleto
- `Eq_ListaDeEquipajes` - Listar equipajes del servicio

## 📋 Requisitos

- **Android:** 7.0+ (API 24)
- **Conexión:** Internet (4G/WiFi)
- **Hardware:** Cámara para escaneo de QR
- **Espacio:** 30 MB libres

## 📁 Estructura del Proyecto

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
│   └── WEBSERVICE_CONFIG.md    # Configuración legacy
├── movil/                      # APKs compiladas
│   ├── EquipajeApp-v1.0.0-debug.apk
│   └── README.md               # Guía de instalación
├── build.gradle.kts            # Configuración Gradle
└── README.md                   # Este archivo
```

## 📖 Documentación

Ver carpeta `docs/` para documentación técnica detallada.

## 🚀 Instalación Rápida

1. Descargar APK desde carpeta `movil/`
2. Transferir al dispositivo Android
3. Permitir instalación de fuentes desconocidas
4. Instalar y ejecutar

## 🛠️ Compilación

```bash
# Debug (para pruebas)
./gradlew assembleDebug

# Release (para producción)
./gradlew assembleRelease
```

## 🧪 Credenciales de Prueba

- **Interno:** 1001, 1002, 1003
- **Contraseña:** (cualquiera o vacía)

## 👨‍💻 Desarrollo

### Para desarrolladores que continúen el proyecto:

1. Clonar repositorio:
```bash
git clone https://github.com/sergioecm60/EquipajeApp.git
```

2. Abrir en Android Studio

3. Sincronizar Gradle

4. Ejecutar en dispositivo/emulador

---

## 📞 Soporte y Contacto

**SECM Gestión de Equipaje**  
**By:** Sergio Cabrera  
**Copyleft:** © 2026

¿Necesitas ayuda?

📧 **Email:** sergiomiers@gmail.com  
💬 **WhatsApp:** +54 11 6759-8452

---

## 📄 Licencia

**Privada** - Uso exclusivo para Delta Transporte.

---

<p align="center">
  <strong>SECM Soluciones TI</strong><br/>
  <em>Tecnología al servicio del transporte</em>
</p>