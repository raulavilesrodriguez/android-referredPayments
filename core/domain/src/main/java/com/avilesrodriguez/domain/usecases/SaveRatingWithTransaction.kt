package com.avilesrodriguez.domain.usecases

import com.avilesrodriguez.domain.interfaces.IReferralRepository
import javax.inject.Inject

class SaveRatingWithTransaction @Inject constructor(
    private val repository: IReferralRepository
) {
    suspend operator fun invoke(
        referralId: String,
        referralUpdates: Map<String, Any>,
        providerId: String,
        ratingReferral: Double
    ) {
        repository.saveRatingWithTransaction(
            referralId,
            referralUpdates,
            providerId,
            ratingReferral
        )
    }
}