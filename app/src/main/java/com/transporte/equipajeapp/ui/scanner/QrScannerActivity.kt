package com.transporte.equipajeapp.ui.scanner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.transporte.equipajeapp.databinding.ActivityQrScannerBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@AndroidEntryPoint
class QrScannerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQrScannerBinding
    private val viewModel: QrScannerViewModel by viewModels()
    private lateinit var cameraExecutor: ExecutorService
    
    private var modoSoloLectura: Boolean = false
    private var qrDetectado: String? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(this, "Permiso de camara requerido", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Verificar si estamos en modo solo lectura (para escanear boleto/marbete)
        modoSoloLectura = intent.getBooleanExtra("modo_solo_lectura", false)

        cameraExecutor = Executors.newSingleThreadExecutor()

        binding.btnClose.setOnClickListener {
            finish()
        }

        observeState()

        if (hasCameraPermission()) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, QrCodeAnalyzer { qrContent ->
                        runOnUiThread {
                            viewModel.onQrScanned(qrContent)
                        }
                    })
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )
            } catch (e: Exception) {
                Toast.makeText(this, "Error al iniciar camara", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        is QrScannerState.Idle -> {}
                        is QrScannerState.Scanning -> {
                            vibrate()
                            // En modo solo lectura, guardamos el QR detectado
                            if (modoSoloLectura && qrDetectado == null) {
                                qrDetectado = state.toString()
                            }
                        }
                        is QrScannerState.QrDetectado -> {
                            if (modoSoloLectura) {
                                // Devolver el resultado inmediatamente
                                val resultIntent = android.content.Intent()
                                resultIntent.putExtra("qr_content", state.boleto.dni) // Usamos el DNI como identificador
                                setResult(RESULT_OK, resultIntent)
                                finish()
                            } else {
                                showConfirmationDialog(state.boleto.pasajero, state.equipaje.codigo)
                            }
                        }
                        is QrScannerState.Associating -> {}
                        is QrScannerState.Success -> {
                            showSuccessDialog()
                        }
                        is QrScannerState.Error -> {
                            if (modoSoloLectura) {
                                // En modo solo lectura, ignoramos errores y seguimos escaneando
                                viewModel.continueScanning()
                            } else {
                                Toast.makeText(this@QrScannerActivity, state.message, Toast.LENGTH_SHORT).show()
                                viewModel.continueScanning()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showConfirmationDialog(pasajero: String, equipajeCodigo: String) {
        AlertDialog.Builder(this)
            .setTitle("Equipaje Detectado")
            .setMessage("Pasajero: $pasajero\nCodigo: $equipajeCodigo\n\nDesea asociar este equipaje?")
            .setPositiveButton(" Asociar") { _, _ ->
                viewModel.asociarEquipaje()
            }
            .setNegativeButton("Cancelar") { _, _ ->
                viewModel.continueScanning()
            }
            .setCancelable(false)
            .show()
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("Equipaje Asociado")
            .setMessage("El equipaje fue asociado correctamente al pasaje")
            .setPositiveButton("OK") { _, _ ->
                viewModel.continueScanning()
            }
            .show()
    }

    private fun vibrate() {
        val vibrator = getSystemService(Vibrator::class.java)
        vibrator?.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private class QrCodeAnalyzer(
        private val onQrCodeDetected: (String) -> Unit
    ) : ImageAnalysis.Analyzer {

        private val scanner = BarcodeScanning.getClient()

        @androidx.camera.core.ExperimentalGetImage
        override fun analyze(imageProxy: androidx.camera.core.ImageProxy) {
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(
                    mediaImage,
                    imageProxy.imageInfo.rotationDegrees
                )
                scanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        for (barcode in barcodes) {
                            if (barcode.valueType == Barcode.TYPE_TEXT ||
                                barcode.valueType == Barcode.TYPE_TEXT) {
                                barcode.rawValue?.let { onQrCodeDetected(it) }
                                break
                            }
                        }
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            } else {
                imageProxy.close()
            }
        }
    }
}
