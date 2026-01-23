package com.avilesrodriguez.data.datasource.firebase

import android.util.Log
import com.avilesrodriguez.data.datasource.firebase.model.ReferralFirestore
import com.avilesrodriguez.data.datasource.firebase.model.UserDataFirestore
import com.avilesrodriguez.data.datasource.firebase.model.toDomain
import com.avilesrodriguez.data.datasource.firebase.model.toUserDataFirestore
import com.avilesrodriguez.domain.model.user.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
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
        val cleanEmail = email.lowercase().trim()
        return try {
            val doc = firestore.collection(AUTHORIZED_PROVIDERS_COLLECTION)
                .document(cleanEmail).get().await()
            Log.d("StoreDataSource", "Buscando documento con ID: '$cleanEmail'")
            doc.exists()
        } catch (e: Exception) {
            Log.e("StoreDataSource", "Error al consultar Firestore: ${e.message}")
            false
        }
    }

    fun getUsersProvider(): Flow<List<UserData>>{
        val query = firestore.collection(USERS_COLLECTION)
            .whereEqualTo(TYPE_FIELD, TYPE_FIELD_PROVIDER)
            .orderBy(ORDER_BY_FIELD_LOWER, Query.Direction.ASCENDING)
        return createUsersFlow(query)
    }

    fun getUsersProviderByIndustry(industry: String): Flow<List<UserData>>{
        val query = firestore.collection(USERS_COLLECTION)
            .whereEqualTo(TYPE_FIELD, TYPE_FIELD_PROVIDER)
            .whereEqualTo(INDUSTRY_FIELD, industry)
            .orderBy(ORDER_BY_FIELD_LOWER, Query.Direction.ASCENDING)
        return createUsersFlow(query)
    }

    fun searchUsersProvider(namePrefix: String, industry: String? = null): Flow<List<UserData>> {
        var query: Query = firestore.collection(USERS_COLLECTION)
            .whereEqualTo(TYPE_FIELD, TYPE_FIELD_PROVIDER)

        if (!industry.isNullOrBlank()) {
            query = query.whereEqualTo(INDUSTRY_FIELD, industry)
        }

        query = query.orderBy(ORDER_BY_FIELD_LOWER, Query.Direction.ASCENDING)

        if (namePrefix.isNotBlank()) {
            query = query.startAt(namePrefix)
                .endAt(namePrefix + "\uf8ff")
        }

        return createUsersFlow(query)
    }

    fun getUsersClient(currentUserId: String): Flow<List<UserData>> =
        searchUsersClient("", currentUserId = currentUserId)

    fun searchUsersClient(namePrefix: String, currentUserId: String): Flow<List<UserData>> = callbackFlow {
        val query = firestore.collection(REFERRALS_COLLECTION)
            .whereEqualTo(PROVIDER_ID_FIELD, currentUserId)

        val listener: ListenerRegistration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val referrals = snapshot?.documents?.mapNotNull { document ->
                document.toObject(ReferralFirestore::class.java)?.toDomain()
            } ?: emptyList()

            if(referrals.isEmpty()){
                trySend(emptyList()).isSuccess
                return@addSnapshotListener
            }
            launch {
                try {
                    val uniqueClientIds = referrals.map { it.clientId }.distinct()

                    // Ejecutamos todas las peticiones en paralelo usando async
                    val deferredUsers = uniqueClientIds.map { clientId ->
                        async {
                            try {
                                val userDoc = firestore.collection(USERS_COLLECTION)
                                    .document(clientId)
                                    .get()
                                    .await()

                                if (userDoc.exists()) {
                                    // 3. Manejo correcto de polimorfismo (Client vs Provider)
                                    val type = userDoc.getString("type")
                                    val firestoreUser = when (type) {
                                        "CLIENT" -> userDoc.toObject(UserDataFirestore.Client::class.java)
                                        "PROVIDER" -> userDoc.toObject(UserDataFirestore.Provider::class.java)
                                        else -> null
                                    }
                                    firestoreUser?.toDomain()
                                } else null
                            } catch (e: Exception) {
                                Log.e("StoreDataSource", "Error al obtener cliente $clientId: ${e.message}")
                                null
                            }
                        }
                    }

                    // Esperamos a que todas terminen y filtramos los nulos
                    val users = deferredUsers.awaitAll().filterNotNull()
                    val filterUsers = if(namePrefix.isBlank()){
                        users
                    } else{
                        users.filter{it.nameLowercase?.startsWith(namePrefix) == true}
                    }
                    val sortedFilterUsers = filterUsers.sortedBy { it.nameLowercase }
                    trySend(sortedFilterUsers).isSuccess

                } catch (e: Exception) {
                    Log.e("StoreDataSource", "Error en el flujo de usuarios: ${e.message}")
                }
            }
        }
        awaitClose { listener.remove() }
    }

    private fun createUsersFlow(query: Query): Flow<List<UserData>> = callbackFlow {
        val listenerRegistration: ListenerRegistration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val users = snapshot?.documents?.mapNotNull { document ->
                val type = document.getString("type")
                val firestoreUser = when (type) {
                    "CLIENT" -> document.toObject(UserDataFirestore.Client::class.java)
                    "PROVIDER" -> document.toObject(UserDataFirestore.Provider::class.java)
                    else -> null
                }
                firestoreUser?.toDomain()
            } ?: emptyList()
            trySend(users).isSuccess
        }
        awaitClose { listenerRegistration.remove() }
    }


    companion object{
        private const val USERS_COLLECTION = "users"
        private const val IS_ACTIVE_FIELD = "isActive"
        private const val AUTHORIZED_PROVIDERS_COLLECTION = "authorized_providers"
        private const val  TYPE_FIELD = "type"
        private const val TYPE_FIELD_PROVIDER = "PROVIDER"
        private const val ORDER_BY_FIELD_LOWER = "nameLowercase"
        private const val INDUSTRY_FIELD = "industry"
        private const val PROVIDER_ID_FIELD = "providerId"
        private const val REFERRALS_COLLECTION = "referrals"
    }
}
