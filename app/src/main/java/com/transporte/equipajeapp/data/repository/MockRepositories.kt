package com.transporte.equipajeapp.data.repository

import com.transporte.equipajeapp.domain.model.Boleto
import com.transporte.equipajeapp.domain.model.Equipaje
import com.transporte.equipajeapp.domain.model.EquipajeListado
import com.transporte.equipajeapp.domain.model.Result
import com.transporte.equipajeapp.domain.model.Servicio
import com.transporte.equipajeapp.domain.model.Usuario
import com.transporte.equipajeapp.domain.repository.AuthRepository
import com.transporte.equipajeapp.domain.repository.BoletoInfo
import com.transporte.equipajeapp.domain.repository.EquipajeInfo
import com.transporte.equipajeapp.domain.repository.EquipajeRepository
import com.transporte.equipajeapp.domain.repository.ServicioRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorios MOCK para pruebas offline
 * Usar cuando no hay conexión al servidor
 */

@Singleton
class AuthRepositoryMock @Inject constructor() : AuthRepository {
    
    private var currentUser: Usuario? = null
    
    override suspend fun login(interno: String, password: String): Result<Usuario> {
        android.util.Log.d("AuthMock", "Login llamado con interno: '$interno'")
        // Aceptar cualquier interno que empiece con 100
        return if (interno.startsWith("100")) {
            val usuario = Usuario(
                id = interno.toIntOrNull() ?: 1001,
                interno = interno,
                nombre = when(interno) {
                    "1001" -> "Juan Pérez"
                    "1002" -> "Carlos Gómez"
                    "1003" -> "Roberto Díaz"
                    else -> "Chofer $interno"
                },
                empresa = "Transportes del Norte"
            )
            android.util.Log.d("AuthMock", "Usuario creado: ${usuario.interno} - ${usuario.nombre}")
            currentUser = usuario
            Result.success(usuario)
        } else {
            Result.error("Interno no encontrado. Usar: 1001, 1002 o 1003")
        }
    }
    
    override suspend fun logout() {
        currentUser = null
    }
    
    override suspend fun getCurrentUser(): Usuario? = currentUser
    
    override suspend fun isLoggedIn(): Boolean = currentUser != null
}

@Singleton
class ServicioRepositoryMock @Inject constructor() : ServicioRepository {
    
    private val serviciosMock = listOf(
        Servicio(
            id = 1,
            interno = "1001",
            origen = "Buenos Aires",
            destino = "Rosario",
            horaSalida = "12:30",
            horaLlegada = "16:00",
            empresa = "Transportes del Norte",
            fecha = "2026-02-18",
            estado = "programado"
        ),
        Servicio(
            id = 2,
            interno = "1001",
            origen = "Rosario",
            destino = "Buenos Aires",
            horaSalida = "18:00",
            horaLlegada = "21:30",
            empresa = "Transportes del Norte",
            fecha = "2026-02-18",
            estado = "programado"
        ),
        Servicio(
            id = 3,
            interno = "1002",
            origen = "Buenos Aires",
            destino = "La Plata",
            horaSalida = "14:00",
            horaLlegada = "16:00",
            empresa = "Transportes del Norte",
            fecha = "2026-02-18",
            estado = "programado"
        )
    )
    
    override suspend fun getServiciosPorInterno(interno: String, fecha: String): Result<List<Servicio>> {
        android.util.Log.d("ServicioMock", "Buscando servicios para interno: $interno")
        val filtrados = serviciosMock.filter { it.interno == interno }
        android.util.Log.d("ServicioMock", "Encontrados ${filtrados.size} servicios")
        return Result.success(filtrados)
    }
    
    override suspend fun getServicioActual(interno: String): Result<Servicio?> {
        val servicios = getServiciosPorInterno(interno, "")
        return if (servicios.success && servicios.data?.isNotEmpty() == true) {
            Result.success(servicios.data!!.first())
        } else {
            Result.success(null)
        }
    }
    
    override suspend fun leerBoleto(empresa: String, boleto: Long): Result<Boleto> {
        return Result.success(Boleto(
            id = boleto.toInt(),
            numero = boleto.toString(),
            pasajero = "María González",
            dni = "12345678",
            origen = "Buenos Aires",
            destino = "Rosario",
            fecha = "2026-02-18",
            servicioId = 1
        ))
    }
}

@Singleton
class EquipajeRepositoryMock @Inject constructor() : EquipajeRepository {
    
    private val equipajesRegistrados = mutableListOf<EquipajeListado>()
    
    private val boletosMock = mapOf(
        "QR_BOLETO_001" to BoletoInfo("BOL-001", "María González", "12345678", "Buenos Aires", "Rosario"),
        "QR_BOLETO_002" to BoletoInfo("BOL-002", "José Martínez", "87654321", "Buenos Aires", "Rosario"),
        "QR_BOLETO_003" to BoletoInfo("BOL-003", "Ana Rodríguez", "45678912", "Rosario", "Buenos Aires"),
        "QR_BOLETO_004" to BoletoInfo("BOL-004", "Luis Fernández", "78912345", "Buenos Aires", "La Plata")
    )
    
    private val ribetesMock = listOf(
        "QR_RIBETE_001", "QR_RIBETE_002", "QR_RIBETE_003", "QR_RIBETE_004", "QR_RIBETE_005"
    )
    
    private val ribetesUsados = mutableSetOf<String>()
    
    override suspend fun asociarEquipaje(codigoQr: String, numeroBoleto: String): Result<Equipaje> {
        return Result.success(Equipaje(
            id = 1,
            codigoQr = codigoQr,
            descripcion = "Equipaje registrado",
            numeroBoleto = numeroBoleto,
            fechaAlta = "2026-02-18",
            estado = "registrado"
        ))
    }
    
    override suspend fun getEquipajePorBoleto(numeroBoleto: String): Result<List<Equipaje>> {
        return Result.success(emptyList())
    }
    
    override suspend fun escanearCodigo(qrContent: String): Result<Pair<BoletoInfo, EquipajeInfo>> {
        val boleto = boletosMock[qrContent]
        return if (boleto != null) {
            Result.success(Pair(
                boleto,
                EquipajeInfo("EQ-001", "Equipaje de ${boleto.pasajero}", "pendiente")
            ))
        } else {
            Result.error("Boleto no encontrado. Códigos válidos: QR_BOLETO_001 a QR_BOLETO_004")
        }
    }
    
    override suspend fun leerEquipaje(idBoleto: Int, marbete: String): Result<Boolean> {
        // Verificar si el ribete está disponible
        return if (ribetesMock.contains(marbete) && !ribetesUsados.contains(marbete)) {
            ribetesUsados.add(marbete)
            Result.success(true)
        } else if (ribetesUsados.contains(marbete)) {
            Result.error("Ribete ya usado")
        } else {
            Result.error("Ribete inválido. Usar: QR_RIBETE_001 a QR_RIBETE_005")
        }
    }
    
    override suspend fun listaDeEquipajes(): Result<List<EquipajeListado>> {
        return Result.success(equipajesRegistrados.toList())
    }
}