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
    suspend fun getMessageById(messageId: String): Message?
    fun getMessagesByReferralSince(referralId: String, since: Long): Flow<List<Message>>
    suspend fun getMessagesByReferralPaged(referralId: String, currentUserId: String, pageSize: Long, lastMessage: Message?=null, subjectPrefix: String) : Pair<List<Message>, Message?>
}