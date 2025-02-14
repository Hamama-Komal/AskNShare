package com.example.asknshare.ui.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.asknshare.R
import com.example.asknshare.databinding.ActivityEditProfileBinding
import com.example.asknshare.repo.UserProfileRepo
import com.example.asknshare.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding

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

        loadUserProfile()

        binding.buttonSaveChanges.setOnClickListener {
            saveUserProfile()
        }

    }

    private fun loadUserProfile() {
        UserProfileRepo.fetchUserProfile { userData ->
            binding.editUserName.setText(userData[Constants.USER_NAME] as? String ?: "")
            binding.editFullName.setText(userData[Constants.FULL_NAME] as? String ?: "")
            binding.editRole.setText(userData[Constants.PROFESSION] as? String ?: "")
            binding.editDob.setText(userData[Constants.DOB] as? String ?: "")
            binding.editGender.setText(userData[Constants.GENDER] as? String ?: "")
            binding.editOrg.setText(userData[Constants.ORGANIZATION] as? String ?: "")
            binding.editLocation.setText(userData[Constants.LOCATION] as? String ?: "")
            binding.editBio.setText(userData[Constants.BIO] as? String ?: "")

            val profilePicUrl = userData[Constants.PROFILE_PIC] as? String
            if (!profilePicUrl.isNullOrEmpty()) {
                Glide.with(this)
                    .load(profilePicUrl)
                    .placeholder(R.drawable.user)
                    .into(binding.profilePicHolder)
            } else {
                binding.profilePicHolder.setImageResource(R.drawable.user)
            }
        }
    }

    private fun saveUserProfile() {
        val updatedData = mapOf(
            "user_name" to binding.editUserName.text.toString(),
            "full_name" to binding.editFullName.text.toString(),
            "profession" to binding.editRole.text.toString(),
            "dob" to binding.editDob.text.toString(),
            "gender" to binding.editGender.text.toString(),
            "organization" to binding.editOrg.text.toString(),
            "location" to binding.editLocation.text.toString(),
            "bio" to binding.editBio.text.toString()
        )

        UserProfileRepo.updateUserProfile(updatedData) { success ->
            if (success) {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
            }
        }
    }


}