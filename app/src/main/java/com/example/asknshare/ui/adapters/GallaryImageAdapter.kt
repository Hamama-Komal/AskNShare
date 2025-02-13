package com.example.asknshare.ui.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.asknshare.R

class GallaryImageAdapter (private var imageList: List<Uri>): RecyclerView.Adapter<GallaryImageAdapter.GalleryImageViewHolder>() {

    class GalleryImageViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val imageView: ImageView = view.findViewById(R.id.imageViewPost)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.image_item, parent, false)
        return GalleryImageViewHolder(view)
    }

    override fun getItemCount(): Int {
       return imageList.size
    }

    override fun onBindViewHolder(holder: GalleryImageViewHolder, position: Int) {
        holder.imageView.setImageURI(imageList[position])
    }

    // Function to update the adapter with a Catured Image
    fun setSingleImage(uri: Uri) {
        imageList = listOf(uri)  // Convert single Uri to a list
        notifyDataSetChanged()
    }

    // Function to update the adapter with images List
    fun setMultipleImages(images: List<Uri>) {
        imageList = images
        notifyDataSetChanged()
    }
}