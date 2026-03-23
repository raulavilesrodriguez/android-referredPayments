package com.avilesrodriguez.data.datasource.firebase

import android.util.Log
import com.avilesrodriguez.data.datasource.firebase.model.ReferralFirestore
import com.avilesrodriguez.data.datasource.firebase.model.toReferralDomain
import com.avilesrodriguez.data.datasource.firebase.model.toReferralFirestore
import com.avilesrodriguez.domain.model.referral.Referral
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

class ReferralDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    suspend fun saveReferral(referral: Referral): Boolean {
        val referralFirestore = referral.toReferralFirestore()
        val docRef = firestore.collection(REFERRALS_COLLECTION)
            .document(referral.numberPhone)
        return try {
            val transactionResult = firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                if (snapshot.exists()) {
                    false
                } else {
                    transaction.set(docRef, referralFirestore.copy(id = docRef.id))
                    true
                }
            }.await()
            transactionResult == true
        } catch (e: Exception){
            Log.e("ReferralDataSource", "Error saving referral", e)
            throw e
        }
    }

    suspend fun updateReferralFields(referralId: String, updates: Map<String, Any>) {
        if (referralId.isEmpty()) return

        val firestoreUpdates = updates.mapValues { entry ->
            if (entry.key == CREATED_AT_FIELD && entry.value is Long) {
                Timestamp(Date(entry.value as Long))
            } else {
                entry.value
            }
        }

        firestore.collection(REFERRALS_COLLECTION)
            .document(referralId)
            .update(firestoreUpdates)
            .await()
    }

    fun getReferralsByClient(clientId: String): Flow<List<Referral>> {
        val query = firestore.collection(REFERRALS_COLLECTION)
            .whereEqualTo(CLIENT_ID_FIELD, clientId)
            .orderBy(CREATED_AT_FIELD, Query.Direction.DESCENDING)

        return createReferralFlow(query)
    }

    fun getReferralsByProvider(providerId: String): Flow<List<Referral>> {
        val query = firestore.collection(REFERRALS_COLLECTION)
            .whereEqualTo(PROVIDER_ID_FIELD, providerId)
            .orderBy(CREATED_AT_FIELD, Query.Direction.DESCENDING)

        return createReferralFlow(query)
    }

    fun getReferralsByClientByProvider(clientId: String, providerId: String): Flow<List<Referral>>{
        val query = firestore.collection(REFERRALS_COLLECTION)
            .whereEqualTo(CLIENT_ID_FIELD, clientId)
            .whereEqualTo(PROVIDER_ID_FIELD, providerId)
            .orderBy(CREATED_AT_FIELD, Query.Direction.DESCENDING) //the more nuevos primero

        return createReferralFlow(query)
    }

    suspend fun getReferralById(referralId: String): Referral? {
        val documentSnapshot = firestore.collection(REFERRALS_COLLECTION)
            .document(referralId)
            .get()
            .await()

        return documentSnapshot.toObject(ReferralFirestore::class.java)?.toReferralDomain()
    }

    fun getReferralByIdFlow(id: String): Flow<Referral?> = callbackFlow {
        val query = firestore.collection(REFERRALS_COLLECTION).document(id)
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val referral = snapshot?.toObject(ReferralFirestore::class.java)?.toReferralDomain()
            trySend(referral).isSuccess
        }
        awaitClose { listener.remove() }
    }

    suspend fun updateReferralStatus(referralId: String, status: String, voucherUrl: String?) {
        val updates = mutableMapOf<String, Any>(
            STATUS_FIELD to status
        )
        voucherUrl?.let { updates[VOUCHER_URL_FIELD] = it }

        firestore.collection(REFERRALS_COLLECTION)
            .document(referralId)
            .update(updates)
            .await()
    }

    fun searchReferralsByClient(namePrefix: String, currentUserId: String): Flow<List<Referral>> {
        val query = firestore.collection(REFERRALS_COLLECTION)
            .whereEqualTo(CLIENT_ID_FIELD, currentUserId)
            .orderBy(ORDER_BY_FIELD_LOWER, Query.Direction.ASCENDING)
            .startAt(namePrefix)
            .endAt(namePrefix + "\uf8ff")
        return createReferralFlow(query)
    }

    fun searchReferralsByProvider(namePrefix: String, currentUserId: String): Flow<List<Referral>> {
        val query = firestore.collection(REFERRALS_COLLECTION)
            .whereEqualTo(PROVIDER_ID_FIELD, currentUserId)
            .orderBy(ORDER_BY_FIELD_LOWER, Query.Direction.ASCENDING)
            .startAt(namePrefix)
            .endAt(namePrefix + "\uf8ff")
        return createReferralFlow(query)
    }

    fun searchReferralsByClientAndProvider(
        namePrefix: String,
        clientId: String,
        providerId: String
    ): Flow<List<Referral>> {
        val query = firestore.collection(REFERRALS_COLLECTION)
            .whereEqualTo(CLIENT_ID_FIELD, clientId)
            .whereEqualTo(PROVIDER_ID_FIELD, providerId)
            .orderBy(ORDER_BY_FIELD_LOWER, Query.Direction.ASCENDING)
            .startAt(namePrefix)
            .endAt(namePrefix + "\uf8ff")
        return createReferralFlow(query)
    }

    private fun createReferralFlow(query: Query): Flow<List<Referral>> = callbackFlow{
        val listenerRegistration: ListenerRegistration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val referrals = snapshot?.documents?.mapNotNull { document ->
                document.toObject(ReferralFirestore::class.java)?.toReferralDomain()
            } ?: emptyList()

            trySend(referrals).isSuccess
        }
        awaitClose { listenerRegistration.remove() }
    }

    suspend fun saveRatingWithTransaction(
        referralId: String,
        referralUpdates: Map<String, Any>,
        providerId: String,
        ratingReferral: Double
    ) {
        firestore.runTransaction { transaction ->
            val referralRef = firestore.collection(REFERRALS_COLLECTION).document(referralId)
            val providerRef = firestore.collection(USERS_COLLECTION).document(providerId)

            val providerSnapshot = transaction.get(providerRef)
            val oldCount = providerSnapshot.getLong(RATING_COUNT_FIELD_USER) ?: 0L
            val oldRating = providerSnapshot.getDouble(PAYMENT_RATING_FIELD_USER) ?: 0.0

            // persistencia atómica
            val newCount = oldCount + 1
            val newAverage = ((oldRating * oldCount) + ratingReferral) / newCount

            // 3. ESCRITURA
            transaction.update(referralRef, referralUpdates)
            transaction.update(providerRef, mapOf(
                PAYMENT_RATING_FIELD_USER to newAverage,
                RATING_COUNT_FIELD_USER to newCount
            ))
        }.await()
    }

    fun getReferralsByClientSince(clientId: String, since: Long) : Flow<List<Referral>>{
        val sinceTimestamp = Timestamp(Date(since))
        val query = firestore.collection(REFERRALS_COLLECTION)
            .whereEqualTo(CLIENT_ID_FIELD, clientId)
            .orderBy(CREATED_AT_FIELD, Query.Direction.DESCENDING) // mas viejos al final
            .whereGreaterThanOrEqualTo(CREATED_AT_FIELD, sinceTimestamp)

        return createReferralFlow(query)
    }

    fun getReferralsByProviderSince(providerId: String, since: Long) : Flow<List<Referral>>{
        val sinceTimestamp = Timestamp(Date(since))
        val query = firestore.collection(REFERRALS_COLLECTION)
            .whereEqualTo(PROVIDER_ID_FIELD, providerId)
            .orderBy(CREATED_AT_FIELD, Query.Direction.DESCENDING) // mas viejos al final
            .whereGreaterThanOrEqualTo(CREATED_AT_FIELD, sinceTimestamp)

        return createReferralFlow(query)
    }

    suspend fun getReferralsByClientPaged(
        clientId: String,
        pageSize: Long,
        lastReferral: Referral? = null
    ) : Pair<List<Referral>, Referral?> {
        var query = firestore.collection(REFERRALS_COLLECTION)
            .whereEqualTo(CLIENT_ID_FIELD, clientId)
            .orderBy(CREATED_AT_FIELD, Query.Direction.DESCENDING) //mas olds final
            .limit(pageSize)

        // continue the consult from the last referral
        if (lastReferral != null) {
            val lastReferralRef = firestore.collection(REFERRALS_COLLECTION).document(lastReferral.id)
            val lastDocument = lastReferralRef.get().await()
            if(lastDocument !=null) query = query.startAfter(lastDocument)
        }

        val snapshot = query.get().await()

        val referrals = snapshot.documents.mapNotNull { doc ->
            val referral = doc.toObject(ReferralFirestore::class.java)?.toReferralDomain()
            referral
        }

        val lastVisibleDocument = snapshot?.documents?.lastOrNull()
        val lastVisibleReferral = lastVisibleDocument?.toObject(ReferralFirestore::class.java)?.toReferralDomain()
        return referrals to lastVisibleReferral
    }

    suspend fun getReferralsByProviderPaged(
        providerId: String,
        pageSize: Long,
        lastReferral: Referral? = null
    ) : Pair<List<Referral>, Referral?>{
        var query = firestore.collection(REFERRALS_COLLECTION)
            .whereEqualTo(PROVIDER_ID_FIELD, providerId)
            .orderBy(CREATED_AT_FIELD, Query.Direction.DESCENDING) //mas olds final
            .limit(pageSize)

        // continue the consult from the last referral
        if (lastReferral != null) {
            val lastReferralRef = firestore.collection(REFERRALS_COLLECTION).document(lastReferral.id)
            val lastDocument = lastReferralRef.get().await()
            if(lastDocument !=null) query = query.startAfter(lastDocument)
        }

        val snapshot = query.get().await()

        val referrals = snapshot.documents.mapNotNull { doc ->
            val referral = doc.toObject(ReferralFirestore::class.java)?.toReferralDomain()
            referral
        }
        val lastVisibleDocument = snapshot?.documents?.lastOrNull()
        val lastVisibleReferral = lastVisibleDocument?.toObject(ReferralFirestore::class.java)?.toReferralDomain()
        return referrals to lastVisibleReferral
    }

    companion object {
        private const val REFERRALS_COLLECTION = "referrals"
        private const val USERS_COLLECTION = "users"
        private const val CLIENT_ID_FIELD = "clientId"
        private const val PROVIDER_ID_FIELD = "providerId"
        private const val STATUS_FIELD = "status"
        private const val VOUCHER_URL_FIELD = "voucherUrl"
        private const val ORDER_BY_FIELD_LOWER = "nameLowercase"
        private const val CREATED_AT_FIELD = "createdAt"
        private const val RATING_COUNT_FIELD_USER = "ratingCount"
        private const val PAYMENT_RATING_FIELD_USER = "paymentRating"
    }
}
