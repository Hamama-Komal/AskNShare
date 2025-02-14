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
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.asknshare.R
import com.example.asknshare.ui.adapters.ProfileViewPagerAdapter
import com.example.asknshare.databinding.ActivitySetUpProfileBinding
import com.example.asknshare.utils.Constants
import com.example.asknshare.viewmodels.ProfileSetUpViewModel
import com.github.drjacky.imagepicker.ImagePicker
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class SetUpProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetUpProfileBinding
    private lateinit var adapter: ProfileViewPagerAdapter
    private var profileImageUrl: String? = null
    private val profileSetupViewModel: ProfileSetUpViewModel by viewModels()


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


        // Setting up fragments
        setupViewPager()

        // Handle profile picture upload click
        binding.cardUploadPic.setOnClickListener {
            showImagePickerBottomSheet()
        }

        // Save the data in Firebase
        binding.buttonDone.setOnClickListener {
            showLoading(true)
            saveUserDataToFirebase()
        }

    }

    private fun saveUserDataToFirebase() {
        // Retrieve data from ViewModel
        val username = profileSetupViewModel.username.value?.trim() ?: ""
        val fullName = profileSetupViewModel.fullName.value?.trim() ?: ""
        val email = profileSetupViewModel.email.value?.trim() ?: ""
        val dob = profileSetupViewModel.dob.value?.trim() ?: ""
        val profession = profileSetupViewModel.profession.value?.trim() ?: ""
        val expertise = profileSetupViewModel.selectedExpertise.value ?: mutableListOf()
        val skills = profileSetupViewModel.selectedInterests.value ?: mutableListOf()
        val location = profileSetupViewModel.location.value?.trim() ?: ""
        val gender = profileSetupViewModel.gender.value?.trim() ?: ""
        val organization = profileSetupViewModel.organization.value?.trim() ?: ""
        val bio = profileSetupViewModel.bio.value?.trim() ?: ""


        // Generate a unique user ID
        val userId = FirebaseDatabase.getInstance().reference.push().key ?: UUID.randomUUID().toString()

        // Upload the profile image to Firebase Storage
        val imageUri = binding.profilePicHolder.drawable?.let { drawable ->
            // Convert drawable to Bitmap
            val bitmap = (drawable.toBitmap())
            // Save the Bitmap to a temporary file and get its URI
            saveBitmapToTempFile(bitmap)
        } // Convert drawable to URI
        if (imageUri != null) {
            uploadImageToFirebaseStorage(imageUri, userId, username, fullName, email, dob, profession, expertise, skills, location, gender, organization, bio)
        } else {
            saveUserDataToDatabase(
                userId,
                username,
                fullName,
                email,
                dob,
                profession,
                expertise,
                skills,
                location,
                gender,
                organization,
                bio,
                null
            )
        }
    }

    private fun uploadImageToFirebaseStorage(imageUri: Uri, userId: String, username: String, fullName: String, email: String, dob: String, profession: String, expertise: List<String>, skills: List<String>, location: String, gender: String, organization: String, bio: String) {
        val storageRef = FirebaseStorage.getInstance().reference.child("profile_images/${userId}.jpg")
        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                // Get the download URL of the uploaded image
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    profileImageUrl = uri.toString()
                    saveUserDataToDatabase(
                        userId,
                        username,
                        fullName,
                        email,
                        dob,
                        profession,
                        expertise,
                        skills,
                        location,
                        gender,
                        organization,
                        bio,
                        profileImageUrl
                    )
                }.addOnFailureListener {
                    showLoading(false)
                    Toast.makeText(this, "Failed to get image URL", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                showLoading(false)
                Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveBitmapToTempFile(bitmap: Bitmap): Uri? {
        return try {
            // Create a temporary file in the cache directory
            val file = File.createTempFile("profile_image", ".jpg", cacheDir)
            val stream = FileOutputStream(file)
            // Compress the Bitmap and write it to the file
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.close()
            // Return the URI of the file
            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun saveUserDataToDatabase(userId: String, username: String, fullName: String, email: String, dob: String, profession: String, expertise: List<String>, skills: List<String>, location: String, gender: String, organization: String, bio: String, imageUrl: String?) {
        val databaseRef = FirebaseDatabase.getInstance().reference.child(Constants.USER_NODE).child(userId)
        val userData = mapOf(
            Constants.FULL_NAME to fullName,
            Constants.USER_NAME to username,
            Constants.EMAIL to email,
            Constants.PROFILE_PIC to imageUrl,
            Constants.DOB to dob,
            Constants.PROFESSION to profession,
            Constants.EXPERTISE to expertise,
            Constants.SKILLS to skills,
            Constants.LOCATION to location,
            Constants.GENDER to gender,
            Constants.ORGANIZATION to organization,
            Constants.BIO to bio,
            Constants.BOOKMARKED_QUESTIONS to emptyMap<String, Boolean>(), // Initialize as empty
            Constants.POSTED_ANSWERS to emptyMap<String, Boolean>(),
            Constants.POSTED_QUESTIONS to emptyMap<String, Boolean>(),
        )

        databaseRef.setValue(userData)
            .addOnSuccessListener {
                showLoading(false)
                Toast.makeText(this, "Data saved successfully!", Toast.LENGTH_SHORT).show()
                navigateToHomeScreen()
            }
            .addOnFailureListener {
                showLoading(false)
                Toast.makeText(this, "Failed to save data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToHomeScreen() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
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
        val intent = ImagePicker.with(this)
            .galleryOnly()  // Open only the gallery
            .crop()  // Enable cropping
            .createIntent()

        imagePickerLauncher.launch(intent)
    }

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
                    binding.profilePicHolder.setImageURI(uri) // Set the selected image
                    Toast.makeText(this, "Image Uploaded!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error: Image not found", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Image selection failed!", Toast.LENGTH_SHORT).show()
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
        binding.textViewPageNumber.text = "${position + 1}/3"
    }
    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.spinKit.visibility = View.VISIBLE
            binding.buttonDone.isActivated = false

            // Disable user interaction
            window.setFlags(
                android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )
        } else {
            binding.spinKit.visibility = View.GONE
            binding.buttonDone.isActivated = true

            // Enable user interaction
            window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
    }

}