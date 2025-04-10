package com.example.asknshare.ui.fragments

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
import com.example.asknshare.models.LeaderboardItem
import com.example.asknshare.models.Post
import com.example.asknshare.models.PostModel
import com.example.asknshare.repo.UserProfileRepo
import com.example.asknshare.utils.Constants
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var leaderboardAdapter: LeaderboardAdapter
    private val leaderboardList = mutableListOf<LeaderboardItem>()
    private lateinit var postAdapter: PostAdapter
    private val postList = mutableListOf<Post>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        fetchAndDisplayUserData()
        setupTrendingQuestionPager()
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


    private fun setupTrendingQuestionPager() {

        val postList = listOf(
            PostModel("John Doe", "@john123", "5 min ago", "We evaluate Qwen2.5-Max alongside leading models, whether proprietary or open-weight, across a range of benchmarks that are of significant interest to the community. These include MMLU-Pro, which tests knowledge through college-level problems, LiveCodeBench, which assesses coding capabilities, LiveBench, which comprehensively tests the general capabilities, and Arena-Hard, which approximates human preferences. Our findings include the performance scores for both base models and instruct models.\n" +
                    "\n" + "We begin by directly comparing the performance of the instruct models, which can serve for downstream applications such as chat and coding. We present the performance results of Qwen2.5-Max alongside leading state-of-the-art models, including DeepSeek V3, GPT-4o, and Claude-3.5-Sonnet."),
            PostModel("Jane Smith", "@jane_s", "10 min ago", "is widely recognized that continuously scaling both data size and model size can lead to significant improvements in model intelligence. However, the research and industry community has limited experience in effectively scaling extremely large models, whether they are dense or Mixture-of-Expert (MoE) models"),
            PostModel("Alex Brown", "@alexb", "20 min ago", "java.lang.IllegalStateException: Pages must fill the whole ViewPager2 (use match_parent)\n" +
                    "suggests that your item layout (item_post_layout.xml) is not filling the ViewPager2 properly. ViewPager2 requires that each page completely fills its width, but your item layout might have"),
            PostModel("John Doe", "@john123", "5 min ago", "We evaluate Qwen2.5-Max alongside leading models, whether proprietary or open-weight, across a range of benchmarks that are of significant interest to the community. These include MMLU-Pro, which tests knowledge through college-level problems, LiveCodeBench, which assesses coding capabilities, LiveBench, which comprehensively tests the general capabilities, and Arena-Hard, which approximates human preferences. Our findings include the performance scores for both base models and instruct models.\n" +
                    "\n" + "We begin by directly comparing the performance of the instruct models, which can serve for downstream applications such as chat and coding. We present the performance results of Qwen2.5-Max alongside leading state-of-the-art models, including DeepSeek V3, GPT-4o, and Claude-3.5-Sonnet."),
            PostModel("Jane Smith", "@jane_s", "10 min ago", "is widely recognized that continuously scaling both data size and model size can lead to significant improvements in model intelligence. However, the research and industry community has limited experience in effectively scaling extremely large models, whether they are dense or Mixture-of-Expert (MoE) models"),
            PostModel("Alex Brown", "@alexb", "20 min ago", "java.lang.IllegalStateException: Pages must fill the whole ViewPager2 (use match_parent)\n" +
                    "suggests that your item layout (item_post_layout.xml) is not filling the ViewPager2 properly. ViewPager2 requires that each page completely fills its width, but your item layout might have")

        )

        val adapter = PostPagerAdapter(postList)
        binding.viewPager.adapter = adapter

        // Attach dots indicator to ViewPager2
        binding.dotsIndicator.attachTo(binding.viewPager)
    }

    private fun setupLatestQuestionRecyclerView() {

        binding.latestQuestionRecycler.layoutManager = LinearLayoutManager(requireContext())
        postAdapter = PostAdapter(postList)
        binding.latestQuestionRecycler.adapter = postAdapter
        binding.latestQuestionRecycler.setHasFixedSize(true)

        fetchLatestPostsFromFirebase()

    }

    private fun fetchLatestPostsFromFirebase() {
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
    }



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