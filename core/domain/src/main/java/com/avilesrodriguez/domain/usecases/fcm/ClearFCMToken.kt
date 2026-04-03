package com.avilesrodriguez.domain.usecases.fcm

import com.avilesrodriguez.domain.interfaces.IInternalTokenRepository
import javax.inject.Inject

class ClearFCMToken @Inject constructor(
    private val internalTokenRepository: IInternalTokenRepository
) {
    suspend operator fun invoke(uid: String) {
        internalTokenRepository.clearFCMToken(uid)
    }
}