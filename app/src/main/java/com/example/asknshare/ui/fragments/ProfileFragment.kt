package com.example.asknshare.ui.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.asknshare.R
import com.example.asknshare.data.local.DataStoreHelper
import com.example.asknshare.ui.activities.EditProfileActivity
import com.example.asknshare.databinding.FragmentProfileBinding
import com.example.asknshare.repo.UserProfileRepo
import com.example.asknshare.ui.activities.BookmarkActivity
import com.example.asknshare.ui.activities.WelcomeActivity
import com.example.asknshare.ui.custom.CustomDialog
import com.example.asknshare.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch


class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var dataStoreHelper: DataStoreHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        // Initialize DataStoreHelper
        dataStoreHelper = DataStoreHelper(requireContext())

        fetchAndDisplayUserData()

        binding.buttonEditProfile.setOnClickListener {
            startActivity(Intent(context, EditProfileActivity::class.java))
        }

        binding.bookmarks.setOnClickListener {
            startActivity(Intent(context, BookmarkActivity::class.java))
        }

        binding.logout.setOnClickListener {
            showLogoutDialog()
        }


        return binding.root
    }

    private fun fetchAndDisplayUserData() {
        UserProfileRepo.fetchUserProfile { userData ->

            binding.textViewUserName.text = userData[Constants.USER_NAME] as? String ?: "Unknown User"

            val profilePicUrl = userData[Constants.PROFILE_PIC] as? String
            if (!profilePicUrl.isNullOrEmpty()) {
                Glide.with(requireContext())
                    .load(profilePicUrl)
                    .placeholder(R.drawable.user)
                    .into(binding.profilePicHolder)
            } else {
                binding.profilePicHolder.setImageResource(R.drawable.user)
            }
        }
    }

    private fun logoutUser() {
        lifecycleScope.launch {
            // Clear DataStore using DataStoreHelper
            dataStoreHelper.clearDataStore()

            // Sign out from Firebase
            FirebaseAuth.getInstance().signOut()

            // Navigate to Login Activity
            val intent = Intent(requireContext(), WelcomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun showLogoutDialog() {
        val customDialog = CustomDialog(
            context = requireContext(),
            title = "Confirm Logout",
            subtitle = "Are you sure you want to logout?",
            positiveButtonText = "Logout",
            negativeButtonText = "Cancel",
            onPositiveClick = {
                logoutUser() // Perform logout action
            },
            onNegativeClick = {
                // Do nothing or handle cancel action
            }
        )
        customDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}