# WebService Delta - Configuración SOAP

## Información del Servidor

**URL Base:** `http://servidordeltapy.dyndns.org/WSDelta_POS/wsdelta_pos.asmx`

**Tipo:** SOAP 1.1

**Namespace:** `Delta`

## Métodos Disponibles

### 1. Eq_Login - Autenticación
**Uso:** Login del chofer con número de interno

**Request:**
```xml
<Eq_Login xmlns="Delta">
  <NroInterno>string</NroInterno>        <!-- CHAR(10) -->
  <PasswordUsuario>string</PasswordUsuario>  <!-- CHAR(7) -->
  <Usuario>string</Usuario>              <!-- CHAR(7) - Sistema: dUDl7aR -->
  <Password>string</Password>            <!-- CHAR(7) - Sistema: dPu8rSH -->
</Eq_Login>
```

**Response:**
- Error = 0: OK
- Error ≠ 0: Mensaje de error en Descr
- **Devuelve LISTA de servicios asignados al chofer** (puede ser 1 o varios)

---

### 2. Eq_LeerBoleto - Leer datos de boleto
**Uso:** Validar que un boleto pertenece al servicio y obtener datos del pasajero

**Request:**
```xml
<Eq_LeerBoleto xmlns="Delta">
  <Empresa>string</Empresa>          <!-- CHAR(3) - Ej: "EPA" -->
  <Boleto>string</Boleto>            <!-- BIGINT - Ej: 100010720003193 -->
  <IdServicio>int</IdServicio>       <!-- INTEGER -->
  <Usuario>string</Usuario>          <!-- CHAR(7) -->
  <Password>string</Password>        <!-- CHAR(7) -->
</Eq_LeerBoleto>
```

**Response:**
- **Error = 0**: Boleto válido, devuelve datos del pasajero
  - IdBoleto
  - Butaca (número de asiento)
  - Pasajero (nombre y apellido)
  - Documento (DNI)
  
- **Error ≠ 0**: Boleto NO pertenece al servicio o error
  - Mostrar mensaje: "El boleto no pertenece a este servicio" o el error devuelto

**Flujo:**
1. Chofer escanea boleto
2. App envía IdServicio (del login) + número de boleto
3. Si es válido: muestra datos del pasajero
4. Si es inválido: muestra error

---

### 3. Eq_LeerEquipaje - Asociar marbete con boleto
**Uso:** Registrar un marbete a un boleto cuando se sube equipaje a la bodega

**Request:**
```xml
<Eq_LeerEquipaje xmlns="Delta">
  <IdServicio>int</IdServicio>       <!-- INTEGER -->
  <IdBoleto>int</IdBoleto>           <!-- INTEGER - ID del boleto leído -->
  <Marbete>string</Marbete>          <!-- VARCHAR(15) - Ej: "MARBETE000KKK002" -->
  <Usuario>string</Usuario>          <!-- CHAR(7) -->
  <Password>string</Password>        <!-- CHAR(7) -->
</Eq_LeerEquipaje>
```

**Response:**
- **Error = 0**: Marbete asociado correctamente al boleto
- **Error ≠ 0**: Error al asociar (marbete inválido, ya usado, etc.)

**Flujo:**
1. Chofer lee boleto (Eq_LeerBoleto) → obtiene IdBoleto
2. Chofer escanea marbete del equipaje
3. App envía: IdServicio + IdBoleto + Marbete
4. Sistema asocia el marbete con ese pasajero/boleto
5. Equipaje queda registrado para ese pasajero

---

### 4. Eq_ListaDeEquipajes - Consultar equipaje de pasajero
**Uso:** Consultar qué marbetes tiene asociados un pasajero (para Gendarmería/Tránsito)

**Request:**
```xml
<Eq_ListaDeEquipajes xmlns="Delta">
  <IdServicio>int</IdServicio>       <!-- INTEGER -->
  <Usuario>string</Usuario>          <!-- CHAR(7) -->
  <Password>string</Password>        <!-- CHAR(7) -->
</Eq_ListaDeEquipajes>
```

