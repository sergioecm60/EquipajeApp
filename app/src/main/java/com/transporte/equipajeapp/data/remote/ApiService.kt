package com.transporte.equipajeapp.data.remote

import com.transporte.equipajeapp.data.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * API Service según especificación del PDF de stored procedures
 * Eq = Equipaje
 */
interface ApiService {

    /**
     * 1) Eq_Login - Autenticación
     * SP: Eq_Login
     */
    @POST("Eq_Login")
    suspend fun login(@Body request: EqLoginRequest): Response<EqLoginResponse>

    /**
     * 2) Eq_LeerBoleto - Leer datos de boleto
     * SP: Eq_LeerBoleto
     */
    @POST("Eq_LeerBoleto")
    suspend fun leerBoleto(@Body request: EqLeerBoletoRequest): Response<EqLeerBoletoResponse>

    /**
     * 3) Eq_LeerEquipeje - Leer equipaje por marbete
     * SP: Eq_LeerEquipeje
     */
    @POST("Eq_LeerEquipeje")
    suspend fun leerEquipaje(@Body request: EqLeerEquipajeRequest): Response<EqLeerEquipajeResponse>

    /**
     * 4) Eq_ListaDeEquipajes - Listar equipajes de un servicio
     * SP: Eq_ListaDeEquipajes
     */
    @POST("Eq_ListaDeEquipajes")
    suspend fun listaDeEquipajes(@Body request: EqListaEquipajesRequest): Response<EqListaEquipajesResponse>

    // Endpoints legacy (mantenidos por compatibilidad)
    @POST("auth/login")
    suspend fun loginLegacy(@Body request: LoginRequest): Response<LoginResponse>

    @GET("servicios/{interno}/cercanos")
    suspend fun getServiciosCercanos(@Path("interno") interno: String): Response<ServiciosResponse>

    @POST("equipaje/registrar")
    suspend fun registrarEquipaje(@Body request: AssociateEquipajeRequest): Response<EquipajeResponse>

    @GET("equipaje/verificar")
    suspend fun verificarEquipaje(@Query("codigo_ribete") codigoRibete: String): Response<EquipajeResponse>

    @GET("equipajes/servicio/{servicio_id}")
    suspend fun getEquipajesPorServicio(@Path("servicio_id") servicioId: Int): Response<EquipajeResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<Map<String, Any>>
}
