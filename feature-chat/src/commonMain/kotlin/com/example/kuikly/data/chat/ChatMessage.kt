package com.example.kuikly.data.chat

data class ChatMessage(
    val id: String,
    val conversationId: String,
    val content: String,
    val isSelf: Boolean,
    /**
     * Mirrors Flutter `MessageReadStatus` (read/unread) as its rendered caption.
     * Flutter seeds both conversation-1 messages as `read`, which the detail
     * page renders as 「已读」 under the self bubble.
     */
    val readStatus: String = "已读",
)
