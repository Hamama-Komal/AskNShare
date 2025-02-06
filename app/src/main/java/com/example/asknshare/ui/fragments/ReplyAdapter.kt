package com.example.asknshare.ui.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.asknshare.R
import com.example.asknshare.models.Reply

class ReplyAdapter(private val replies: List<Reply>) : RecyclerView.Adapter<ReplyAdapter.ReplyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReplyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.reply_item, parent, false)
        return ReplyViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReplyViewHolder, position: Int) {
        val reply = replies[position]
        holder.username.text = reply.username
        holder.message.text = reply.message
        holder.profileImage.setImageResource(reply.profileImageRes)
    }

    override fun getItemCount(): Int = replies.size

    class ReplyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val username: TextView = itemView.findViewById(R.id.textViewUserFullName)
        val message: TextView = itemView.findViewById(R.id.post_text)
        val profileImage: ImageView = itemView.findViewById(R.id.profile_pic_holder)
    }
}