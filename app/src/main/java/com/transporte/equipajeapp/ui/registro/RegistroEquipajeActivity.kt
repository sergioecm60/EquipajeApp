package com.transporte.equipajeapp.ui.registro

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.transporte.equipajeapp.databinding.ActivityRegistroEquipajeBinding
import com.transporte.equipajeapp.ui.scanner.QrScannerActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Actividad para registrar equipaje según el flujo del PDF:
 * 1. Escanear/ingresar código de boleto → Eq_LeerBoleto
 * 2. Escanear/ingresar marbete → Eq_LeerEquipeje
 */
@AndroidEntryPoint
class RegistroEquipajeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistroEquipajeBinding
    private val viewModel: RegistroEquipajeViewModel by viewModels()

    private var isScanningBoleto = true // true = escaneando boleto, false = escaneando marbete

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            iniciarEscaneo()
        } else {
            Toast.makeText(this, "Se necesita permiso de cámara", Toast.LENGTH_LONG).show()
        }
    }

    private val scanLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val qrContent = result.data?.getStringExtra("qr_content")
            if (!qrContent.isNullOrEmpty()) {
                procesarCodigoEscaneado(qrContent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroEquipajeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.btnScanBoleto.setOnClickListener {
            isScanningBoleto = true
            checkCameraPermission()
        }

        binding.btnScanRibete.setOnClickListener {
            isScanningBoleto = false
            checkCameraPermission()
        }

        binding.btnLeerBoleto.setOnClickListener {
            leerBoletoManual()
        }

        binding.btnValidarMarbete.setOnClickListener {
            validarMarbeteManual()
        }

        binding.btnCancelar.setOnClickListener {
            if (isScanningBoleto) {
                finish()
            } else {
                // Volver a escanear boleto
                isScanningBoleto = true
                resetearVistaBoleto()
            }
        }

        binding.btnNuevo.setOnClickListener {
            viewModel.resetState()
            isScanningBoleto = true
            resetearVistaBoleto()
        }

        binding.btnVerificarEquipaje.setOnClickListener {
            // Abrir actividad de verificación (para control policial)
            // Intent(this, VerificarEquipajeActivity::class.java)
            Toast.makeText(this, "Funcionalidad de verificación", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == 
                PackageManager.PERMISSION_GRANTED -> {
                iniciarEscaneo()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                Toast.makeText(this, "Se necesita cámara para escanear códigos", Toast.LENGTH_LONG).show()
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun iniciarEscaneo() {
        val intent = Intent(this, QrScannerActivity::class.java).apply {
            putExtra("title", if (isScanningBoleto) "Escanear Boleto" else "Escanear Marbete")
        }
        scanLauncher.launch(intent)
    }

    private fun procesarCodigoEscaneado(codigo: String) {
        if (isScanningBoleto) {
            // Parsear código de boleto
            // Formato esperado: empresa + número de boleto
            if (codigo.length >= 3) {
                val empresa = codigo.take(3)
                val boletoStr = codigo.drop(3)
                
                binding.etEmpresa.setText(empresa)
                binding.etBoleto.setText(boletoStr)
                
                try {
                    val boleto = boletoStr.toLong()
                    viewModel.leerBoleto(empresa, boleto)
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "Código de boleto inválido", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Código demasiado corto", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Es un marbete
            binding.etMarbete.setText(codigo)
            viewModel.validarMarbete(codigo)
        }
    }

    private fun leerBoletoManual() {
        val empresa = binding.etEmpresa.text.toString().trim()
        val boletoStr = binding.etBoleto.text.toString().trim()
        
        if (empresa.length == 3 && boletoStr.isNotEmpty()) {
            try {
                val boleto = boletoStr.toLong()
                viewModel.leerBoleto(empresa, boleto)
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Número de boleto inválido", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Ingrese empresa (3 caracteres) y boleto", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validarMarbeteManual() {
        val marbete = binding.etMarbete.text.toString().trim()
        if (marbete.isNotEmpty()) {
            viewModel.validarMarbete(marbete)
        } else {
            Toast.makeText(this, "Ingrese el código del marbete", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        is RegistroEquipajeState.Idle -> {
                            binding.progressBar.visibility = android.view.View.GONE
                            binding.btnNuevo.isEnabled = false
                        }
                        is RegistroEquipajeState.LoadingBoleto -> {
                            binding.progressBar.visibility = android.view.View.VISIBLE
                            binding.tvStatus.text = "Leyendo boleto..."
                            binding.tvStatus.visibility = android.view.View.VISIBLE
                        }
                        is RegistroEquipajeState.LoadingEquipaje -> {
                            binding.progressBar.visibility = android.view.View.VISIBLE
                            binding.tvStatus.text = "Validando marbete..."
                            binding.tvStatus.visibility = android.view.View.VISIBLE
                        }
                        is RegistroEquipajeState.BoletoDetectado -> {
                            binding.progressBar.visibility = android.view.View.GONE
                            mostrarDatosBoleto(state.boleto, state.empresa)
                        }
                        is RegistroEquipajeState.EquipajeValidado -> {
                            binding.progressBar.visibility = android.view.View.GONE
                            if (state.exitoso) {
                                Toast.makeText(
                                    this@RegistroEquipajeActivity,
                                    "Marbete validado correctamente",
                                    Toast.LENGTH_SHORT
                                ).show()
                                binding.btnNuevo.isEnabled = true
                            } else {
                                binding.tvStatus.text = "Marbete no válido"
                            }
                        }
                        is RegistroEquipajeState.Error -> {
                            binding.progressBar.visibility = android.view.View.GONE
                            Toast.makeText(
                                this@RegistroEquipajeActivity,
                                state.message,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }
    }

    private fun mostrarDatosBoleto(boleto: com.transporte.equipajeapp.domain.model.Boleto, empresa: String) {
        isScanningBoleto = false
        
        // Mostrar datos del boleto
        binding.llPasajeroInfo.visibility = android.view.View.VISIBLE
        binding.tvPasajeroNombre.text = "Nombre: ${boleto.pasajero}"
        binding.tvPasajeroDni.text = "DNI: ${boleto.dni}"
        binding.tvBoletoId.text = "IdBoleto: ${boleto.id}"
        
        // Cambiar UI para entrada de marbete
        binding.layoutBoleto.visibility = android.view.View.GONE
        binding.layoutMarbete.visibility = android.view.View.VISIBLE
    }

    private fun resetearVistaBoleto() {
        binding.llPasajeroInfo.visibility = android.view.View.GONE
        binding.layoutBoleto.visibility = android.view.View.VISIBLE
        binding.layoutMarbete.visibility = android.view.View.GONE
        
        binding.etEmpresa.text?.clear()
        binding.etBoleto.text?.clear()
        binding.etMarbete.text?.clear()
        binding.tvStatus.visibility = android.view.View.GONE
    }
}
