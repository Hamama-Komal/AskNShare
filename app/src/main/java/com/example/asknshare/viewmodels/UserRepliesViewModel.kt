package com.example.asknshare.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.asknshare.models.Reply
import com.example.asknshare.utils.Constants
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserRepliesViewModel : ViewModel() {

    private val _userReplies = MutableLiveData<List<Reply>>()
    val userReplies: LiveData<List<Reply>> = _userReplies
    private val database = FirebaseDatabase.getInstance().reference

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun fetchUserReplies(currentUserEmail: String) {
        _isLoading.value = true
        val usersRef = FirebaseDatabase.getInstance().getReference(Constants.USER_NODE)

        usersRef.orderByChild(Constants.EMAIL).equalTo(currentUserEmail)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (userSnapshot in snapshot.children) {
                            val postedAnswersRef = userSnapshot.ref.child("postedAnswers")

                            postedAnswersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(answersSnapshot: DataSnapshot) {
                                    val replyIds = answersSnapshot.children.mapNotNull { it.key }
                                    fetchRepliesByIds(replyIds, userSnapshot.key!!)
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    _userReplies.postValue(emptyList())
                                    _isLoading.postValue(false)
                                }
                            })
                        }
                    } else {
                        _userReplies.postValue(emptyList())
                        _isLoading.postValue(false)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    _userReplies.postValue(emptyList())
                    _isLoading.postValue(false)
                }
            })
    }

    fun toggleReplyVote(postId: String, replyId: String, userId: String, isUpvote: Boolean) {
        val replyRef = database.child(Constants.POSTS_NODE).child(postId).child(Constants.POST_REPLIES).child(replyId)
        val upvotePath = replyRef.child(Constants.POST_UP_VOTES).child(userId)
        val downvotePath = replyRef.child(Constants.POST_DOWN_VOTES).child(userId)

        if (isUpvote) {
            upvotePath.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    upvotePath.removeValue()
                } else {
                    upvotePath.setValue(true)
                    downvotePath.removeValue()
                }
            }
        } else {
            downvotePath.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    downvotePath.removeValue()
                } else {
                    downvotePath.setValue(true)
                    upvotePath.removeValue()
                }
            }
        }
    }

    private fun fetchRepliesByIds(replyIds: List<String>, userId: String) {
        if (replyIds.isEmpty()) {
            _userReplies.postValue(emptyList())
            _isLoading.postValue(false)
            return
        }

        val replies = mutableListOf<Reply>()
        var loadedCount = 0

        for (replyId in replyIds) {
            FirebaseDatabase.getInstance()
                .getReference(Constants.POSTS_NODE)
                .orderByChild("${Constants.POST_REPLIES}/$replyId/replyId")
                .equalTo(replyId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(postSnapshot: DataSnapshot) {
                        if (postSnapshot.exists()) {
                            for (post in postSnapshot.children) {
                                val reply = post.child(Constants.POST_REPLIES).child(replyId).getValue(Reply::class.java)
                                reply?.let {
                                    it.postId = post.key ?: ""
                                    replies.add(it)
                                }
                            }
                        }
                        loadedCount++
                        if (loadedCount == replyIds.size) {
                            _userReplies.postValue(replies)
                            _isLoading.postValue(false)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        loadedCount++
                        if (loadedCount == replyIds.size) {
                            _userReplies.postValue(replies)
                            _isLoading.postValue(false)
                        }
                    }
                })
        }
    }


    fun markReplyAsPosted(postId: String, replyId: String, userId: String, onComplete: (Boolean) -> Unit) {
        database.child(Constants.USER_NODE).child(userId).child(Constants.POSTED_ANSWERS)
            .child(replyId).setValue(true)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    fun fetchUserReplies(userId: String, onComplete: (List<String>) -> Unit) {
        database.child(Constants.USER_NODE).child(userId).child(Constants.POSTED_ANSWERS)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val replyIds = snapshot.children.map { it.key ?: "" }
                    onComplete(replyIds)
                }
                override fun onCancelled(error: DatabaseError) {
                    onComplete(emptyList())
                }
            })
    }


    fun deleteReply(postId: String, replyId: String, currentUserEmail: String, onComplete: (Boolean) -> Unit) {
        val usersRef = FirebaseDatabase.getInstance().getReference(Constants.USER_NODE)

        usersRef.orderByChild(Constants.EMAIL).equalTo(currentUserEmail)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (userSnapshot in snapshot.children) {
                            // Remove from user's postedAnswers
                            userSnapshot.ref.child(Constants.POSTED_ANSWERS).child(replyId).removeValue()

                            // Remove from post's replies
                            FirebaseDatabase.getInstance()
                                .getReference(Constants.POSTS_NODE)
                                .child(postId)
                                .child(Constants.POST_REPLIES)
                                .child(replyId)
                                .removeValue()
                                .addOnSuccessListener {
                                    // Update local list
                                    val currentList = _userReplies.value?.toMutableList() ?: mutableListOf()
                                    _userReplies.postValue(currentList.filter { it.replyId != replyId })
                                    onComplete(true)
                                }
                                .addOnFailureListener {
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

    fun updateReply(postId: String, reply: Reply, currentUserEmail: String, onComplete: (Boolean) -> Unit) {
        FirebaseDatabase.getInstance()
            .getReference(Constants.POSTS_NODE)
            .child(postId)
            .child(Constants.POST_REPLIES)
            .child(reply.replyId)
            .setValue(reply)
            .addOnSuccessListener {
                // Update local list
                val currentList = _userReplies.value?.toMutableList() ?: mutableListOf()
                val index = currentList.indexOfFirst { it.replyId == reply.replyId }
                if (index != -1) {
                    currentList[index] = reply
                    _userReplies.postValue(currentList)
                }
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }
}