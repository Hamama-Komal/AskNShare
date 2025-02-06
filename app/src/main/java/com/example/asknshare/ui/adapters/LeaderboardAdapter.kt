package com.example.asknshare.ui.adapters


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.asknshare.databinding.LeaderboardRecyclerItemBinding
import com.example.asknshare.models.LeaderboardItem

class LeaderboardAdapter(private val leaderboardList: List<LeaderboardItem>) :
    RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder>() {

    inner class LeaderboardViewHolder(private val binding: LeaderboardRecyclerItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: LeaderboardItem) {
            binding.leaderPicHolder.setImageResource(item.imageRes)
            binding.textViewLeaderName.text = item.name
            binding.textViewLeaderPoints.text = item.points
            binding.textViewLeaderTitle.text = item.title
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val binding = LeaderboardRecyclerItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return LeaderboardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        holder.bind(leaderboardList[position])
    }

    override fun getItemCount(): Int = leaderboardList.size
}
