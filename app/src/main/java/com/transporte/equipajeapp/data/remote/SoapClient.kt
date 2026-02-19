package com.transporte.equipajeapp.data.remote

import android.util.Log
import com.transporte.equipajeapp.data.model.EqLeerBoletoRequest
import com.transporte.equipajeapp.data.model.EqLeerEquipajeRequest
import com.transporte.equipajeapp.data.model.EqListaEquipajesRequest
import com.transporte.equipajeapp.data.model.EqLoginRequest
import com.transporte.equipajeapp.data.model.EqLoginResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ksoap2.SoapEnvelope
import org.ksoap2.serialization.SoapObject
import org.ksoap2.serialization.SoapSerializationEnvelope
import org.ksoap2.transport.HttpTransportSE

/**
 * Cliente SOAP para comunicarse con el WebService de Delta
 * URL: http://servidordeltapy.dyndns.org/WSDelta_POS/wsdelta_pos.asmx
 */
class SoapClient {
    
    companion object {
        private const val NAMESPACE = "Delta"
        private const val URL = "http://servidordeltapy.dyndns.org/WSDelta_POS/wsdelta_pos.asmx"
        private const val SOAP_ACTION_PREFIX = "Delta/"
        private const val TAG = "SoapClient"
        
        // Timeout en milisegundos
        private const val TIMEOUT = 30000
    }
    
    /**
     * Eq_Login - Autenticación del chofer
     */
    suspend fun login(request: EqLoginRequest): Result<EqLoginResponse> = withContext(Dispatchers.IO) {
        try {
            val soapObject = SoapObject(NAMESPACE, "Eq_Login").apply {
                addProperty("NroInterno", request.nroInterno)
                addProperty("PasswordUsuario", request.passwordUsuario)
                addProperty("Usuario", request.usuario)
                addProperty("Password", request.password)
            }
            
            val envelope = createEnvelope(soapObject)
            val transport = HttpTransportSE(URL, TIMEOUT).apply {
                setXmlVersionTag("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
            }
            
            Log.d(TAG, "Enviando Eq_Login con interno: ${request.nroInterno}")
            
            transport.call("$SOAP_ACTION_PREFIX Eq_Login", envelope)
            
            val response = envelope.response
            Log.d(TAG, "Respuesta Eq_Login: $response")
            
            // Parsear respuesta SOAP
            if (response is SoapObject) {
                val loginResponse = parseLoginResponse(response)
                if (loginResponse.error == 0) {
                    Result.success(loginResponse)
                } else {
                    Result.failure(Exception(loginResponse.descr ?: "Error en login (código ${loginResponse.error})"))
                }
            } else {
                Result.failure(Exception("Respuesta inválida del servidor"))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error en Eq_Login", e)
            Result.failure(e)
        }
    }
    
    /**
     * Eq_LeerBoleto - Leer datos de un boleto
     */
    suspend fun leerBoleto(request: EqLeerBoletoRequest): Result<SoapObject> = withContext(Dispatchers.IO) {
        try {
            val soapObject = SoapObject(NAMESPACE, "Eq_LeerBoleto").apply {
                addProperty("Empresa", request.empresa)
                addProperty("Boleto", request.boleto.toString())
                addProperty("IdServicio", request.idServicio)
                addProperty("Usuario", request.usuario)
                addProperty("Password", request.password)
            }
            
            val envelope = createEnvelope(soapObject)
            val transport = HttpTransportSE(URL, TIMEOUT)
            
            Log.d(TAG, "Enviando Eq_LeerBoleto: ${request.boleto}")
            
            transport.call("$SOAP_ACTION_PREFIX Eq_LeerBoleto", envelope)
            
            val response = envelope.response
            if (response is SoapObject) {
                Result.success(response)
            } else {
                Result.failure(Exception("Respuesta inválida"))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error en Eq_LeerBoleto", e)
            Result.failure(e)
        }
    }
    
    /**
     * Eq_LeerEquipaje - Leer/validar equipaje por marbete
     */
    suspend fun leerEquipaje(request: EqLeerEquipajeRequest): Result<SoapObject> = withContext(Dispatchers.IO) {
        try {
            val soapObject = SoapObject(NAMESPACE, "Eq_LeerEquipaje").apply {
                addProperty("IdBoleto", request.idBoleto)
                addProperty("Marbete", request.marbete)
                addProperty("Usuario", request.usuario)
                addProperty("Password", request.password)
            }
            
            val envelope = createEnvelope(soapObject)
            val transport = HttpTransportSE(URL, TIMEOUT)
            
            Log.d(TAG, "Enviando Eq_LeerEquipaje: ${request.marbete}")
            
            transport.call("$SOAP_ACTION_PREFIX Eq_LeerEquipaje", envelope)
            
            val response = envelope.response
            if (response is SoapObject) {
                Result.success(response)
            } else {
                Result.failure(Exception("Respuesta inválida"))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error en Eq_LeerEquipaje", e)
            Result.failure(e)
        }
    }
    
    /**
     * Eq_ListaDeEquipajes - Listar equipajes de un servicio
     */
    suspend fun listaDeEquipajes(request: EqListaEquipajesRequest): Result<SoapObject> = withContext(Dispatchers.IO) {
        try {
            val soapObject = SoapObject(NAMESPACE, "Eq_ListaDeEquipajes").apply {
                addProperty("IdServicio", request.idServicio)
                addProperty("Usuario", request.usuario)
                addProperty("Password", request.password)
            }
            
            val envelope = createEnvelope(soapObject)
            val transport = HttpTransportSE(URL, TIMEOUT)
            
            Log.d(TAG, "Enviando Eq_ListaDeEquipajes: ${request.idServicio}")
            
            transport.call("$SOAP_ACTION_PREFIX Eq_ListaDeEquipajes", envelope)
            
            val response = envelope.response
            if (response is SoapObject) {
                Result.success(response)
            } else {
                Result.failure(Exception("Respuesta inválida"))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error en Eq_ListaDeEquipajes", e)
            Result.failure(e)
        }
    }
    
    private fun createEnvelope(soapObject: SoapObject): SoapSerializationEnvelope {
        return SoapSerializationEnvelope(SoapEnvelope.VER11).apply {
            setOutputSoapObject(soapObject)
            dotNet = true  // Importante para webservices .NET
        }
    }
    
    private fun parseLoginResponse(soapObject: SoapObject): EqLoginResponse {
        return EqLoginResponse(
            error = soapObject.getPropertySafelyAsString("Error")?.toIntOrNull() ?: -1,
            descr = soapObject.getPropertySafelyAsString("Descr"),
            idServicio = soapObject.getPropertySafelyAsString("IdServicio")?.toIntOrNull() ?: 0,
            servicio = soapObject.getPropertySafelyAsString("Servicio")
        )
    }
}

// Extensión para obtener propiedades de forma segura
private fun SoapObject.getPropertySafelyAsString(name: String): String? {
    return try {
        val property = getProperty(name)
        property?.toString()?.takeIf { it != "anyType{}" }
    } catch (e: Exception) {
        null
    }
}