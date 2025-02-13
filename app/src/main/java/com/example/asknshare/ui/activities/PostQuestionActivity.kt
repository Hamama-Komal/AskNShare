package com.example.asknshare.ui.activities

import android.app.Activity
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.asknshare.R
import com.example.asknshare.databinding.ActivityPostQuestionBinding
import com.example.asknshare.ui.adapters.GallaryImageAdapter
import com.example.asknshare.viewmodels.TagsViewModel
import com.github.drjacky.imagepicker.ImagePicker
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup


class PostQuestionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostQuestionBinding
    private lateinit var galleryAdapter: GallaryImageAdapter
    private var imageList = mutableListOf<Uri>()
    private val tagsViewModel : TagsViewModel by viewModels()

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
        binding.tagsChip.visibility = View.GONE
       setupRecyclerView()

        binding.buttnGallery.setOnClickListener {
            binding.tagsChip.visibility = View.GONE
            pickMultipleImages()
        }

        binding.buttonCamera.setOnClickListener {
            binding.tagsChip.visibility = View.GONE
            captureImageFromCamera()
        }

// Tags Button
        binding.buttonTag.setOnClickListener {
            val tags = listOf("Android Development", "Kotlin", "Java", "Python", "JavaScript",
                "TypeScript", "Swift", "Flutter", "React Native", "Node.js",
                "HTML", "CSS", "Tailwind CSS", "Bootstrap", "React.js",
                "Next.js", "Angular", "Vue.js", "Svelte", "WebAssembly",
                "Firebase", "MongoDB", "MySQL", "PostgreSQL", "SQLite",
                "Redis", "GraphQL", "REST API", "Supabase", "AWS",
                "Git", "GitHub", "Docker", "Kubernetes", "CI/CD",
                "Jenkins", "Microservices", "Serverless", "Agile", "Cloud Computing",
                "Artificial Intelligence", "Machine Learning", "Deep Learning", "NLP",
                "OpenAI", "Cybersecurity", "Ethical Hacking", "Blockchain", "IoT",
                "Quantum Computing")
            addTagsToChipGroup(tags)
            binding.tagsChip.visibility = View.VISIBLE
        }
        observeSelectedTags()
    }

//Handle the Chip Functionallity
    fun addTagsToChipGroup(tags: List<String>) {
        val chipGroup: ChipGroup = binding.tagsChip
        chipGroup.removeAllViews()

        for (tag in tags){
            val chip = layoutInflater.inflate(R.layout.item_chip, chipGroup, false) as Chip
            chip.text = tag
            chip.isCheckable = true
            chip.isClickable = true
            chip.setTextColor(Color.BLACK)

            chip.isChecked = tagsViewModel.isTagSelected(tag)
            updateChipStyle(chip,chip.isChecked)

            chip.setOnCheckedChangeListener { _, isChecked ->
                tagsViewModel.toggleTag(tag) // Update ViewModel
                updateChipStyle(chip, isChecked)
            }
            chipGroup.addView(chip)
        }
    }

    // Handle chip selection (color change)
    private fun updateChipStyle(chip: Chip, isChecked: Boolean) {
        if (isChecked) {
            chip.setTextColor(Color.WHITE)
            chip.chipBackgroundColor = ContextCompat.getColorStateList(this, R.color.app_dark_blue)
        } else {
            chip.setTextColor(Color.BLACK)
            chip.chipBackgroundColor = ContextCompat.getColorStateList(this, R.color.app_light_blue)
        }
    }

    private fun observeSelectedTags() {
        tagsViewModel.selectedTags.observe(this) { selectedTags ->
            for (i in 0 until binding.tagsChip.childCount) {
                val chip = binding.tagsChip.getChildAt(i) as Chip
                chip.isChecked = selectedTags.contains(chip.text.toString())
                updateChipStyle(chip, chip.isChecked)
            }
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