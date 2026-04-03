package com.avilesrodriguez.domain.usecases.message

import com.avilesrodriguez.domain.interfaces.IMessageRepository
import com.avilesrodriguez.domain.model.message.Message
import javax.inject.Inject

class GetMessageById @Inject constructor(
    private val repository: IMessageRepository
) {
    suspend operator fun invoke(messageId: String): Message? = repository.getMessageById(messageId)
}