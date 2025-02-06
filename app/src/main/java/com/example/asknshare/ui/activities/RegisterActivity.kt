package com.example.asknshare.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.asknshare.R
import com.example.asknshare.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

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

        binding.buttonRegister.setOnClickListener {
           // registerUser()
            val intent = Intent(this@RegisterActivity, SetUpProfileActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }

    }

    private fun registerUser() {
        val username = binding.textfieldUsername.text.toString().trim()
        val email = binding.textfieldEmail.text.toString().trim()
        val password = binding.textfieldPassword.text.toString().trim()
        val isTermsChecked = binding.checkboxTerms.isChecked

        if (!isValidUsername(username)) {
            binding.textfieldUsername.error = "Username must contain only letters and numbers"
            return
        }

        if (!isValidEmail(email)) {
            binding.textfieldEmail.error = "Enter a valid email"
            return
        }

        if (!isValidPassword(password)) {
            binding.textfieldPassword.error = "Password must be at least 7 characters long"
            return
        }

        if (!isTermsChecked) {
            Toast.makeText(this, "You must agree to the Terms and Conditions", Toast.LENGTH_SHORT).show()
            return
        }

        // Register user in Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@RegisterActivity, SetUpProfileActivity::class.java)
                    startActivity(intent)
                    finishAffinity()
                } else {
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
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
}
