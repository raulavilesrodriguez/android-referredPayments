package com.avilesrodriguez.domain.usecases

import com.avilesrodriguez.domain.interfaces.IFCMTokenRepository
import com.avilesrodriguez.domain.interfaces.IInternalTokenRepository
import com.avilesrodriguez.domain.interfaces.ILocalFCMPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetAndStoreFCMToken @Inject constructor(
    private val fcmRepository: IFCMTokenRepository,
    private val internalTokenRepository: IInternalTokenRepository,
    private val localFCMPreferenceRepository: ILocalFCMPreference
) {
    // withContext ya que se ejecuta en un hilo secundario. Son operaciones entrada/salida (I/O)
    suspend operator fun invoke(uid: String): Boolean = withContext(Dispatchers.IO){
        val newToken = fcmRepository.getFCMToken()
        val (lastUid, lastToken) = localFCMPreferenceRepository.getLastFCMToken()

        if(newToken.isNotBlank() && (newToken != lastToken || uid != lastUid)){
            internalTokenRepository.storeFCMToken(uid, newToken)
            localFCMPreferenceRepository.saveLastRegistration(uid, newToken)
            true
        }else{
            false
        }
    }
}