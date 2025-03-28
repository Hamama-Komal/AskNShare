package com.example.asknshare.ui.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.asknshare.R
import com.example.asknshare.databinding.LatestQuestionRecyclerItemBinding
import com.example.asknshare.models.Post
import com.example.asknshare.ui.activities.FullViewActivity
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
        binding.postTitle.text = post.heading

        binding.textViewUserFullName.text = post.postedByFullName
        binding.textViewUserName.text = post.postedByUsername

        Glide.with(holder.itemView.context)
            .load(post.postedByProfile)
            .placeholder(R.drawable.user)
            .into(binding.profilePicHolder)


        binding.replyText.text = (post.replies.size ?: 0).toString()
        binding.upvoteText.text = (post.upVotes.size ?: 0).toString()
        binding.downvoteText.text = (post.downVotes.size ?: 0).toString()
        binding.viewsText.text = (post.views ?: 0).toString()



        // Load Images into ImageRecycler (Grid)
        val imageAdapter = ImageAdapter(post.images.values.toList())
        binding.imageRecycler.layoutManager = GridLayoutManager(holder.itemView.context, 3)
        binding.imageRecycler.adapter = imageAdapter


        // Handle Post Click - Open FullViewActivity
        binding.root.setOnClickListener {
            val intent = Intent(holder.itemView.context, FullViewActivity::class.java)
            intent.putExtra("postId", post.postId)
            holder.itemView.context.startActivity(intent)
        }

        // Update vote counts and check user vote status
        updateVoteCounts(postRef, binding)
        checkUserVoteStatus(postRef, binding, userId)

        // Handle Upvote Click
        binding.upvoteBox.setOnClickListener {
            postRef.child(Constants.POST_UP_VOTES).child(userId).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    // Remove upvote
                    postRef.child(Constants.POST_UP_VOTES).child(userId).removeValue()
                    binding.upvoteIcon.setImageResource(R.drawable.ic_upvotes)
                } else {
                    // Add upvote
                    postRef.child(Constants.POST_UP_VOTES).child(userId).setValue(true)
                    binding.upvoteIcon.setImageResource(R.drawable.ic_selected_upvotes)

                    // Remove downvote if exists
                    postRef.child(Constants.POST_DOWN_VOTES).child(userId).removeValue()
                    binding.upvoteIcon.setImageResource(R.drawable.ic_upvotes)
                }
                updateVoteCounts(postRef, binding)
                checkUserVoteStatus(postRef, binding, userId)
            }
        }

        // Handle Downvote Click
        binding.downvoteBox.setOnClickListener {
            postRef.child(Constants.POST_DOWN_VOTES).child(userId).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    // Remove downvote
                    postRef.child(Constants.POST_DOWN_VOTES).child(userId).removeValue()
                    binding.downvoteIcon.setImageResource(R.drawable.ic_downvotes)
                } else {
                    // Add downvote
                    postRef.child(Constants.POST_DOWN_VOTES).child(userId).setValue(true)
                    binding.downvoteIcon.setImageResource(R.drawable.ic_selected_downvotes)

                    // Remove upvote if exists
                    postRef.child(Constants.POST_UP_VOTES).child(userId).removeValue()
                    binding.downvoteIcon.setImageResource(R.drawable.ic_downvotes)
                }
                updateVoteCounts(postRef, binding)
                checkUserVoteStatus(postRef, binding, userId)
            }
        }
    }

    private fun checkUserVoteStatus(postRef: DatabaseReference, binding: LatestQuestionRecyclerItemBinding, userId: String) {
        postRef.child(Constants.POST_UP_VOTES).child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    binding.upvoteIcon.setImageResource(R.drawable.ic_selected_upvotes)
                } else {
                    binding.upvoteIcon.setImageResource(R.drawable.ic_upvotes)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        postRef.child(Constants.POST_DOWN_VOTES).child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    binding.downvoteIcon.setImageResource(R.drawable.ic_selected_downvotes)
                } else {
                    binding.downvoteIcon.setImageResource(R.drawable.ic_downvotes)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }


    private fun updateVoteCounts(postRef: DatabaseReference, binding: LatestQuestionRecyclerItemBinding) {
        postRef.child(Constants.POST_UP_VOTES).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val upvoteCount = snapshot.childrenCount.toInt()
                binding.upvoteText.text = upvoteCount.toString()
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        postRef.child(Constants.POST_DOWN_VOTES).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val downvoteCount = snapshot.childrenCount.toInt()
                binding.downvoteText.text = downvoteCount.toString()
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        postRef.child(Constants.POST_REPLIES).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val commentCount = snapshot.childrenCount.toInt()
                binding.replyText.text = commentCount.toString()
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        postRef.child(Constants.POST_VIEWS).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val views = snapshot.getValue(Int::class.java) ?: 0
                binding.viewsText.text = views.toString()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }


    override fun getItemCount() = postList.size

}

