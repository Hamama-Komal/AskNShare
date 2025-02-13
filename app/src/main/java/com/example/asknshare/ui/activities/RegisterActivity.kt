package com.example.asknshare.ui.activities

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.asknshare.R
import com.example.asknshare.data.local.DataStoreHelper
import com.example.asknshare.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private var isPasswordVisible = false
    private lateinit var dataStoreHelper: DataStoreHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        dataStoreHelper = DataStoreHelper(this)

        binding.buttonRegister.setOnClickListener {
            registerUser()
        }

        binding.InputPasswordLayout.setEndIconOnClickListener {
            togglePasswordVisibility()
        }

        binding.textViewLogin.setOnClickListener {
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }



    }

    private fun registerUser() {
        val username = binding.textfieldUsername.text.toString().trim()
        val email = binding.textfieldEmail.text.toString().trim()
        val password = binding.textfieldPassword.text.toString().trim()
        val isTermsChecked = binding.checkboxTerms.isChecked

        // Validate input fields
        if (!validateInput(username, email, password, isTermsChecked)) {
            return
        }

        showLoading(true)

        // Register user in Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            showLoading(false)
            if (task.isSuccessful) {
                handleRegistrationSuccess(email, username)
            } else {
                handleRegistrationFailure(task.exception?.message)
            }
        }
    }

    private fun validateInput(
        username: String,
        email: String,
        password: String,
        isTermsChecked: Boolean
    ): Boolean {
        if (!isValidUsername(username)) {
            binding.textfieldUsername.error = "Username must contain only letters and numbers"
            return false
        }
        if (!isValidEmail(email)) {
            binding.textfieldEmail.error = "Enter a valid email"
            return false
        }
        if (!isValidPassword(password)) {
            binding.textfieldPassword.error = "Password must be at least 7 characters long"
            return false
        }
        if (!isTermsChecked) {
            Toast.makeText(this, "You must agree to the Terms and Conditions", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun handleRegistrationSuccess(email: String, username: String) {
        lifecycleScope.launch {
            // Save registration status, email, and username
            dataStoreHelper.saveUserRegistrationStatus(true)
            dataStoreHelper.saveUserEmail(email)
            dataStoreHelper.saveUsername(username)
        }
        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()

        // Navigate to the next activity without passing data via Intent
        val intent = Intent(this@RegisterActivity, SetUpProfileActivity::class.java)
        startActivity(intent)
        finishAffinity()
    }

    private fun handleRegistrationFailure(errorMessage: String?) {
        Toast.makeText(
            this, "Registration failed: $errorMessage", Toast.LENGTH_LONG
        ).show()
    }

    private fun isValidUsername(username: String): Boolean {
        val regex = "^[a-zA-Z0-9]+$".toRegex()
        return username.matches(regex)
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 7
    }

    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            binding.textfieldPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.InputPasswordLayout.endIconDrawable = getDrawable(R.drawable.ic_visible)
        } else {
            binding.textfieldPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.InputPasswordLayout.endIconDrawable = getDrawable(R.drawable.ic_hide)
        }
        isPasswordVisible = !isPasswordVisible
        binding.textfieldPassword.setSelection(binding.textfieldPassword.text?.length ?: 0)

        // Log the current state for debugging
        println("Password visibility toggled. isPasswordVisible: $isPasswordVisible")
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.spinKit.visibility = View.VISIBLE
            binding.buttonRegister.isEnabled = false
            binding.textfieldEmail.isEnabled = false
            binding.textfieldUsername.isEnabled = false
            binding.textfieldPassword.isEnabled = false
            binding.checkboxTerms.isEnabled = false
        } else {
            binding.spinKit.visibility = View.GONE
            binding.buttonRegister.isEnabled = true
            binding.textfieldEmail.isEnabled = true
            binding.textfieldUsername.isEnabled = true
            binding.textfieldPassword.isEnabled = true
            binding.checkboxTerms.isEnabled = true
        }
    }
}
