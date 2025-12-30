package com.avilesrodriguez.domain.interfaces

interface IStorageRepository {

    suspend fun uploadPhoto(localPhoto: String, remotePath: String)
    suspend fun downloadUrlPhoto(remotePath: String) : String
}