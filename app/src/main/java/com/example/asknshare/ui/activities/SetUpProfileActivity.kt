package com.example.asknshare.ui.activities

import android.os.Bundle
import android.transition.Visibility
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.asknshare.R
import com.example.asknshare.ui.adapters.ProfileViewPagerAdapter
import com.example.asknshare.databinding.ActivityRegisterBinding
import com.example.asknshare.databinding.ActivitySetUpProfileBinding

class SetUpProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetUpProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySetUpProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val adapter = ProfileViewPagerAdapter(this)
        binding.viewpager.adapter = adapter

        // Handle page change
        binding.viewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateButtons(position)
            }
        })

        // Handle Next Button Click
        binding.buttonNext.setOnClickListener {
            if (binding.viewpager.currentItem == 0) {
                binding.viewpager.currentItem = 1 // Move to Second Fragment
            } else {
                Toast.makeText(this, "Done!", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle Skip/Back Button Click
        binding.buttonSkip.setOnClickListener {
            if (binding.viewpager.currentItem == 1) {
                binding.viewpager.currentItem = 0 // Move back to First Fragment
            } else {
                // Toast.makeText(this, "Skipped!", Toast.LENGTH_SHORT).show()
             // binding.buttonSkip.visibility =
            }
        }

       // setupClickListeners()
    }

    // Update button text dynamically
    private fun updateButtons(position: Int) {
        if (position == 0) {
            binding.buttonSkip.text = "Skip"
            binding.buttonNext.text = "Next"
        } else {
            binding.buttonSkip.text = "Back"
            binding.buttonNext.text = "Done"
        }
    }


    private fun setupClickListeners() {
        binding.buttonNext.setOnClickListener {
            // Handle Next button click
        }

        binding.buttonSkip.setOnClickListener {
            // Handle Skip button click
        }

        binding.cardUploadPic.setOnClickListener {
            // Handle profile picture upload click
        }
    }
}