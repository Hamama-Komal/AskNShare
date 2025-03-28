package com.example.asknshare.ui.custom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.asknshare.R
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

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.reply_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase Realtime Database and Auth
        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        val publishReplyButton = view.findViewById<Button>(R.id.button_publish_reply)
       // val questionTitle = view.findViewById<EditText>(R.id.questionTitle)
        val questionDescription = view.findViewById<EditText>(R.id.questionDescription)

        publishReplyButton.setOnClickListener {
            val replyBody = questionDescription.text.toString().trim()

            if (replyBody.isEmpty()) {
                return@setOnClickListener
            }

            val currentUser = auth.currentUser
            if (currentUser == null) {
                return@setOnClickListener
            }

            val postId = arguments?.getString("postId")
            if (postId == null) {
                Toast.makeText(requireContext(), "Error: Post ID not found", Toast.LENGTH_SHORT).show()
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
                        imageList = listOf() // Update when image upload is implemented
                    )

                    database.child(Constants.POSTS_NODE).child(postId).child("replies").child(replyId)
                        .setValue(reply)
                        .addOnSuccessListener {
                            dismiss()
                            Toast.makeText(requireContext(), "Your reply was published successfully!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            e.printStackTrace()
                        }
                }
            }
        }



    }


}