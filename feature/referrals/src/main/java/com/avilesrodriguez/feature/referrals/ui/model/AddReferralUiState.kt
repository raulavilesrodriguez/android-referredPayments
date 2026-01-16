package com.avilesrodriguez.feature.referrals.ui.model

import com.avilesrodriguez.domain.model.referral.Referral
import com.avilesrodriguez.domain.model.referral.ReferralStatus
import com.avilesrodriguez.presentation.ext.normalizeName

data class AddReferralUiState(
    val name: String = "",
    val email: String = "",
    val numberPhone: String = "",
    val isSaving: Boolean = false,
    val isEntryValid: Boolean = false
)

fun AddReferralUiState.toReferral(
    clientId: String,
    providerId: String,
    createdAt: Long,
    voucherUrl: String?
): Referral {
    return Referral(
        clientId = clientId,
        providerId = providerId,
        name = name,
        nameLowercase = name.normalizeName(),
        email = email,
        numberPhone = numberPhone,
        status = ReferralStatus.PENDING,
        createdAt = createdAt,
        voucherUrl = voucherUrl
    )
}

fun Referral.toAddReferralUiState(): AddReferralUiState{
    return AddReferralUiState(
        name = name,
        email = email,
        numberPhone = numberPhone,
        isSaving = false,
        isEntryValid = true
    )
}
