package com.example.asknshare.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.asknshare.R
import com.example.asknshare.models.TagModel

class TagsAdapter(private val tagList: List<TagModel>) :
    RecyclerView.Adapter<TagsAdapter.TagViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.popular_tag_item, parent, false)
        return TagViewHolder(view)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        val tag = tagList[position]
        holder.bind(tag)
    }

    override fun getItemCount(): Int = tagList.size

    class TagViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.tag_vector)
        private val tagName: TextView = itemView.findViewById(R.id.tag_name)

        fun bind(tag: TagModel) {
            tagName.text = tag.name

            // Load image from Firebase Storage using Glide
            Glide.with(itemView.context)
                .load(tag.imageUrl)
                .placeholder(R.drawable.user) // Set placeholder image
                .into(imageView)
        }
    }
}
