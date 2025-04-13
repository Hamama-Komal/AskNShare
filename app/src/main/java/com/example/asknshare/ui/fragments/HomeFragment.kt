package com.example.asknshare.ui.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.asknshare.R
import com.example.asknshare.ui.adapters.LeaderboardAdapter
import com.example.asknshare.ui.adapters.PostAdapter
import com.example.asknshare.ui.adapters.PostPagerAdapter
import com.example.asknshare.databinding.FragmentHomeBinding
import com.example.asknshare.databinding.TrendingQuestionItemBinding
import com.example.asknshare.models.LeaderboardItem
import com.example.asknshare.models.Post
import com.example.asknshare.models.PostModel
import com.example.asknshare.repo.UserProfileRepo
import com.example.asknshare.ui.activities.FullViewActivity
import com.example.asknshare.utils.Constants
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var leaderboardAdapter: LeaderboardAdapter
    private val leaderboardList = mutableListOf<LeaderboardItem>()
    private lateinit var postAdapter: PostAdapter
    private val postList = mutableListOf<Post>()
    private val trendingList = mutableListOf<Post>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        fetchAndDisplayUserData()
        setupLeaderboardRecyclerView()
        loadLeaderboardData()
        setupLatestQuestionRecyclerView()


        return binding.root
    }

    private fun fetchAndDisplayUserData() {
        UserProfileRepo.fetchUserProfile { userData ->
            binding.textViewUserName.text = userData[Constants.USER_NAME] as? String ?: "Unknown User"
            binding.textViewUserFullName.text = userData[Constants.FULL_NAME] as? String ?: "Unknown Full Name"

            val profilePicUrl = userData[Constants.PROFILE_PIC] as? String
            if (!profilePicUrl.isNullOrEmpty()) {
                Glide.with(requireContext())
                    .load(profilePicUrl)
                    .placeholder(R.drawable.user)
                    .into(binding.profilePicHolder)
            } else {
                binding.profilePicHolder.setImageResource(R.drawable.user)
            }
        }
    }

    private fun fetchLatestPostsFromFirebase() {
        val databaseRef = FirebaseDatabase.getInstance().getReference(Constants.POSTS_NODE)

        databaseRef.orderByChild(Constants.POST_TIME).limitToLast(20)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    postList.clear()
                    val allPosts = mutableListOf<Post>()

                    for (postSnapshot in snapshot.children) {
                        val post = postSnapshot.getValue(Post::class.java)
                        post?.let { allPosts.add(it) }
                    }

                    postList.addAll(allPosts)
                    postAdapter.notifyDataSetChanged()

                    // Sort and display trending (top 5 by views)
                    val trendingPosts = allPosts.sortedByDescending { it.views }.take(5)
                    displayTrendingPosts(trendingPosts)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Error fetching posts", Toast.LENGTH_SHORT).show()
                }
            })
    }


    private fun displayTrendingPosts(trendingPosts: List<Post>) {
        binding.viewFlipper.removeAllViews()

        for (post in trendingPosts) {
            val flipperViewBinding = TrendingQuestionItemBinding.inflate(layoutInflater)

            flipperViewBinding.textViewUserFullName.text = post.postedByFullName
            flipperViewBinding.textViewUserName.text = post.postedByUsername
            flipperViewBinding.textViewPostTime.text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(
                Date(post.timestamp)
            )
            flipperViewBinding.postText.text = post.body
            flipperViewBinding.postTitle.text = post.heading



            if (!post.postedByProfile.isNullOrEmpty()) {
                Glide.with(requireContext())
                    .load(post.postedByProfile)
                    .placeholder(R.drawable.user)
                    .into(flipperViewBinding.profilePicHolder)
            } else {
                flipperViewBinding.profilePicHolder.setImageResource(R.drawable.user)
            }

            // Handle Post Click - Open FullViewActivity with postId
            flipperViewBinding.root.setOnClickListener {
                val intent = Intent(requireContext(), FullViewActivity::class.java)
                intent.putExtra("postId", post.postId)
                startActivity(intent)
            }


            binding.viewFlipper.addView(flipperViewBinding.root)
        }

        binding.viewFlipper.isAutoStart = true
        binding.viewFlipper.startFlipping()
    }


    private fun setupLatestQuestionRecyclerView() {

        binding.latestQuestionRecycler.layoutManager = LinearLayoutManager(requireContext())
        postAdapter = PostAdapter(postList)
        binding.latestQuestionRecycler.adapter = postAdapter
        binding.latestQuestionRecycler.setHasFixedSize(true)

        fetchLatestPostsFromFirebase()

    }

   /* private fun fetchLatestPostsFromFirebase() {
        val databaseRef = FirebaseDatabase.getInstance().getReference(Constants.POSTS_NODE)

        databaseRef.orderByChild(Constants.POST_TIME).limitToLast(20)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    postList.clear()
                    val tempList = mutableListOf<Post>()  // Temporary list to store posts

                    for (postSnapshot in snapshot.children) {
                        val post = postSnapshot.getValue(Post::class.java)
                        post?.let { tempList.add(it) }
                    }

                    // Sort the list in descending order (latest first)
                    postList.addAll(tempList.sortedByDescending { it.timestamp })
                    postAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Failed to fetch posts", Toast.LENGTH_SHORT).show()
                }
            })
    }*/


    private fun setupLeaderboardRecyclerView() {
        leaderboardAdapter = LeaderboardAdapter(leaderboardList)
        binding.leaderboardRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = leaderboardAdapter
        }
    }

    private fun loadLeaderboardData() {

        leaderboardList.apply {
            add(LeaderboardItem(R.drawable.user, "User123", "200 Points", "Legend"))
            add(LeaderboardItem(R.drawable.user, "John Doe", "180 Points", "Champion"))
            add(LeaderboardItem(R.drawable.user, "Alice", "150 Points", "Warrior"))
            add(LeaderboardItem(R.drawable.user, "User123", "200 Points", "Legend"))
            add(LeaderboardItem(R.drawable.user, "John Doe", "180 Points", "Champion"))
            add(LeaderboardItem(R.drawable.user, "Alice", "150 Points", "Warrior"))
        }
        //leaderboardAdapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}