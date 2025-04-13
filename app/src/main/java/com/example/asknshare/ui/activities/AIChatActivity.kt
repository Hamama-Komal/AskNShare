package com.example.asknshare.ui.activities

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import android.graphics.Rect
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.asknshare.R
import com.example.asknshare.databinding.ActivityAichatBinding
import com.example.asknshare.models.Message
import com.example.asknshare.repo.GeminiAIService
import com.example.asknshare.repo.UserProfileRepo
import com.example.asknshare.ui.adapters.ChatAdapter
import com.example.asknshare.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AIChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAichatBinding
    private lateinit var chatAdapter: ChatAdapter

    private val geminiAIService = GeminiAIService(Constants.GEMINI_API_KEY)
    private val messages = mutableListOf<Message>()
    private var userProfile: Map<String, Any?> = emptyMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAichatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        window.statusBarColor = ContextCompat.getColor(this, R.color.app_light_blue)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        chatAdapter = ChatAdapter(mutableListOf())
        binding.rvChat.apply {
            layoutManager = LinearLayoutManager(this@AIChatActivity)
            adapter = chatAdapter
        }

        setupRecyclerView()
        setupClickListeners()
        fetchUserAndShowWelcome()

    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(messages)
        binding.rvChat.apply {
            layoutManager = LinearLayoutManager(this@AIChatActivity)
            adapter = chatAdapter
        }
        binding.rvChat.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if (bottom < oldBottom) {
                binding.rvChat.post {
                    if (messages.isNotEmpty()) {
                        binding.rvChat.smoothScrollToPosition(messages.size - 1)
                    }
                }
            }
        }
    }


    private fun setupClickListeners() {
        binding.btnSend.setOnClickListener {
            val userMessage = binding.etMessageInput.text.toString().trim()
            if (userMessage.isNotEmpty()) {
                sendMessage(userMessage)
                binding.etMessageInput.text.clear()
            } else {
                Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchUserAndShowWelcome() {
        // Show loading message
        val loadingMessage = Message.BotMessage("Loading your profile...")
        chatAdapter.addMessage(loadingMessage)

        UserProfileRepo.fetchUserProfile { userData ->
            // Remove loading message
            messages.removeAt(messages.size - 1)
            chatAdapter.notifyItemRemoved(messages.size)

            userProfile = userData.filterKeys {
                it in listOf(
                    Constants.USER_NAME,
                    Constants.FULL_NAME,
                    Constants.PROFESSION,
                    Constants.SKILLS, // Add this if you have skills field
                    Constants.BIO
                )
            }

            // Generate welcome message with user context
            generateWelcomeMessage()
        }
    }

    private fun generateWelcomeMessage() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val userContext = buildUserContextString()
                val prompt = """
                    You are a helpful assistant. Greet the user and offer your help.
                    Here's what you know about the user:
                    $userContext
                    
                    Keep your greeting friendly and professional, about 1-2 sentences.
                    Offer specific help based on their profile if possible.
                """.trimIndent()

                val welcomeMessage = geminiAIService.generateResponse(prompt)
                val botMessage = Message.BotMessage(welcomeMessage)
                chatAdapter.addMessage(botMessage)
                binding.rvChat.smoothScrollToPosition(messages.size - 1)
            } catch (e: Exception) {
                val errorMessage = Message.BotMessage("Hello! How can I help you today?")
                chatAdapter.addMessage(errorMessage)
                binding.rvChat.smoothScrollToPosition(messages.size - 1)
            }
        }
    }

    private fun buildUserContextString(): String {
        return buildString {
            append("User Profile:\n")
            userProfile[Constants.FULL_NAME]?.let { append("- Name: $it\n") }
            userProfile[Constants.PROFESSION]?.let { append("- Profession: $it\n") }
            userProfile[Constants.BIO]?.let { append("- About: $it\n") }
            if (isEmpty()) append("No profile information available")
        }
    }

    private fun sendMessage(message: String) {
        // Add user message to chat
        val userMessage = Message.UserMessage(message)
        chatAdapter.addMessage(userMessage)
        binding.rvChat.smoothScrollToPosition(messages.size - 1)

        // Show loading indicator
        val loadingMessage = Message.BotMessage("Typing...")
        chatAdapter.addMessage(loadingMessage)
        binding.rvChat.smoothScrollToPosition(messages.size - 1)

        // Get AI response with user context
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val userContext = buildUserContextString()
                val prompt = """ $userContext
                    Current conversation:
                    ${messages.joinToString("\n") {
                    when(it) {
                        is Message.UserMessage -> "User: ${it.content}"
                        is Message.BotMessage -> "Assistant: ${it.content}"
                    }
                }}
                     New message from user: $message
                    Respond helpfully and concisely.
                """.trimIndent()

                val response = geminiAIService.generateResponse(prompt)

                // Remove loading message
                messages.removeAt(messages.size - 1)
                chatAdapter.notifyItemRemoved(messages.size)

                // Add actual response
                val botMessage = Message.BotMessage(response)
                chatAdapter.addMessage(botMessage)
                binding.rvChat.smoothScrollToPosition(messages.size - 1)
            } catch (e: Exception) {
                // Remove loading message
                messages.removeAt(messages.size - 1)
                chatAdapter.notifyItemRemoved(messages.size)

                // Show error message
                val errorMessage = Message.BotMessage("Sorry, I encountered an error. Please try again.")
                chatAdapter.addMessage(errorMessage)
                binding.rvChat.smoothScrollToPosition(messages.size - 1)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }
}
