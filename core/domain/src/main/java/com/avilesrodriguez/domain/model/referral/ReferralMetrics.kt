package com.avilesrodriguez.domain.model.referral

data class ReferralMetrics(
    val totalReferrals: Int = 0,
    val pendingReferrals: Int = 0,
    val processingReferrals: Int = 0,
    val rejectedReferrals: Int = 0,
    val paidReferrals: Int = 0
)