package com.transporte.equipajeapp.data.remote

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkClient {

    /**
     * URL base del webservice del proveedor.
     * IMPORTANTE: Cambiar esta URL por la URL real cuando esté disponible.
     * 
     * Según el PDF, los endpoints esperados son:
     * - POST Eq_Login
     * - POST Eq_LeerBoleto
     * - POST Eq_LeerEquipeje
     * - POST Eq_ListaDeEquipajes
     */
    // Para emulador Android: usa 10.0.2.2 para acceder al localhost del PC
    // Para teléfono físico: usa la IP de tu PC en la red (ej: http://192.168.1.100/EquipajeApp/api/)
    private const val BASE_URL = "http://192.168.0.167:8081/EquipajeApp/api/"
    private const val TIMEOUT_SECONDS = 30L

    private val gson: Gson = GsonBuilder()
        .setLenient()
        .create()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val original = chain.request()
            val request = original.newBuilder()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .method(original.method, original.body)
                .build()
            chain.proceed(request)
        }
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}
