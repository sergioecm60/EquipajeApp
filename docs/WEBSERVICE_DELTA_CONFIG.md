# WebService Delta - Configuración SOAP

## Información del Servidor

**URL Base:** `http://servidordeltapy.dyndns.org/WSDelta_POS/wsdelta_pos.asmx`

**Tipo:** SOAP 1.1 / SOAP 1.2

**Namespace:** `Delta`

## Métodos Disponibles

### 1. Eq_Login - Autenticación
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

**Parámetros:**
- NroInterno: Número de interno del chofer (10 chars, con padding)
- PasswordUsuario: Contraseña del chofer (7 chars, con padding)
- Usuario: Usuario fijo del sistema (7 chars) = `dUDl7aR`
- Password: Password fijo del sistema (7 chars) = `dPu8rSHsA*` (usar primeros 7)

### 2. Eq_LeerBoleto - Leer boleto
```xml
<Eq_LeerBoleto xmlns="Delta">
  <Empresa>string</Empresa>
  <Boleto>string</Boleto>
  <IdServicio>int</IdServicio>
  <Usuario>string</Usuario>
  <Password>string</Password>
</Eq_LeerBoleto>
```

### 3. Eq_LeerEquipaje - Validar marbete
```xml
<Eq_LeerEquipaje xmlns="Delta">
  <IdBoleto>int</IdBoleto>
  <Marbete>string</Marbete>
  <Usuario>string</Usuario>
  <Password>string</Password>
</Eq_LeerEquipaje>
```

### 4. Eq_ListaDeEquipajes - Listar equipajes
```xml
<Eq_ListaDeEquipajes xmlns="Delta">
  <IdServicio>int</IdServicio>
  <Usuario>string</Usuario>
  <Password>string</Password>
</Eq_ListaDeEquipajes>
```

## Configuración en la App

### 1. Dependencias (ya agregadas en build.gradle.kts)
```kotlin
implementation("com.google.code.ksoap2-android:ksoap2-android:3.6.4")
```

### 2. Cliente SOAP
Archivo: `app/src/main/java/com/transporte/equipajeapp/data/remote/SoapClient.kt`

Ya está configurado con:
- URL: `http://servidordeltapy.dyndns.org/WSDelta_POS/wsdelta_pos.asmx`
- Namespace: `Delta`
- Timeout: 30 segundos
- Version SOAP: 1.1

### 3. Cambiar a WebService Real

En `app/src/main/java/com/transporte/equipajeapp/di/AppModule.kt`:

**Cambiar esto:**
```kotlin
@Provides
@Singleton
fun provideAuthRepository(): AuthRepository = AuthRepositoryMock()

@Provides
@Singleton
fun provideServicioRepository(): ServicioRepository = ServicioRepositoryMock()

@Provides
@Singleton
fun provideEquipajeRepository(): EquipajeRepository = EquipajeRepositoryMock()
```

**Por esto:**
```kotlin
@Provides
@Singleton
fun provideSoapClient(): SoapClient = SoapClient()

@Provides
@Singleton
fun provideAuthRepository(soapClient: SoapClient, prefs: PreferencesManager): AuthRepository = 
    AuthRepositoryImpl(soapClient, prefs)

@Provides
@Singleton
fun provideServicioRepository(soapClient: SoapClient, prefs: PreferencesManager): ServicioRepository = 
    ServicioRepositoryImpl(soapClient, prefs)

@Provides
@Singleton
fun provideEquipajeRepository(soapClient: SoapClient, prefs: PreferencesManager): EquipajeRepository = 
    EquipajeRepositoryImpl(soapClient, prefs)
```

**IMPORTANTE:** También hay que actualizar los repositorios (AuthRepositoryImpl, etc.) para que reciban SoapClient.

## Para Volver a Modo Mock (Pruebas Offline)

Si necesitás probar sin conexión al servidor:

### Opción 1: Usar Mocks (sin servidor)
En `AppModule.kt`, volver a:
```kotlin
@Provides
@Singleton
fun provideAuthRepository(): AuthRepository = AuthRepositoryMock()

@Provides
@Singleton
fun provideServicioRepository(): ServicioRepository = ServicioRepositoryMock()

@Provides
@Singleton
fun provideEquipajeRepository(): EquipajeRepository = EquipajeRepositoryMock()
```

Datos de prueba disponibles:
- Internos: 1001, 1002, 1003
- Servicios: Buenos Aires ↔ Rosario, Buenos Aires ↔ La Plata
- Boletos: QR_BOLETO_001 a QR_BOLETO_005
- Ribetes: QR_RIBETE_001 a QR_RIBETE_005

### Opción 2: Servidor Local PHP
Si querés usar el servidor PHP local (requiere Laragon/XAMPP):

1. Asegurar que esté corriendo Apache
2. Verificar que la BD MySQL tenga datos
3. Cambiar la URL en SoapClient o usar Retrofit con la API local

## Credenciales Fijas del Sistema

Para todos los métodos SOAP, usar:
- **Usuario:** `dUDl7aR` (7 caracteres)
- **Password:** `dPu8rSH` (primeros 7 de `dPu8rSHsA*`)

## Archivos Importantes

- `SoapClient.kt` - Cliente SOAP
- `AppModule.kt` - Configuración de inyección de dependencias
- `AuthRepositoryImpl.kt` - Login
- `ServicioRepositoryImpl.kt` - Servicios y boletos
- `EquipajeRepositoryImpl.kt` - Equipajes y marbetes

## Notas

- Las respuestas son DataSets XML (formato complejo)
- Los campos de texto deben tener padding con espacios
- Timeout configurado a 30 segundos
- Usar `dotNet = true` en el envelope (servidor es .NET)