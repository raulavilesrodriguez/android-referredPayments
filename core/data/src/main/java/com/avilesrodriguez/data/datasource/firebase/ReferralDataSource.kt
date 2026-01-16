package com.avilesrodriguez.data.datasource.firebase

import com.avilesrodriguez.data.datasource.firebase.model.ReferralFirestore
import com.avilesrodriguez.data.datasource.firebase.model.toDomain
import com.avilesrodriguez.data.datasource.firebase.model.toFirestore
import com.avilesrodriguez.domain.model.referral.Referral
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ReferralDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    suspend fun saveReferral(referral: Referral) {
        val referralFirestore = referral.toFirestore()
        val docRef = if (referral.id.isEmpty()) {
            firestore.collection(REFERRALS_COLLECTION).document()
        } else {
            firestore.collection(REFERRALS_COLLECTION).document(referral.id)
        }

        docRef.set(referralFirestore.copy(id = docRef.id), SetOptions.merge()).await()
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

    suspend fun getReferralById(referralId: String): Referral? {
        val documentSnapshot = firestore.collection(REFERRALS_COLLECTION)
            .document(referralId)
            .get()
            .await()

        return documentSnapshot.toObject(ReferralFirestore::class.java)?.toDomain()
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

    private fun createReferralFlow(query: Query): Flow<List<Referral>> = callbackFlow{
        val listenerRegistration: ListenerRegistration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val referrals = snapshot?.documents?.mapNotNull { document ->
                document.toObject(ReferralFirestore::class.java)?.toDomain()
            } ?: emptyList()

            trySend(referrals).isSuccess
        }
        awaitClose { listenerRegistration.remove() }
    }

    companion object {
        private const val REFERRALS_COLLECTION = "referrals"
        private const val CLIENT_ID_FIELD = "clientId"
        private const val PROVIDER_ID_FIELD = "providerId"
        private const val STATUS_FIELD = "status"
        private const val VOUCHER_URL_FIELD = "voucherUrl"
        private const val ORDER_BY_FIELD_LOWER = "nameLowercase"
        private const val CREATED_AT_FIELD = "createdAt"
    }
}