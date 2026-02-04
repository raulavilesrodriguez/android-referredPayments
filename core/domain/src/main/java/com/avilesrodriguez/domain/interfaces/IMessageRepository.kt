package com.avilesrodriguez.domain.interfaces

import com.avilesrodriguez.domain.model.message.Message
import kotlinx.coroutines.flow.Flow

interface IMessageRepository {
    suspend fun saveMessage(message: Message)
    fun getMessagesByReferral(referralId: String): Flow<List<Message>>
    suspend fun markAsRead(messageId: String)
    suspend fun markAsDeletedBySender(messageId: String)
    suspend fun markAsDeletedByReceiver(messageId: String)
    suspend fun deleteMessagePermanently(messageId: String)
}