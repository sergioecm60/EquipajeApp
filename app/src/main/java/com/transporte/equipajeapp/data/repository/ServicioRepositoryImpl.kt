package com.transporte.equipajeapp.data.repository

import android.util.Log
import com.transporte.equipajeapp.data.local.PreferencesManager
import com.transporte.equipajeapp.data.model.EqLeerBoletoRequest
import com.transporte.equipajeapp.data.remote.SoapClient
import com.transporte.equipajeapp.domain.model.Boleto
import com.transporte.equipajeapp.domain.model.Result
import com.transporte.equipajeapp.domain.model.Servicio
import com.transporte.equipajeapp.domain.repository.ServicioRepository
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
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

    private fun parseBoletoResponse(xml: String): BoletoResult {
        return try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xml))
            
            var error = -1
            var descr: String? = null
            var idBoleto: Int? = null
            var dni: String? = null
            var pasajero: String? = null
            var origen: String? = null
            var destino: String? = null
            
            var inBoleto = false
            
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            "Error" -> error = parser.nextText().toIntOrNull() ?: -1
                            "Descr" -> descr = parser.nextText()
                            "IdBoleto" -> {
                                inBoleto = true
                                idBoleto = parser.nextText().toIntOrNull()
                            }
                            "Dni" -> if (inBoleto) dni = parser.nextText()
                            "Pasajero" -> if (inBoleto) pasajero = parser.nextText()
                            "Origen" -> if (inBoleto) origen = parser.nextText()
                            "Destino" -> if (inBoleto) destino = parser.nextText()
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name == "IdBoleto") inBoleto = false
                    }
                }
                eventType = parser.next()
            }
            
            BoletoResult(error, descr, idBoleto, dni, pasajero, origen, destino)
        } catch (e: Exception) {
            Log.e(TAG, "Error parseando boleto", e)
            BoletoResult(-1, "Error parseando: ${e.message}", null, null, null, null, null)
        }
    }

    data class BoletoResult(
        val error: Int,
        val descr: String?,
        val idBoleto: Int?,
        val dni: String?,
        val pasajero: String?,
        val origen: String?,
        val destino: String?
    )

    override suspend fun getServiciosPorInterno(interno: String, fecha: String): Result<List<Servicio>> {
        return try {
            Log.d(TAG, "Obteniendo servicios guardados para interno: $interno")
            
            val serviciosJson = preferencesManager.getServicios()
            
            if (serviciosJson.isNullOrEmpty()) {
                Log.d(TAG, "No hay servicios guardados")
                return Result.success(emptyList())
            }
            
            val servicios = serviciosJson.split("|").mapNotNull { item ->
                val parts = item.split(";")
                if (parts.size >= 2) {
                    val id = parts[0].toIntOrNull()
                    val nombre = parts[1]
                    if (id != null) {
                        Servicio(
                            id = id,
                            interno = interno,
                            origen = parts.getOrNull(3)?.takeIf { it.isNotEmpty() } ?: "Consultar",
                            destino = parts.getOrNull(4)?.takeIf { it.isNotEmpty() } ?: "Consultar",
                            horaSalida = parts.getOrNull(5)?.takeIf { it.isNotEmpty() } ?: "",
                            horaLlegada = parts.getOrNull(6)?.takeIf { it.isNotEmpty() } ?: "",
                            empresa = parts.getOrNull(2)?.takeIf { it.isNotEmpty() } ?: "Delta",
                            fecha = parts.getOrNull(7)?.takeIf { it.isNotEmpty() } ?: fecha,
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
                empresa = empresa,
                boleto = boleto,
                idServicio = creds.idServicio,
                usuario = creds.usuario,
                password = creds.password
            )
            
            val result = soapClient.leerBoleto(request)
            
            if (result.isSuccess) {
                val xml = result.getOrNull()!!
                val parsed = parseBoletoResponse(xml)
                
                if (parsed.error == 0 && parsed.idBoleto != null) {
                    val boletoInfo = Boleto(
                        id = parsed.idBoleto,
                        numero = boleto.toString(),
                        pasajero = parsed.pasajero ?: "Desconocido",
                        dni = parsed.dni ?: "",
                        origen = parsed.origen ?: "",
                        destino = parsed.destino ?: "",
                        fecha = "",
                        servicioId = creds.idServicio
                    )
                    Result.success(boletoInfo)
                } else {
                    Result.error(parsed.descr ?: "Boleto no válido")
                }
            } else {
                Result.error(result.exceptionOrNull()?.message ?: "Error de conexión")
            }
        } catch (e: Exception) {
            Result.error(e.message ?: "Error inesperado")
        }
    }
}