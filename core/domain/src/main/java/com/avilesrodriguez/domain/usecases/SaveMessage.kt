package com.avilesrodriguez.domain.usecases

import com.avilesrodriguez.domain.interfaces.IMessageRepository
import com.avilesrodriguez.domain.model.message.Message
import javax.inject.Inject

class SaveMessage @Inject constructor(
    private val repository: IMessageRepository
) {
    suspend operator fun invoke(message: Message) {
        repository.saveMessage(message)
    }
}