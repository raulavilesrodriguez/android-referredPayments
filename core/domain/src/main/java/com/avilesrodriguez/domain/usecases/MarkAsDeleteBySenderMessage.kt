package com.avilesrodriguez.domain.usecases

import com.avilesrodriguez.domain.interfaces.IMessageRepository
import javax.inject.Inject

class MarkAsDeleteBySenderMessage @Inject constructor(
    private val repository: IMessageRepository
) {
    suspend operator fun invoke(messageId: String) {
        repository.markAsDeletedBySender(messageId)
    }
}