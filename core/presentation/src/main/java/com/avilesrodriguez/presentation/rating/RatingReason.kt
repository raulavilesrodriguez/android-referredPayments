package com.avilesrodriguez.presentation.rating

import androidx.annotation.StringRes
import com.avilesrodriguez.presentation.R

enum class RatingReason(
    @param:StringRes val resId: Int,
    val type: ReasonType
) {
    // NEGATIVE
    NO_PAYMENT(R.string.reason_no_payment, ReasonType.NEGATIVE),
    LATE_PAYMENT(R.string.reason_late_payment, ReasonType.NEGATIVE),
    BAD_COMMUNICATION(R.string.reason_bad_communication, ReasonType.NEGATIVE),
    WRONG_AMOUNT(R.string.reason_wrong_amount, ReasonType.NEGATIVE),

    // NEUTRAL
    MET_EXPECTATIONS(R.string.reason_met_expectations, ReasonType.NEUTRAL),
    AVERAGE_SERVICE(R.string.reason_average_service, ReasonType.NEUTRAL),
    MINOR_ISSUES(R.string.reason_minor_issues, ReasonType.NEUTRAL),

    // POSITIVE
    FAST_PAYMENT(R.string.reason_fast_payment, ReasonType.POSITIVE),
    GREAT_COMMUNICATION(R.string.reason_great_communication, ReasonType.POSITIVE),
    PROFESSIONAL(R.string.reason_professional, ReasonType.POSITIVE),
    ON_TIME(R.string.reason_on_time, ReasonType.POSITIVE);

    companion object
}

enum class ReasonType {
    NEGATIVE,
    NEUTRAL,
    POSITIVE
}

fun getReasonsByRating(rating: Int): List<RatingReason> {
    return when (rating) {
        in 1..2 -> RatingReason.entries.filter { it.type == ReasonType.NEGATIVE }
        3 -> RatingReason.entries.filter { it.type == ReasonType.NEUTRAL }
        in 4..5 -> RatingReason.entries.filter { it.type == ReasonType.POSITIVE }
        else -> emptyList()
    }
}