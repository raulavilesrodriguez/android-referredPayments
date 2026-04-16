package com.avilesrodriguez.domain.usecases.message

import com.avilesrodriguez.domain.interfaces.IMessageRepository
import com.avilesrodriguez.domain.model.message.Message
import jakarta.inject.Inject

class GetMessagesByReferralPaged @Inject constructor(
    private val repository: IMessageRepository
) {
    suspend operator fun invoke(
        referralId: String,
        currentUserId: String,
        pageSize: Long,
        lastMessage: Message?,
        subjectPrefix: String
    ): Pair<List<Message>, Message?>{
        return repository.getMessagesByReferralPaged(referralId, currentUserId, pageSize, lastMessage, subjectPrefix)
    }
}