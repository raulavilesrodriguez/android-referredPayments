package com.avilesrodriguez.data.datasource.firebase.model

import com.avilesrodriguez.domain.model.message.Message
import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import java.util.Date

data class MessageFirestore(
    val id: String? = null,
    val referralId: String? = null,
    val senderId: String? = null,
    val receiverId: String? = null,
    val subject: String? = null,
    val content: String? = null,
    val imagesUrl: List<String> = emptyList(),
    val createdAt: Timestamp? = null,
    @get:PropertyName("isRead")
    val isRead: Boolean? = null,
    @get:PropertyName("isDeletedBySender")
    val isDeletedBySender: Boolean? = null,
    @get:PropertyName("isDeletedByReceiver")
    val isDeletedByReceiver: Boolean? = null
)

fun Message.toMessageFirestore(): MessageFirestore {
    return MessageFirestore(
        id = id,
        referralId = referralId,
        senderId = senderId,
        receiverId = receiverId,
        subject = subject,
        content = content,
        imagesUrl = imagesUrl,
        createdAt = Timestamp(Date(createdAt)),
        isRead = isRead,
        isDeletedBySender = isDeletedBySender,
        isDeletedByReceiver = isDeletedByReceiver
    )
}

fun MessageFirestore.toMessageDomain(): Message {
    return Message(
        id = id ?: "",
        referralId = referralId ?: "",
        senderId = senderId ?: "",
        receiverId = receiverId ?: "",
        subject = subject ?: "",
        content = content ?: "",
        imagesUrl = imagesUrl,
        createdAt = createdAt?.toDate()?.time ?: System.currentTimeMillis(),
        isRead = isRead ?: false,
        isDeletedBySender = isDeletedBySender ?: false,
        isDeletedByReceiver = isDeletedByReceiver ?: false
    )
}
