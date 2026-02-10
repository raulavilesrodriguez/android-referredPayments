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
    val name: String? = null,
    val nameLowercase: String? = null,
    val email: String? = null,
    val numberPhone: String? = null,
    val status: String? = null,
    val createdAt: Timestamp? = null,
    val voucherUrl: String? = null,
    val amountPaid: Double? = null
)

fun Referral.toReferralFirestore(): ReferralFirestore {
    return ReferralFirestore(
        id = id,
        clientId = clientId,
        providerId = providerId,
        name = name,
        nameLowercase = nameLowercase,
        email = email,
        numberPhone = numberPhone,
        status = status.name, // "PENDING", "PAID", etc.
        createdAt = Timestamp(Date(createdAt)),
        voucherUrl = voucherUrl,
        amountPaid = amountPaid
    )
}

fun ReferralFirestore.toReferralDomain(): Referral{
    val referralStatusType = try {
        ReferralStatus.valueOf(status?.uppercase() ?: "PENDING")
    } catch (e: Exception){
        Log.e("ReferralFirestore", "Error al convertir el estado de referencia: ${e.message}")
        ReferralStatus.PENDING
    }
    return Referral(
        id = id ?: "",
        clientId = clientId ?: "",
        providerId = providerId ?: "",
        name = name ?: "",
        nameLowercase = nameLowercase ?: "",
        email = email ?: "",
        numberPhone = numberPhone ?: "",
        status = referralStatusType,
        createdAt = createdAt?.toDate()?.time ?: System.currentTimeMillis(),
        voucherUrl = voucherUrl,
        amountPaid = amountPaid ?: 0.0
    )
}
