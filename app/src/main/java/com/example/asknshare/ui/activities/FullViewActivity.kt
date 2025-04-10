package com.example.asknshare.ui.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
    private var postId: String? = null
    private val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()

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

        // Set Status Bar Color to Light Blue
        window.statusBarColor = ContextCompat.getColor(this, R.color.app_light_blue)


        postId = intent.getStringExtra("postId")

        if (postId == null) {
            Toast.makeText(this, "Error: Post not found!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        postRef = FirebaseDatabase.getInstance().getReference(Constants.POSTS_NODE).child(postId!!)

        fetchPostData()
        incrementViewCount()

        // Open Bottom Sheet on button click
        binding.buttonReplyPost.setOnClickListener {
            val bottomSheet = ReplyBottomSheetDialog()
            val bundle = Bundle()
            bundle.putString("postId", postId) // Pass postId
            bottomSheet.arguments = bundle
            bottomSheet.show(supportFragmentManager, "ReplyBottomSheetDialog")
        }

    }


    private fun fetchPostData() {
        postRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val post = snapshot.getValue(Post::class.java)
                if (post != null) {
                    displayPostDetails(post)
                } else {
                    Toast.makeText(this@FullViewActivity, "Post not found", Toast.LENGTH_SHORT)
                        .show()
                    finish()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@FullViewActivity, "Error loading post", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private fun displayPostDetails(post: Post) {
        binding.textViewUserFullName.text = post.postedByFullName
        binding.textViewUserName.text = post.postedByUsername
        binding.postTitle.text = post.heading
        binding.postText.text = post.body
        binding.textViewPostTime.text =
            java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
                .format(java.util.Date(post.timestamp))

        // Load profile picture
        Glide.with(this).load(post.postedByProfile).placeholder(R.drawable.user)
            .into(binding.profilePicHolder)

        // Set Views, Likes, and Comments Count
        binding.viewsText.text = post.views.toString()
        binding.upvoteText.text = post.upVotes.size.toString()
        binding.downvoteText.text = post.downVotes.size.toString()
        binding.replyText.text = post.replies.size.toString()

        // Load Images into ImageRecycler
        val imageAdapter = ImageAdapter(post.images.values.toList())
        binding.imageRecycler.layoutManager = GridLayoutManager(this, 3)
        binding.imageRecycler.adapter = imageAdapter

        // Load replies in RecyclerView
        val replyList = mutableListOf<Reply>()
        val replyAdapter = ReplyAdapter(replyList)
        binding.repliesRecycler.layoutManager = LinearLayoutManager(this)
        binding.repliesRecycler.adapter = replyAdapter

        postRef.child("replies").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                replyList.clear()
                for (replySnapshot in snapshot.children) {
                    val reply = replySnapshot.getValue(Reply::class.java)
                    if (reply != null) {
                        replyList.add(reply)
                    }
                }
                replyAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })


        // Update vote counts and check user vote status
        updateVoteCounts(postRef, binding)
        checkUserVoteStatus(postRef, binding, userId)

        // Handle Upvote Click
        binding.upvoteBox.setOnClickListener {
            postRef.child(Constants.POST_UP_VOTES).child(userId).get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        // Remove upvote
                        postRef.child(Constants.POST_UP_VOTES).child(userId).removeValue()
                        binding.upvoteIcon.setImageResource(R.drawable.ic_upvotes)
                    } else {
                        // Add upvote
                        postRef.child(Constants.POST_UP_VOTES).child(userId).setValue(true)
                        binding.upvoteIcon.setImageResource(R.drawable.ic_selected_upvotes)

                        // Remove downvote if exists
                        postRef.child(Constants.POST_DOWN_VOTES).child(userId).removeValue()
                        binding.upvoteIcon.setImageResource(R.drawable.ic_upvotes)
                    }
                    updateVoteCounts(postRef, binding)
                    checkUserVoteStatus(postRef, binding, userId)
                }
        }

        // Handle Downvote Click
        binding.downvoteBox.setOnClickListener {
            postRef.child(Constants.POST_DOWN_VOTES).child(userId).get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        // Remove downvote
                        postRef.child(Constants.POST_DOWN_VOTES).child(userId).removeValue()
                        binding.downvoteIcon.setImageResource(R.drawable.ic_downvotes)
                    } else {
                        // Add downvote
                        postRef.child(Constants.POST_DOWN_VOTES).child(userId).setValue(true)
                        binding.downvoteIcon.setImageResource(R.drawable.ic_selected_downvotes)

                        // Remove upvote if exists
                        postRef.child(Constants.POST_UP_VOTES).child(userId).removeValue()
                        binding.downvoteIcon.setImageResource(R.drawable.ic_downvotes)
                    }
                    updateVoteCounts(postRef, binding)
                    checkUserVoteStatus(postRef, binding, userId)
                }
        }
    }

    private fun updateVoteCounts(postRef: DatabaseReference, binding: ActivityFullViewBinding) {
        postRef.child(Constants.POST_UP_VOTES).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val upvoteCount = snapshot.childrenCount.toInt()
                binding.upvoteText.text = upvoteCount.toString()
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        postRef.child(Constants.POST_DOWN_VOTES).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val downvoteCount = snapshot.childrenCount.toInt()
                binding.downvoteText.text = downvoteCount.toString()
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        postRef.child(Constants.POST_REPLIES).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val commentCount = snapshot.childrenCount.toInt()
                binding.replyText.text = commentCount.toString()
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        postRef.child(Constants.POST_VIEWS).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val views = snapshot.getValue(Int::class.java) ?: 0
                binding.viewsText.text = views.toString()
            }

            override fun onCancelled(error: DatabaseError) {}
        })

    }

    private fun incrementViewCount() {
        postRef.child(Constants.POST_VIEWS).runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val currentViews = mutableData.getValue(Int::class.java) ?: 0
                mutableData.value = currentViews + 1
                return Transaction.success(mutableData)
            }

            override fun onComplete(
                databaseError: DatabaseError?, committed: Boolean, dataSnapshot: DataSnapshot?
            ) {
                if (databaseError != null) {
                    Toast.makeText(
                        this@FullViewActivity, "Failed to update views", Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

    private fun checkUserVoteStatus(postRef: DatabaseReference, binding: ActivityFullViewBinding, userId: String){

        postRef.child(Constants.POST_UP_VOTES).child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        binding.upvoteIcon.setImageResource(R.drawable.ic_selected_upvotes)
                    } else {
                        binding.upvoteIcon.setImageResource(R.drawable.ic_upvotes)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        postRef.child(Constants.POST_DOWN_VOTES).child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        binding.downvoteIcon.setImageResource(R.drawable.ic_selected_downvotes)
                    } else {
                        binding.downvoteIcon.setImageResource(R.drawable.ic_downvotes)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

}

