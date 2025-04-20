package com.example.asknshare.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.asknshare.R
import com.example.asknshare.databinding.PopularTagItemBinding
import com.example.asknshare.models.TagModel


class TagsAdapter(
    private val tagList: List<TagModel>,
    private val onTagClick: (String) -> Unit
) : RecyclerView.Adapter<TagsAdapter.TagViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val binding = PopularTagItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TagViewHolder(binding, onTagClick)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        holder.bind(tagList[position])
    }

    override fun getItemCount(): Int = tagList.size

    class TagViewHolder(
        private val binding: PopularTagItemBinding,
        private val onTagClick: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(tag: TagModel) {
            binding.tagName.text = tag.name
            binding.tagVector.setImageResource(tag.drawableResId)

            binding.root.setOnClickListener {
                onTagClick(tag.name)
            }
        }
    }
}



