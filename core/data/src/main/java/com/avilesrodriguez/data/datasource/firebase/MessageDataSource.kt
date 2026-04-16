package com.avilesrodriguez.data.datasource.firebase

import com.avilesrodriguez.data.datasource.firebase.model.MessageFirestore
import com.avilesrodriguez.data.datasource.firebase.model.toMessageDomain
import com.avilesrodriguez.data.datasource.firebase.model.toMessageFirestore
import com.avilesrodriguez.domain.ext.normalizeName
import com.avilesrodriguez.domain.model.message.Message
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
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
            .orderBy(CREATED_AT_FIELD, Query.Direction.DESCENDING) //more nuevos al inicio

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

    fun getMessagesByReferralSince(
        referralId: String,
        since: Long
    ): Flow<List<Message>> = callbackFlow {
        val sinceTimestamp = Timestamp(Date(since))
        var query = firestore.collection(MESSAGES_COLLECTION)
            .whereEqualTo(REFERRAL_ID_FIELD, referralId)
            .whereGreaterThanOrEqualTo(CREATED_AT_FIELD, sinceTimestamp)

        query = query.orderBy(CREATED_AT_FIELD, Query.Direction.DESCENDING) //more nuevos al inicio

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

    suspend fun getMessagesByReferralPaged(
        referralId: String,
        currentUserId: String,
        pageSize: Long,
        lastMessage: Message? = null,
        subjectPrefix: String
    ) : Pair<List<Message>, Message?>{
        val finalVisibleMessages = mutableListOf<Message>()
        var lastDocPointer = lastMessage
        var hasMoreInFirestore = true

        while (finalVisibleMessages.size < pageSize && hasMoreInFirestore) {
            val limitToFetch = pageSize - finalVisibleMessages.size

            var query = firestore.collection(MESSAGES_COLLECTION)
                .whereEqualTo(REFERRAL_ID_FIELD, referralId)

            if(subjectPrefix.isNotEmpty()){
                val subjectNormalized = subjectPrefix.normalizeName()
                query = query.orderBy(SUBJECT_LOWER_CASE_FIELD)
                    .whereGreaterThanOrEqualTo(SUBJECT_LOWER_CASE_FIELD, subjectNormalized)
                    .whereLessThanOrEqualTo(SUBJECT_LOWER_CASE_FIELD, subjectNormalized + "\uf8ff")
                    .orderBy(ID_FIELD)
                if(lastDocPointer != null){
                    query = query.startAfter(lastDocPointer.subjectLowercase, lastDocPointer.id)
                }
            } else {
                query = query.orderBy(CREATED_AT_FIELD, Query.Direction.DESCENDING)
                    .orderBy(ID_FIELD, Query.Direction.DESCENDING)
                if(lastDocPointer != null){
                    val lastTimestamp = Timestamp(Date(lastDocPointer.createdAt))
                    query = query.startAfter(lastTimestamp, lastDocPointer.id)
                }
            }
            query = query.limit(limitToFetch)
            val snapshot = query.get().await()

            if(snapshot.isEmpty){
                hasMoreInFirestore = false
                break
            }
            val fetchedBatch = snapshot.documents.mapNotNull { doc ->
                doc.toObject(MessageFirestore::class.java)?.toMessageDomain()
            }
            val visibleInBatch = fetchedBatch.filter { msg ->
                if(msg.senderId == currentUserId) !msg.isDeletedBySender
                else !msg.isDeletedByReceiver
            }
            finalVisibleMessages.addAll(visibleInBatch)
            // uso de fetchedBatch aunque sea el borrado para que no se repita datos
            lastDocPointer = fetchedBatch.lastOrNull()
            // Si Firestore devolvió menos de lo que pedimos, es que ya no hay más datos
            if(fetchedBatch.size < limitToFetch){
                hasMoreInFirestore = false
            }
        }

        return finalVisibleMessages to lastDocPointer
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

    suspend fun getMessageById(messageId: String): Message? {
        val documentSnapshot = firestore.collection(MESSAGES_COLLECTION)
            .document(messageId)
            .get()
            .await()

        return documentSnapshot.toObject(MessageFirestore::class.java)?.toMessageDomain()
    }

    companion object {
        private const val MESSAGES_COLLECTION = "messages"
        private const val REFERRAL_ID_FIELD = "referralId"
        private const val CREATED_AT_FIELD = "createdAt"
        private const val IS_READ_FIELD = "isRead"
        private const val IS_DELETED_BY_SENDER_FIELD = "isDeletedBySender"
        private const val IS_DELETED_BY_RECEIVER_FIELD = "isDeletedByReceiver"
        private const val SUBJECT_LOWER_CASE_FIELD = "subjectLowercase"
        private const val ID_FIELD = "id"
    }
}
