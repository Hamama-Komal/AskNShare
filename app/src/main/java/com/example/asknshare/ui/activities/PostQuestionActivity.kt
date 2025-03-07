package com.example.asknshare.ui.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.asknshare.R
import com.example.asknshare.databinding.ActivityPostQuestionBinding
import com.example.asknshare.ui.adapters.GallaryImageAdapter
import com.example.asknshare.ui.adapters.TagAdapter
import com.example.asknshare.ui.custom.CustomDialog
import com.example.asknshare.utils.Constants
import com.example.asknshare.viewmodels.TagViewModel
import com.github.drjacky.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.util.UUID


class PostQuestionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostQuestionBinding
    private lateinit var galleryAdapter: GallaryImageAdapter
    private var imageList = mutableListOf<Uri>()
    private lateinit var tagViewModel: TagViewModel
    private lateinit var tagAdapter: TagAdapter
    private var capturedImageUri: Uri? = null


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


        tagViewModel = ViewModelProvider(this)[TagViewModel::class.java]
        tagAdapter = TagAdapter(mutableListOf()) { tag -> tagViewModel.removeTag(tag) }
        binding.recyclerTags.adapter = tagAdapter
        binding.recyclerTags.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        setupRecyclerView()

        // Show Input Layout on Button Click
        binding.buttonTag.setOnClickListener {
            binding.tagInputLayout.visibility = View.VISIBLE
        }

        // Hide Input Layout
        binding.buttonCancel.setOnClickListener {
            binding.tagInputLayout.visibility = View.GONE
            binding.editTag.text.clear()
        }

        // Add Tag
        binding.buttonDone.setOnClickListener {
            val tagText = binding.editTag.text.toString().trim()
            if (tagText.isNotEmpty()) {
                tagViewModel.addTag(tagText)
                binding.editTag.text.clear()
                binding.tagInputLayout.visibility = View.GONE
            }
        }

        // Observe and Update UI
        tagViewModel.tags.observe(this) { tags ->
            tagAdapter = TagAdapter(tags.toMutableList()) { tagViewModel.removeTag(it) }
            binding.recyclerTags.adapter = tagAdapter
        }

        binding.questionDescription.setOnTouchListener { v, event ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            v.onTouchEvent(event)
            true
        }


        binding.buttnGallery.setOnClickListener {
            pickMultipleImages()
        }

        binding.buttonCamera.setOnClickListener {
            captureImageFromCamera()
        }


        binding.backButton.setOnClickListener {
            finish()
        }

        binding.buttonPublishPost.setOnClickListener {
            val title = binding.questionTitle.text.toString().trim()
            val body = binding.questionDescription.text.toString().trim()

            if (title.isEmpty() || body.isEmpty()) {
                Toast.makeText(this, "Title and description cannot be empty!", Toast.LENGTH_SHORT).show()
            }
            else{
                showLoading(true)
                uploadImagesToFirebase()
            }


        }


        binding.buttonDiscard.setOnClickListener{
            showDiscardDialog()
        }

    }

    private fun showDiscardDialog() {
        binding.buttonDiscard.setOnClickListener {
            CustomDialog(
                context = this,
                title = "Discard Post?",
                subtitle = "Are you sure you want to discard this post? All changes will be lost.",
                positiveButtonText = "Yes",
                negativeButtonText = "Cancel",
                onPositiveClick = {
                    clearAllData()
                },
                onNegativeClick = {

                }
            ).show()
        }

    }

    private fun clearAllData() {
        // Clear input fields
        binding.questionTitle.text.clear()
        binding.questionDescription.text.clear()

        // Clear selected images
        imageList.clear()
        galleryAdapter.setMultipleImages(imageList)
        binding.recylerGalleryImg.visibility = View.GONE



        // Hide tag input layout
        tagViewModel.clearTags()
        binding.tagInputLayout.visibility = View.GONE
    }


    private fun captureImageFromCamera() {
        val imageFile = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "${System.currentTimeMillis()}.jpg")

        capturedImageUri = FileProvider.getUriForFile(this, "${packageName}.provider", imageFile)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageUri)

        if (intent.resolveActivity(packageManager) != null) {
            imagePickerLauncher.launch(intent)
        } else {
            Toast.makeText(this, "No camera app found!", Toast.LENGTH_SHORT).show()
        }
    }


    // Image Picker Launcher (Handles both Gallery & Camera)
    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                capturedImageUri?.let { uri ->
                    imageList.add(uri)  // Add image to list
                    galleryAdapter.setMultipleImages(imageList)  // Update RecyclerView

                    // Show RecyclerView
                    binding.recylerGalleryImg.visibility = View.VISIBLE
                } ?: run {
                    Toast.makeText(this, "Error: Image not found", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Image capture failed!", Toast.LENGTH_SHORT).show()
            }
        }


    // Pick Multiple Images from Gallery
    private fun pickMultipleImages() {
        pickImagesLauncher.launch("image/*")
    }

    private val pickImagesLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri>? ->
        uris?.let {
            imageList.addAll(it) // ✅ Add multiple images from gallery
            galleryAdapter.setMultipleImages(imageList)

            // Show RecyclerView if images are selected
            if (imageList.isNotEmpty()) {
                Log.d("GalleryImages", "Images selected: $imageList")
                binding.recylerGalleryImg.visibility = View.VISIBLE
            }
        }
    }

    //  Initializes RecyclerView for displaying selected images
    private fun setupRecyclerView() {
        galleryAdapter = GallaryImageAdapter(imageList)
        binding.recylerGalleryImg.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recylerGalleryImg.adapter = galleryAdapter
        binding.recylerGalleryImg.visibility = View.GONE // Initially hidden
    }

    // 🔹 Upload Images to Firebase Storage
    private fun uploadImagesToFirebase() {
        if (imageList.isEmpty()) {
            savePostToDatabase(mapOf()) // Save post even if no images
            return
        }

        val storageRef = FirebaseStorage.getInstance().reference.child(Constants.POSTS_IMAGES_NODE)
        val imageUrls = mutableMapOf<String, String>()
        var uploadCount = 0

        for ((index, uri) in imageList.withIndex()) {
            val fileRef = storageRef.child("${UUID.randomUUID()}.jpg")
            fileRef.putFile(uri).addOnSuccessListener { taskSnapshot ->
                fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    imageUrls["img${index + 1}"] = downloadUri.toString()
                    uploadCount++

                    if (uploadCount == imageList.size) {
                        savePostToDatabase(imageUrls) // Save post after all images are uploaded
                    }
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Image upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 🔹 Save Post to Firebase Realtime Database
    private fun savePostToDatabase(imageUrls: Map<String, String>) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"
        val postId = FirebaseDatabase.getInstance().reference.child(Constants.POSTS_NODE).push().key ?: return

        val post = mapOf(
            Constants.POSTED_BY to userId,
            Constants.POST_HEADING to binding.questionTitle.text.toString().trim(),
            Constants.POST_BODY to binding.questionDescription.text.toString().trim(),
            Constants.POST_IMAGES to imageUrls,
            Constants.POST_TIME to System.currentTimeMillis(),
            Constants.POST_UP_VOTES to emptyMap<String, Boolean>(),
            Constants.POST_DOWN_VOTES to emptyMap<String, Boolean>(),
            Constants.POST_VIEWS to 0,
            Constants.POST_BOOKMARKS to emptyMap<String, Boolean>(),
            Constants.POST_REPLIES to emptyMap<String, Any>(),
            Constants.POST_TAGS to tagViewModel.tags.value.orEmpty()
        )

        FirebaseDatabase.getInstance().reference.child("Posts").child(postId).setValue(post)
            .addOnSuccessListener {
                showLoading(false)
                Toast.makeText(this, "Post uploaded successfully!", Toast.LENGTH_SHORT).show()
                finish() // Close activity
            }
            .addOnFailureListener {
                showLoading(false)
                Toast.makeText(this, "Failed to upload post!", Toast.LENGTH_SHORT).show()
            }
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