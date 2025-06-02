package com.example.asknshare.ui.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.asknshare.R
import com.example.asknshare.databinding.ActivitySearchResultBinding
import com.example.asknshare.models.Post
import com.example.asknshare.ui.adapters.PostAdapter
import com.example.asknshare.utils.Constants
import com.google.firebase.database.FirebaseDatabase

class SearchResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchResultBinding
    private lateinit var adapter: PostAdapter
    private val matchedPosts = mutableListOf<Post>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySearchResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        window.statusBarColor = ContextCompat.getColor(this, R.color.app_grey)

        val keyword = intent.getStringExtra("search_keyword") ?: return

        binding.recyclerSearchResults.layoutManager = LinearLayoutManager(this)
        adapter = PostAdapter(matchedPosts)
        binding.recyclerSearchResults.adapter = adapter

        searchPostsByTag(keyword)
    }

    private fun searchPostsByTag(keyword: String) {

        binding.spinKit.visibility = View.VISIBLE
        binding.recyclerSearchResults.visibility = View.GONE
        binding.noResultContainer.visibility = View.GONE

        val ref = FirebaseDatabase.getInstance().getReference(Constants.POSTS_NODE)
        ref.get().addOnSuccessListener { snapshot ->
            matchedPosts.clear()
            for (postSnapshot in snapshot.children) {
                val post = postSnapshot.getValue(Post::class.java)
                if (post != null && post.tags.any { it.contains(keyword, ignoreCase = true) }) {
                    matchedPosts.add(post)
                }
            }

            if (matchedPosts.isEmpty()) {
                binding.spinKit.visibility = View.GONE
                binding.recyclerSearchResults.visibility = View.GONE
                binding.noResultContainer.visibility = View.VISIBLE
                binding.resultText.text = "No results found for \"$keyword\""
            } else {
                binding.spinKit.visibility = View.GONE
                binding.recyclerSearchResults.visibility = View.VISIBLE
                binding.noResultContainer.visibility = View.GONE
                binding.resultText.text = "Results for: \"$keyword\""
            }

        }.addOnFailureListener {
            Toast.makeText(this, "Search failed: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

}