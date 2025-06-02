package com.example.asknshare.ui.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.asknshare.R
import com.example.asknshare.ui.adapters.LeaderboardAdapter
import com.example.asknshare.ui.adapters.PostAdapter
import com.example.asknshare.databinding.FragmentHomeBinding
import com.example.asknshare.databinding.TrendingQuestionItemBinding
import com.example.asknshare.models.Post
import com.example.asknshare.ui.activities.FullViewActivity
import com.example.asknshare.ui.activities.SeeAllActivity
import com.example.asknshare.viewmodels.HomeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val homeVM: HomeViewModel by activityViewModels()
    private lateinit var leaderboardAdapter: LeaderboardAdapter
    private lateinit var postAdapter: PostAdapter
    private val initialPostCount = 10

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        fetchData()
        observeViewModel()
    }

    private fun setupUI() {
        // show a loading spinner until data arrives
        binding.spinKit.visibility = View.VISIBLE

        leaderboardAdapter = LeaderboardAdapter(emptyList())
        postAdapter = PostAdapter(emptyList())

        binding.leaderboardRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = leaderboardAdapter
        }

        binding.latestQuestionRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = postAdapter
        }

        binding.textSeeMore.setOnClickListener {
            // Navigate to the See More Activity
            val intent = Intent(requireContext(), SeeAllActivity::class.java)
            startActivity(intent)
        }
    }

    private fun fetchData() {
        // simply ask the VM to reload everything
        homeVM.fetchAllData()
    }

    private fun observeViewModel() {
        homeVM.userProfile.observe(viewLifecycleOwner) { user ->
            binding.spinKit.visibility = View.GONE
            binding.textViewUserName.text     = user.name.orEmpty()
            binding.textViewUserFullName.text = user.fullName.orEmpty()
            Glide.with(this)
                .load(user.photoUrl)
                .placeholder(R.drawable.user)
                .into(binding.profilePicHolder)
        }

        homeVM.leaderboard.observe(viewLifecycleOwner) { list ->
            leaderboardAdapter.updateList(list)
        }

        homeVM.latestPosts.observe(viewLifecycleOwner) { posts ->
            // Show only first 10 posts initially
            val initialPosts = if (posts.size > initialPostCount) {
                posts.subList(0, initialPostCount)
            } else {
                posts
            }
            postAdapter.updateList(initialPosts)
        }

        homeVM.trendingPosts.observe(viewLifecycleOwner) { trending ->
            displayTrendingPosts(trending)
        }
    }

    private fun displayTrendingPosts(posts: List<Post>) {
        binding.viewFlipper.apply {
            removeAllViews()
            posts.forEach { post ->
                val item = TrendingQuestionItemBinding
                    .inflate(layoutInflater)
                    .apply {
                        textViewUserFullName.text = post.postedByFullName
                        textViewUserName.text     = post.postedByUsername
                        textViewPostTime.text     = SimpleDateFormat(
                            "dd MMM yyyy", Locale.getDefault()
                        ).format(Date(post.timestamp))
                        postText.text  = post.body
                        postTitle.text = post.heading
                        Glide.with(requireContext())
                            .load(post.postedByProfile.takeIf { it.isNotEmpty() })
                            .placeholder(R.drawable.user)
                            .into(profilePicHolder)
                        root.setOnClickListener {
                            val intent = Intent(requireContext(), FullViewActivity::class.java)
                            intent.putExtra("postId", post.postId)
                            startActivity(intent)
                        }
                    }
                addView(item.root)
            }
            isAutoStart = true
            startFlipping()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}