package com.example.asknshare.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.asknshare.R
import com.example.asknshare.databinding.LatestQuestionRecyclerItemBinding
import com.example.asknshare.models.Post
import com.example.asknshare.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PostAdapter(private val postList: List<Post>) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    inner class PostViewHolder(val binding: LatestQuestionRecyclerItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = LatestQuestionRecyclerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]
        val binding = holder.binding
        val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val postRef = FirebaseDatabase.getInstance().reference.child(Constants.POSTS_NODE).child(post.postId)


        // Check if postId is available
        if (post.postId.isEmpty()) {
            return  // Skip this post if postId is missing
        }

        // Set post details
        binding.textViewPostTime.text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(post.timestamp))
        binding.postText.text = post.body

        binding.textViewUserFullName.text = post.postedByFullName
        binding.textViewUserName.text = post.postedByUsername

        Glide.with(holder.itemView.context)
            .load(post.postedByProfile)
            .placeholder(R.drawable.user)
            .into(binding.profilePicHolder)


        binding.comments.text = (post.replies.size ?: 0).toString()
        binding.likes.text = (post.upVotes.size ?: 0).toString()
        binding.dislikes.text = (post.downVotes.size ?: 0).toString()
        binding.views.text = (post.views ?: 0).toString()



        // Load Images into ImageRecycler (Grid)
        val imageAdapter = ImageAdapter(post.images.values.toList())
        binding.imageRecycler.layoutManager = GridLayoutManager(holder.itemView.context, 3)
        binding.imageRecycler.adapter = imageAdapter
        

        // Update vote counts and check user vote status
        updateVoteCounts(postRef, binding)
        checkUserVoteStatus(postRef, binding, userId)

        // Handle Upvote Click
        binding.likes.setOnClickListener {
            postRef.child(Constants.POST_UP_VOTES).child(userId).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    // Remove upvote
                    postRef.child(Constants.POST_UP_VOTES).child(userId).removeValue()
                    binding.likes.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_likes, 0, 0, 0)
                } else {
                    // Add upvote
                    postRef.child(Constants.POST_UP_VOTES).child(userId).setValue(true)
                    binding.likes.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_selected_like, 0, 0, 0)

                    // Remove downvote if exists
                    postRef.child(Constants.POST_DOWN_VOTES).child(userId).removeValue()
                    binding.dislikes.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_dislike, 0, 0, 0)
                }
                updateVoteCounts(postRef, binding)
                checkUserVoteStatus(postRef, binding, userId)  // 🔹 Update icon state
            }
        }

        // Handle Downvote Click
        binding.dislikes.setOnClickListener {
            postRef.child(Constants.POST_DOWN_VOTES).child(userId).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    // Remove downvote
                    postRef.child(Constants.POST_DOWN_VOTES).child(userId).removeValue()
                    binding.dislikes.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_dislike, 0, 0, 0)
                } else {
                    // Add downvote
                    postRef.child(Constants.POST_DOWN_VOTES).child(userId).setValue(true)
                    binding.dislikes.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_selected_dislike, 0, 0, 0)

                    // Remove upvote if exists
                    postRef.child(Constants.POST_UP_VOTES).child(userId).removeValue()
                    binding.likes.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_likes, 0, 0, 0)
                }
                updateVoteCounts(postRef, binding)
                checkUserVoteStatus(postRef, binding, userId)  // 🔹 Update icon state
            }
        }
    }

    private fun checkUserVoteStatus(postRef: DatabaseReference, binding: LatestQuestionRecyclerItemBinding, userId: String) {
        postRef.child(Constants.POST_UP_VOTES).child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    binding.likes.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_selected_like, 0, 0, 0)
                } else {
                    binding.likes.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_likes, 0, 0, 0)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        postRef.child(Constants.POST_DOWN_VOTES).child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    binding.dislikes.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_selected_dislike, 0, 0, 0)
                } else {
                    binding.dislikes.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_dislike, 0, 0, 0)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }


    private fun updateVoteCounts(postRef: DatabaseReference, binding: LatestQuestionRecyclerItemBinding) {
        postRef.child(Constants.POST_UP_VOTES).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val upvoteCount = snapshot.childrenCount.toInt()
                binding.likes.text = upvoteCount.toString()
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        postRef.child(Constants.POST_DOWN_VOTES).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val downvoteCount = snapshot.childrenCount.toInt()
                binding.dislikes.text = downvoteCount.toString()
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        postRef.child(Constants.POST_REPLIES).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val commentCount = snapshot.childrenCount.toInt()
                binding.comments.text = commentCount.toString()
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        postRef.child(Constants.POST_VIEWS).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val views = snapshot.getValue(Int::class.java) ?: 0
                binding.views.text = views.toString()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }


    override fun getItemCount() = postList.size

}

