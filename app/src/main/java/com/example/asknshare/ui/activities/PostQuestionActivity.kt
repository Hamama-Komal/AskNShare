package com.example.asknshare.ui.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.asknshare.R
import com.example.asknshare.databinding.ActivityPostQuestionBinding
import com.example.asknshare.ui.adapters.GallaryImageAdapter


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
            openGallery()
        }
    }

    private fun openGallery() {
        pickMultipleImages()
    }

  // Initializes RecyclerView for displaying selected images
    private fun setupRecyclerView() {
        galleryAdapter = GallaryImageAdapter(imageList)
        binding.recylerGalleryImg.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recylerGalleryImg.adapter = galleryAdapter
        binding.recylerGalleryImg.visibility = View.GONE // Initially hidden
    }

    // Handles selected images and updates RecyclerView
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

    private fun pickMultipleImages() {
        pickImagesLauncher.launch("image/*")
    }

}