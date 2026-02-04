package com.avilesrodriguez.data.datasource.firebase

import com.avilesrodriguez.data.datasource.firebase.model.MessageFirestore
import com.avilesrodriguez.data.datasource.firebase.model.toDomain
import com.avilesrodriguez.data.datasource.firebase.model.toFirestore
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

class MessagesDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun saveMessage(message: Message) {
        val messageFirestore = message.toFirestore()
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
            .orderBy(CREATED_AT_FIELD, Query.Direction.ASCENDING)

        val listener: ListenerRegistration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val messages = snapshot?.documents?.mapNotNull { document ->
                document.toObject(MessageFirestore::class.java)?.toDomain()
            } ?: emptyList()

            trySend(messages).isSuccess
        }
        awaitClose { listener.remove() }
    }

    suspend fun deleteMessage(messageId: String) {
        firestore.collection(MESSAGES_COLLECTION).document(messageId).delete().await()
    }

    companion object {
        private const val MESSAGES_COLLECTION = "messages"
        private const val REFERRAL_ID_FIELD = "referralId"
        private const val CREATED_AT_FIELD = "createdAt"
    }
}
