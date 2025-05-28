package com.example.asknshare.ui.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.asknshare.R
import com.example.asknshare.data.local.DataStoreHelper
import com.example.asknshare.databinding.ActivitySettingBinding
import com.example.asknshare.utils.Constants
import com.google.firebase.BuildConfig
import kotlinx.coroutines.launch

class SettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingBinding
    private lateinit var dataStoreHelper: DataStoreHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dataStoreHelper = DataStoreHelper(this)

        initDarkModeToggle()
        initNotificationsToggle()
        initChangePassword()
        initRateUs()
        initAboutDialog()

    }


    private fun initDarkModeToggle() {
        // Disable the switch and show toast when clicked
        binding.switchDarkMode.isEnabled = false
        binding.switchDarkMode.isChecked = false

        binding.darkMode.setOnClickListener {
            Toast.makeText(
                this,
                "Dark Mode not available",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Optional: Change appearance to look disabled
        binding.switchDarkMode.alpha = 0.5f
    }

    private fun initNotificationsToggle() {
        // Observe notifications preference
        lifecycleScope.launch {
            dataStoreHelper.getBooleanPreference(Constants.KEY_NOTIFICATIONS, true)
                .collect { enabled ->
                    binding.switchNotifications.isChecked = enabled
                }
        }

        binding.switchNotifications.setOnCheckedChangeListener { _, checked ->
            lifecycleScope.launch {
                dataStoreHelper.saveBooleanPreference(Constants.KEY_NOTIFICATIONS, checked)
            }

            Toast.makeText(
                this,
                if (checked) "Notifications enabled" else "Notifications disabled",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun initChangePassword() {
        binding.itemChangePassword.setOnClickListener {
            val intent = Intent(this, ForgetPasswordActivity::class.java)
            intent.putExtra(Constants.RESET_PASSWORD_KEY, true)
            startActivity(intent)
        }
    }

    private fun initRateUs() {
        binding.itemRateUs.setOnClickListener {
            val uri = Uri.parse("market://details?id=$packageName")
            val goToMarket = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP
                )
            }
            try {
                startActivity(goToMarket)
            } catch (e: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                    )
                )
            }
        }
    }

    private fun initAboutDialog() {
        binding.itemAbout.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("About")
                .setMessage("Version: ${BuildConfig.VERSION_NAME}\n\nDeveloped by Hamama Komal & Zain Ali")
                .setPositiveButton("OK", null)
                .show()
        }
    }
}