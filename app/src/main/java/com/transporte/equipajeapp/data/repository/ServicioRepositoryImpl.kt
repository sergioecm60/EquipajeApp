package com.transporte.equipajeapp.data.repository

import android.util.Log
import com.transporte.equipajeapp.data.local.PreferencesManager
import com.transporte.equipajeapp.data.model.EqLeerBoletoRequest
import com.transporte.equipajeapp.data.remote.SoapClient
import com.transporte.equipajeapp.domain.model.Boleto
import com.transporte.equipajeapp.domain.model.Result
import com.transporte.equipajeapp.domain.model.Servicio
import com.transporte.equipajeapp.domain.repository.ServicioRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServicioRepositoryImpl @Inject constructor(
    private val soapClient: SoapClient,
    private val preferencesManager: PreferencesManager
) : ServicioRepository {

    companion object {
        private const val TAG = "ServicioRepoImpl"
    }

    override suspend fun getServiciosPorInterno(interno: String, fecha: String): Result<List<Servicio>> {
        return try {
            Log.d(TAG, "Obteniendo servicios guardados para interno: $interno")
            
            val serviciosJson = preferencesManager.getServicios()
            
            if (serviciosJson.isNullOrEmpty()) {
                Log.d(TAG, "No hay servicios guardados")
                return Result.success(emptyList())
            }
            
            // Parsear servicios desde el formato: "id;servicio;origen;destino|id;servicio;origen;destino"
            val servicios = serviciosJson.split("|").mapNotNull { item ->
                val parts = item.split(";")
                if (parts.size >= 2) {
                    val id = parts[0].toIntOrNull()
                    val nombre = parts[1]
                    if (id != null) {
                        Servicio(
                            id = id,
                            interno = interno,
                            origen = parts.getOrNull(2)?.takeIf { it.isNotEmpty() } ?: "Consultar",
                            destino = parts.getOrNull(3)?.takeIf { it.isNotEmpty() } ?: "Consultar",
                            horaSalida = "",
                            horaLlegada = "",
                            empresa = "Delta",
                            fecha = fecha,
                            estado = "activo"
                        )
                    } else null
                } else null
            }
            
            Log.d(TAG, "Servicios parseados: ${servicios.size}")
            Result.success(servicios)
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo servicios", e)
            Result.error(e.message ?: "Error inesperado")
        }
    }

    override suspend fun getServicioActual(interno: String): Result<Servicio?> {
        val result = getServiciosPorInterno(interno, "")
        return if (result.success) {
            Result.success(result.data?.firstOrNull())
        } else {
            Result.success(null)
        }
    }
    
    override suspend fun leerBoleto(empresa: String, boleto: Long): Result<Boleto> {
        return try {
            val creds = preferencesManager.getCredentials()
                ?: return Result.error("No hay sesión activa")
            
            val request = EqLeerBoletoRequest(
                empresa = empresa.padEnd(3, ' ').take(3),
                boleto = boleto,
                idServicio = creds.idServicio,
                usuario = creds.usuario.padEnd(7, ' ').take(7),
                password = creds.password.padEnd(7, ' ').take(7)
            )
            
            val result = soapClient.leerBoleto(request)
            
            if (result.isSuccess) {
                // TODO: Parsear XML de respuesta
                // Por ahora retornamos un boleto de ejemplo para testing
                val boletoInfo = Boleto(
                    id = 1,
                    numero = boleto.toString(),
                    pasajero = "Pasajero Test",
                    dni = "12345678",
                    origen = "",
                    destino = "",
                    fecha = "",
                    servicioId = creds.idServicio
                )
                Result.success(boletoInfo)
            } else {
                Result.error(result.exceptionOrNull()?.message ?: "Error de conexión")
            }
        } catch (e: Exception) {
            Result.error(e.message ?: "Error inesperado")
        }
    }
}