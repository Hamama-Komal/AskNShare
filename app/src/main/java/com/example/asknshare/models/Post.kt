package com.example.asknshare.models

data class Post(
    val postId: String = "",
    val postedByUid: String = "",
    val postedByFullName: String = "",
    val postedByUsername: String = "",
    val postedByProfile: String = "",
    val heading: String = "",
    val body: String = "",
    val images: Map<String, String> = emptyMap(),
    val timestamp: Long = 0,
    val upVotes: Map<String, Boolean> = emptyMap(),
    val downVotes: Map<String, Boolean> = emptyMap(),
    val views: Int = 0,
    val bookmarks: Map<String, Boolean> = emptyMap(),
    val replies: Map<String, Any> = emptyMap(),
    val tags: List<String> = emptyList()
)
