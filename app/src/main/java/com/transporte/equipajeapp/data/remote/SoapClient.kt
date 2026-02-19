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
            
            Log.d(TAG, "Respuesta Eq_Login: $responseBody")
            
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
    
    private fun parseLoginResponse(xml: String): EqLoginResponse {
        return try {
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xml))
            
            var error = -1
            var descr: String? = null
            val servicios = mutableListOf<ServicioLoginItem>()
            
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
            
            EqLoginResponse(error = error, descr = descr, servicios = servicios)
        } catch (e: Exception) {
            Log.e(TAG, "Error parseando XML", e)
            EqLoginResponse(error = -1, descr = "Error parseando respuesta", servicios = null)
        }
    }
}