package com.example.asknshare.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.example.asknshare.R
import com.example.asknshare.ui.adapters.TagsAdapter
import com.example.asknshare.databinding.FragmentProfileBinding
import com.example.asknshare.databinding.FragmentSearchBinding
import com.example.asknshare.models.LeaderboardItem
import com.example.asknshare.models.TagModel

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        _binding = FragmentSearchBinding.inflate(inflater, container, false)

        setupPopularTagsRecycler()

        return binding.root
    }

    private fun setupPopularTagsRecycler() {

        val dummyTags = listOf(
            TagModel("Technology", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSlpRhlkY4IceCk9uv38j2f_MQQ-_woqkJ1nA&s"),
            TagModel("Science", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSlpRhlkY4IceCk9uv38j2f_MQQ-_woqkJ1nA&s"),
            TagModel("Education", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSlpRhlkY4IceCk9uv38j2f_MQQ-_woqkJ1nA&s"),
            TagModel("Health", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSlpRhlkY4IceCk9uv38j2f_MQQ-_woqkJ1nA&s"),
            TagModel("Sports", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSlpRhlkY4IceCk9uv38j2f_MQQ-_woqkJ1nA&s"),
            TagModel("Entertainment", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSlpRhlkY4IceCk9uv38j2f_MQQ-_woqkJ1nA&s")
        )

        binding.popularTagsRecycler.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.popularTagsRecycler.adapter = TagsAdapter(dummyTags)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}