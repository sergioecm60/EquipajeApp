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
    
    override suspend fun leerEquipaje(idBoleto: Int, marbete: String): Result<Boolean> {
        return try {
            val creds = preferencesManager.getCredentials()
                ?: return Result.error("No hay sesión activa")
            
            val request = EqLeerEquipajeRequest(
                idServicio = creds.idServicio,
                idBoleto = idBoleto,
                marbete = marbete.take(15),
                usuario = creds.usuario.padEnd(7, ' ').take(7),
                password = creds.password.padEnd(7, ' ').take(7)
            )
            
            val result = soapClient.leerEquipaje(request)
            
            if (result.isSuccess) {
                // TODO: Parsear XML de respuesta para verificar error
                Result.success(true)
            } else {
                Result.error(result.exceptionOrNull()?.message ?: "Error de conexión")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en leerEquipaje", e)
            Result.error(e.message ?: "Error inesperado")
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
                usuario = creds.usuario.padEnd(7, ' ').take(7),
                password = creds.password.padEnd(7, ' ').take(7)
            )
            
            val result = soapClient.listaDeEquipajes(request)
            
            if (result.isSuccess) {
                // TODO: Parsear XML de respuesta
                Result.success(emptyList())
            } else {
                Result.error(result.exceptionOrNull()?.message ?: "Error de conexión")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en listaDeEquipajes", e)
            Result.error(e.message ?: "Error inesperado")
        }
    }
}