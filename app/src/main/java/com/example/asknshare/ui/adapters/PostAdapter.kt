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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
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
    }

    override fun getItemCount() = postList.size
}

