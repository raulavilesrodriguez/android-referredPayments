package com.avilesrodriguez.domain.usecases

import com.avilesrodriguez.domain.interfaces.IStorageRepository
import javax.inject.Inject

class UploadPhoto  @Inject constructor(
    private val repository: IStorageRepository
)  {
    suspend operator fun invoke(localPhoto: String, remotePath: String) {
        repository.uploadPhoto(localPhoto, remotePath)
    }
}