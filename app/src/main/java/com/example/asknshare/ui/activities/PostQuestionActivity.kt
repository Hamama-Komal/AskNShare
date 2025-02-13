package com.example.asknshare.ui.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle

import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.asknshare.R
import com.example.asknshare.databinding.ActivityPostQuestionBinding
import com.example.asknshare.ui.adapters.GallaryImageAdapter
import com.github.drjacky.imagepicker.ImagePicker


class PostQuestionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostQuestionBinding
    private lateinit var galleryAdapter: GallaryImageAdapter
    private var imageList = mutableListOf<Uri>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPostQuestionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        binding.questionDescription.setOnTouchListener { v, event ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            v.onTouchEvent(event)
            true
        }


       setupRecyclerView()

        binding.buttnGallery.setOnClickListener {
            pickMultipleImages()
        }

        binding.buttonCamera.setOnClickListener {
            captureImageFromCamera()
        }
    }

// Image Capture by the Camera
    private fun captureImageFromCamera() {
        val intent = ImagePicker.with(this)
            .cameraOnly()  // Open only the camera
            .crop()  // Enable cropping
            .createIntent()

        imagePickerLauncher.launch(intent)
    }

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri: Uri? = result.data?.data
                if (uri != null) {
                   imageList.add(uri) // Set the selected image
                    galleryAdapter.setSingleImage(uri) // Update RecyclerView with the selected image
                    // Show RecyclerView if images are selected
                    if (imageList.isNotEmpty()) {
                        binding.recylerGalleryImg.visibility = View.VISIBLE
                    }
                } else {
                    Toast.makeText(this, "Error: Image not found", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Image selection failed!", Toast.LENGTH_SHORT).show()
            }
        }

 // Images Selections from the gallery
    private fun pickMultipleImages() {
        pickImagesLauncher.launch("image/*")
    }

    private val pickImagesLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri>? ->
        uris?.let {
            imageList.clear() // Clear old selection
            imageList.addAll(it)
            galleryAdapter.setMultipleImages(imageList)

            // Show RecyclerView if images are selected
            if (imageList.isNotEmpty()) {
                binding.recylerGalleryImg.visibility = View.VISIBLE
            }
        }
    }


    // Initializes RecyclerView for displaying selected images
    private fun setupRecyclerView() {
        galleryAdapter = GallaryImageAdapter(imageList)
        binding.recylerGalleryImg.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recylerGalleryImg.adapter = galleryAdapter
        binding.recylerGalleryImg.visibility = View.GONE // Initially hidden
    }

}