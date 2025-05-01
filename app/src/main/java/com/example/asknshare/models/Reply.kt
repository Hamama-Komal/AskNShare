package com.example.asknshare.models

data class Reply(
    val replyId: String = "",
    val replyBy: String = "",
    val userName: String = "",
    val userProfile: String = "",
    val replyText: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val upVotes: Map<String, Boolean> = emptyMap(),
    val downVotes: Map<String, Boolean> = emptyMap(),
    val imageList: List<String> = emptyList(),
    var postId: String = ""
)
