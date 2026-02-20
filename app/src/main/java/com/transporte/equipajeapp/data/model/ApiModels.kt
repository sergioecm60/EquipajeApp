package com.transporte.equipajeapp.data.model

import com.google.gson.annotations.SerializedName

// ==================== REQUESTS/RESPONSES LEGACY ====================

data class LoginRequest(
    @SerializedName("interno") val interno: String,
    @SerializedName("password") val password: String
)

data class LoginResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: UsuarioDto?
)

data class UsuarioDto(
    @SerializedName("id") val id: Int,
    @SerializedName("interno") val interno: String,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("empresa") val empresa: String
)

data class ServiciosResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: List<ServicioDto>?
)

data class ServicioDto(
    @SerializedName("id") val id: Int,
    @SerializedName("interno") val interno: String,
    @SerializedName("origen") val origen: String,
    @SerializedName("destino") val destino: String,
    @SerializedName("hora_salida") val horaSalida: String,
    @SerializedName("hora_llegada") val horaLlegada: String,
    @SerializedName("empresa") val empresa: String,
    @SerializedName("fecha") val fecha: String,
    @SerializedName("estado") val estado: String
)

data class AssociateEquipajeRequest(
    @SerializedName("codigo_boleto") val codigoBoleto: String,
    @SerializedName("codigo_ribete") val codigoRibete: String,
    @SerializedName("servicio_id") val servicioId: Int
)

data class EquipajeResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: EquipajeDto?
)

data class EquipajeDto(
    @SerializedName("id") val id: Int,
    @SerializedName("codigo_qr") val codigoQr: String,
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("numero_boleto") val numeroBoleto: String,
    @SerializedName("pasajero_nombre") val pasajeroNombre: String?,
    @SerializedName("pasajero_dni") val pasajeroDni: String?,
    @SerializedName("ribete_qr") val ribeteQr: String?,
    @SerializedName("ribete_numero") val ribeteNumero: String?,
    @SerializedName("fecha_alta") val fechaAlta: String,
    @SerializedName("estado") val estado: String
)

data class QrScanResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("boleto") val boleto: BoletoDto?,
    @SerializedName("equipaje") val equipaje: EquipajeInfoDto?,
    @SerializedName("message") val message: String?
)

data class BoletoDto(
    @SerializedName("numero") val numero: String,
    @SerializedName("pasajero") val pasajero: String,
    @SerializedName("dni") val dni: String,
    @SerializedName("origen") val origen: String,
    @SerializedName("destino") val destino: String
)

data class EquipajeInfoDto(
    @SerializedName("codigo") val codigo: String,
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("estado") val estado: String
)

// ==================== STORED PROCEDURES SEGÚN PDF ====================

/**
 * 1) Eq_Login - Autenticación
 */
data class EqLoginRequest(
    @SerializedName("NrOInterno") val nroInterno: String,           // CHAR(10)
    @SerializedName("PasswordUsuario") val passwordUsuario: String, // CHAR(7)
    @SerializedName("Usuario") val usuario: String,                 // CHAR(7)
    @SerializedName("Password") val password: String                // CHAR(7)
)

/**
 * Servicio devuelto por Eq_Login
 */
data class ServicioLoginItem(
    val idServicio: Int,
    val servicio: String,  // Ej: "EPA ASU-ENC 31/01/2026 17:30"
    val empresa: String?,
    val origen: String?,
    val destino: String?,
    val horaSalida: String?,
    val horaLlegada: String?,
    val fecha: String?
)

data class EqLoginResponse(
    @SerializedName("Error") val error: Int,                        // INTEGER (0 = OK)
    @SerializedName("Descr") val descr: String?,                    // STRING (OK / Mensaje de Error)
    @SerializedName("Servicios") val servicios: List<ServicioLoginItem>? = null  // Lista de servicios del chofer
)

/**
 * 2) Eq_LeerBoleto - Leer datos de boleto
 */
