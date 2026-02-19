package com.transporte.equipajeapp.domain.repository

import com.transporte.equipajeapp.domain.model.Boleto
import com.transporte.equipajeapp.domain.model.Equipaje
import com.transporte.equipajeapp.domain.model.EquipajeListado
import com.transporte.equipajeapp.domain.model.Result
import com.transporte.equipajeapp.domain.model.Servicio
import com.transporte.equipajeapp.domain.model.Usuario

interface AuthRepository {
    suspend fun login(interno: String, password: String): Result<Usuario>
    suspend fun logout()
    suspend fun getCurrentUser(): Usuario?
    suspend fun isLoggedIn(): Boolean
}

interface ServicioRepository {
    suspend fun getServiciosPorInterno(interno: String, fecha: String): Result<List<Servicio>>
    suspend fun getServicioActual(interno: String): Result<Servicio?>
    suspend fun leerBoleto(empresa: String, boleto: Long): Result<Boleto>
}

interface EquipajeRepository {
    suspend fun asociarEquipaje(codigoQr: String, numeroBoleto: String): Result<Equipaje>
    suspend fun getEquipajePorBoleto(numeroBoleto: String): Result<List<Equipaje>>
    suspend fun escanearCodigo(qrContent: String): Result<Pair<BoletoInfo, EquipajeInfo>>
    suspend fun leerEquipaje(idBoleto: Int, marbete: String): Result<Boolean>
    suspend fun listaDeEquipajes(): Result<List<EquipajeListado>>
}

data class BoletoInfo(
    val numero: String,
    val pasajero: String,
    val dni: String,
    val origen: String,
    val destino: String
)

data class EquipajeInfo(
    val codigo: String,
    val descripcion: String,
    val estado: String
)
