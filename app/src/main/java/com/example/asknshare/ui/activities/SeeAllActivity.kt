package com.example.asknshare.ui.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.asknshare.R
import com.example.asknshare.databinding.ActivitySeeAllBinding
import com.example.asknshare.ui.adapters.PostAdapter
import com.example.asknshare.viewmodels.HomeViewModel


class SeeAllActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySeeAllBinding
    private val homeVM: HomeViewModel by viewModels()
    private lateinit var postAdapter: PostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySeeAllBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        window.statusBarColor = ContextCompat.getColor(this, R.color.app_grey)

        setupRecyclerView()
        observeViewModel()

    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter(emptyList())
        binding.recyclerSearchResults.apply {
            layoutManager = LinearLayoutManager(this@SeeAllActivity)
            adapter = postAdapter
        }
    }

    private fun observeViewModel() {
        homeVM.latestPosts.observe(this) { posts ->
            postAdapter.updateList(posts)
        }
    }
}