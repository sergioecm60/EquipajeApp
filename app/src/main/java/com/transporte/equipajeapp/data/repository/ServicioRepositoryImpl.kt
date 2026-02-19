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

    /**
     * NOTA: El WebService SOAP no tiene método para listar servicios.
     * Solo devuelve el servicio actual en el login.
     * Esta función retorna el servicio guardado en preferencias o lista vacía.
     */
    override suspend fun getServiciosPorInterno(interno: String, fecha: String): Result<List<Servicio>> {
        return try {
            Log.d(TAG, "Obteniendo servicio guardado para interno: $interno")
            
            val prefs = preferencesManager.getUser()
            val servicio = prefs?.let {
                // Crear un único servicio con la info del login
                Servicio(
                    id = it.id,
                    interno = it.interno,
                    origen = "Consultar en boleto",  // No viene en el login
                    destino = it.nombre,  // El nombre del servicio viene en 'nombre'
                    horaSalida = "",
                    horaLlegada = "",
                    empresa = it.empresa,
                    fecha = fecha,
                    estado = "activo"
                )
            }
            
            if (servicio != null) {
                Result.success(listOf(servicio))
            } else {
                Result.success(emptyList())
            }
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
    
    /**
     * Eq_LeerBoleto - Obtener información de un boleto
     */
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
                val soapResponse = result.getOrNull()
                // Parsear respuesta SOAP
                val error = soapResponse?.getPropertyAsString("Error")?.toIntOrNull() ?: -1
                
                if (error == 0) {
                    val boletoInfo = Boleto(
                        id = soapResponse?.getPropertyAsString("IdBoleto")?.toIntOrNull() ?: 0,
                        numero = boleto.toString(),
                        pasajero = soapResponse?.getPropertyAsString("Pasajero") ?: "",
                        dni = soapResponse?.getPropertyAsString("Documento") ?: "",
                        origen = "",
                        destino = "",
                        fecha = "",
                        servicioId = creds.idServicio
                    )
                    Result.success(boletoInfo)
                } else {
                    val descr = soapResponse?.getPropertyAsString("Descr") ?: "Error al leer boleto"
                    Result.error(descr)
                }
            } else {
                Result.error(result.exceptionOrNull()?.message ?: "Error de conexión")
            }
        } catch (e: Exception) {
            Result.error(e.message ?: "Error inesperado")
        }
    }
}