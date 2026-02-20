# EquipajeApp - Contexto Rápido

## ¿Qué es?
App Android para control de equipaje en micros de larga distancia (Delta Transporte).

## Estado Actual
- **Versión:** 1.1.1
- **Estado:** Funcional - Parseo de servicios implementado
- **WebService:** SOAP en `http://servidordeltapy.dyndns.org/WSDelta_POS/wsdelta_pos.asmx`

## Arquitectura
- MVVM + Clean Architecture
- Hilt (DI)
- Kotlin + Coroutines
- DataStore (sesión)
- CameraX + ML Kit (QR)

## Estructura Clave
```
app/src/main/java/com/transporte/equipajeapp/
├── data/
│   ├── remote/SoapClient.kt      # Cliente SOAP principal
│   ├── repository/               # Implementaciones de repositorios
│   └── model/ApiModels.kt        # Modelos API
├── domain/
│   ├── model/Models.kt           # Entidades del dominio
│   └── usecase/UseCases.kt      # Casos de uso
└── ui/
    ├── login/                    # LoginActivity
    ├── dashboard/               # DashboardActivity + Adapter
    └── servicio/                # ServicioDetalleActivity
```

## Endpoints WebService
| Método | Función |
|--------|---------|
| Eq_Login | Login chofer (interno + password) |
| Eq_LeerBoleto | Leer datos del boleto |
| Eq_LeerEquipaje | Associar marbete a boleto |
| Eq_ListaDeEquipajes | Listar equipajes del servicio |

## Credenciales
- Usuario sistema: `dUDl7aR`
- Password sistema: `dPu8r$HsA*`
- Interno prueba: `144`

## Formato del campo Servicio (del WS)
El campo `Servicio` viene así: `"EPA FAR-CDE 19/02/26 22:00"`
- Empresa: EPA
- Ruta: FAR-CDE (origen-destino)
- Fecha: 19/02/2026
- Hora: 22:00

## Pendientes (ver docs/SOLICITUD_WEBSERVICE.md)
1. Confirmar flujo Eq_LeerBoleto (origen de IdServicio, Empresa)
2. Confirmar flujo Eq_LeerEquipaje (cómo obtener IdBoleto)
3. Confirmar flujo Eq_ListaDeEquipajes
4. Agregar nombre del chofer en Eq_Login

## Comandos Útiles
```bash
./gradlew assembleDebug    # Compilar APK
```

## Logs importantes
- Tag: `DashboardVM` - Estado del dashboard
- Tag: `SoapClient` - Requests/responses del WebService
- Tag: `ServicioRepoImpl` - Repositorio de servicios
