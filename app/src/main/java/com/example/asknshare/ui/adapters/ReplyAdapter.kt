package com.example.asknshare.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.asknshare.R
import com.example.asknshare.models.Reply
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.asknshare.databinding.ReplyItemBinding
import com.google.firebase.database.*


class ReplyAdapter(private val replyList: List<Reply>) : RecyclerView.Adapter<ReplyAdapter.ReplyViewHolder>() {

    inner class ReplyViewHolder(val binding: ReplyItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReplyViewHolder {
        val binding = ReplyItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReplyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReplyViewHolder, position: Int) {
        val reply = replyList[position]
        val binding = holder.binding

        binding.textViewUserName.text = reply.userName
        binding.postText.text = reply.replyText
        binding.textViewPostTime.text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(reply.timestamp))
        binding.upvoteText.text = reply.upVotes.toString()
        binding.downvoteText.text = reply.downVotes.toString()

        Glide.with(holder.itemView.context)
            .load(reply.userProfile)
            .placeholder(R.drawable.user)
            .into(binding.profilePicHolder)

        if (reply.imageList.isNotEmpty()) {
            binding.imageRecycler.visibility = View.VISIBLE
            binding.imageRecycler.layoutManager = GridLayoutManager(holder.itemView.context, 3)
            binding.imageRecycler.adapter = ImageAdapter(reply.imageList)
        } else {
            binding.imageRecycler.visibility = View.GONE
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val replyRef = FirebaseDatabase.getInstance().getReference("replies").child(reply.replyId)

        binding.upvoteBox.setOnClickListener {
            updateVote(replyRef, userId, true, binding)
        }

        binding.downvoteBox.setOnClickListener {
            updateVote(replyRef, userId, false, binding)
        }
    }

    override fun getItemCount(): Int = replyList.size

    private fun updateVote(
        replyRef: DatabaseReference,
        userId: String,
        isUpvote: Boolean,
        binding: ReplyItemBinding
    ) {
        val upvotePath = replyRef.child("upVotes").child(userId)
        val downvotePath = replyRef.child("downVotes").child(userId)

        upvotePath.get().addOnSuccessListener { upvoteSnapshot ->
            downvotePath.get().addOnSuccessListener { downvoteSnapshot ->

                if (isUpvote) {
                    if (upvoteSnapshot.exists()) {
                        upvotePath.removeValue()  // Remove upvote
                        binding.upvoteIcon.setImageResource(R.drawable.ic_upvotes)
                    } else {
                        upvotePath.setValue(true)  // Add upvote
                        downvotePath.removeValue()  // Remove downvote if exists
                        binding.upvoteIcon.setImageResource(R.drawable.ic_selected_upvotes)
                        binding.downvoteIcon.setImageResource(R.drawable.ic_downvotes)
                    }
                } else {
                    if (downvoteSnapshot.exists()) {
                        downvotePath.removeValue()  // Remove downvote
                        binding.downvoteIcon.setImageResource(R.drawable.ic_selected_downvotes)
                    } else {
                        downvotePath.setValue(true)  // Add downvote
                        upvotePath.removeValue()  // Remove upvote if exists
                        binding.downvoteIcon.setImageResource(R.drawable.ic_selected_downvotes)
                        binding.upvoteIcon.setImageResource(R.drawable.ic_upvotes)
                    }
                }

                updateVoteCounts(replyRef, binding)  // Update UI vote counts
            }
        }
    }

    private fun updateVoteCounts(replyRef: DatabaseReference, binding: ReplyItemBinding) {
        replyRef.child("upVotes").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.upvoteText.text = snapshot.childrenCount.toString()
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        replyRef.child("downVotes").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.downvoteText.text = snapshot.childrenCount.toString()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}


