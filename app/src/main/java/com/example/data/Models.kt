package com.example.data

import java.util.UUID

data class Worksheet(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val content: String,
    val answerKey: String,
    val difficulty: String,
    val dateGenerated: Long = System.currentTimeMillis(),
    val illustrationRes: Int? = null,
    val illustrationUrl: String? = null
)

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean
)
