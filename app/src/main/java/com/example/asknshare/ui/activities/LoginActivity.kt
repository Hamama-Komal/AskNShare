package com.example.asknshare.ui.activities

import android.annotation.SuppressLint
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
import com.example.asknshare.R
import com.example.asknshare.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        auth = FirebaseAuth.getInstance()



        binding.InputPasswordLayout.setEndIconOnClickListener {
            togglePasswordVisibility()
        }

        binding.buttonLogin.setOnClickListener {
            loginUser()
        }

        binding.textViewRegister.setOnClickListener {
            navigateToRegisterScreen()
        }
    }

    private fun navigateToRegisterScreen() {
        val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
        startActivity(intent)
        finishAffinity()
    }

    private fun loginUser() {

        val email = binding.textfieldEmail.text.toString().trim()
        val password = binding.textfieldPassword.text.toString().trim()


        if (!isValidEmail(email)) {
            binding.textfieldEmail.error = "Enter a valid email"
            return
        }

        if (!isValidPassword(password)) {
            binding.textfieldPassword.error = "Password must be at least 7 characters long"
            return
        }


        showLoading(true)

        // Authenticate user with Firebase
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                showLoading(false)
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finishAffinity()
                } else {
                    Toast.makeText(
                        this,
                        "Login failed: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
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
       // println("Password visibility toggled. isPasswordVisible: $isPasswordVisible")
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.spinKit.visibility = View.VISIBLE
            // Disable user interaction
            window.setFlags(
                android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )
        } else {
            binding.spinKit.visibility = View.GONE
            // Enable user interaction
            window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
    }
}