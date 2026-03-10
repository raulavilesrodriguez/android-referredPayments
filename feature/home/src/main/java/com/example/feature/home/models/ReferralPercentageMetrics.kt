package com.example.feature.home.models

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.avilesrodriguez.presentation.R

data class ReferralPercentageMetrics(
    val percentageRejected: Double = 0.0,
    val percentagePaid: Double = 0.0,
    val percentageProcessing: Double = 0.0,
    val percentagePending: Double = 0.0
)

@Composable
fun ReferralPercentageMetrics.toList(): List<Pair<Double, String>> {
    return listOf(
        percentageRejected to stringResource(R.string.rejected),
        percentagePaid to stringResource(R.string.paid),
        percentageProcessing to stringResource(R.string.processing),
        percentagePending to stringResource(R.string.pending)
    )
}


