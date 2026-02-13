package com.avilesrodriguez.data.repository

import com.avilesrodriguez.data.datasource.firebase.MessageDataSource
import com.avilesrodriguez.domain.interfaces.IMessageRepository
import com.avilesrodriguez.domain.model.message.Message
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MessageRepository @Inject constructor(
    private val data: MessageDataSource
): IMessageRepository {
    override suspend fun saveMessage(message: Message) {
        data.saveMessage(message)
    }

    override fun getMessagesByReferral(referralId: String): Flow<List<Message>> {
        return data.getMessagesByReferral(referralId)
    }

    override suspend fun markAsRead(messageId: String) {
        data.markAsRead(messageId)
    }

    override suspend fun markAsDeletedBySender(messageId: String) {
        data.markAsDeletedBySender(messageId)
    }

    override suspend fun markAsDeletedByReceiver(messageId: String) {
        data.markAsDeletedByReceiver(messageId)
    }

    override suspend fun deleteMessagePermanently(messageId: String) {
        data.deleteMessagePermanently(messageId)
    }

    override suspend fun getMessageById(messageId: String): Message? {
        return data.getMessageById(messageId)
    }
}