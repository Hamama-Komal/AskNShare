package com.example.asknshare.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.asknshare.models.LeaderboardItem
import com.example.asknshare.models.Post
import com.example.asknshare.models.UserData
import com.example.asknshare.utils.Constants
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.example.asknshare.repo.UserProfileRepo

class HomeViewModel : ViewModel() {

    private val _leaderboard = MutableLiveData<List<LeaderboardItem>>()
    val leaderboard: LiveData<List<LeaderboardItem>> = _leaderboard

    private val _latestPosts = MutableLiveData<List<Post>>()
    val latestPosts: LiveData<List<Post>> = _latestPosts

    private val _trendingPosts = MutableLiveData<List<Post>>()
    val trendingPosts: LiveData<List<Post>> = _trendingPosts

    private val _userProfile = MutableLiveData<UserData>()
    val userProfile: LiveData<UserData> = _userProfile

    init {
        fetchAllData()
    }

    fun fetchAllData() {
        fetchUserProfile()
        fetchLeaderboard()
        fetchLatestPosts()
    }

    private fun fetchUserProfile() {
        UserProfileRepo.fetchUserProfile { data ->
            _userProfile.postValue(
                UserData(
                    name     = data[Constants.USER_NAME] as? String,
                    fullName = data[Constants.FULL_NAME] as? String,
                    photoUrl = data[Constants.PROFILE_PIC] as? String
                )
            )
        }
    }

    private fun fetchLeaderboard() {
        FirebaseDatabase
            .getInstance()
            .getReference("Leaderboard")
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snap: DataSnapshot) {
                    val list = snap.children
                        .mapNotNull { it.getValue(LeaderboardItem::class.java) }
                    _leaderboard.postValue(list)
                }
                override fun onCancelled(e: DatabaseError) { /* handle */ }
            })
    }

    private fun fetchLatestPosts() {
        FirebaseDatabase
            .getInstance()
            .getReference(Constants.POSTS_NODE)
            .orderByChild(Constants.POST_TIME)
            .limitToLast(20)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snap: DataSnapshot) {
                    val posts = snap.children
                        .mapNotNull { it.getValue(Post::class.java) }
                    _latestPosts.postValue(posts)
                    _trendingPosts.postValue(
                        posts.sortedByDescending { it.views }.take(5)
                    )
                }
                override fun onCancelled(e: DatabaseError) { /* handle */ }
            })
    }
}


