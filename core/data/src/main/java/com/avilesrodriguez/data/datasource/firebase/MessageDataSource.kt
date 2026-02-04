package com.avilesrodriguez.data.datasource.firebase

import com.avilesrodriguez.data.datasource.firebase.model.MessageFirestore
import com.avilesrodriguez.data.datasource.firebase.model.toMessageDomain
import com.avilesrodriguez.data.datasource.firebase.model.toMessageFirestore
import com.avilesrodriguez.domain.model.message.Message
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class MessageDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun saveMessage(message: Message) {
        val messageFirestore = message.toMessageFirestore()
        val docRef = if (message.id.isEmpty()) {
            firestore.collection(MESSAGES_COLLECTION).document()
        } else {
            firestore.collection(MESSAGES_COLLECTION).document(message.id)
        }

        docRef.set(messageFirestore.copy(id = docRef.id), SetOptions.merge()).await()
    }

    fun getMessagesByReferral(referralId: String): Flow<List<Message>> = callbackFlow {
        val query = firestore.collection(MESSAGES_COLLECTION)
            .whereEqualTo(REFERRAL_ID_FIELD, referralId)
            .orderBy(CREATED_AT_FIELD, Query.Direction.DESCENDING) //mas nuevos al inicio

        val listener: ListenerRegistration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val messages = snapshot?.documents?.mapNotNull { document ->
                document.toObject(MessageFirestore::class.java)?.toMessageDomain()
            } ?: emptyList()

            trySend(messages).isSuccess
        }
        awaitClose { listener.remove() }
    }

    suspend fun markAsRead(messageId: String) {
        firestore.collection(MESSAGES_COLLECTION).document(messageId)
            .update(IS_READ_FIELD, true)
            .await()
    }

    suspend fun markAsDeletedBySender(messageId: String) {
        firestore.collection(MESSAGES_COLLECTION).document(messageId)
            .update(IS_DELETED_BY_SENDER_FIELD, true)
            .await()
    }

    suspend fun markAsDeletedByReceiver(messageId: String){
        firestore.collection(MESSAGES_COLLECTION).document(messageId)
            .update(IS_DELETED_BY_RECEIVER_FIELD, true)
            .await()
    }

    suspend fun deleteMessagePermanently(messageId: String) {
        firestore.collection(MESSAGES_COLLECTION).document(messageId).delete().await()
    }

    companion object {
        private const val MESSAGES_COLLECTION = "messages"
        private const val REFERRAL_ID_FIELD = "referralId"
        private const val CREATED_AT_FIELD = "createdAt"
        private const val IS_READ_FIELD = "isRead"
        private const val IS_DELETED_BY_SENDER_FIELD = "isDeletedBySender"
        private const val IS_DELETED_BY_RECEIVER_FIELD = "isDeletedByReceiver"
    }
}
