package com.fanyiadrien.ictu_ex.data.model

/**
 * Message document stored under chats/{threadId}/messages.
 */
data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val text: String = "",
    val imageUrl: String? = null,
    val createdAt: Long = 0L,
    val listingId: String? = null
)

