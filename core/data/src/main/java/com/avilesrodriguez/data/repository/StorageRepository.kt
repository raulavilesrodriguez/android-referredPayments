package com.avilesrodriguez.data.repository

import com.avilesrodriguez.data.datasource.firebase.StorageDataSource
import com.avilesrodriguez.domain.interfaces.IStorageRepository
import javax.inject.Inject

class StorageRepository @Inject constructor(
    private val data: StorageDataSource
) : IStorageRepository {

    override suspend fun uploadPhoto(localPhoto: String, remotePath: String) {
        data.uploadPhoto(localPhoto, remotePath)
    }

    override suspend fun downloadUrlPhoto(remotePath: String): String {
        return data.downloadUrlPhoto(remotePath)
    }

    override suspend fun uploadFile(localUri: String, remotePath: String): String {
        return data.uploadFile(localUri, remotePath)
    }
}