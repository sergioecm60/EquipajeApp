package com.transporte.equipajeapp.data.repository

import android.util.Log
import com.transporte.equipajeapp.data.local.PreferencesManager
import com.transporte.equipajeapp.data.model.EqLeerEquipajeRequest
import com.transporte.equipajeapp.data.model.EqListaEquipajesRequest
import com.transporte.equipajeapp.data.remote.SoapClient
import com.transporte.equipajeapp.domain.model.Equipaje
import com.transporte.equipajeapp.domain.model.EquipajeListado
import com.transporte.equipajeapp.domain.model.Result
import com.transporte.equipajeapp.domain.repository.BoletoInfo
import com.transporte.equipajeapp.domain.repository.EquipajeInfo
import com.transporte.equipajeapp.domain.repository.EquipajeRepository
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EquipajeRepositoryImpl @Inject constructor(
    private val soapClient: SoapClient,
    private val preferencesManager: PreferencesManager
) : EquipajeRepository {

    companion object {
        private const val TAG = "EquipajeRepoImpl"
    }

    override suspend fun asociarEquipaje(codigoQr: String, numeroBoleto: String): Result<Equipaje> {
        return Result.error("Funcionalidad no disponible - usar Eq_LeerEquipaje para validar marbete")
    }

    override suspend fun getEquipajePorBoleto(numeroBoleto: String): Result<List<Equipaje>> {
        return Result.error("Funcionalidad no disponible en WebService SOAP")
    }

    override suspend fun escanearCodigo(qrContent: String): Result<Pair<BoletoInfo, EquipajeInfo>> {
        return Result.error("Funcionalidad no disponible en WebService SOAP")
    }

    private fun parseEquipajeResponse(xml: String): SoapClient.EquipajeResult {
        return try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xml))
            
            var error = -1
            var descr: String? = null
            
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            "Error" -> error = parser.nextText().toIntOrNull() ?: -1
                            "Descr" -> descr = parser.nextText()
                        }
                    }
                }
                eventType = parser.next()
            }
            
            SoapClient.EquipajeResult(error, descr)
        } catch (e: Exception) {
            Log.e(TAG, "Error parseando XML", e)
            SoapClient.EquipajeResult(-1, "Error parseando: ${e.message}")
        }
    }
    
    override suspend fun leerEquipaje(idBoleto: Int, marbete: String): Result<Boolean> {
        return try {
            val creds = preferencesManager.getCredentials()
                ?: return Result.error("No hay sesión activa")
            
            val request = EqLeerEquipajeRequest(
                idServicio = creds.idServicio,
                idBoleto = idBoleto,
                marbete = marbete.take(15),
                usuario = creds.usuario,
                password = creds.password
            )
            
            val result = soapClient.leerEquipaje(request)
            
            if (result.isSuccess) {
                val xml = result.getOrNull()!!
                val parsed = parseEquipajeResponse(xml)
                
                if (parsed.error == 0) {
                    Log.d(TAG, "LeerEquipaje OK: ${parsed.descr}")
                    Result.success(true)
                } else {
                    Log.e(TAG, "LeerEquipaje error: ${parsed.descr}")
                    Result.error(parsed.descr ?: "Error en validación de equipaje")
                }
            } else {
                Result.error(result.exceptionOrNull()?.message ?: "Error de conexión")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en leerEquipaje", e)
            Result.error(e.message ?: "Error inesperado")
        }
    }

    private fun parseListaEquipajes(xml: String): List<SoapClient.EquipajeListItem> {
        return try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xml))
            
            val equipajes = mutableListOf<SoapClient.EquipajeListItem>()
            var inEquipaje = false
            var currentIdBoleto: Int? = null
            var currentMarbete: String? = null
            var currentDescripcion: String? = null
            var currentObservaciones: String? = null
            
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            "Eq_ListaDeEquipajes" -> inEquipaje = true
                            "IdBoleto" -> if (inEquipaje) currentIdBoleto = parser.nextText().toIntOrNull()
                            "Marbete" -> if (inEquipaje) currentMarbete = parser.nextText()
                            "Descripcion" -> if (inEquipaje) currentDescripcion = parser.nextText()
                            "Observaciones" -> if (inEquipaje) currentObservaciones = parser.nextText()
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name == "Eq_ListaDeEquipajes" && inEquipaje) {
                            if (currentIdBoleto != null && currentMarbete != null) {
                                equipajes.add(SoapClient.EquipajeListItem(
                                    idBoleto = currentIdBoleto!!,
                                    marbete = currentMarbete!!,
                                    descripcion = currentDescripcion,
                                    observaciones = currentObservaciones
                                ))
                            }
                            inEquipaje = false
                            currentIdBoleto = null
                            currentMarbete = null
                            currentDescripcion = null
                            currentObservaciones = null
                        }
                    }
                }
                eventType = parser.next()
            }
            
            equipajes
        } catch (e: Exception) {
            Log.e(TAG, "Error parseando lista equipajes", e)
            emptyList()
        }
    }
    
    override suspend fun listaDeEquipajes(): Result<List<EquipajeListado>> {
        return try {
            val creds = preferencesManager.getCredentials()
            
            if (creds == null || creds.idServicio == 0) {
                return Result.success(emptyList())
            }
            
            val request = EqListaEquipajesRequest(
                idServicio = creds.idServicio,
                usuario = creds.usuario,
                password = creds.password
            )
            
            val result = soapClient.listaDeEquipajes(request)
            
            if (result.isSuccess) {
                val xml = result.getOrNull()!!
                val equipajes = parseListaEquipajes(xml)
                
                val lista = equipajes.map { item ->
                    EquipajeListado(
                        hdIdBoleto = item.idBoleto,
                        cantidad = 1,
                        documento = item.descripcion ?: "",
                        pasajero = item.observaciones ?: "",
                        marbetes = listOf(item.marbete)
                    )
                }
                
                Log.d(TAG, "Lista equipajes: ${lista.size} items")
                Result.success(lista)
            } else {
                Result.error(result.exceptionOrNull()?.message ?: "Error de conexión")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en listaDeEquipajes", e)
            Result.error(e.message ?: "Error inesperado")
        }
    }
}