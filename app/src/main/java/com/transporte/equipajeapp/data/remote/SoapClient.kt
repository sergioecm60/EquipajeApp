package com.transporte.equipajeapp.data.remote

import android.util.Log
import com.transporte.equipajeapp.data.model.EqLeerBoletoRequest
import com.transporte.equipajeapp.data.model.EqLeerEquipajeRequest
import com.transporte.equipajeapp.data.model.EqListaEquipajesRequest
import com.transporte.equipajeapp.data.model.EqLoginRequest
import com.transporte.equipajeapp.data.model.EqLoginResponse
import com.transporte.equipajeapp.data.model.ServicioLoginItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.util.concurrent.TimeUnit

/**
 * Cliente SOAP usando OkHttp para comunicarse con el WebService de Delta
 * URL: http://servidordeltapy.dyndns.org/WSDelta_POS/wsdelta_pos.asmx
 */
class SoapClient {
    
    companion object {
        private const val NAMESPACE = "Delta"
        private const val URL = "http://servidordeltapy.dyndns.org/WSDelta_POS/wsdelta_pos.asmx"
        private const val SOAP_ACTION_PREFIX = "Delta/"
        private const val TAG = "SoapClient"
        private const val TIMEOUT = 30L
        
        private val XML_MEDIA_TYPE = "text/xml; charset=utf-8".toMediaType()
    }
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
        .build()
    
