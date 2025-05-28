package com.example.asknshare.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.asknshare.R
import com.example.asknshare.databinding.ActivityBookmarkBinding
import com.example.asknshare.models.Post
import com.example.asknshare.models.Reply
import com.example.asknshare.ui.adapters.PostAdapter
import com.example.asknshare.ui.adapters.ReplyAdapter
import com.example.asknshare.viewmodels.BookmarkViewModel
import com.example.asknshare.viewmodels.UserPostsViewModel
import com.example.asknshare.viewmodels.UserRepliesViewModel
import com.google.firebase.auth.FirebaseAuth

class BookmarkActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookmarkBinding
    private val bookmarkVM: BookmarkViewModel by viewModels()
    private val userPostsVM: UserPostsViewModel by viewModels()
    private val userRepliesVM: UserRepliesViewModel by viewModels()

    private lateinit var postAdapter: PostAdapter
    private lateinit var replyAdapter: ReplyAdapter
    private val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
    private var contentType: String = "bookmarks"

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

        window.statusBarColor = ContextCompat.getColor(this, R.color.app_grey)
        // Get the content type from intent
        contentType = intent.getStringExtra("content_type") ?: "bookmarks"

        setTopTitle()
        setupRecyclerView()
        setupObservers()
        loadContent()
    }

    private fun setTopTitle() {
        when (contentType) {
            "bookmarks" -> binding.titleText.text = "Your Bookmarks"
            "questions" -> binding.titleText.text= "Your Questions"
            "answers" -> binding.titleText.text= "Your Answers"
        }
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter(emptyList())
        replyAdapter = ReplyAdapter(
            emptyList(),
            currentUserId = currentUserEmail,
            viewModel = userRepliesVM
        )

        binding.recyclerBookmarks.apply {
            layoutManager = LinearLayoutManager(this@BookmarkActivity)
            setHasFixedSize(true)
        }
    }

    private fun setupObservers() {
        when (contentType) {
            "bookmarks" -> {
                bookmarkVM.bookmarkedPosts.observe(this) { posts ->
                    handlePostsResult(posts)
                }
                bookmarkVM.isLoading.observe(this) { isLoading ->
                    handleLoadingState(isLoading)
                }
            }
            "questions" -> {
                userPostsVM.userPosts.observe(this) { posts ->
                    if (posts.isEmpty()) {
                        showEmptyState()
                    } else {
                        postAdapter.updateList(posts)
                        binding.recyclerBookmarks.adapter = postAdapter
                        showContentState()
                    }
                }
                userPostsVM.isLoading.observe(this) { isLoading ->
                    handleLoadingState(isLoading)
                }

                // Fetch user's questions when the activity starts
                userPostsVM.fetchUserPosts(currentUserEmail)
            }
            "answers" -> {
                userRepliesVM.userReplies.observe(this) { replies ->
                    if (replies.isEmpty()) {
                        showEmptyState()
                    } else {
                        replyAdapter.updateList(replies)
                        binding.recyclerBookmarks.adapter = replyAdapter
                        showContentState()
                    }
                }
                userRepliesVM.isLoading.observe(this) { isLoading ->
                    handleLoadingState(isLoading)
                }
            }
        }
    }

    private fun handlePostsResult(posts: List<Post>) {
        if (posts.isEmpty()) {
            showEmptyState()
        } else {
            postAdapter.updateList(posts)
            binding.recyclerBookmarks.adapter = postAdapter
            showContentState()
        }
    }

    private fun handleLoadingState(isLoading: Boolean) {
        if (isLoading) {
            showLoadingState()
        }
    }

    private fun loadContent() {
        when (contentType) {
            "bookmarks" -> bookmarkVM.fetchBookmarkedPosts(currentUserEmail)
            "questions" -> userPostsVM.fetchUserPosts(currentUserEmail)
            "answers" -> userRepliesVM.fetchUserReplies(currentUserEmail)
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

        // Update empty state message based on content type
        binding.noBookmarksText.text = when (contentType) {
            "bookmarks" -> "You haven't bookmarked any posts yet"
            "questions" -> "You haven't asked any questions yet"
            "answers" -> "You haven't answered any questions yet"
            else -> "No content found"
        }
    }

    private fun showContentState() {
        binding.noBookmarksContainer.visibility = View.GONE
        binding.recyclerBookmarks.visibility = View.VISIBLE
        binding.spinKit.visibility = View.GONE
    }

}

private fun ReplyAdapter.updateList(replies: kotlin.collections.List<com.example.asknshare.models.Reply>) {}
