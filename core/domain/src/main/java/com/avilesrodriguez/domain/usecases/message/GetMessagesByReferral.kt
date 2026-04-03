package com.avilesrodriguez.domain.usecases.message

import com.avilesrodriguez.domain.interfaces.IMessageRepository
import javax.inject.Inject

class GetMessagesByReferral @Inject constructor(
    private val repository: IMessageRepository
) {
    operator fun invoke(referralId: String) = repository.getMessagesByReferral(referralId)
}