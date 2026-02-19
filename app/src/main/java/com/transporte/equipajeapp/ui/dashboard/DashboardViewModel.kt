package com.transporte.equipajeapp.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.transporte.equipajeapp.domain.model.EquipajeListado
import com.transporte.equipajeapp.domain.model.Servicio
import com.transporte.equipajeapp.domain.model.Usuario
import com.transporte.equipajeapp.domain.usecase.GetServicioActualUseCase
import com.transporte.equipajeapp.domain.usecase.GetServiciosUseCase
import com.transporte.equipajeapp.domain.usecase.ListaDeEquipajesUseCase
import com.transporte.equipajeapp.domain.usecase.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getServiciosUseCase: GetServiciosUseCase,
    private val getServicioActualUseCase: GetServicioActualUseCase,
    private val listaDeEquipajesUseCase: ListaDeEquipajesUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<DashboardUiState>(DashboardUiState.Idle)
    val state: StateFlow<DashboardUiState> = _state.asStateFlow()

    private var currentInterno: String = ""

    fun loadData(usuario: Usuario) {
        currentInterno = usuario.interno
        viewModelScope.launch {
            _state.value = DashboardUiState.Loading
            android.util.Log.d("DashboardVM", "Cargando datos para interno: ${usuario.interno}")

            // Cargar servicios primero (esto es lo importante)
            val serviciosResult = getServiciosUseCase(usuario.interno)
            android.util.Log.d("DashboardVM", "Resultado servicios - success: ${serviciosResult.success}, data size: ${serviciosResult.data?.size}")
            val servicios = if (serviciosResult.success) {
                serviciosResult.data ?: emptyList()
            } else {
                android.util.Log.e("DashboardVM", "Error servicios: ${serviciosResult.message}")
                emptyList()
            }
            
            val actualResult = getServicioActualUseCase(usuario.interno)
            android.util.Log.d("DashboardVM", "Resultado servicio actual - success: ${actualResult.success}")
            val servicioActual = if (actualResult.success) {
                actualResult.data
            } else null
            
            // Equipajes son opcionales, no bloquean la carga
            val equipajesResult = listaDeEquipajesUseCase()
            val equipajes = if (equipajesResult.success) {
                equipajesResult.data ?: emptyList()
            } else {
                emptyList()
            }
            
            android.util.Log.d("DashboardVM", "Actualizando UI con ${servicios.size} servicios")
            _state.value = DashboardUiState.Success(
                usuario = usuario,
                servicios = servicios,
                servicioActual = servicioActual,
                equipajes = equipajes
            )
        }
    }
    
    fun refreshEquipajes() {
        viewModelScope.launch {
            val currentState = _state.value
            if (currentState is DashboardUiState.Success) {
                _state.value = DashboardUiState.Loading
                
                val result = listaDeEquipajesUseCase()
                if (result.success) {
                    _state.value = currentState.copy(
                        equipajes = result.data ?: emptyList()
                    )
                } else {
                    _state.value = DashboardUiState.Error(result.message ?: "Error al actualizar")
                }
            }
        }
    }

    fun selectServicio(servicio: Servicio) {
        val currentState = _state.value
        if (currentState is DashboardUiState.Success) {
            _state.value = currentState.copy(servicioActual = servicio)
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            _state.value = DashboardUiState.LoggedOut
        }
    }

    fun getCurrentInterno(): String = currentInterno
}

sealed class DashboardUiState {
    object Idle : DashboardUiState()
    object Loading : DashboardUiState()
    object LoggedOut : DashboardUiState()
    data class Success(
        val usuario: Usuario,
        val servicios: List<Servicio>,
        val servicioActual: Servicio?,
        val equipajes: List<EquipajeListado> = emptyList()
    ) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}
