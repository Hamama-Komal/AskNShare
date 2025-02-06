package com.example.asknshare.ui.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.example.asknshare.R
import com.example.asknshare.data.local.DataStoreHelper
import com.example.asknshare.databinding.ActivitySplashBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var dataStoreHelper: DataStoreHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        dataStoreHelper = DataStoreHelper(this)

        // Apply Animation to Logo
        YoYo.with(Techniques.FadeIn)
            .duration(1500)
            .playOn(binding.logo)

        // Check Registration Status and Navigate
        checkUserStatus()
    }

    private fun checkUserStatus() {

        lifecycleScope.launch {
            val isUserRegistered = dataStoreHelper.isUserRegistered.first()

            val nextActivity = when {
                auth.currentUser != null -> MainActivity::class.java
                isUserRegistered -> MainActivity::class.java
                else -> WelcomeActivity::class.java
            }

            startActivity(Intent(this@SplashActivity, nextActivity))
            finish()
        }
    }
}