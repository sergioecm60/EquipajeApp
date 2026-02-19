package com.transporte.equipajeapp.domain.model

data class Usuario(
    val id: Int,
    val interno: String,
    val nombre: String,
    val empresa: String
)

data class Servicio(
    val id: Int,
    val interno: String,
    val origen: String,
    val destino: String,
    val horaSalida: String,
    val horaLlegada: String,
    val empresa: String,
    val fecha: String,
    val estado: String
)

data class Boleto(
    val id: Int,
    val numero: String,
    val pasajero: String,
    val dni: String,
    val origen: String,
    val destino: String,
    val fecha: String,
    val servicioId: Int
)

data class Equipaje(
    val id: Int,
    val codigoQr: String,
    val descripcion: String,
    val numeroBoleto: String,
    val ribeteQr: String? = null,
    val ribeteNumero: String? = null,
    val pasajeroNombre: String? = null,
    val pasajeroDni: String? = null,
    val fechaAlta: String,
    val estado: String
)

/**
 * Modelo para el listado de equipajes según Eq_ListaDeEquipajes
 */
data class EquipajeListado(
    val hdIdBoleto: Int,
    val cantidad: Int,
    val documento: String,
    val pasajero: String,
    val marbetes: List<String>
)

data class Ribete(
    val id: Int,
    val numero: String,
    val codigoQr: String,
    val estado: String
)

data class Result<out T>(
    val success: Boolean,
    val data: T?,
    val message: String?
) {
    companion object {
        fun <T> success(data: T): Result<T> = Result(true, data, null)
        fun <T> error(message: String): Result<T> = Result(false, null, message)
    }
}
