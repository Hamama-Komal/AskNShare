package com.example.asknshare.ui.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.asknshare.R
import com.example.asknshare.data.local.DataStoreHelper
import com.example.asknshare.ui.activities.EditProfileActivity
import com.example.asknshare.databinding.FragmentProfileBinding
import com.example.asknshare.repo.UserProfileRepo
import com.example.asknshare.ui.activities.BookmarkActivity
import com.example.asknshare.ui.activities.SettingActivity
import com.example.asknshare.ui.activities.WelcomeActivity
import com.example.asknshare.ui.custom.CustomDialog
import com.example.asknshare.utils.Constants
import com.example.asknshare.viewmodels.HomeViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch


class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var dataStoreHelper: DataStoreHelper
    private val homeVM: HomeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        dataStoreHelper = DataStoreHelper(requireContext())

        setupClicks()
        observeUserProfile()

        return binding.root
    }

    private fun setupClicks() {

        binding.buttonEditProfile.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }

        binding.bookmarks.setOnClickListener {
            startActivity(
                Intent(requireContext(), BookmarkActivity::class.java).apply {
                    putExtra("content_type", "bookmarks")
                }
            )
        }


        binding.notifications.setOnClickListener {
            showNotificationsDialog()
        }

        binding.questions.setOnClickListener {
            startActivity(
                Intent(requireContext(), BookmarkActivity::class.java).apply {
                    putExtra("content_type", "questions")
                }
            )
        }

        binding.answers.setOnClickListener {
            startActivity(
                Intent(requireContext(), BookmarkActivity::class.java).apply {
                    putExtra("content_type", "answers")
                }
            )
        }

        binding.settings.setOnClickListener {
            startActivity(Intent(requireContext(), SettingActivity::class.java))
        }

        binding.logout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun observeUserProfile() {
        // Observe the LiveData<UserData> exposed by HomeViewModel
        homeVM.userProfile.observe(viewLifecycleOwner) { user ->
            // Populate username
            binding.textViewUserName.text = user.name.orEmpty()

            // Populate profile picture
            val profilePicUrl = user.photoUrl
            if (!profilePicUrl.isNullOrEmpty()) {
                Glide.with(requireContext())
                    .load(profilePicUrl)
                    .placeholder(R.drawable.user)
                    .into(binding.profilePicHolder)
            } else {
                binding.profilePicHolder.setImageResource(R.drawable.user)
            }
        }

        // Trigger a fetch if you need to reload fresh data
        homeVM.fetchAllData()
    }

    private fun showNotificationsDialog() {
        CustomDialog(
            context = requireContext(),
            title = "Notifications",
            subtitle = "Would you like to enable or manage notifications?",
            positiveButtonText = "Manage",
            negativeButtonText = "Cancel",
            onPositiveClick = {
                // Toast.makeText(requireContext(), "Navigating to Notification Settings", Toast.LENGTH_SHORT).show()
                val intent = Intent().apply {
                    action = android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
                }
                startActivity(intent)
            },
            onNegativeClick = {
                // No action needed
            }
        ).show()
    }


    private fun logoutUser() {
        lifecycleScope.launch {
            dataStoreHelper.clearDataStore()
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(requireContext(), WelcomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun showLogoutDialog() {
        CustomDialog(
            context             = requireContext(),
            title               = "Confirm Logout",
            subtitle            = "Are you sure you want to logout?",
            positiveButtonText  = "Logout",
            negativeButtonText  = "Cancel",
            onPositiveClick     = { logoutUser() },
            onNegativeClick     = { /* no-op */ }
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}