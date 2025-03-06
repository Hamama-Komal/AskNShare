package com.example.asknshare.ui.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.asknshare.R
import com.example.asknshare.databinding.ActivityMainBinding
import com.example.asknshare.repo.NetworkMonitor
import com.example.asknshare.ui.custom.showCustomToast
import com.example.asknshare.ui.fragments.AskFragment
import com.example.asknshare.ui.fragments.HomeFragment
import com.example.asknshare.ui.fragments.ProfileFragment
import com.example.asknshare.ui.fragments.SearchFragment
import me.ibrahimsn.lib.SmoothBottomBar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var networkMonitor: NetworkMonitor

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

        // Start monitoring the network
        networkMonitor.startMonitoring {
            showCustomToast(this, "Slow or Poor Internet Connection")
        }

    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }




}