package com.example.asknshare.ui.activities

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.asknshare.R
import com.example.asknshare.ui.custom.ReplyBottomSheetDialog
import com.example.asknshare.databinding.ActivityFullViewBinding
import com.example.asknshare.models.Post
import com.example.asknshare.ui.adapters.ReplyAdapter
import com.example.asknshare.models.Reply
import com.example.asknshare.ui.adapters.ImageAdapter
import com.example.asknshare.utils.Constants
import com.example.asknshare.viewmodels.BookmarkViewModel
import com.example.asknshare.viewmodels.UserRepliesViewModel
import com.example.asknshare.viewmodels.VoteViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FullViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFullViewBinding
    private lateinit var postRef: DatabaseReference
    private lateinit var bookmarkViewModel: BookmarkViewModel
    private lateinit var voteViewModel: VoteViewModel
    private lateinit var userRepliesViewModel: UserRepliesViewModel
    private var currentPost: Post? = null
    private val userId by lazy { FirebaseAuth.getInstance().currentUser?.uid.orEmpty() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = ContextCompat.getColor(this, R.color.app_light_blue)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        // Initialize ViewModels
        bookmarkViewModel = ViewModelProvider(this).get(BookmarkViewModel::class.java)
        voteViewModel = ViewModelProvider(this).get(VoteViewModel::class.java)
        userRepliesViewModel = ViewModelProvider(this).get(UserRepliesViewModel::class.java)

        setupObservers()

        val postId = intent.getStringExtra("postId") ?: run {
            Toast.makeText(this, "Error: Post not found!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        postRef = FirebaseDatabase.getInstance()
            .getReference(Constants.POSTS_NODE)
            .child(postId)

        fetchPostData()
        setupClickListeners()
    }

    private fun setupObservers() {
        bookmarkViewModel.bookmarkedPosts.observe(this) { posts ->
            currentPost?.let { post ->
                binding.bookmarkButton.setImageResource(
                    if (posts.any { it.postId == post.postId })
                        R.drawable.ic_selected_bookmark
                    else
                        R.drawable.ic_bookmark
                )
            }
        }

        voteViewModel.apply {
            upvoteCount.observe(this@FullViewActivity) { count ->
                binding.upvoteText.text = count.toString()
            }
            downvoteCount.observe(this@FullViewActivity) { count ->
                binding.downvoteText.text = count.toString()
            }
            userUpvoted.observe(this@FullViewActivity) { upvoted ->
                binding.upvoteIcon.setImageResource(
                    if (upvoted) R.drawable.ic_selected_upvotes else R.drawable.ic_upvotes
                )
            }
            userDownvoted.observe(this@FullViewActivity) { downvoted ->
                binding.downvoteIcon.setImageResource(
                    if (downvoted) R.drawable.ic_selected_downvotes else R.drawable.ic_downvotes
                )
            }
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            buttonReplyPost.setOnClickListener {
                ReplyBottomSheetDialog().apply {
                    arguments = Bundle().apply {
                        putString("postId", currentPost?.postId)
                    }
                }.show(supportFragmentManager, "ReplyBottomSheetDialog")
            }

            bookmarkButton.setOnClickListener {
                currentPost?.let { post ->
                    FirebaseAuth.getInstance().currentUser?.email?.let { email ->
                        bookmarkViewModel.toggleBookmark(post, email)
                    }
                }
            }

            upvoteBox.setOnClickListener {
                currentPost?.postId?.let { postId ->
                    voteViewModel.toggleUpVote(postId, userId)
                }
            }

            downvoteBox.setOnClickListener {
                currentPost?.postId?.let { postId ->
                    voteViewModel.toggleDownVote(postId, userId)
                }
            }
        }
    }

    private fun fetchPostData() {
        postRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val post = snapshot.getValue(Post::class.java) ?: run {
                    Toast.makeText(this@FullViewActivity, "Post not found", Toast.LENGTH_SHORT).show()
                    finish()
                    return
                }

                currentPost = post
                displayPostDetails(post)
                incrementViewCount()

                // Load initial states
                FirebaseAuth.getInstance().currentUser?.email?.let {
                    bookmarkViewModel.fetchBookmarkedPosts(it)
                }
                voteViewModel.fetchVoteStatus(post.postId, userId)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@FullViewActivity, "Error loading post", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayPostDetails(post: Post) {
        binding.apply {
            textViewUserFullName.text = post.postedByFullName
            textViewUserName.text = post.postedByUsername
            postTitle.text = post.heading
            postText.text = post.body
            textViewPostTime.text = SimpleDateFormat(
                "dd MMM yyyy", Locale.getDefault()
            ).format(Date(post.timestamp))

            Glide.with(this@FullViewActivity)
                .load(post.postedByProfile)
                .placeholder(R.drawable.user)
                .into(profilePicHolder)

            // Replies
            val replies = mutableListOf<Reply>()
            val replyAdapter = ReplyAdapter(replies, userId, userRepliesViewModel)
            repliesRecycler.layoutManager = LinearLayoutManager(this@FullViewActivity)
            repliesRecycler.adapter = replyAdapter

            postRef.child(Constants.POST_REPLIES).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    replies.clear()
                    snapshot.children.mapNotNull { it.getValue(Reply::class.java) }
                        .also { replies.addAll(it) }
                    replyAdapter.notifyDataSetChanged()
                }
                override fun onCancelled(error: DatabaseError) {}
            })

            // Images
            imageRecycler.layoutManager = GridLayoutManager(this@FullViewActivity, 3)
            imageRecycler.adapter = ImageAdapter(post.images.values.toList())
        }
    }

    private fun incrementViewCount() {
        postRef.child(Constants.POST_VIEWS).runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val current = mutableData.getValue(Int::class.java) ?: 0
                mutableData.value = current + 1
                return Transaction.success(mutableData)
            }
            override fun onComplete(error: DatabaseError?, committed: Boolean, data: DataSnapshot?) {
                if (error != null) {
                    Toast.makeText(
                        this@FullViewActivity,
                        "Failed to update views",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }
}

