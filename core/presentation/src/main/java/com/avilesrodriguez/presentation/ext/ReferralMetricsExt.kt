package com.avilesrodriguez.presentation.ext

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.avilesrodriguez.domain.model.referral.ReferralMetrics
import com.avilesrodriguez.presentation.R

fun ReferralMetrics.toList(): List<Int> {
    return listOf(
        pendingReferrals,
        processingReferrals,
        rejectedReferrals,
        paidReferrals
    )
}

@Composable
fun referralMetricsLabels(): List<String> {
    return listOf(
        stringResource(R.string.pending),
        stringResource(R.string.processing),
        stringResource(R.string.rejected),
        stringResource(R.string.paid)
    )
}

fun referralMetricsColors(): List<Color> {
    return listOf(
        Color(0xFFF5AD18),
        Color(0xFF6594B1),
        Color(0XFFDC0E0E),
        Color(0xFF08CB00)
    )
}