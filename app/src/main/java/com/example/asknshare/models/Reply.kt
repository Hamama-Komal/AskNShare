package com.example.asknshare.models

data class Reply(
    val replyId: String = "",  // Unique reply ID
    val replyBy: String = "",  // User ID of the person who replied
    val userName: String = "",  // User's name
    val userProfile: String = "",  // URL of the user's profile picture
    val replyText: String = "",  // Reply content
    val timestamp: Long = System.currentTimeMillis(),  // Time when reply was posted
    var upVotes: Int = 0,  // Number of upvotes
    var downVotes: Int = 0,  // Number of downvotes
    val imageList: List<String> = emptyList()  // List of image URLs in the reply
)
