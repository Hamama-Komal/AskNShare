package com.example.asknshare.models

data class Post(
    val userFullName: String,
    val userName: String,
    val postTime: String,
    val postText: String,
    val imageUrls: List<String>,
    val views: Int,
    val comments: Int,
    val likes: Int,
    val dislikes: Int
)
