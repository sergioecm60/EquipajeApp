package com.transporte.equipajeapp.data.repository

import com.transporte.equipajeapp.data.local.PreferencesManager
import com.transporte.equipajeapp.data.model.EqLoginRequest
import com.transporte.equipajeapp.data.remote.SoapClient
import com.transporte.equipajeapp.domain.model.Result
import com.transporte.equipajeapp.domain.model.Usuario
import com.transporte.equipajeapp.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val soapClient: SoapClient,
    private val preferencesManager: PreferencesManager
) : AuthRepository {

    companion object {
        // Credenciales del sistema WebService Delta
        private const val SYSTEM_USER = "cEl\$*eM"
        private const val SYSTEM_PASSWORD = "eMbA*mArB#\$"
    }

    override suspend fun login(interno: String, password: String): Result<Usuario> {
        return try {
            // Preparar request con padding requerido
            val request = EqLoginRequest(
                nroInterno = interno.padEnd(10, ' ').take(10),
                passwordUsuario = password.padEnd(7, ' ').take(7),
                usuario = SYSTEM_USER,
                password = SYSTEM_PASSWORD
            )
            
            val result = soapClient.login(request)
            
            if (result.isSuccess) {
                val response = result.getOrNull()!!
                if (response.error == 0) {
                    // El login devuelve una LISTA de servicios
                    val servicios = response.servicios
                    
                    if (servicios.isNullOrEmpty()) {
                        return Result.error("No hay servicios asignados para este interno")
                    }
                    
                    // Guardar todos los servicios como JSON
                    val serviciosJson = servicios.joinToString("|") { "${it.idServicio};${it.servicio};${it.origen ?: ""};${it.destino ?: ""}" }
                    preferencesManager.saveServicios(serviciosJson)
                    
                    // Tomamos el primer servicio como referencia
                    val primerServicio = servicios.first()
                    
                    val usuario = Usuario(
                        id = primerServicio.idServicio,
                        interno = interno.trim(),
                        nombre = primerServicio.servicio.trim(),
                        empresa = "Delta"  // Del nombre del WS
                    )
                    
                    // Guardar en preferencias
                    preferencesManager.saveUser(
                        id = usuario.id,
                        interno = usuario.interno,
                        nombre = usuario.nombre,
                        empresa = usuario.empresa,
                        token = "soap_token"
                    )
                    
                    // Guardar credenciales del sistema para otros métodos
                    preferencesManager.saveCredentials(
                        usuario = SYSTEM_USER,
                        password = SYSTEM_PASSWORD,
                        idServicio = usuario.id
                    )
                    
                    Result.success(usuario)
                } else {
                    Result.error(response.descr ?: "Error en login (código ${response.error})")
                }
            } else {
                Result.error(result.exceptionOrNull()?.message ?: "Error de conexión")
            }
        } catch (e: Exception) {
            Result.error(e.message ?: "Error inesperado")
        }
    }

    override suspend fun logout() {
        preferencesManager.clearUser()
        preferencesManager.clearCredentials()
    }

    override suspend fun getCurrentUser(): Usuario? {
        val prefs = preferencesManager.getUser()
        return prefs?.let {
            Usuario(
                id = it.id,
                interno = it.interno,
                nombre = it.nombre,
                empresa = it.empresa
            )
        }
    }

    override suspend fun isLoggedIn(): Boolean {
        return preferencesManager.isLoggedIn()
    }
}