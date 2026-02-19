package com.transporte.equipajeapp.ui.registro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.transporte.equipajeapp.domain.model.Boleto
import com.transporte.equipajeapp.domain.usecase.LeerBoletoUseCase
import com.transporte.equipajeapp.domain.usecase.LeerEquipajeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class RegistroEquipajeState {
    object Idle : RegistroEquipajeState()
    object LoadingBoleto : RegistroEquipajeState()
    object LoadingEquipaje : RegistroEquipajeState()
    data class BoletoDetectado(
        val boleto: Boleto,
        val empresa: String
    ) : RegistroEquipajeState()
    data class EquipajeValidado(
        val idBoleto: Int,
        val marbete: String,
        val exitoso: Boolean
    ) : RegistroEquipajeState()
    data class Error(val message: String) : RegistroEquipajeState()
}

@HiltViewModel
class RegistroEquipajeViewModel @Inject constructor(
    private val leerBoletoUseCase: LeerBoletoUseCase,
    private val leerEquipajeUseCase: LeerEquipajeUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<RegistroEquipajeState>(RegistroEquipajeState.Idle)
    val state: StateFlow<RegistroEquipajeState> = _state.asStateFlow()

    private var currentBoleto: Boleto? = null
    private var currentEmpresa: String = ""

    fun leerBoleto(empresa: String, numeroBoleto: Long) {
        currentEmpresa = empresa
        
        viewModelScope.launch {
            _state.value = RegistroEquipajeState.LoadingBoleto
            
            val result = leerBoletoUseCase(empresa, numeroBoleto)
            if (result.success) {
                currentBoleto = result.data
                _state.value = RegistroEquipajeState.BoletoDetectado(
                    boleto = result.data!!,
                    empresa = empresa
                )
            } else {
                _state.value = RegistroEquipajeState.Error(
                    result.message ?: "Error al leer boleto"
                )
            }
        }
    }

    fun validarMarbete(marbete: String) {
        val boleto = currentBoleto
        if (boleto == null) {
            _state.value = RegistroEquipajeState.Error("No hay boleto seleccionado")
            return
        }
        
        viewModelScope.launch {
            _state.value = RegistroEquipajeState.LoadingEquipaje
            
            val result = leerEquipajeUseCase(boleto.id, marbete)
            if (result.success) {
                _state.value = RegistroEquipajeState.EquipajeValidado(
                    idBoleto = boleto.id,
                    marbete = marbete,
                    exitoso = result.data ?: false
                )
            } else {
                _state.value = RegistroEquipajeState.Error(
                    result.message ?: "Error al validar marbete"
                )
            }
        }
    }

    fun resetState() {
        currentBoleto = null
        currentEmpresa = ""
        _state.value = RegistroEquipajeState.Idle
    }
    
    fun getCurrentBoleto(): Boleto? = currentBoleto
}
