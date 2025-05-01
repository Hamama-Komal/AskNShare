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

class UserPostsViewModel : ViewModel() {

    private val _userPosts = MutableLiveData<List<Post>>()
    val userPosts: LiveData<List<Post>> = _userPosts

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun fetchUserPosts(currentUserEmail: String) {
        _isLoading.value = true
        val usersRef = FirebaseDatabase.getInstance().getReference(Constants.USER_NODE)

        usersRef.orderByChild(Constants.EMAIL).equalTo(currentUserEmail)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (userSnapshot in snapshot.children) {
                            val userId = userSnapshot.key
                            // Fetch posts where the authorId matches the current user's ID
                            FirebaseDatabase.getInstance()
                                .getReference(Constants.POSTS_NODE)
                                .orderByChild("authorId")
                                .equalTo(userId)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(postsSnapshot: DataSnapshot) {
                                        val posts = mutableListOf<Post>()
                                        for (postSnapshot in postsSnapshot.children) {
                                            val post = postSnapshot.getValue(Post::class.java)
                                            post?.let {
                                                posts.add(it)
                                            }
                                        }
                                        _userPosts.postValue(posts)
                                        _isLoading.postValue(false)
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        _userPosts.postValue(emptyList())
                                        _isLoading.postValue(false)
                                    }
                                })
                        }
                    } else {
                        _userPosts.postValue(emptyList())
                        _isLoading.postValue(false)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    _userPosts.postValue(emptyList())
                    _isLoading.postValue(false)
                }
            })
    }

    fun deleteUserPost(post: Post, currentUserEmail: String, onComplete: (Boolean) -> Unit) {
        val usersRef = FirebaseDatabase.getInstance().getReference(Constants.USER_NODE)

        usersRef.orderByChild(Constants.EMAIL).equalTo(currentUserEmail)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (userSnapshot in snapshot.children) {
                            val userId = userSnapshot.key
                            // Verify the post belongs to the current user before deleting
                            if (post.postedByUid == userId) {
                                FirebaseDatabase.getInstance()
                                    .getReference(Constants.POSTS_NODE)
                                    .child(post.postId)
                                    .removeValue()
                                    .addOnSuccessListener {
                                        // Remove from local list
                                        val currentList = _userPosts.value?.toMutableList() ?: mutableListOf()
                                        _userPosts.postValue(currentList.filter { it.postId != post.postId })
                                        onComplete(true)
                                    }
                                    .addOnFailureListener {
                                        onComplete(false)
                                    }
                            } else {
                                onComplete(false)
                            }
                        }
                    } else {
                        onComplete(false)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    onComplete(false)
                }
            })
    }

    fun markPostAsPosted(postId: String, currentUserEmail: String, onComplete: (Boolean) -> Unit) {
        val usersRef = FirebaseDatabase.getInstance().getReference(Constants.USER_NODE)

        usersRef.orderByChild(Constants.EMAIL).equalTo(currentUserEmail)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (userSnapshot in snapshot.children) {
                            userSnapshot.ref.child("postedQuestions")
                                .child(postId)
                                .setValue(true)
                                .addOnSuccessListener { onComplete(true) }
                                .addOnFailureListener { onComplete(false) }
                        }
                    } else {
                        onComplete(false)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    onComplete(false)
                }
            })
    }
}