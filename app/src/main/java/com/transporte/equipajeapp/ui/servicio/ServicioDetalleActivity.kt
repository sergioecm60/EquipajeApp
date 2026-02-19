package com.transporte.equipajeapp.ui.servicio

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.transporte.equipajeapp.databinding.ActivityServicioDetalleBinding
import com.transporte.equipajeapp.domain.model.Servicio
import com.transporte.equipajeapp.ui.scanner.QrScannerActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ServicioDetalleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityServicioDetalleBinding
    private val viewModel: ServicioDetalleViewModel by viewModels()
    
    private var servicioId: Int = 0
    private var boletoEscaneado: String? = null
    private var pasajeroNombre: String? = null
    private var pasajeroDni: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServicioDetalleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener datos del servicio
        servicioId = intent.getIntExtra("servicio_id", 0)
        val origen = intent.getStringExtra("origen") ?: ""
        val destino = intent.getStringExtra("destino") ?: ""
        val horaSalida = intent.getStringExtra("hora_salida") ?: ""
        val horaLlegada = intent.getStringExtra("hora_llegada") ?: ""
        val interno = intent.getStringExtra("interno") ?: ""
        val chofer = intent.getStringExtra("chofer") ?: ""
        
        // Mostrar datos del servicio
        binding.tvRuta.text = "$origen → $destino"
        binding.tvHorario.text = "Salida: $horaSalida - Llegada: $horaLlegada"
        binding.tvInterno.text = "Interno: $interno"
        binding.tvChofer.text = "Chofer: $chofer"
        
        setupClickListeners()
        observeState()
    }

    private fun setupClickListeners() {
        // Botón para escanear boleto
        binding.btnEscanearBoleto.setOnClickListener {
            val intent = Intent(this, QrScannerActivity::class.java)
            intent.putExtra("modo_solo_lectura", true)
            startActivityForResult(intent, REQUEST_CODE_BOLETO)
        }
        
        // Botón para escanear marbete (solo habilitar después de escanear boleto)
        binding.btnEscanearMarbete.setOnClickListener {
            if (boletoEscaneado == null) {
                Toast.makeText(this, "Primero escanee el boleto del pasajero", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(this, QrScannerActivity::class.java)
            intent.putExtra("modo_solo_lectura", true)
            startActivityForResult(intent, REQUEST_CODE_MARBETE)
        }
        
        // Botón para registrar equipaje
        binding.btnRegistrarEquipaje.setOnClickListener {
            if (boletoEscaneado == null) {
                Toast.makeText(this, "Debe escanear el boleto primero", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            registrarEquipaje()
        }
        
        // Botón volver
        binding.btnVolver.setOnClickListener {
            finish()
        }
    }

    private fun registrarEquipaje() {
        // Aquí llamarías al usecase para registrar el equipaje
        // Por ahora mostramos un mensaje de éxito
        Toast.makeText(
            this, 
            "Equipaje registrado:\nPasajero: $pasajeroNombre\nBoleto: $boletoEscaneado", 
            Toast.LENGTH_LONG
        ).show()
        
        // Limpiar para siguiente registro
        boletoEscaneado = null
        pasajeroNombre = null
        pasajeroDni = null
        updateUI()
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Aquí observarías el estado del viewmodel si lo necesitas
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (resultCode == RESULT_OK && data != null) {
            val qrContent = data.getStringExtra("qr_content") ?: return
            
            when (requestCode) {
                REQUEST_CODE_BOLETO -> {
                    // Procesar boleto escaneado
                    procesarBoleto(qrContent)
                }
                REQUEST_CODE_MARBETE -> {
                    // Procesar marbete escaneado
                    procesarMarbete(qrContent)
                }
            }
        }
    }

    private fun procesarBoleto(qrContent: String) {
        // Simular procesamiento de boleto
        // En la realidad, harías una llamada al servidor para verificar el boleto
        boletoEscaneado = qrContent
        pasajeroNombre = "Pasajero del boleto $qrContent" // Esto vendría del servidor
        pasajeroDni = "12345678" // Esto vendría del servidor
        
        updateUI()
        Toast.makeText(this, "Boleto válido: $qrContent", Toast.LENGTH_SHORT).show()
    }

    private fun procesarMarbete(qrContent: String) {
        // Aquí registrarías el equipaje con el boleto + marbete
        Toast.makeText(
            this, 
            "Marbete escaneado: $qrContent\nListo para registrar", 
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun updateUI() {
        if (boletoEscaneado != null) {
            binding.tvBoletoInfo.text = "Boleto: $boletoEscaneado\nPasajero: $pasajeroNombre"
            binding.tvBoletoInfo.visibility = android.view.View.VISIBLE
            binding.btnEscanearMarbete.isEnabled = true
            binding.btnEscanearMarbete.alpha = 1.0f
        } else {
            binding.tvBoletoInfo.visibility = android.view.View.GONE
            binding.btnEscanearMarbete.isEnabled = false
            binding.btnEscanearMarbete.alpha = 0.5f
        }
    }

    companion object {
        const val REQUEST_CODE_BOLETO = 1001
        const val REQUEST_CODE_MARBETE = 1002
    }
}