data class EqLeerBoletoRequest(
    @SerializedName("Empresa") val empresa: String,                 // CHAR(3) - Ej: "EPA"
    @SerializedName("Boleto") val boleto: Long,                     // BIGINT - Ej: 100010720003193
    @SerializedName("IdServicio") val idServicio: Int,              // INTEGER
    @SerializedName("Usuario") val usuario: String,                 // CHAR(7)
    @SerializedName("Password") val password: String                // CHAR(7)
)

data class EqLeerBoletoResponse(
    @SerializedName("Error") val error: Int,                        // INTEGER (0 = OK)
    @SerializedName("Descr") val descr: String?,                    // STRING
    @SerializedName("IdBoleto") val idBoleto: Int?,                 // INTEGER
    @SerializedName("Butaca") val butaca: Int?,                     // INTEGER
    @SerializedName("Pasajero") val pasajero: String?,              // VARCHAR(30)
    @SerializedName("Documento") val documento: String?             // CHAR(20)
)

/**
 * 3) Eq_LeerEquipeje - Leer equipaje por marbete
 */
data class EqLeerEquipajeRequest(
    @SerializedName("IdServicio") val idServicio: Int,              // INTEGER
    @SerializedName("IdBoleto") val idBoleto: Int,                  // INTEGER
    @SerializedName("Marbete") val marbete: String,                 // VARCHAR(15) - Ej: "MARBETE000KKK002"
    @SerializedName("Usuario") val usuario: String,                 // CHAR(7)
    @SerializedName("Password") val password: String                // CHAR(7)
)

data class EqLeerEquipajeResponse(
    @SerializedName("Error") val error: Int,                        // INTEGER (0 = OK)
    @SerializedName("Descr") val descr: String?                     // STRING (OK / Mensaje de Error)
)

/**
 * 4) Eq_ListaDeEquipajes - Listar equipajes de un servicio
 * Nota: El campo Texto parece tener un formato especial con múltiples datos concatenados
 */
data class EqListaEquipajesRequest(
    @SerializedName("IdServicio") val idServicio: Int,              // INTEGER
    @SerializedName("Usuario") val usuario: String,                 // CHAR(7)
    @SerializedName("Password") val password: String                // CHAR(7)
)

data class EqListaEquipajesItem(
    @SerializedName("HD_IdBoleto") val hdIdBoleto: Int,             // INTEGER
    @SerializedName("Texto") val texto: String?                     // VARCHAR(130) - Formato: "15 -39503917- BARRIOS, ARTURO MOISES 0014367,0014368 0014369"
)

data class EqListaEquipajesResponse(
    @SerializedName("Error") val error: Int,                        // INTEGER (0 = OK)
    @SerializedName("Descr") val descr: String?,                    // STRING
    @SerializedName("Equipajes") val equipajes: List<EqListaEquipajesItem>?
)

// Helper para parsear el campo Texto de Eq_ListaDeEquipajes
data class EquipajeListadoInfo(
    val cantidad: Int,
    val documento: String,
    val pasajero: String,
    val marbetes: List<String>
) {
    companion object {
        fun parse(texto: String): EquipajeListadoInfo? {
            // Formato esperado: "15 -39503917- BARRIOS, ARTURO MOISES 0014367,0014368 0014369"
            val parts = texto.trim().split(" ")
            if (parts.size < 3) return null

            return try {
                val cantidad = parts[0].toInt()
                val documento = parts[1].replace("-", "")
                // El resto es pasajero + marbetes
                val resto = texto.substringAfter(parts[1]).trim()
                val pasajeroEndIndex = resto.indexOfAny("0123456789".toCharArray())
                val pasajero = if (pasajeroEndIndex > 0) resto.substring(0, pasajeroEndIndex).trim() else resto
                val marbetesStr = if (pasajeroEndIndex > 0) resto.substring(pasajeroEndIndex).trim() else ""
                val marbetes = marbetesStr.split(" ", ",").filter { it.isNotBlank() }

                EquipajeListadoInfo(
                    cantidad = cantidad,
                    documento = documento,
                    pasajero = pasajero,
                    marbetes = marbetes
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
