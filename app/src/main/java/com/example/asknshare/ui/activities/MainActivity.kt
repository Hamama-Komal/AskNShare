package com.example.asknshare.ui.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.asknshare.R
import com.example.asknshare.databinding.ActivityMainBinding
import com.example.asknshare.repo.NetworkMonitor
import com.example.asknshare.ui.custom.CustomDialog
import com.example.asknshare.ui.fragments.AskFragment
import com.example.asknshare.ui.fragments.HomeFragment
import com.example.asknshare.ui.fragments.ProfileFragment
import com.example.asknshare.ui.fragments.SearchFragment
import me.ibrahimsn.lib.SmoothBottomBar
import showCustomToast

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var networkMonitor: NetworkMonitor
    private var hasShownNetworkToast = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set Status Bar Color to Light Blue
        window.statusBarColor = ContextCompat.getColor(this, R.color.app_light_blue)

        // Initialize the network monitor
        networkMonitor = NetworkMonitor(this)

        // Load default fragment (HomeFragment)
        loadFragment(HomeFragment())

        val bottomBar: SmoothBottomBar = findViewById(R.id.bottomBar)


        bottomBar.onItemSelected = { position ->
            val fragment = when (position) {
                0 -> HomeFragment()
                1 -> AskFragment()
                2 -> SearchFragment()
                3 -> ProfileFragment()
                else -> null
            }

            fragment?.let { loadFragment(it) }

        }


        // onBackPress function
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitConfirmationDialog()
            }
        })



        // Network Monitoring
        networkMonitor = NetworkMonitor(this)
        networkMonitor.startMonitoring()

        lifecycleScope.launchWhenStarted {
            networkMonitor.networkStatus.collect { status ->
                if (!hasShownNetworkToast) {
                    val (msg, icon) = when (status) {
                        is NetworkMonitor.NetworkStatus.Disconnected ->
                            "No internet connection" to R.drawable.ic_no_internet

                        is NetworkMonitor.NetworkStatus.Connected ->
                            when (status.quality) {
                                NetworkMonitor.ConnectionQuality.Slow     -> "Slow connection detected"      to R.drawable.ic_warning
                                NetworkMonitor.ConnectionQuality.Moderate -> "Moderate connection (mobile)" to R.drawable.ic_warning
                                else                                     -> "Connected!"                   to R.drawable.ic_check_connection
                            }
                        else -> return@collect
                    }

                    showCustomToast(this@MainActivity, msg, icon)
                    hasShownNetworkToast = true
                }
            }
        }

    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }


    private fun showExitConfirmationDialog() {
        CustomDialog(
            context = this,
            title = "Exit App",
            subtitle = "Are you sure you want to exit?",
            positiveButtonText = "Yes",
            negativeButtonText = "No",
            onPositiveClick = {
                finishAffinity()
            },
            onNegativeClick = {
            }
        ).show()
    }


}