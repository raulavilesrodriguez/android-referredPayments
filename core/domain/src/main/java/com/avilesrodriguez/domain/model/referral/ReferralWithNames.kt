package com.avilesrodriguez.domain.model.referral

data class ReferralWithNames(
    val referral: Referral,
    val otherPartyName: String,
)
