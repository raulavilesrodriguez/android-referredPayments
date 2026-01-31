package com.example.feature.home.models

import com.avilesrodriguez.domain.model.referral.ReferralMetrics
import com.avilesrodriguez.domain.model.user.UserData

data class UserAndReferralMetrics(
    val user: UserData,
    val referralMetrics: ReferralMetrics
)
