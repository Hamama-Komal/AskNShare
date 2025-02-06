package com.example.asknshare.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.asknshare.R
import com.example.asknshare.models.Post

class PostAdapter(private val postList: List<Post>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userFullName: TextView = itemView.findViewById(R.id.textViewUserFullName)
        val userName: TextView = itemView.findViewById(R.id.textViewUserName)
        val postTime: TextView = itemView.findViewById(R.id.textViewPostTime)
        val postText: TextView = itemView.findViewById(R.id.post_text)
        val views: TextView = itemView.findViewById(R.id.views)
        val comments: TextView = itemView.findViewById(R.id.comments)
        val likes: TextView = itemView.findViewById(R.id.likes)
        val dislikes: TextView = itemView.findViewById(R.id.dislikes)
        val imageRecycler: RecyclerView = itemView.findViewById(R.id.imageRecycler)

        fun bind(post: Post) {
            userFullName.text = post.userFullName
            userName.text = post.userName
            postTime.text = post.postTime
            postText.text = post.postText
            views.text = post.views.toString()
            comments.text = post.comments.toString()
            likes.text = post.likes.toString()
            dislikes.text = post.dislikes.toString()

            // Set up the nested image RecyclerView
            val imageAdapter = ImageAdapter(post.imageUrls)
            imageRecycler.layoutManager = GridLayoutManager(itemView.context, 3)
            imageRecycler.adapter = imageAdapter
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.latest_question_recycler_item, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(postList[position])
    }

    override fun getItemCount(): Int = postList.size
}
