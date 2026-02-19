package com.transporte.equipajeapp.ui.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.transporte.equipajeapp.domain.repository.BoletoInfo
import com.transporte.equipajeapp.domain.repository.EquipajeInfo
import com.transporte.equipajeapp.domain.usecase.AsociarEquipajeUseCase
import com.transporte.equipajeapp.domain.usecase.EscanearQrUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class QrScannerState {
    object Idle : QrScannerState()
    object Scanning : QrScannerState()
    data class QrDetectado(
        val boleto: BoletoInfo,
        val equipaje: EquipajeInfo
    ) : QrScannerState()
    object Associating : QrScannerState()
    object Success : QrScannerState()
    data class Error(val message: String) : QrScannerState()
}

@HiltViewModel
class QrScannerViewModel @Inject constructor(
    private val escanearQrUseCase: EscanearQrUseCase,
    private val asociarEquipajeUseCase: AsociarEquipajeUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<QrScannerState>(QrScannerState.Idle)
    val state: StateFlow<QrScannerState> = _state.asStateFlow()

    private var currentQrContent: String = ""
    private var currentBoletoNumero: String = ""

    fun onQrScanned(qrContent: String) {
        if (_state.value is QrScannerState.Scanning || _state.value is QrScannerState.Associating) {
            return
        }

        currentQrContent = qrContent
        _state.value = QrScannerState.Scanning

        viewModelScope.launch {
            val result = escanearQrUseCase(qrContent)
            if (result.success) {
                val (boleto, equipaje) = result.data!!
                currentBoletoNumero = boleto.numero
                _state.value = QrScannerState.QrDetectado(boleto, equipaje)
            } else {
                _state.value = QrScannerState.Error(result.message ?: "QR invalido")
            }
        }
    }

    fun asociarEquipaje() {
        _state.value = QrScannerState.Associating

        viewModelScope.launch {
            val result = asociarEquipajeUseCase(currentQrContent, currentBoletoNumero)
            if (result.success) {
                _state.value = QrScannerState.Success
            } else {
                _state.value = QrScannerState.Error(result.message ?: "Error al asociar")
            }
        }
    }

    fun continueScanning() {
        _state.value = QrScannerState.Idle
    }

    fun resetState() {
        _state.value = QrScannerState.Idle
    }
}
