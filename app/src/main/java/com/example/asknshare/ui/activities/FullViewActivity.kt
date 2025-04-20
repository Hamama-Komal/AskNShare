package com.example.asknshare.ui.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
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
import com.example.asknshare.viewmodels.VoteViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener

class FullViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFullViewBinding
    private lateinit var postRef: DatabaseReference
    private lateinit var bookmarkViewModel: BookmarkViewModel
    private lateinit var voteViewModel: VoteViewModel
    private var currentPost: Post? = null
    private val userId by lazy { FirebaseAuth.getInstance().currentUser?.uid.orEmpty() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFullViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        window.statusBarColor = ContextCompat.getColor(this, R.color.app_light_blue)

        // ViewModels
        bookmarkViewModel = ViewModelProvider(this).get(BookmarkViewModel::class.java)
        voteViewModel = ViewModelProvider(this).get(VoteViewModel::class.java)
        subscribeBookmarkState()
        subscribeVoteState()

        binding.mainLayout.visibility = View.GONE
        binding.spinKit.visibility = View.VISIBLE

        val postId = intent.getStringExtra("postId")
        if (postId.isNullOrEmpty()) {
            binding.spinKit.visibility = View.GONE
            Toast.makeText(this, "Error: Post not found!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        postRef = FirebaseDatabase
            .getInstance()
            .getReference(Constants.POSTS_NODE)
            .child(postId)

        fetchPostData()
        incrementViewCount()

        binding.buttonReplyPost.setOnClickListener {
            ReplyBottomSheetDialog().apply {
                arguments = Bundle().apply { putString("postId", postId) }
            }.show(supportFragmentManager, "ReplyBottomSheetDialog")
        }

        binding.bookmarkButton.setOnClickListener {
            currentPost?.let { post ->
                FirebaseAuth.getInstance().currentUser?.email?.let { email ->
                    bookmarkViewModel.toggleBookmark(post, email)
                }
            }
        }

        binding.upvoteBox.setOnClickListener {
            currentPost?.postId?.let { voteViewModel.toggleUpVote(it, userId) }
        }

        binding.downvoteBox.setOnClickListener {
            currentPost?.postId?.let { voteViewModel.toggleDownVote(it, userId) }
        }
    }

    private fun subscribeBookmarkState() {
        bookmarkViewModel.bookmarkedPosts.observe(this) { posts ->
            val isBookmarked = currentPost?.let { post ->
                posts.any { it.postId == post.postId }
            } ?: false
            binding.bookmarkButton.setImageResource(
                if (isBookmarked) R.drawable.ic_selected_bookmark else R.drawable.ic_bookmark
            )
        }
    }

    private fun subscribeVoteState() {
        voteViewModel.upvoteCount.observe(this) { count ->
            binding.upvoteText.text = count.toString()
        }
        voteViewModel.downvoteCount.observe(this) { count ->
            binding.downvoteText.text = count.toString()
        }
        voteViewModel.userUpvoted.observe(this) { upvoted ->
            binding.upvoteIcon.setImageResource(
                if (upvoted) R.drawable.ic_selected_upvotes else R.drawable.ic_upvotes
            )
        }
        voteViewModel.userDownvoted.observe(this) { downvoted ->
            binding.downvoteIcon.setImageResource(
                if (downvoted) R.drawable.ic_selected_downvotes else R.drawable.ic_downvotes
            )
        }
    }

    private fun fetchPostData() {
        postRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val post = snapshot.getValue(Post::class.java)
                if (post != null) {
                    currentPost = post
                    binding.mainLayout.visibility = View.VISIBLE
                    binding.spinKit.visibility = View.GONE
                    displayPostDetails(post)

                    // load bookmark list & vote status
                    FirebaseAuth.getInstance().currentUser?.email?.let {
                        bookmarkViewModel.fetchBookmarkedPosts(it)
                    }
                    voteViewModel.fetchVoteStatus(post.postId, userId)
                } else {
                    binding.spinKit.visibility = View.GONE
                    Toast.makeText(this@FullViewActivity, "Post not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                binding.spinKit.visibility = View.GONE
                Toast.makeText(this@FullViewActivity, "Error loading post", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayPostDetails(post: Post) {
        binding.textViewUserFullName.text = post.postedByFullName
        binding.textViewUserName.text = post.postedByUsername
        binding.postTitle.text = post.heading
        binding.postText.text = post.body
        binding.textViewPostTime.text = java.text.SimpleDateFormat(
            "dd MMM yyyy", java.util.Locale.getDefault()
        ).format(java.util.Date(post.timestamp))

        Glide.with(this)
            .load(post.postedByProfile)
            .placeholder(R.drawable.user)
            .into(binding.profilePicHolder)

        // Replies
        val replies = mutableListOf<Reply>()
        val replyAdapter = ReplyAdapter(replies)
        binding.repliesRecycler.layoutManager = LinearLayoutManager(this)
        binding.repliesRecycler.adapter = replyAdapter
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
        binding.imageRecycler.layoutManager = GridLayoutManager(this, 3)
        binding.imageRecycler.adapter = ImageAdapter(post.images.values.toList())
    }

    private fun incrementViewCount() {
        postRef.child(Constants.POST_VIEWS).runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val current = mutableData.getValue(Int::class.java) ?: 0
                mutableData.value = current + 1
                return Transaction.success(mutableData)
            }
            override fun onComplete(error: DatabaseError?, committed: Boolean, data: DataSnapshot?) {
                if (error != null) Toast.makeText(
                    this@FullViewActivity,
                    "Failed to update views",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

}

