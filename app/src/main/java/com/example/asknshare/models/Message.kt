package com.example.asknshare.models

sealed class Message {
    data class UserMessage(val content: String) : Message()
    data class BotMessage(val content: String) : Message()
}