    /**
     * Eq_Login - Autenticación del chofer
     */
    suspend fun login(request: EqLoginRequest): Result<EqLoginResponse> = withContext(Dispatchers.IO) {
        try {
            val soapBody = """
                <?xml version="1.0" encoding="utf-8"?>
                <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
                               xmlns:xsd="http://www.w3.org/2001/XMLSchema" 
                               xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body>
                    <Eq_Login xmlns="Delta">
                      <NroInterno>${request.nroInterno}</NroInterno>
                      <PasswordUsuario>${request.passwordUsuario}</PasswordUsuario>
                      <Usuario>${request.usuario}</Usuario>
                      <Password>${request.password}</Password>
                    </Eq_Login>
                  </soap:Body>
                </soap:Envelope>
            """.trimIndent()
            
            val requestBody = soapBody.toRequestBody(XML_MEDIA_TYPE)
            val httpRequest = Request.Builder()
                .url(URL)
                .post(requestBody)
                .header("Content-Type", "text/xml; charset=utf-8")
                .header("SOAPAction", "\"Delta/Eq_Login\"")
                .build()
            
            Log.d(TAG, "Enviando Eq_Login con interno: ${request.nroInterno}")
            
            val response = client.newCall(httpRequest).execute()
            val responseBody = response.body?.string()
            
            Log.d(TAG, "Respuesta Eq_Login RAW: >>>${responseBody}<<<")
            
            if (response.isSuccessful && responseBody != null) {
                val loginResponse = parseLoginResponse(responseBody)
                if (loginResponse.error == 0) {
                    Result.success(loginResponse)
                } else {
                    Result.failure(Exception(loginResponse.descr ?: "Error en login"))
                }
            } else {
                Result.failure(Exception("Error HTTP: ${response.code}"))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error en Eq_Login", e)
            Result.failure(e)
        }
    }
    
    /**
     * Eq_LeerBoleto - Leer datos de un boleto
     */
    suspend fun leerBoleto(request: EqLeerBoletoRequest): Result<String> = withContext(Dispatchers.IO) {
        try {
            val soapBody = """
                <?xml version="1.0" encoding="utf-8"?>
                <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
                               xmlns:xsd="http://www.w3.org/2001/XMLSchema" 
                               xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body>
                    <Eq_LeerBoleto xmlns="Delta">
                      <Empresa>${request.empresa}</Empresa>
                      <Boleto>${request.boleto}</Boleto>
                      <IdServicio>${request.idServicio}</IdServicio>
                      <Usuario>${request.usuario}</Usuario>
                      <Password>${request.password}</Password>
                    </Eq_LeerBoleto>
                  </soap:Body>
                </soap:Envelope>
            """.trimIndent()
            
            val requestBody = soapBody.toRequestBody(XML_MEDIA_TYPE)
            val httpRequest = Request.Builder()
                .url(URL)
                .post(requestBody)
                .header("Content-Type", "text/xml; charset=utf-8")
                .header("SOAPAction", "\"Delta/Eq_LeerBoleto\"")
                .build()
            
            Log.d(TAG, "Enviando Eq_LeerBoleto: ${request.boleto}")
            
            val response = client.newCall(httpRequest).execute()
            val responseBody = response.body?.string()
            
            if (response.isSuccessful && responseBody != null) {
                Result.success(responseBody)
            } else {
                Result.failure(Exception("Error HTTP: ${response.code}"))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error en Eq_LeerBoleto", e)
            Result.failure(e)
        }
    }
    
    /**
     * Eq_LeerEquipaje - Leer/validar equipaje por marbete
     */
    suspend fun leerEquipaje(request: EqLeerEquipajeRequest): Result<String> = withContext(Dispatchers.IO) {
        try {
            val soapBody = """
                <?xml version="1.0" encoding="utf-8"?>
                <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
                               xmlns:xsd="http://www.w3.org/2001/XMLSchema" 
                               xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body>
                    <Eq_LeerEquipaje xmlns="Delta">
                      <IdServicio>${request.idServicio}</IdServicio>
                      <IdBoleto>${request.idBoleto}</IdBoleto>
                      <Marbete>${request.marbete}</Marbete>
                      <Usuario>${request.usuario}</Usuario>
                      <Password>${request.password}</Password>
                    </Eq_LeerEquipaje>
                  </soap:Body>
                </soap:Envelope>
            """.trimIndent()
            
            val requestBody = soapBody.toRequestBody(XML_MEDIA_TYPE)
            val httpRequest = Request.Builder()
                .url(URL)
                .post(requestBody)
                .header("Content-Type", "text/xml; charset=utf-8")
                .header("SOAPAction", "\"Delta/Eq_LeerEquipaje\"")
                .build()
            
            Log.d(TAG, "Enviando Eq_LeerEquipaje: ${request.marbete}")
            
            val response = client.newCall(httpRequest).execute()
            val responseBody = response.body?.string()
            
            if (response.isSuccessful && responseBody != null) {
                Result.success(responseBody)
            } else {
                Result.failure(Exception("Error HTTP: ${response.code}"))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error en Eq_LeerEquipaje", e)
            Result.failure(e)
        }
    }
    
    /**
     * Eq_ListaDeEquipajes - Listar equipajes de un servicio
     */
    suspend fun listaDeEquipajes(request: EqListaEquipajesRequest): Result<String> = withContext(Dispatchers.IO) {
        try {
            val soapBody = """
                <?xml version="1.0" encoding="utf-8"?>
                <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
                               xmlns:xsd="http://www.w3.org/2001/XMLSchema" 
                               xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body>
                    <Eq_ListaDeEquipajes xmlns="Delta">
                      <IdServicio>${request.idServicio}</IdServicio>
                      <Usuario>${request.usuario}</Usuario>
                      <Password>${request.password}</Password>
                    </Eq_ListaDeEquipajes>
                  </soap:Body>
                </soap:Envelope>
            """.trimIndent()
            
            val requestBody = soapBody.toRequestBody(XML_MEDIA_TYPE)
            val httpRequest = Request.Builder()
                .url(URL)
                .post(requestBody)
                .header("Content-Type", "text/xml; charset=utf-8")
                .header("SOAPAction", "\"Delta/Eq_ListaDeEquipajes\"")
                .build()
            
            Log.d(TAG, "Enviando Eq_ListaDeEquipajes: ${request.idServicio}")
            
            val response = client.newCall(httpRequest).execute()
            val responseBody = response.body?.string()
            
            if (response.isSuccessful && responseBody != null) {
                Result.success(responseBody)
            } else {
                Result.failure(Exception("Error HTTP: ${response.code}"))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error en Eq_ListaDeEquipajes", e)
            Result.failure(e)
        }
    }
    
    private data class ParsedServicio(
        val empresa: String,
        val origen: String,
        val destino: String,
        val fecha: String?,
        val hora: String?
    )

    private fun parsearCampoServicio(servicioText: String): ParsedServicio {
        // Formato: "EPA FAR-CDE 19/02/26 22:00" o "EPA ASU-ENC 19/02/26 23:30"
        // Empresa (1-3 chars) + Espacio + Ruta (ORIGEN-DEST) + Espacio + Fecha (DD/MM/YY) + Espacio + Hora (HH:MM)
        
        val fechaPattern = Regex("\\d{2}/\\d{2}/\\d{2,4}")
        val horaPattern = Regex("\\d{2}:\\d{2}")
        
        val fechaMatch = fechaPattern.find(servicioText)
        val horaMatch = horaPattern.find(servicioText)
        
        val fecha = fechaMatch?.value
        val hora = horaMatch?.value
        
        // Convertir fecha de DD/MM/YY a DD/MM/AAAA
        val fechaFormateada = fecha?.let {
            val parts = it.split("/")
            if (parts.size == 3) {
                val year = parts[2]
                val yearCompleto = if (year.length == 2) "20$year" else year
                "${parts[0]}/${parts[1]}/$yearCompleto"
            } else it
        }
        
        // Obtener texto sin fecha y hora
        var sinFechaHora = servicioText
        fechaMatch?.let { sinFechaHora = sinFechaHora.replace(it.value, "") }
        horaMatch?.let { sinFechaHora = sinFechaHora.replace(it.value, "") }
        sinFechaHora = sinFechaHora.trim()
        
        // Ahora queda: "EPA FAR-CDE" -> dividir en empresa y ruta
        val partes = sinFechaHora.split(" ", limit = 2)
        val empresa = partes.getOrNull(0) ?: ""
        val ruta = partes.getOrNull(1) ?: ""
        
        // Ruta: "FAR-CDE" -> origen y destino
        val rutaPartes = ruta.split("-", limit = 2)
        val origen = rutaPartes.getOrNull(0) ?: ""
        val destino = rutaPartes.getOrNull(1) ?: ""
        
        return ParsedServicio(empresa, origen, destino, fechaFormateada, hora)
    }
    
    private fun parseLoginResponse(xml: String): EqLoginResponse {
        return try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xml))
            
            var error = -1
            var descr: String? = null
            val servicios = mutableListOf<ServicioLoginItem>()
            
            var inEqLogin = false
            var currentIdServicio: Int? = null
            var currentServicio: String? = null
            
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            "Error" -> {
                                val text = parser.nextText()
                                error = text.toIntOrNull() ?: -1
                            }
                            "Descr" -> {
                                descr = parser.nextText()
                            }
                            "Eq_Login" -> {
                                inEqLogin = true
                                currentIdServicio = null
                                currentServicio = null
                            }
                            "IdServicio" -> {
                                if (inEqLogin) {
                                    currentIdServicio = parser.nextText().toIntOrNull()
                                }
                            }
                            "Servicio" -> {
                                if (inEqLogin) {
                                    currentServicio = parser.nextText()
                                }
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name == "Eq_Login" && inEqLogin) {
                            if (currentIdServicio != null && currentServicio != null) {
                                Log.d("SoapClient", "Servicio raw: '$currentServicio'")
                                val parsed = parsearCampoServicio(currentServicio!!)
                                Log.d("SoapClient", "Parsed - empresa: '${parsed.empresa}', origen: '${parsed.origen}', destino: '${parsed.destino}', fecha: '${parsed.fecha}', hora: '${parsed.hora}'")
                                servicios.add(ServicioLoginItem(
                                    idServicio = currentIdServicio!!,
                                    servicio = currentServicio!!,
                                    empresa = parsed.empresa,
                                    origen = parsed.origen,
                                    destino = parsed.destino,
                                    horaSalida = parsed.hora ?: "",
                                    horaLlegada = parsed.hora ?: "",
                                    fecha = parsed.fecha
                                ))
                            }
                            inEqLogin = false
                        }
                    }
                }
                eventType = parser.next()
            }
            
            EqLoginResponse(error = error, descr = descr, servicios = servicios)
        } catch (e: Exception) {
            Log.e(TAG, "Error parseando XML", e)
            EqLoginResponse(error = -1, descr = "Error parseando respuesta: ${e.message}", servicios = null)
        }
    }

    data class EquipajeResult(val error: Int, val descr: String?)
    
    private fun parseEquipajeResponse(xml: String): EquipajeResult {
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
            
            EquipajeResult(error, descr)
        } catch (e: Exception) {
            Log.e(TAG, "Error parseando XML", e)
            EquipajeResult(-1, "Error parseando: ${e.message}")
        }
    }

    data class EquipajeListItem(
        val idBoleto: Int,
        val marbete: String,
        val descripcion: String?,
        val observaciones: String?
    )

    private fun parseListaEquipajesResponse(xml: String): List<EquipajeListItem> {
        return try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xml))
            
            val equipajes = mutableListOf<EquipajeListItem>()
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
                                equipajes.add(EquipajeListItem(
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
}