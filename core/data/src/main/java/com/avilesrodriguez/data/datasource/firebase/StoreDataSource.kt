package com.avilesrodriguez.data.datasource.firebase

import com.avilesrodriguez.data.datasource.firebase.model.UserDataFirestore
import com.avilesrodriguez.data.datasource.firebase.model.toDomain
import com.avilesrodriguez.data.datasource.firebase.model.toUserDataFirestore
import com.avilesrodriguez.domain.model.user.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class StoreDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {

    suspend fun saveUser(user: UserData) {
        val firestoreUser = user.toUserDataFirestore()

        firestore.collection(USERS_COLLECTION)
            .document(user.uid)
            .set(firestoreUser, SetOptions.merge())
            .await()
    }

    suspend fun getUser(uid: String): UserData? {
        val document = firestore.collection(USERS_COLLECTION)
            .document(uid)
            .get()
            .await()

        if (!document.exists()) return null

        val type = document.getString("type")
        val firestoreUser = when (type) {
            "CLIENT" -> document.toObject(UserDataFirestore.Client::class.java)
            "PROVIDER" -> document.toObject(UserDataFirestore.Provider::class.java)
            else -> null
        }

        return firestoreUser?.toDomain()
    }

    suspend fun deactivateUser(uid: String) {
        firestore.collection(USERS_COLLECTION)
            .document(uid)
            .update(IS_ACTIVE_FIELD, false)
            .await()
    }

    suspend fun reactivateUser(uid: String) {
        firestore.collection(USERS_COLLECTION)
            .document(uid)
            .update(IS_ACTIVE_FIELD, true)
            .await()
    }

    suspend fun secureDeleteAccount(uid: String){
        val document = firestore.collection(USERS_COLLECTION)
            .document(uid).get().await()

        if (!document.exists()) return

        val updateMap = mutableMapOf<String, Any?>(
            "isActive" to false,
            "name" to null,
            "email" to null,
            "photoUrl" to null,
            "fcmToken" to null
        )

        // Update in Firestore
        firestore.collection(USERS_COLLECTION)
            .document(uid)
            .update(updateMap)
            .await()

        // Delete of Firebase AUTH (para que l email quede libre)
        firebaseAuth.currentUser?.delete()?.await()
    }

    suspend fun isAuthorizedProvider(email: String): Boolean {
        return try {
            val doc = firestore.collection(AUTHORIZED_PROVIDERS_COLLECTION)
                .document(email.lowercase()).get().await()
            doc.exists()
        } catch (e: Exception) {
            false
        }
    }



    companion object{
        private const val USERS_COLLECTION = "users"
        private const val IS_ACTIVE_FIELD = "isActive"
        private const val AUTHORIZED_PROVIDERS_COLLECTION = "authorized_providers"

    }
}