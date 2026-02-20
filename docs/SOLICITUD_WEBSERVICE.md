# Solicitud de Cambios al Programador del WebService

## Estado Actual

La app está funcionando correctamente con los siguientes endpoints:
- ✅ Eq_Login - Login de chofer

---

## Dudas y Solicitudes

### 1. Eq_LeerBoleto (Leer datos del pasajero/boleto)

**Endpoint actual:**
- Empresa: CHAR(3)
- Boleto: BIGINT
- IdServicio: INTEGER
- Usuario: CHAR(7)
- Password: CHAR(7)

**Problema:** 
- ¿De dónde obtenemos el `IdServicio` y `Empresa` si el chofer ya está logueado?
- ¿Debería venir del resultado del login o se debe seleccionar primero el servicio?

**Solicitud:** Indicar el flujo correcto para usar este endpoint.

---

### 2. Eq_LeerEquipaje (Asociar marbete al boleto)

**Endpoint actual:**
- IdBoleto: INTEGER
- Marbete: VARCHAR(15)
- Usuario: CHAR(7)
- Password: CHAR(7)

**Problema:**
- Falta `IdServicio` en los parámetros
- ¿Cómo obtenemos el `IdBoleto`? ¿Viene del resultado de Eq_LeerBoleto?

**Solicitud:** Confirmar el flujo:
1. ¿El chofer primero escanea el QR del boleto?
2. ¿Eq_LeerBoleto devuelve el IdBoleto?
3. ¿Después escanea el marbete y usa ese IdBoleto?

---

### 3. Eq_ListaDeEquipajes (Listar equipajes del servicio)

**Endpoint actual:**
- IdServicio: INTEGER
- Usuario: CHAR(7)
- Password: CHAR(7)

**Pregunta sobre el flujo:**
- ¿Cómo sería el flujo de uso?
  - Opción A: Click en botón "Listar equipajes" → muestra todos los equipajes del servicio
  - Opción B: Escanear boleto de un pasajero → mostrar los marbetes asociados a ese boleto

**Solicitud:** Confirmar qué flujo se debe implementar o si ambos son necesarios.

---

### 4. Eq_Login - Campo adicional

**Solicitud:** Agregar campo con el nombre del chofer en la respuesta.

**Respuesta actual:**
```
IdServicio
Servicio (ej: "EPA FAR-CDE 19/02/26 22:00")
Error
Descr
```

**Solicitud:** Agregar campo adicional, por ejemplo:
- `NombreChofer` o `Chofer` - Nombre del chofer logueado

---

### 5. Eq_ListaDeEquipajes - Formato de respuesta

**Pregunta:** ¿Qué campos devuelve esta tabla? según el PDF original:
```
HD_IdBoleto: INTEGER
Texto: VARCHAR(130) - Formato: "15 -39503917- BARRIOS, ARTURO MOISES 0014367,0014368 0014369"
```

**Confirmación:** ¿Es correcto este formato? ¿O hay campos separados?

---

## Resumen de lo que necesitamos saber

| Endpoint | Parámetros que faltan | Duda |
|----------|----------------------|------|
| Eq_LeerBoleto | IdServicio, Empresa | ¿De dónde vienen estos datos? |
| Eq_LeerEquipaje | IdServicio | ¿Cómo obtenemos IdBoleto? |
| Eq_ListaDeEquipajes | - | ¿Qué flujo usar? (todos o por boleto) |
| Eq_Login | - | ¿Se puede agregar nombre del chofer? |

---

## Credenciales de prueba actuales

- Usuario sistema: `dUDl7aR`
- Password sistema: `dPu8r$HsA*` (nota: el PDF dice `dPu8rSHsA*` pero el código usa `dPu8r$HsA*`)
- Interno de prueba: `144`

---

**Fecha:** 19/02/2026
