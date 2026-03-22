package com.avilesrodriguez.data.datasource.firebase

import com.avilesrodriguez.data.datasource.firebase.model.toMessageFirestore
import com.avilesrodriguez.domain.model.message.Message
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class TransactionsDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun sendPayTransaction(
        referralId: String,
        referralUpdates: Map<String, Any>,
        message: Message,
        clientUid: String,
        providerUid: String,
        amountPaid: Double
    ){
        firestore.runTransaction { transaction ->
            val referralRef = firestore.collection(REFERRALS_COLLECTION).document(referralId)
            val messageRef = firestore.collection(MESSAGES_COLLECTION).document(message.id)
            val clientRef = firestore.collection(USERS_COLLECTION).document(clientUid)
            val providerRef = firestore.collection(USERS_COLLECTION).document(providerUid)

            transaction.update(referralRef, referralUpdates)
            val messageFirestore = message.toMessageFirestore()
            transaction.set(messageRef, messageFirestore)
            transaction.update(clientRef, mapOf(
                MONEY_EARNED_CLIENT_FIELD to FieldValue.increment(amountPaid)
            ))
            transaction.update(providerRef, mapOf(
                MONEY_PAID_PROVIDER_FIELD to FieldValue.increment(amountPaid),
                TOTAL_PAYOUTS_PROVIDER_FIELD to FieldValue.increment(1),
                PROCESSING_REFERRALS_COUNT to FieldValue.increment(-1)
            ))
        }.await()
    }

    suspend fun rejectReferralTransaction(
        referralId: String,
        referralUpdates: Map<String, Any>,
        message: Message,
        providerUid: String
    ){
        firestore.runTransaction { transaction ->
            val referralRef = firestore.collection(REFERRALS_COLLECTION).document(referralId)
            val messageRef = firestore.collection(MESSAGES_COLLECTION).document(message.id)
            val providerRef = firestore.collection(USERS_COLLECTION).document(providerUid)

            transaction.update(referralRef, referralUpdates)
            val messageFirestore = message.toMessageFirestore()
            transaction.set(messageRef, messageFirestore)
            transaction.update(providerRef, mapOf(
                PROCESSING_REFERRALS_COUNT to FieldValue.increment(-1)
            ))
        }.await()
    }

    companion object{
        private const val MESSAGES_COLLECTION = "messages"
        private const val USERS_COLLECTION = "users"
        private const val REFERRALS_COLLECTION = "referrals"
        private const val MONEY_EARNED_CLIENT_FIELD = "moneyEarned"
        private const val MONEY_PAID_PROVIDER_FIELD = "moneyPaid"
        private const val TOTAL_PAYOUTS_PROVIDER_FIELD = "totalPayouts"
        private const val PROCESSING_REFERRALS_COUNT = "processingReferralsCount"
    }
}