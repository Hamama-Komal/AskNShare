package com.example.asknshare.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
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
import com.example.asknshare.viewmodels.UserRepliesViewModel
import com.google.firebase.database.*

class ReplyAdapter(
    private val replyList: List<Reply>,
    private val currentUserId: String,
    private val viewModel: UserRepliesViewModel
) : RecyclerView.Adapter<ReplyAdapter.ReplyViewHolder>() {

    inner class ReplyViewHolder(val binding: ReplyItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReplyViewHolder {
        val binding = ReplyItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReplyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReplyViewHolder, position: Int) {
        val reply = replyList[position]
        val binding = holder.binding

        // Highlight user's own replies
        if (reply.replyBy == currentUserId) {
            binding.root.setBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.blue_bg))
        } else {
            binding.root.setBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.grey_bg))
        }

        // Basic reply info
        binding.textViewUserName.text = reply.userName
        binding.postText.text = reply.replyText
        binding.textViewPostTime.text = SimpleDateFormat("hh:mm a", Locale.getDefault())
            .format(Date(reply.timestamp))
        binding.upvoteText.text = reply.upVotes.size.toString()
        binding.downvoteText.text = reply.downVotes.size.toString()

        // User image
        Glide.with(holder.itemView.context)
            .load(reply.userProfile)
            .placeholder(R.drawable.user)
            .into(binding.profilePicHolder)

        // Images in reply
        if (reply.imageList.isNotEmpty()) {
            binding.imageRecycler.visibility = View.VISIBLE
            binding.imageRecycler.layoutManager = GridLayoutManager(holder.itemView.context, 3)
            binding.imageRecycler.adapter = ImageAdapter(reply.imageList)
        } else {
            binding.imageRecycler.visibility = View.GONE
        }

        // Vote states - using map access syntax
        binding.upvoteIcon.setImageResource(
            if (reply.upVotes[currentUserId] == true)
                R.drawable.ic_selected_upvotes
            else
                R.drawable.ic_upvotes
        )
        binding.downvoteIcon.setImageResource(
            if (reply.downVotes[currentUserId] == true)
                R.drawable.ic_selected_downvotes
            else
                R.drawable.ic_downvotes
        )

        // Vote click handlers
        binding.upvoteBox.setOnClickListener {
            viewModel.toggleReplyVote(reply.postId, reply.replyId, currentUserId, true)
        }

        binding.downvoteBox.setOnClickListener {
            viewModel.toggleReplyVote(reply.postId, reply.replyId, currentUserId, false)
        }
    }

    override fun getItemCount(): Int = replyList.size
}
