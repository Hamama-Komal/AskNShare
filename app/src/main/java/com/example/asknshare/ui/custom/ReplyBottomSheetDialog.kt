package com.example.asknshare.ui.custom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.asknshare.R
import com.example.asknshare.databinding.ReplyBottomSheetBinding
import com.example.asknshare.models.Reply
import com.example.asknshare.repo.UserProfileRepo
import com.example.asknshare.utils.Constants
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ReplyBottomSheetDialog : BottomSheetDialogFragment() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var _binding: ReplyBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ReplyBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase
        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        binding.buttonPublishReply.setOnClickListener {
            val replyBody = binding.questionDescription.text.toString().trim()

            if (replyBody.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter your reply", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Show loading and hide button
            binding.buttonPublishReply.visibility = View.GONE
            binding.spinKit.visibility = View.VISIBLE

            val currentUser = auth.currentUser
            if (currentUser == null) {
                showErrorAndResetUI("Authentication error")
                return@setOnClickListener
            }

            val postId = arguments?.getString("postId")
            if (postId == null) {
                showErrorAndResetUI("Error: Post ID not found")
                return@setOnClickListener
            }

            // Fetch user data from Firebase
            UserProfileRepo.fetchUserProfile { userData ->
                val userName = userData[Constants.USER_NAME] as? String ?: "Unknown User"
                val userProfile = userData[Constants.PROFILE_PIC] as? String ?: ""

                val replyId = database.child(Constants.POSTS_NODE).child(postId).child("replies").push().key

                if (replyId != null) {
                    val reply = Reply(
                        replyId = replyId,
                        replyBy = currentUser.uid,
                        userName = userName,
                        userProfile = userProfile,
                        replyText = replyBody,
                        timestamp = System.currentTimeMillis(),
                        upVotes = 0,
                        downVotes = 0,
                        imageList = listOf()
                    )

                    database.child(Constants.POSTS_NODE).child(postId).child("replies").child(replyId)
                        .setValue(reply)
                        .addOnSuccessListener {
                            dismiss()
                            Toast.makeText(requireContext(), "Your reply was published successfully!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            showErrorAndResetUI("Failed to publish reply")
                            e.printStackTrace()
                        }
                } else {
                    showErrorAndResetUI("Error generating reply ID")
                }
            }
        }
    }

    private fun showErrorAndResetUI(errorMessage: String) {
        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
        binding.buttonPublishReply.visibility = View.VISIBLE
        binding.spinKit.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}