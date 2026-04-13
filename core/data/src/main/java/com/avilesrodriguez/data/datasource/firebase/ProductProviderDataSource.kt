package com.avilesrodriguez.data.datasource.firebase

import com.avilesrodriguez.data.datasource.firebase.model.ProductProviderFirestore
import com.avilesrodriguez.data.datasource.firebase.model.toProductProviderDomain
import com.avilesrodriguez.data.datasource.firebase.model.toProductProviderFirestore
import com.avilesrodriguez.domain.ext.normalizeName
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
        val finalUpdates = updates.mapValues { (key, value) ->
            when {
                (key == CREATED_AT_FIELD || key == UPDATED_AT_FIELD) && value is Long -> {
                    Timestamp(Date(value))
                }
                key == PAY_BY_REFERRAL_FIELD && value is String -> {
                    value.toDoubleOrNull() ?: 0.0
                }
                else -> value
            }
        }

        firestore.collection(PRODUCTS_PROVIDER_COLLECTION)
            .document(id)
            .update(finalUpdates)
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

    fun getProductsRealTime(
        limit: Long
    ) : Flow<List<ProductProvider>>{
        val query: Query = firestore.collection(PRODUCTS_PROVIDER_COLLECTION)
            .whereEqualTo(IS_ACTIVE_FIELD, true)
            .orderBy(UPDATED_AT_FIELD, Query.Direction.DESCENDING)
            .limit(limit)

        return getProductsFlow(query)
    }

    fun getProductsByProviderRealTime(
        providerId: String,
        limit: Long
    ) : Flow<List<ProductProvider>>{
        val query: Query = firestore.collection(PRODUCTS_PROVIDER_COLLECTION)
            .whereEqualTo(IS_ACTIVE_FIELD, true)
            .whereEqualTo(PROVIDER_ID_FIELD, providerId)
            .orderBy(UPDATED_AT_FIELD, Query.Direction.DESCENDING)
            .limit(limit)
        return getProductsFlow(query)
    }

    suspend fun getAllProducts(
        pageSize: Long,
        industry: String? = null,
        namePrefix: String,
        lastProduct: ProductProvider? = null
    ) : Pair<List<ProductProvider>, ProductProvider?> {
        var query: Query = firestore.collection(PRODUCTS_PROVIDER_COLLECTION)
            .whereEqualTo(IS_ACTIVE_FIELD, true)

        if (!industry.isNullOrBlank()) {
            query = query.whereEqualTo(INDUSTRY_FIELD, industry)
        }

        if (namePrefix.isNotEmpty()) {
            val normalizedPrefix = namePrefix.normalizeName()
            query = query.orderBy(NAME_LOWER_CASE_FIELD)
                .whereGreaterThanOrEqualTo(NAME_LOWER_CASE_FIELD, normalizedPrefix)
                .whereLessThanOrEqualTo(NAME_LOWER_CASE_FIELD, normalizedPrefix + "\uf8ff")
                .orderBy(ID_FIELD)
        } else {
            query = query.orderBy(CREATED_AT_FIELD, Query.Direction.DESCENDING).orderBy(ID_FIELD)
        }

        if (lastProduct != null) {
            val lastTimestamp = Timestamp(Date(lastProduct.createdAt))
            query = query.startAfter(lastTimestamp, lastProduct.id)
        }

        query = query.limit(pageSize)
        val snapshot = query.get().await()

        val products = snapshot.documents.mapNotNull {
            it.toObject(ProductProviderFirestore::class.java)
                ?.toProductProviderDomain()
        }

        val last = products.lastOrNull()
        return products to last
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

        if (namePrefix.isNotEmpty()) {
            val normalizedPrefix = namePrefix.normalizeName()
            query = query.orderBy(NAME_LOWER_CASE_FIELD)
                .whereGreaterThanOrEqualTo(NAME_LOWER_CASE_FIELD, normalizedPrefix)
                .whereLessThanOrEqualTo(NAME_LOWER_CASE_FIELD, normalizedPrefix + "\uf8ff")
                .orderBy(ID_FIELD)
        } else {
            query = query.orderBy(CREATED_AT_FIELD, Query.Direction.DESCENDING).orderBy(ID_FIELD)
        }

        if (lastProduct != null) {
            val lastTimestamp = Timestamp(Date(lastProduct.createdAt))
            query = query.startAfter(lastTimestamp, lastProduct.id)
        }

        query = query.limit(pageSize)
        val snapshot = query.get().await()

        val products = snapshot.documents.mapNotNull {
            it.toObject(ProductProviderFirestore::class.java)
                ?.toProductProviderDomain()
        }

        val last = products.lastOrNull()
        return products to last

    }

    private fun getProductsFlow(query: Query): Flow<List<ProductProvider>> = callbackFlow {
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val products = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(ProductProviderFirestore::class.java)
                    ?.toProductProviderDomain()
            } ?: emptyList()

            trySend(products).isSuccess
        }
        awaitClose { listener.remove() }
    }

    companion object{
        private const val PRODUCTS_PROVIDER_COLLECTION = "products_provider"
        private const val IS_ACTIVE_FIELD = "isActive"
        private const val CREATED_AT_FIELD = "createdAt"
        private const val UPDATED_AT_FIELD = "updatedAt"
        private const val PROVIDER_ID_FIELD = "providerId"
        private const val  NAME_LOWER_CASE_FIELD = "nameLowercase"
        private const val INDUSTRY_FIELD = "industry"
        private const val ID_FIELD = "id"
        private const val PAY_BY_REFERRAL_FIELD = "payByReferral"

    }
}