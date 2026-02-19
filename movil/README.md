# EquipajeApp - Versión Móvil

Carpeta con los APKs compilados de la aplicación para instalación directa en dispositivos Android.

## 📱 Versión Actual

**Archivo:** `EquipajeApp-v1.0.0-debug.apk`

**Versión:** 1.0.0 (Debug)

**Fecha:** 19/02/2026

**Tamaño:** ~28 MB

## 📥 Instalación

### Método 1: Descargar desde GitHub
1. Abrir este repositorio en el navegador del celular: `https://github.com/sergioecm60/EquipajeApp`
2. Navegar a la carpeta `movil/`
3. Descargar el archivo APK
4. Abrir el archivo descargado
5. Permitir instalación de fuentes desconocidas (si pide)
6. Instalar

### Método 2: Transferencia directa
1. Descargar el APK en la PC
2. Transferir al celular por:
   - Cable USB
   - Bluetooth
   - WhatsApp Web
   - Google Drive / Dropbox
3. Abrir el archivo en el celular
4. Instalar

## ⚙️ Requisitos

- **Android:** 7.0 o superior (API 24+)
- **Permisos:** Cámara, Internet
- **Espacio:** 30 MB libres

## 🔧 Configuración inicial

1. **Conexión:** Asegurar que el celular tenga acceso a Internet (4G/WiFi)
2. **URL del servidor:** Ya está configurada en la app
   - `http://servidordeltapy.dyndns.org/WSDelta_POS/wsdelta_pos.asmx`

## 🚀 Uso

### Primer inicio:
1. Abrir la app "EquipajeApp"
2. Ingresar número de interno (ej: 1001, 1002, 1003)
3. Login
4. Seleccionar servicio de la lista

### Registrar equipaje:
1. Escanear boleto del pasajero
2. Escanear marbete del equipaje
3. Confirmar registro

### Consultar equipaje (Gendarmería/Tránsito):
1. Escanear boleto del pasajero
2. Ver marbetes asociados
3. Buscar en bodega

## 📋 Historial de versiones

### v1.0.0 (19/02/2026)
- Login con número de interno
- Lista de servicios asignados
- Registro de equipaje (boleto + marbete)
- Consulta de equipaje para control policial
- Integración con WebService SOAP Delta

## 🐛 Solución de problemas

### "No se puede instalar"
- Verificar que Android sea 7.0 o superior
- Permitir "Fuentes desconocidas" en Configuración > Seguridad

### "No conecta al servidor"
- Verificar conexión a Internet
- El servidor debe estar online: `servidordeltapy.dyndns.org`

### "Error al leer boleto"
- El boleto debe pertenecer al servicio seleccionado
- Verificar que el número de boleto sea correcto

## 📞 Soporte

**Desarrollador:** [Nombre del desarrollador]
**Email:** [Email de soporte]
**WebService:** Delta Transporte

## 📝 Notas

- Esta es una versión **DEBUG** para pruebas
- En producción se usará versión firmada (RELEASE)
- Los mocks están deshabilitados, usa el servidor real

---

**Para desarrolladores:** Ver carpeta `docs/` para documentación técnica.