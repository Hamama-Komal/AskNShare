package com.example.asknshare.models

data class Reply(
    val replyId: String = "",
    val replyBy: String = "",
    val userName: String = "",
    val userProfile: String = "",
    val replyText: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    var upVotes: Int = 0,
    var downVotes: Int = 0,
    val imageList: List<String> = emptyList()
)
