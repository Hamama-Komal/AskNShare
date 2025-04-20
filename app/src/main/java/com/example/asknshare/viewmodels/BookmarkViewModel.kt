package com.example.asknshare.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.asknshare.models.Post
import com.example.asknshare.utils.Constants
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BookmarkViewModel : ViewModel() {

    private val _bookmarkedPosts = MutableLiveData<List<Post>>()
    val bookmarkedPosts: LiveData<List<Post>> = _bookmarkedPosts

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun fetchBookmarkedPosts(currentUserEmail: String) {
        _isLoading.value = true
        val usersRef = FirebaseDatabase.getInstance().getReference(Constants.USER_NODE)

        usersRef.orderByChild(Constants.EMAIL).equalTo(currentUserEmail)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (userSnapshot in snapshot.children) {
                            val bookmarksRef = userSnapshot.ref.child(Constants.BOOKMARKED_QUESTIONS)

                            bookmarksRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(bookmarksSnapshot: DataSnapshot) {
                                    val postIds = bookmarksSnapshot.children.mapNotNull { it.key }
                                    val posts = mutableListOf<Post>()
                                    var loadedCount = 0

                                    if (postIds.isEmpty()) {
                                        _bookmarkedPosts.postValue(emptyList())
                                        _isLoading.postValue(false)
                                        return
                                    }

                                    for (postId in postIds) {
                                        FirebaseDatabase.getInstance()
                                            .getReference(Constants.POSTS_NODE)
                                            .child(postId)
                                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                                override fun onDataChange(postSnapshot: DataSnapshot) {
                                                    val post = postSnapshot.getValue(Post::class.java)
                                                    post?.let {
                                                        // Ensure the post has the bookmark status set
                                                        it.bookmarks = it.bookmarks.toMutableMap().apply {
                                                            put(userSnapshot.key!!, true)
                                                        }
                                                        posts.add(it)
                                                    }
                                                    loadedCount++

                                                    if (loadedCount == postIds.size) {
                                                        _bookmarkedPosts.postValue(posts)
                                                        _isLoading.postValue(false)
                                                    }
                                                }

                                                override fun onCancelled(error: DatabaseError) {
                                                    loadedCount++
                                                    if (loadedCount == postIds.size) {
                                                        _bookmarkedPosts.postValue(posts)
                                                        _isLoading.postValue(false)
                                                    }
                                                }
                                            })
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    _bookmarkedPosts.postValue(emptyList())
                                    _isLoading.postValue(false)
                                }
                            })
                        }
                    } else {
                        _bookmarkedPosts.postValue(emptyList())
                        _isLoading.postValue(false)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    _bookmarkedPosts.postValue(emptyList())
                    _isLoading.postValue(false)
                }
            })
    }
    fun toggleBookmark(post: Post, currentUserEmail: String) {
        val usersRef = FirebaseDatabase.getInstance().getReference(Constants.USER_NODE)

        usersRef.orderByChild(Constants.EMAIL).equalTo(currentUserEmail)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (userSnapshot in snapshot.children) {
                            val userBookmarksRef = userSnapshot.ref.child(Constants.BOOKMARKED_QUESTIONS)
                            val postBookmarksRef = FirebaseDatabase.getInstance()
                                .getReference(Constants.POSTS_NODE)
                                .child(post.postId)
                                .child("bookmarks")

                            userBookmarksRef.child(post.postId).get()
                                .addOnSuccessListener { bookmarkSnapshot ->
                                    if (bookmarkSnapshot.exists()) {
                                        // Remove bookmark - update UI immediately
                                        val currentList = _bookmarkedPosts.value?.toMutableList() ?: mutableListOf()
                                        _bookmarkedPosts.postValue(currentList.filter { it.postId != post.postId })

                                        // Then update Firebase
                                        userBookmarksRef.child(post.postId).removeValue()
                                        postBookmarksRef.child(userSnapshot.key!!).removeValue()
                                    } else {
                                        // Add bookmark - update UI immediately
                                        val currentList = _bookmarkedPosts.value?.toMutableList() ?: mutableListOf()
                                        if (!currentList.any { it.postId == post.postId }) {
                                            currentList.add(post.copy(bookmarks = mapOf(userSnapshot.key!! to true)))
                                            _bookmarkedPosts.postValue(currentList)
                                        }

                                        // Then update Firebase
                                        userBookmarksRef.child(post.postId).setValue(true)
                                        postBookmarksRef.child(userSnapshot.key!!).setValue(true)
                                    }
                                }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
    }
}