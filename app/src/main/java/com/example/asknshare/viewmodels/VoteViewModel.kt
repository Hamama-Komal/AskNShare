package com.example.asknshare.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.asknshare.utils.Constants
import com.google.firebase.database.*

class VoteViewModel : ViewModel() {

    private val postsRef = FirebaseDatabase.getInstance().getReference(Constants.POSTS_NODE)

    private val _upvoteCount = MutableLiveData<Int>()
    val upvoteCount: LiveData<Int> = _upvoteCount

    private val _downvoteCount = MutableLiveData<Int>()
    val downvoteCount: LiveData<Int> = _downvoteCount

    private val _userUpvoted = MutableLiveData<Boolean>()
    val userUpvoted: LiveData<Boolean> = _userUpvoted

    private val _userDownvoted = MutableLiveData<Boolean>()
    val userDownvoted: LiveData<Boolean> = _userDownvoted

    /**
     * Initialize vote counts and user vote status for a given post.
     */
    fun fetchVoteStatus(postId: String, userId: String) {
        // Listen for total upvotes
        postsRef.child(postId)
            .child(Constants.POST_UP_VOTES)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    _upvoteCount.value = snapshot.childrenCount.toInt()
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        // Listen for total downvotes
        postsRef.child(postId)
            .child(Constants.POST_DOWN_VOTES)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    _downvoteCount.value = snapshot.childrenCount.toInt()
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        // Check if current user has upvoted
        postsRef.child(postId)
            .child(Constants.POST_UP_VOTES)
            .child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    _userUpvoted.value = snapshot.exists()
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        // Check if current user has downvoted
        postsRef.child(postId)
            .child(Constants.POST_DOWN_VOTES)
            .child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    _userDownvoted.value = snapshot.exists()
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    /**
     * Toggle an upvote for the current user: adds or removes their upvote, and clears any downvote.
     */
    fun toggleUpVote(postId: String, userId: String) {
        val upRef = postsRef.child(postId).child(Constants.POST_UP_VOTES).child(userId)
        val downRef = postsRef.child(postId).child(Constants.POST_DOWN_VOTES).child(userId)

        upRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                // already upvoted: remove
                upRef.removeValue()
            } else {
                // add upvote
                upRef.setValue(true)
                // remove any existing downvote
                downRef.removeValue()
            }
            // refresh status
            fetchVoteStatus(postId, userId)
        }
    }

    /**
     * Toggle a downvote for the current user: adds or removes their downvote, and clears any upvote.
     */
    fun toggleDownVote(postId: String, userId: String) {
        val upRef = postsRef.child(postId).child(Constants.POST_UP_VOTES).child(userId)
        val downRef = postsRef.child(postId).child(Constants.POST_DOWN_VOTES).child(userId)

        downRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                // already downvoted: remove
                downRef.removeValue()
            } else {
                // add downvote
                downRef.setValue(true)
                // remove any existing upvote
                upRef.removeValue()
            }
            // refresh status
            fetchVoteStatus(postId, userId)
        }
    }
}
