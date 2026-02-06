package com.avilesrodriguez.domain.model.message

data class Message(
    val id: String = "",
    val referralId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val subject: String = "",
    val content: String = "",
    val attachmentsUrl: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val isDeletedBySender: Boolean = false,
    val isDeletedByReceiver: Boolean = false
)
