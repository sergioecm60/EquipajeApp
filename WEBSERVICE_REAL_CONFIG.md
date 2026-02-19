# Configuración del WebService Real

## URL del WebService
```
http://servidordeltapy.dyndns.org/WSDelta_POS/wsdelta_pos.asmx
```

## Métodos Disponibles (según especificación)

Los métodos comienzan con `eq_`:

1. **eq_Login** - Autenticación del chofer
2. **eq_LeerBoleto** - Leer datos de un boleto
3. **eq_LeerEquipeje** - Leer/validar equipaje por marbete
4. **eq_ListaDeEquipajes** - Listar equipajes de un servicio

## Configuración para WebService Real

### 1. Actualizar URL en NetworkClient.kt

Archivo: `app/src/main/java/com/transporte/equipajeapp/data/remote/NetworkClient.kt`

```kotlin
private const val BASE_URL = "http://servidordeltapy.dyndns.org/WSDelta_POS/"
```

### 2. Cambiar Repositorios a Implementaciones Reales

Archivo: `app/src/main/java/com/transporte/equipajeapp/di/AppModule.kt`

Cambiar de:
```kotlin
@Provides
@Singleton
fun provideAuthRepository(): AuthRepository = AuthRepositoryMock()

@Provides
@Singleton
fun provideServicioRepository(): ServicioRepository = ServicioRepositoryMock()

@Provides
@Singleton
fun provideEquipajeRepository(): EquipajeRepository = EquipajeRepositoryImpl()
```

A:
```kotlin
@Provides
@Singleton
fun provideAuthRepository(impl: AuthRepositoryImpl): AuthRepository = impl

@Provides
@Singleton
fun provideServicioRepository(impl: ServicioRepositoryImpl): ServicioRepository = impl

@Provides
@Singleton
fun provideEquipajeRepository(impl: EquipajeRepositoryImpl): EquipajeRepository = impl
```

O usar @Binds (recomendado):
```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindServicioRepository(impl: ServicioRepositoryImpl): ServicioRepository

    @Binds
    @Singleton
    abstract fun bindEquipajeRepository(impl: EquipajeRepositoryImpl): EquipajeRepository
}
```

### 3. Actualizar AndroidManifest.xml

Agregar permiso para red (ya debería estar):
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

## Para Volver a Modo Mock (Pruebas Offline)

Si necesitás probar sin conexión al servidor:

### 1. Cambiar URL
```kotlin
private const val BASE_URL = "http://10.0.2.2/EquipajeApp/api/"  // Localhost para emulador
// o
private const val BASE_URL = "http://192.168.0.167:8081/android/EquipajeApp/api/"  // Tu IP local
```

### 2. Cambiar Repositorios a Mocks

En `AppModule.kt`:
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

### 3. Asegurar que el servidor local esté corriendo (si usás mocks con servidor local)
- Laragon: Apache + MySQL activos
- Base de datos `equipaje_test` creada
- Script SQL ejecutado: `database/test_db.sql`

## Estructura de Datos Esperada

### Login Request
```json
{
  "nroInterno": "1001      ",
  "passwordUsuario": "       ",
  "usuario": "dUDl7aR  ",
  "password": "dPu8rSHsA*"
}
```

### Login Response
```json
{
  "error": 0,
  "descr": "OK",
  "idServicio": 123,
  "servicio": "Buenos Aires - Rosario"
}
```

## Notas Importantes

1. **Codificación**: Los campos de texto deben tener padding con espacios hasta su longitud máxima
2. **Usuario/Password fijos**: Según el PDF, usar:
   - usuario: `dUDl7aR` (7 caracteres)
   - password: `dPu8rSHsA*` (10 caracteres, pero solo se usan 7)
3. **Timeouts**: Considerar agregar timeouts más largos para conexiones lentas

## Archivos Relacionados

- `app/src/main/java/com/transporte/equipajeapp/data/remote/NetworkClient.kt`
- `app/src/main/java/com/transporte/equipajeapp/di/AppModule.kt`
- `app/src/main/java/com/transporte/equipajeapp/data/repository/AuthRepositoryImpl.kt`
- `app/src/main/java/com/transporte/equipajeapp/data/repository/ServicioRepositoryImpl.kt`
- `app/src/main/java/com/transporte/equipajeapp/data/repository/EquipajeRepositoryImpl.kt`
- `app/src/main/java/com/transporte/equipajeapp/data/model/ApiModels.kt`