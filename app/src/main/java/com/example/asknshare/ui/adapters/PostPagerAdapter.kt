package com.example.asknshare.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.asknshare.R
import com.example.asknshare.models.PostModel

class PostPagerAdapter(private val postList: List<PostModel>) :
    RecyclerView.Adapter<PostPagerAdapter.PostViewHolder>() {

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userName: TextView = itemView.findViewById(R.id.textViewUserFullName)
        val userHandle: TextView = itemView.findViewById(R.id.textViewUserName)
        val postTime: TextView = itemView.findViewById(R.id.textViewPostTime)
        val postText: TextView = itemView.findViewById(R.id.post_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.trending_question_item, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]
        holder.userName.text = post.fullName
        holder.userHandle.text = post.userName
        holder.postTime.text = post.postTime
        holder.postText.text = post.postText
    }

    override fun getItemCount(): Int = postList.size
}
