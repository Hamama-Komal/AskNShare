package com.example.asknshare.ui.activities

import android.app.Activity
import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.asknshare.R
import com.example.asknshare.databinding.ActivityEditProfileBinding
import com.example.asknshare.repo.UserProfileRepo
import com.example.asknshare.utils.Constants
import com.github.drjacky.imagepicker.ImagePicker
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.Calendar
import java.util.Locale

@Suppress("DEPRECATION")
class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        loadUserProfile()

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.buttonSaveChanges.setOnClickListener {
            saveUserProfile()
        }

        binding.cardUploadPic.setOnClickListener {
            showImagePickerBottomSheet()
        }

        binding.editDob.setOnClickListener {
            showDatePicker()
        }

        binding.textfieldRole.setOnClickListener {
            setupRoleAutocomplete()
        }


        binding.editGender.setOnClickListener {
            showGenderOptions(binding.editGender)
        }

        // Request location
        binding.editLocation.setOnClickListener {
            requestLocationPermission()
        }


    }

    private fun showGenderOptions(editText: com.google.android.material.textfield.TextInputEditText) {

        val popupMenu = PopupMenu(this, editText).apply {
            menu.add("Male")
            menu.add("Female")
            menu.add("Not prefer to say")

            setOnMenuItemClickListener { item: MenuItem ->
                editText.setText(item.title)
                true
            }
        }
        popupMenu.show()
    }

    private fun setupRoleAutocomplete() {

        // Access the string array from resources
        val roles: List<String> = resources.getStringArray(R.array.roles_list).toList()

        val adapter = ArrayAdapter(
            this,  // Use appropriate context
            android.R.layout.simple_dropdown_item_1line,
            roles
        )

        binding.textfieldRole.setAdapter(adapter)

        binding.textfieldRole.setOnItemClickListener { parent, _, position, _ ->
            val selectedRole = parent.getItemAtPosition(position).toString()
            binding.textfieldRole.setText(selectedRole)
        }

    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.spinKit.visibility = View.VISIBLE
            window.setFlags(
                android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )
        } else {
            binding.spinKit.visibility = View.GONE
            window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            R.style.CustomDatePickerDialog,  // Apply the custom style here
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate =
                    String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
                binding.editDob.setText(formattedDate)
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun loadUserProfile() {
        showLoading(true)
        UserProfileRepo.fetchUserProfile { userData ->
            binding.editUserName.setText(userData[Constants.USER_NAME] as? String ?: "")
            binding.editFullName.setText(userData[Constants.FULL_NAME] as? String ?: "")
            binding.textfieldRole.setText(userData[Constants.PROFESSION] as? String ?: "")
            binding.editDob.setText(userData[Constants.DOB] as? String ?: "")
            binding.editGender.setText(userData[Constants.GENDER] as? String ?: "")
            binding.editOrg.setText(userData[Constants.ORGANIZATION] as? String ?: "")
            binding.editLocation.setText(userData[Constants.LOCATION] as? String ?: "")
            binding.editBio.setText(userData[Constants.BIO] as? String ?: "")

            val profilePicUrl = userData[Constants.PROFILE_PIC] as? String
            if (!profilePicUrl.isNullOrEmpty()) {
                Glide.with(this).load(profilePicUrl).placeholder(R.drawable.user)
                    .into(binding.profilePicHolder)
            } else {
                binding.profilePicHolder.setImageResource(R.drawable.user)
            }
            showLoading(false)
        }
    }

    private fun saveUserProfile() {

        showLoading(true)

        val dob = binding.editDob.text.toString()
        if (!isValidDOB(dob)) {
            showLoading(false)
            Toast.makeText(this, "Invalid Date Format! Use DD/MM/YYYY", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedData = mapOf(
            Constants.USER_NAME to binding.editUserName.text.toString(),
            Constants.FULL_NAME to binding.editFullName.text.toString(),
            Constants.PROFESSION to binding.textfieldRole.text.toString(),
            Constants.DOB to dob,
            Constants.GENDER to binding.editGender.text.toString(),
            Constants.ORGANIZATION to binding.editOrg.text.toString(),
            Constants.LOCATION to binding.editLocation.text.toString(),
            Constants.BIO to binding.editBio.text.toString()
        )

        UserProfileRepo.updateUserProfile(updatedData) { success ->
            showLoading(false)
            if (success) {
                loadUserProfile()
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isValidDOB(dob: String): Boolean {
        val regex = Regex("""^(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[0-2])/\d{4}$""")
        return regex.matches(dob)
    }

    private fun showImagePickerBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_image_picker, null)
        bottomSheetDialog.setContentView(view)

        view.findViewById<View>(R.id.tv_gallery)?.setOnClickListener {
            pickImageFromGallery()
            bottomSheetDialog.dismiss()
        }

        view.findViewById<View>(R.id.tv_camera)?.setOnClickListener {
            captureImageFromCamera()
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun pickImageFromGallery() {
        val intent = ImagePicker.with(this).galleryOnly().crop().createIntent()
        imagePickerLauncher.launch(intent)
    }

    private fun captureImageFromCamera() {
        val intent = ImagePicker.with(this).cameraOnly().crop().createIntent()
        imagePickerLauncher.launch(intent)
    }

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri: Uri? = result.data?.data
                if (uri != null) {
                    binding.profilePicHolder.setImageURI(uri)
                    uploadImageToFirebase(uri)
                } else {
                    Toast.makeText(this, "Error: Image not found", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Image selection failed!", Toast.LENGTH_SHORT).show()
            }
        }

    private fun uploadImageToFirebase(imageUri: Uri) {
        if (userId == null) return

        val usersRef = FirebaseDatabase.getInstance().getReference("Users").child(userId)
        usersRef.child(Constants.PROFILE_PIC).get().addOnSuccessListener { snapshot ->
            val existingImageUrl = snapshot.getValue(String::class.java)
            val storageRef = FirebaseStorage.getInstance().reference.child("profile_images/${userId}.jpg")

            if (existingImageUrl != null) {
                storageRef.delete().addOnSuccessListener {
                    storageRef.putFile(imageUri).addOnSuccessListener {
                        storageRef.downloadUrl.addOnSuccessListener { uri ->
                            updateUserProfilePic(uri.toString())
                        }
                    }
                }
            } else {
                storageRef.putFile(imageUri).addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        updateUserProfilePic(uri.toString())
                    }
                }
            }
        }
    }

    private fun updateUserProfilePic(imageUrl: String) {
        showLoading(true)
        val userEmail = FirebaseAuth.getInstance().currentUser?.email
        if (userEmail.isNullOrEmpty()) return

        val usersRef = FirebaseDatabase.getInstance().getReference("Users")

        usersRef.get().addOnSuccessListener { snapshot ->
            var userIdToUpdate: String? = null

            for (userSnapshot in snapshot.children) {
                val email = userSnapshot.child("email").getValue(String::class.java)
                if (email == userEmail) {
                    userIdToUpdate = userSnapshot.key
                    break
                }
            }

            if (userIdToUpdate != null) {
                usersRef.child(userIdToUpdate).child("profile_pic").setValue(imageUrl)
                    .addOnCompleteListener { task ->
                        showLoading(false)
                        if (task.isSuccessful) {
                            Glide.with(this)
                                .load(imageUrl)
                                .placeholder(R.drawable.user)
                                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .into(binding.profilePicHolder)

                            Toast.makeText(this, "Profile picture updated!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Failed to update profile picture", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "User not found!", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            showLoading(false)
            Toast.makeText(this, "Database error: ${it.message}", Toast.LENGTH_SHORT).show()
            Log.d("Error", "Database error: ${it.message}")
        }

    }

    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            getUserLocation()
        }
    }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getUserLocation()
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getUserLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                getCityName(location.latitude, location.longitude)
            } else {
                Toast.makeText(this, "Could not get location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getCityName(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)

        if (!addresses.isNullOrEmpty()) {
            val cityName = addresses[0].locality ?: "Unknown City"
            binding.editLocation.setText(cityName)
        } else {
            Toast.makeText(this, "City not found", Toast.LENGTH_SHORT).show()
        }
    }
}