**Response:**
- **Error = 0**: Lista de pasajeros con sus marbetes
  - HD_IdBoleto
  - Texto (formato: "15 -39503917- BARRIOS, ARTURO MOISES 0014367,0014368")
    - Cantidad de equipajes
    - Documento
    - Nombre y apellido
    - Lista de marbetes separados por coma/espacio

**Flujo:**
1. Micro es retenido por Gendarmería/Tránsito
2. Agente pide verificar equipaje de un pasajero
3. Chofer escanea boleto del pasajero (Eq_LeerBoleto) → obtiene IdBoleto
4. App consulta Eq_ListaDeEquipajes → obtiene lista de equipajes del servicio
5. App filtra y muestra los marbetes asociados a ese IdBoleto
6. Agente puede ir a la bodega y buscar el equipaje por el número de marbete

---

## Flujos de Uso

### FLUJO 1: Login y Selección de Servicio
1. Chofer abre app
2. Ingresa número de interno
3. App llama `Eq_Login`
4. Sistema devuelve lista de servicios asignados
5. Chofer selecciona en qué servicio está embarcando

### FLUJO 2: Registrar Equipaje (Subir a Bodega)
1. Pasajero llega con boleto y equipaje
2. Chofer escanea **boleto** (`Eq_LeerBoleto`)
   - Si error: "Boleto no pertenece a este servicio"
   - Si OK: Muestra datos del pasajero
3. Chofer escanea **marbete** del equipaje
4. App llama `Eq_LeerEquipaje` (IdBoleto + Marbete)
5. Sistema asocia marbete con boleto
6. Equipaje queda registrado

### FLUJO 3: Consultar Equipaje (Gendarmería/Tránsito)
1. Micro es retenido
2. Agente pide verificar equipaje de pasajero
3. Chofer escanea **boleto** del pasajero (`Eq_LeerBoleto`)
4. App obtiene IdBoleto
5. App llama `Eq_ListaDeEquipajes` (IdServicio)
6. App muestra: "Pasajero X tiene marbetes: 0014367, 0014368"
7. Agente busca esos marbetes en la bodega

---

## Credenciales Fijas del Sistema

Para **todos** los métodos SOAP:
- **Usuario:** `dUDl7aR` (7 caracteres)
- **Password:** `dPu8rSH` (7 caracteres - primeros 7 de `dPu8rSHsA*`)

---

## Datos del Chofer

En Eq_Login:
- **NroInterno:** Número de interno del chofer (ej: "1001")
- **PasswordUsuario:** Password del chofer (si no tiene, espacios en blanco)

---

## Notas Importantes

1. **Eq_Login devuelve LISTA**: Puede devolver 1 o varios servicios. El chofer debe seleccionar cuál está haciendo.

2. **Eq_LeerBoleto valida**: Si el boleto no está en el servicio, devuelve error.

3. **Eq_LeerEquipaje asocia**: Relaciona el marbete con el boleto (para subir equipaje).

4. **Eq_ListaDeEquipajes consulta**: Muestra TODOS los equipajes del servicio con sus pasajeros.

5. **Formato de respuestas**: Las respuestas son DataSets XML (formato complejo de .NET).

6. **Padding**: Los campos de texto deben tener padding con espacios hasta su longitud máxima.

---

## Archivos de la App

- `SoapClient.kt` - Cliente SOAP
- `AuthRepositoryImpl.kt` - Login (maneja lista de servicios)
- `ServicioRepositoryImpl.kt` - Leer boleto
- `EquipajeRepositoryImpl.kt` - Asociar marbete y listar equipajes
- `ServicioDetalleActivity.kt` - UI para registrar equipaje
- `DashboardActivity.kt` - Lista de servicios (del login)