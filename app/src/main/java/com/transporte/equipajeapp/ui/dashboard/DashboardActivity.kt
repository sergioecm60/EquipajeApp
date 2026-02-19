package com.transporte.equipajeapp.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.transporte.equipajeapp.R
import com.transporte.equipajeapp.databinding.ActivityDashboardBinding
import com.transporte.equipajeapp.data.local.PreferencesManager
import com.transporte.equipajeapp.domain.model.Usuario
import com.transporte.equipajeapp.ui.login.LoginActivity
import com.transporte.equipajeapp.ui.servicio.ServicioDetalleActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var adapter: ServicioAdapter
    
    @Inject
    lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        observeState()

        // Obtener usuario del Intent PRIMERO (datos del login)
        val userId = intent.getIntExtra("user_id", 0)
        val interno = intent.getStringExtra("interno") ?: ""
        val nombre = intent.getStringExtra("nombre") ?: ""
        val empresa = intent.getStringExtra("empresa") ?: ""
        
        android.util.Log.d("DashboardActivity", "Intent - ID: $userId, Interno: '$interno', Nombre: '$nombre'")
        
        if (interno.isNotEmpty()) {
            // Usar datos del Intent
            val usuario = Usuario(id = userId, interno = interno, nombre = nombre, empresa = empresa)
            android.util.Log.d("DashboardActivity", "Usando usuario del Intent: $interno")
            
            // Guardar en SharedPreferences para futuras visitas
            lifecycleScope.launch {
                preferencesManager.saveUser(userId, interno, nombre, empresa, "token")
            }
            
            viewModel.loadData(usuario)
        } else {
            // Si el Intent está vacío, leer de SharedPreferences
            lifecycleScope.launch {
                val prefs = preferencesManager.getUser()
                if (prefs != null) {
                    val usuario = Usuario(
                        id = prefs.id,
                        interno = prefs.interno,
                        nombre = prefs.nombre,
                        empresa = prefs.empresa
                    )
                    android.util.Log.d("DashboardActivity", "Usuario desde prefs: ${usuario.interno}")
                    viewModel.loadData(usuario)
                } else {
                    android.util.Log.e("DashboardActivity", "No hay usuario, volviendo al login")
                    startActivity(Intent(this@DashboardActivity, LoginActivity::class.java))
                    finish()
                }
            }
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_logout -> {
                    viewModel.logout()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = ServicioAdapter { servicio ->
            viewModel.selectServicio(servicio)
            // Abrir pantalla de detalle del servicio
            val intent = Intent(this, ServicioDetalleActivity::class.java).apply {
                putExtra("servicio_id", servicio.id)
                putExtra("origen", servicio.origen)
                putExtra("destino", servicio.destino)
                putExtra("hora_salida", servicio.horaSalida)
                putExtra("hora_llegada", servicio.horaLlegada)
                putExtra("interno", servicio.interno)
                putExtra("chofer", servicio.empresa) // El chofer viene en empresa en nuestro mock
            }
            startActivity(intent)
        }
        binding.rvServicios.layoutManager = LinearLayoutManager(this)
        binding.rvServicios.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.btnLogout.setOnClickListener {
            viewModel.logout()
        }

        binding.btnRegistrarEquipaje.setOnClickListener {
            Toast.makeText(this, "Seleccione un servicio de la lista para registrar equipaje", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        is DashboardUiState.Idle -> {}
                        is DashboardUiState.Loading -> {}
                        is DashboardUiState.Success -> {
                            updateUI(state)
                        }
                        is DashboardUiState.Error -> {
                            Toast.makeText(this@DashboardActivity, state.message, Toast.LENGTH_SHORT).show()
                        }
                        is DashboardUiState.LoggedOut -> {
                            // Limpiar datos y volver al login
                            lifecycleScope.launch {
                                preferencesManager.clearUser()
                                preferencesManager.clearCredentials()
                            }
                            val intent = Intent(this@DashboardActivity, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                    }
                }
            }
        }
    }

    private fun updateUI(state: DashboardUiState.Success) {
        binding.tvInterno.text = "Interno: ${state.usuario.interno} - ${state.usuario.nombre}"

        if (state.servicioActual != null) {
            binding.cardServicioActual.visibility = View.VISIBLE
            binding.tvServicioActual.text = "${state.servicioActual.origen} - ${state.servicioActual.destino}"
        } else {
            binding.cardServicioActual.visibility = View.GONE
        }

        if (state.servicios.isEmpty()) {
            binding.tvNoServicios.visibility = View.VISIBLE
            binding.rvServicios.visibility = View.GONE
        } else {
            binding.tvNoServicios.visibility = View.GONE
            binding.rvServicios.visibility = View.VISIBLE
            adapter.submitList(state.servicios)
        }
    }

    override fun onResume() {
        super.onResume()
        val currentState = viewModel.state.value
        if (currentState is DashboardUiState.Success) {
            viewModel.loadData(currentState.usuario)
        }
    }
}
