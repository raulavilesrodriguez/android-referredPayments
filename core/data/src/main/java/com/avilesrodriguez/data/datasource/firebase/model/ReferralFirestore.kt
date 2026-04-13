package com.avilesrodriguez.data.datasource.firebase.model

import android.util.Log
import com.avilesrodriguez.domain.model.referral.Referral
import com.avilesrodriguez.domain.model.referral.ReferralStatus
import com.google.firebase.Timestamp
import java.util.Date

data class ReferralFirestore(
    val id: String? = null,
    val clientId: String? = null,
    val providerId: String? = null,
    val productId: String? = null,
    val name: String? = null,
    val nameLowercase: String? = null,
    val email: String? = null,
    val numberPhone: String? = null,
    val status: String? = null,
    val createdAt: Any? = null,
    val amountPaid: Double? = null,
    val updatedAt: Any? = null,
    val rating: Double = 0.0,
    val feedbackReason: String? = null
)

fun Referral.toReferralFirestore(): ReferralFirestore {
    return ReferralFirestore(
        id = id,
        clientId = clientId,
        providerId = providerId,
        productId = productId,
        name = name,
        nameLowercase = nameLowercase,
        email = email,
        numberPhone = numberPhone,
        status = status.name, // "PENDING", "PAID", etc.
        createdAt = Timestamp(Date(createdAt)),
        amountPaid = amountPaid,
        updatedAt = updatedAt?.let { Timestamp(Date(it)) },
        rating = rating,
        feedbackReason = feedbackReason
    )
}

fun ReferralFirestore.toReferralDomain(): Referral{
    val referralStatusType = try {
        ReferralStatus.valueOf(status?.uppercase() ?: "PENDING")
    } catch (e: Exception){
        Log.e("ReferralFirestore", "Error al convertir el estado de referencia: ${e.message}")
        ReferralStatus.PENDING
    }

    fun toLong(value: Any?): Long {
        return when (value) {
            is Timestamp -> value.toDate().time
            is Long -> value
            else -> System.currentTimeMillis()
        }
    }

    fun toNullableLong(value: Any?): Long? {
        return when (value) {
            is Timestamp -> value.toDate().time
            is Long -> value
            else -> null
        }
    }
    return Referral(
        id = id ?: "",
        clientId = clientId ?: "",
        providerId = providerId ?: "",
        productId = productId ?: "",
        name = name ?: "",
        nameLowercase = nameLowercase ?: "",
        email = email ?: "",
        numberPhone = numberPhone ?: "",
        status = referralStatusType,
        createdAt = toLong(createdAt),
        amountPaid = amountPaid ?: 0.0,
        updatedAt = toNullableLong(updatedAt),
        rating = rating,
        feedbackReason = feedbackReason
    )
}
