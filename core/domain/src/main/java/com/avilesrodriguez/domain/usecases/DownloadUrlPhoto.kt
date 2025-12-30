package com.avilesrodriguez.domain.usecases

import com.avilesrodriguez.domain.interfaces.IStorageRepository
import javax.inject.Inject

class DownloadUrlPhoto @Inject constructor(
    private val repository: IStorageRepository
) {
    suspend operator fun invoke(remotePath: String): String {
        return repository.downloadUrlPhoto(remotePath)
    }
}