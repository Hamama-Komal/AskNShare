package com.example.asknshare.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.asknshare.R
import com.example.asknshare.ui.adapters.LeaderboardAdapter
import com.example.asknshare.ui.adapters.PostAdapter
import com.example.asknshare.databinding.FragmentHomeBinding
import com.example.asknshare.databinding.TrendingQuestionItemBinding
import com.example.asknshare.models.LeaderboardItem
import com.example.asknshare.models.Post
import com.example.asknshare.repo.NetworkMonitor
import com.example.asknshare.repo.UserProfileRepo
import com.example.asknshare.ui.activities.FullViewActivity
import com.example.asknshare.utils.Constants
import com.example.asknshare.viewmodels.HomeViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import showCustomToast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val homeVM: HomeViewModel by activityViewModels()
    private lateinit var leaderboardAdapter: LeaderboardAdapter
    private lateinit var postAdapter: PostAdapter

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
            postAdapter.updateList(posts)
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