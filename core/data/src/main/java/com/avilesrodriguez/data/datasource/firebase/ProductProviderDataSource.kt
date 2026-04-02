package com.avilesrodriguez.data.datasource.firebase

import com.avilesrodriguez.data.datasource.firebase.model.ProductProviderFirestore
import com.avilesrodriguez.data.datasource.firebase.model.toProductProviderDomain
import com.avilesrodriguez.data.datasource.firebase.model.toProductProviderFirestore
import com.avilesrodriguez.domain.model.productsProvider.ProductProvider
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

class ProductProviderDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun saveProductProvider(productProvider: ProductProvider) {
        val productProviderFirestore = productProvider.toProductProviderFirestore()
        val docRef = if(productProvider.id.isEmpty()){
            firestore.collection(PRODUCTS_PROVIDER_COLLECTION)
                .document()
        } else {
            firestore.collection(PRODUCTS_PROVIDER_COLLECTION)
                .document(productProvider.id)
        }

        docRef.set(productProviderFirestore.copy(id = docRef.id), SetOptions.merge())
            .await()
    }

    suspend fun updateProductProvider(id: String, updates: Map<String, Any>) {
        if(id.isEmpty()) return
        val firestoreUpdates = updates.mapValues { entry ->
            if ((entry.key == CREATED_AT_FIELD) && entry.value is Long) {
                Timestamp(Date(entry.value as Long))
            } else {
                entry.value
            }
        }
        firestore.collection(PRODUCTS_PROVIDER_COLLECTION)
            .document(id)
            .update(firestoreUpdates)
            .await()
    }

    suspend fun deactivateProductProvider(productProviderId: String) {
        firestore.collection(PRODUCTS_PROVIDER_COLLECTION)
            .document(productProviderId)
            .update(IS_ACTIVE_FIELD, false)
            .await()
    }

    suspend fun getProductProviderById(productProviderId: String): ProductProvider?{
        val documentSnapshot = firestore.collection(PRODUCTS_PROVIDER_COLLECTION)
            .document(productProviderId)
            .get()
            .await()
        return documentSnapshot.toObject(ProductProviderFirestore::class.java)?.toProductProviderDomain()
    }

    fun getProductProviderByIdFlow(id: String): Flow<ProductProvider?> = callbackFlow {
        val query = firestore.collection(PRODUCTS_PROVIDER_COLLECTION).document(id)
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val productProvider = snapshot?.toObject(ProductProviderFirestore::class.java)?.toProductProviderDomain()
            trySend(productProvider).isSuccess
        }
        awaitClose { listener.remove() }
    }

    suspend fun getAllProducts(
        pageSize: Long,
        valuePayByReferral: Double? = null,
        lastProduct: ProductProvider? = null
    ) : Pair<List<ProductProvider>, ProductProvider?> {
        var query: Query = firestore.collection(PRODUCTS_PROVIDER_COLLECTION)
            .whereEqualTo(IS_ACTIVE_FIELD, true)

        query = if (valuePayByReferral != null) {
            query
                .whereEqualTo(PAY_BY_REFERRAL_FIELD, valuePayByReferral)
        } else {
            query.orderBy(CREATED_AT_FIELD, Query.Direction.DESCENDING)
        }

        if (lastProduct != null) {
            if (valuePayByReferral != null) {
                query = query.startAfter(lastProduct.payByReferral, lastProduct.id)
            } else {
                val lastTimestamp = Timestamp(Date(lastProduct.createdAt))
                query = query.startAfter(lastTimestamp, lastProduct.id)
            }
        }

        val snapshot = query.limit(pageSize).get().await()
        val products = snapshot.documents.mapNotNull { doc ->
            doc.toObject(ProductProviderFirestore::class.java)?.toProductProviderDomain()
        }

        return products to products.lastOrNull()
    }

    suspend fun getProductsByProvider(
        providerId: String,
        pageSize: Long,
        namePrefix: String,
        lastProduct: ProductProvider? = null
    ) : Pair<List<ProductProvider>, ProductProvider?> {
        var query = firestore.collection(PRODUCTS_PROVIDER_COLLECTION)
            .whereEqualTo(PROVIDER_ID_FIELD, providerId)
            .whereEqualTo(IS_ACTIVE_FIELD, true)

        query = if (namePrefix.isNotBlank()) {
            query.orderBy(NAME_LOWER_CASE_FIELD)
                .startAt(namePrefix)
                .endAt(namePrefix + "\uf8ff")
        } else {
            query.orderBy(CREATED_AT_FIELD, Query.Direction.DESCENDING)
        }

        if (lastProduct != null) {
            if (namePrefix.isNotBlank()) {
                query = query.startAfter(lastProduct.name, lastProduct.id)
            } else {
                val lastTimestamp = Timestamp(Date(lastProduct.createdAt))
                query = query.startAfter(lastTimestamp, lastProduct.id)
            }
        }

        val snapshot = query.limit(pageSize).get().await()
        val products = snapshot.documents.mapNotNull { doc ->
            doc.toObject(ProductProviderFirestore::class.java)?.toProductProviderDomain()
        }

        return products to products.lastOrNull()
    }

    companion object{
        private const val PRODUCTS_PROVIDER_COLLECTION = "products_provider"
        private const val IS_ACTIVE_FIELD = "isActive"
        private const val CREATED_AT_FIELD = "createdAt"
        private const val PROVIDER_ID_FIELD = "providerId"
        private const val  NAME_LOWER_CASE_FIELD = "nameLowercase"
        private const val PAY_BY_REFERRAL_FIELD = "payByReferral"
    }
}