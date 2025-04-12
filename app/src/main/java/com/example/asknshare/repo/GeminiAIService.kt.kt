package com.example.asknshare.repo

import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiAIService(private val apiKey: String) {

    private val generativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-2.0-flash",
            apiKey = apiKey
        )
    }

    suspend fun generateResponse(prompt: String): String {
        return try {
            withContext(Dispatchers.IO) {
                val response = generativeModel.generateContent(prompt)
                response.text ?: "Sorry, I couldn't generate a response."
            }
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}