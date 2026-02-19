package com.transporte.equipajeapp.domain.usecase

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
import java.time.LocalDate
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(interno: String, password: String): Result<Usuario> {
        if (interno.isBlank()) {
            return Result.error("El número de interno es obligatorio")
        }
        // Contraseña opcional para pruebas con mocks
        return authRepository.login(interno, password)
    }
}

class GetServiciosUseCase @Inject constructor(
    private val servicioRepository: ServicioRepository
) {
    suspend operator fun invoke(interno: String): Result<List<Servicio>> {
        val fecha = LocalDate.now().toString()
        return servicioRepository.getServiciosPorInterno(interno, fecha)
    }
}

class GetServicioActualUseCase @Inject constructor(
    private val servicioRepository: ServicioRepository
) {
    suspend operator fun invoke(interno: String): Result<Servicio?> {
        return servicioRepository.getServicioActual(interno)
    }
}

/**
 * UseCase para Eq_LeerBoleto según el PDF
 */
class LeerBoletoUseCase @Inject constructor(
    private val servicioRepository: ServicioRepository
) {
    suspend operator fun invoke(empresa: String, boleto: Long) = 
        servicioRepository.leerBoleto(empresa, boleto)
}

/**
 * UseCase legacy - mantenido por compatibilidad pero no en el PDF
 */
@Deprecated("Usar LeerEquipajeUseCase que implementa Eq_LeerEquipeje")
class EscanearQrUseCase @Inject constructor(
    private val equipajeRepository: EquipajeRepository
) {
    suspend operator fun invoke(qrContent: String): Result<Pair<BoletoInfo, EquipajeInfo>> {
        if (qrContent.isBlank()) {
            return Result.error("Codigo QR vacio")
        }
        return equipajeRepository.escanearCodigo(qrContent)
    }
}

/**
 * UseCase para Eq_LeerEquipeje según el PDF
 */
class LeerEquipajeUseCase @Inject constructor(
    private val equipajeRepository: EquipajeRepository
) {
    suspend operator fun invoke(idBoleto: Int, marbete: String) = 
        equipajeRepository.leerEquipaje(idBoleto, marbete)
}

/**
 * UseCase para Eq_ListaDeEquipajes según el PDF
 */
class ListaDeEquipajesUseCase @Inject constructor(
    private val equipajeRepository: EquipajeRepository
) {
    suspend operator fun invoke(): Result<List<EquipajeListado>> = 
        equipajeRepository.listaDeEquipajes()
}

/**
 * UseCase legacy - mantenido por compatibilidad pero no en el PDF
 */
@Deprecated("La asociación se realiza validando el marbete con LeerEquipajeUseCase")
class AsociarEquipajeUseCase @Inject constructor(
    private val equipajeRepository: EquipajeRepository
) {
    suspend operator fun invoke(codigoQr: String, numeroBoleto: String): Result<Equipaje> {
        if (codigoQr.isBlank()) {
            return Result.error("Codigo QR invalido")
        }
        if (numeroBoleto.isBlank()) {
            return Result.error("Numero de boleto invalido")
        }
        return equipajeRepository.asociarEquipaje(codigoQr, numeroBoleto)
    }
}

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() {
        authRepository.logout()
    }
}
