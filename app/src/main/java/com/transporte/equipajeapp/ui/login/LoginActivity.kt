package com.transporte.equipajeapp.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.transporte.equipajeapp.databinding.ActivityLoginBinding
import com.transporte.equipajeapp.domain.model.Usuario
import com.transporte.equipajeapp.ui.dashboard.DashboardActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        observeState()
    }

    private fun setupViews() {
        binding.btnLogin.setOnClickListener {
            val interno = binding.etInterno.text.toString().trim()
            // La contraseña es opcional para pruebas con mocks
            val password = binding.etPassword.text.toString().trim()
            viewModel.login(interno, password)
        }

        binding.etInterno.doAfterTextChanged {
            binding.tilInterno.error = null
        }

        binding.etPassword.doAfterTextChanged {
            binding.tilPassword.error = null
        }
        
        // Ocultar el campo de contraseña para simplificar (opcional)
        // binding.tilPassword.visibility = View.GONE
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        is LoginState.Idle -> {
                            binding.progressBar.visibility = View.GONE
                            binding.btnLogin.isEnabled = true
                        }
                        is LoginState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.btnLogin.isEnabled = false
                        }
                        is LoginState.Success -> {
                            navigateToDashboard(state.usuario)
                        }
                        is LoginState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            binding.btnLogin.isEnabled = true
                            Toast.makeText(this@LoginActivity, state.message, Toast.LENGTH_SHORT).show()
                            viewModel.resetState()
                        }
                    }
                }
            }
        }
    }

    private fun navigateToDashboard(usuario: Usuario) {
        val intent = Intent(this, DashboardActivity::class.java).apply {
            putExtra("user_id", usuario.id)
            putExtra("interno", usuario.interno)
            putExtra("nombre", usuario.nombre)
            putExtra("empresa", usuario.empresa)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}
