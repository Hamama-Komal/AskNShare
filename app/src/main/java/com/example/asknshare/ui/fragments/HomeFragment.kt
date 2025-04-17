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

    private lateinit var leaderboardAdapter: LeaderboardAdapter
    private lateinit var postAdapter: PostAdapter
    private val leaderboardList = mutableListOf<LeaderboardItem>()
    private val postList = mutableListOf<Post>()

    private var networkMonitor: NetworkMonitor? = null



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        networkMonitor = NetworkMonitor(requireContext())
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        monitorNetwork()
    }



    private fun setupUI() {
        showLoader("Checking network...")


        leaderboardAdapter = LeaderboardAdapter(leaderboardList)
        postAdapter = PostAdapter(postList)

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
        if (networkMonitor?.networkStatus?.value is NetworkMonitor.NetworkStatus.Disconnected) {
            showNetworkError("No internet connection")
            return
        }

        showLoader("Fetching data...")

        fetchAndDisplayUserData()
        loadLeaderboardData()
        loadLatestPosts()
    }

    private fun fetchAndDisplayUserData() {
        UserProfileRepo.fetchUserProfile { userData ->
            binding.textViewUserName.text = userData[Constants.USER_NAME] as? String ?: "Unknown"
            binding.textViewUserFullName.text = userData[Constants.FULL_NAME] as? String ?: "No Name"
            Glide.with(requireContext())
                .load(userData[Constants.PROFILE_PIC] as? String)
                .placeholder(R.drawable.user)
                .into(binding.profilePicHolder)
        }
    }

    private fun loadLeaderboardData() {
        FirebaseDatabase.getInstance().getReference("Leaderboard")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    leaderboardList.clear()
                    snapshot.children.mapNotNullTo(leaderboardList) {
                        it.getValue(LeaderboardItem::class.java)
                    }
                    leaderboardAdapter.notifyDataSetChanged()
                    hideLoader()
                }

                override fun onCancelled(error: DatabaseError) {
                    showNetworkError("Failed to load leaderboard")
                }
            })
    }

    private fun loadLatestPosts() {
        FirebaseDatabase.getInstance().getReference(Constants.POSTS_NODE)
            .orderByChild(Constants.POST_TIME).limitToLast(20)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    postList.clear()
                    val posts = snapshot.children.mapNotNull { it.getValue(Post::class.java) }
                    postList.addAll(posts)
                    postAdapter.notifyDataSetChanged()
                    displayTrendingPosts(posts.sortedByDescending { it.views }.take(5))
                    hideLoader()
                }

                override fun onCancelled(error: DatabaseError) {
                    showNetworkError("Failed to load posts")
                }
            })
    }

    private fun displayTrendingPosts(posts: List<Post>) {
        binding.viewFlipper.removeAllViews()
        posts.forEach { post ->
            val flipperView = TrendingQuestionItemBinding.inflate(layoutInflater).apply {
                textViewUserFullName.text = post.postedByFullName
                textViewUserName.text = post.postedByUsername
                textViewPostTime.text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(post.timestamp))
                postText.text = post.body
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
            binding.viewFlipper.addView(flipperView.root)
        }
        binding.viewFlipper.isAutoStart = true
        binding.viewFlipper.startFlipping()
    }

    private fun monitorNetwork() {
        networkMonitor?.startMonitoring()
        lifecycleScope.launch {
            networkMonitor?.networkStatus?.collect { status ->
                when (status) {
                    is NetworkMonitor.NetworkStatus.Disconnected -> {
                        showCustomToast(requireContext(), "No internet connection", R.drawable.ic_no_internet)
                    }
                    is NetworkMonitor.NetworkStatus.Connected -> {
                        when (status.quality) {
                            NetworkMonitor.ConnectionQuality.Slow -> {
                                showCustomToast(requireContext(), "Slow connection detected", R.drawable.ic_warning)
                            }
                            NetworkMonitor.ConnectionQuality.Moderate -> {
                                showCustomToast(requireContext(), "Moderate connection (mobile data)", R.drawable.ic_warning)
                            }
                            else -> {
                                showCustomToast(requireContext(), "Connected!", R.drawable.ic_check_connection)
                            }
                        }
                        fetchData()
                    }
                    else -> Unit
                }
            }
        }

    }

    private fun showLoader(message: String) {
        binding.spinKit.visibility = View.VISIBLE
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun hideLoader() {
        binding.spinKit.visibility = View.GONE
    }

    private fun showNetworkError(message: String) {
        binding.spinKit.visibility = View.VISIBLE
        showCustomToast(requireContext(), message, R.drawable.ic_error)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        networkMonitor?.stopMonitoring()
        _binding = null
    }
}