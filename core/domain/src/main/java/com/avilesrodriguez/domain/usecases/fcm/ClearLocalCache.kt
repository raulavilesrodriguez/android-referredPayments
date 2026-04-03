package com.avilesrodriguez.domain.usecases.fcm

import com.avilesrodriguez.domain.interfaces.ILocalFCMPreference
import javax.inject.Inject

class ClearLocalCache @Inject constructor(
    private val dataSource: ILocalFCMPreference
) {
    suspend operator fun invoke() = dataSource.clearLocalCache()
}