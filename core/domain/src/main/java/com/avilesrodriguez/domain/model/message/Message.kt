package com.avilesrodriguez.domain.model.message

data class Message(
    val id: String = "",
    val referralId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val content: String = "",
    val imagesUrl: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)
