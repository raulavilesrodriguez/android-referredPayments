package com.avilesrodriguez.domain.usecases

import com.avilesrodriguez.domain.interfaces.IStorageRepository
import javax.inject.Inject

class UploadFile @Inject constructor(
    private val repository: IStorageRepository
) {
    suspend operator fun invoke(localUri: String, remotePath: String): String {
        return repository.uploadFile(localUri, remotePath)
    }
}