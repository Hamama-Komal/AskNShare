package com.example.asknshare.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.asknshare.R
import com.example.asknshare.databinding.ActivityBookmarkBinding
import com.example.asknshare.models.Post
import com.example.asknshare.ui.adapters.PostAdapter
import com.example.asknshare.viewmodels.BookmarkViewModel
import com.google.firebase.auth.FirebaseAuth

class BookmarkActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookmarkBinding
    private val viewModel: BookmarkViewModel by viewModels()
    private lateinit var postAdapter: PostAdapter
    private val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBookmarkBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupToolbar()
        setupRecyclerView()
        setupObservers()
        loadBookmarks()
    }


    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        // Initialize with empty list, will be updated when data loads
        postAdapter = PostAdapter(emptyList())

        binding.recyclerBookmarks.apply {
            layoutManager = LinearLayoutManager(this@BookmarkActivity)
            adapter = postAdapter
            setHasFixedSize(true)
        }


    }

    private fun setupObservers() {
        viewModel.bookmarkedPosts.observe(this) { posts ->
            binding.spinKit.visibility = View.GONE
            postAdapter.updateList(posts)

            if (posts.isEmpty()) {
                showEmptyState()
            } else {
                showBookmarkedPosts(posts)
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                showLoadingState()
            }
        }
    }

    private fun showLoadingState() {
        binding.spinKit.visibility = View.VISIBLE
        binding.recyclerBookmarks.visibility = View.GONE
        binding.noBookmarksContainer.visibility = View.GONE
    }

    private fun showEmptyState() {
        binding.noBookmarksContainer.visibility = View.VISIBLE
        binding.recyclerBookmarks.visibility = View.GONE
        binding.spinKit.visibility = View.GONE
    }

    private fun showBookmarkedPosts(posts: List<Post>) {
        binding.noBookmarksContainer.visibility = View.GONE
        binding.recyclerBookmarks.visibility = View.VISIBLE
        binding.spinKit.visibility = View.GONE

        // Update the adapter with new data
        postAdapter = PostAdapter(posts)
        binding.recyclerBookmarks.adapter = postAdapter
    }

    private fun loadBookmarks() {
        viewModel.fetchBookmarkedPosts(currentUserEmail)
    }

}