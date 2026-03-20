package com.example.feature.home.models

import com.avilesrodriguez.domain.model.user.UserData

data class UserAndProcessingReferrals(
    val user: UserData,
    val processingReferrals: Int
)
