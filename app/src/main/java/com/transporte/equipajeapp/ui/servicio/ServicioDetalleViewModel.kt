package com.transporte.equipajeapp.ui.servicio

import androidx.lifecycle.ViewModel
import com.transporte.equipajeapp.domain.usecase.LeerBoletoUseCase
import com.transporte.equipajeapp.domain.usecase.LeerEquipajeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ServicioDetalleViewModel @Inject constructor(
    private val leerBoletoUseCase: LeerBoletoUseCase,
    private val leerEquipajeUseCase: LeerEquipajeUseCase
) : ViewModel() {

    // Aquí puedes agregar el estado si necesitas manejar estados complejos
    // Por ahora lo mantenemos simple en la Activity
}