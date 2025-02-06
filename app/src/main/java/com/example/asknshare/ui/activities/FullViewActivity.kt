package com.example.asknshare.ui.activities

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.asknshare.R
import com.example.asknshare.ui.custom.ReplyBottomSheetDialog
import com.example.asknshare.databinding.ActivityFullViewBinding
import com.example.asknshare.ui.fragments.ReplyAdapter
import com.example.asknshare.models.Reply

class FullViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFullViewBinding
    private lateinit var replyAdapter: ReplyAdapter
    private val replyList = mutableListOf<Reply>()

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

        binding.repliesRecycler.layoutManager = LinearLayoutManager(this)

        // Initialize adapter
        replyAdapter = ReplyAdapter(replyList)
        binding.repliesRecycler.adapter = replyAdapter

        // Fetch replies (replace this with actual data fetching logic)
        fetchReplies()

        // Open Bottom Sheet on button click
        binding.buttonReplyPost.setOnClickListener {
            val bottomSheet = ReplyBottomSheetDialog()
            bottomSheet.show(supportFragmentManager, "ReplyBottomSheetDialog")
        }
    }

    private fun fetchReplies() {
        // Dummy data (replace with actual API call or database query)
        replyList.add(Reply("User1", "This is a sample reply", R.drawable.user))
        replyList.add(Reply("User2", "Another reply here", R.drawable.user))

        // Update UI
        replyAdapter.notifyDataSetChanged()
        binding.repliesRecycler.visibility = if (replyList.isNotEmpty()) View.VISIBLE else View.GONE
    }
}