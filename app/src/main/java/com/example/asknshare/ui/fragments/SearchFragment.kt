package com.example.asknshare.ui.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.example.asknshare.R
import com.example.asknshare.ui.adapters.TagsAdapter
import com.example.asknshare.databinding.FragmentProfileBinding
import com.example.asknshare.databinding.FragmentSearchBinding
import com.example.asknshare.models.LeaderboardItem
import com.example.asknshare.models.TagModel
import com.example.asknshare.ui.activities.SearchResultActivity

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        _binding = FragmentSearchBinding.inflate(inflater, container, false)


        binding.cardSearch.setOnClickListener {
            val keyword = binding.searchEditText.text.toString().trim()
            if (keyword.isNotEmpty()) {
                val intent = Intent(requireContext(), SearchResultActivity::class.java)
                intent.putExtra("search_keyword", keyword)
                startActivity(intent)
            } else {
                Toast.makeText(requireContext(), "Enter keyword to search", Toast.LENGTH_SHORT).show()
            }
        }

        setupPopularTagsRecycler()

        return binding.root
    }

    private fun setupPopularTagsRecycler() {

        val dummyTags = listOf(
            TagModel("Science", R.drawable.tag_science),
            TagModel("Career", R.drawable.tag_career),
            TagModel("Coding", R.drawable.tag_coding),
            TagModel("Skills", R.drawable.tag_skills),
            TagModel("Engineering", R.drawable.tag_engineering),
            TagModel("Fitness", R.drawable.tag_fitness),
            TagModel("Languages", R.drawable.tag_languages),
            TagModel("Psychology", R.drawable.tag_psychology),
            TagModel("Medicine", R.drawable.tag_medicine)

        )

        binding.popularTagsRecycler.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.popularTagsRecycler.adapter = TagsAdapter(dummyTags) { selectedTag ->
            val intent = Intent(requireContext(), SearchResultActivity::class.java)
            intent.putExtra("search_keyword", selectedTag)
            startActivity(intent)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}