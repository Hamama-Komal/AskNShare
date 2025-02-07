package com.example.asknshare.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.transition.Visibility
import android.view.View
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
import com.example.asknshare.utils.Constants
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.bottomsheet.BottomSheetDialog

class SetUpProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetUpProfileBinding
    private lateinit var adapter: ProfileViewPagerAdapter


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


        setupViewPager()
        // setupButtonListeners()

        // Handle profile picture upload click
        binding.cardUploadPic.setOnClickListener {
            showImagePickerBottomSheet()
        }

        // Save the data in Firebase
        binding.buttonDone.setOnClickListener {

        }

    }

    private fun showImagePickerBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_image_picker, null)
        bottomSheetDialog.setContentView(view)

        // Handle gallery option
        view.findViewById<View>(R.id.tv_gallery)?.setOnClickListener {
            pickImageFromGallery()
            bottomSheetDialog.dismiss()
        }

        // Handle camera option
        view.findViewById<View>(R.id.tv_camera)?.setOnClickListener {
            captureImageFromCamera()
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun pickImageFromGallery() {
        ImagePicker.with(this)
            .galleryOnly()
            .crop()
            .compress(1024)
            .start()
    }

    private fun captureImageFromCamera() {
        ImagePicker.with(this)
            .cameraOnly()
            .crop()
            .compress(1024)
            .start()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && data != null) {
            val imageUri: Uri? = data.data // Get the image URI

            if (imageUri != null) {
                binding.profilePicHolder.setImageURI(imageUri)
                Toast.makeText(this, "Image Uploaded!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Error: Image URI is null", Toast.LENGTH_SHORT).show()
            }
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        }
    }


    private fun setupViewPager() {
        adapter = ProfileViewPagerAdapter(this)
        binding.viewpager.adapter = adapter

        // Update page number dynamically
        binding.viewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updatePageNumber(position)
               // updateButtons(position)
            }
        })

        binding.dotsIndicator.attachTo(binding.viewpager)
    }

    private fun updatePageNumber(position: Int) {
        binding.textViewPageNumber.text = "${position + 1}"
    }

}