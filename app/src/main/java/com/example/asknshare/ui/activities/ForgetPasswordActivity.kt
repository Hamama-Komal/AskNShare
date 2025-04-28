package com.example.asknshare.ui.activities

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.asknshare.R
import com.example.asknshare.databinding.ActivityForgetPasswordBinding
import com.example.asknshare.viewmodels.ForgotPasswordUiState
import com.example.asknshare.viewmodels.ForgotPasswordViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class ForgetPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgetPasswordBinding
    private val viewModel: ForgotPasswordViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityForgetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {

        binding.resetPasswordButton.setOnClickListener {
            binding.resetPasswordButton.isEnabled = false
            val email = binding.emailEditText.text.toString().trim()
            viewModel.resetPassword(email)
        }

        binding.textViewLogin.setOnClickListener {
            finish()
        }
    }

    private fun observeViewModel() {

        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is ForgotPasswordUiState.Loading -> showLoading(true)
                    is ForgotPasswordUiState.Success -> {
                        showLoading(false)
                        showSuccess(state.email)
                    }

                    is ForgotPasswordUiState.Error -> {
                        showLoading(false)
                        showError(state.message)
                    }

                    is ForgotPasswordUiState.ValidationError -> {
                        showLoading(false)
                        binding.emailInputLayout.error = state.message
                    }

                    is ForgotPasswordUiState.Idle -> showLoading(false)
                }
            }
        }
    }

    private fun showLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.resetPasswordButton.isEnabled = !loading
    }

    private fun showSuccess(email: String) {
        binding.statusText.visibility = View.VISIBLE
        binding.statusText.text = getString(R.string.password_reset_email_sent, email)
        binding.emailInputLayout.error = null
        // Navigate back to login after 2 seconds
        binding.root.postDelayed({
          finish()
        }, 2000)
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
        binding.emailInputLayout.error = null
    }
}