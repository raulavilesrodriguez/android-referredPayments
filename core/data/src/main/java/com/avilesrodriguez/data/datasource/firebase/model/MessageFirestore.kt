package com.avilesrodriguez.data.datasource.firebase.model

import com.avilesrodriguez.domain.model.message.Message
import com.google.firebase.Timestamp
import java.util.Date

data class MessageFirestore(
    val id: String? = null,
    val referralId: String? = null,
    val senderId: String? = null,
    val receiverId: String? = null,
    val content: String? = null,
    val imagesUrl: List<String> = emptyList(),
    val createdAt: Timestamp? = null
)

fun Message.toFirestore(): MessageFirestore {
    return MessageFirestore(
        id = id,
        referralId = referralId,
        senderId = senderId,
        receiverId = receiverId,
        content = content,
        imagesUrl = imagesUrl,
        createdAt = Timestamp(Date(createdAt))
    )
}

fun MessageFirestore.toDomain(): Message {
    return Message(
        id = id?: "",
        referralId = referralId ?: "",
        senderId = senderId ?: "",
        receiverId = receiverId ?: "",
        content = content ?: "",
        imagesUrl = imagesUrl,
        createdAt = createdAt?.toDate()?.time ?: System.currentTimeMillis()
    )
}
