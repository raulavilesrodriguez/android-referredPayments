package com.avilesrodriguez.feature.referrals.ui

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun ReferralDetailScreen(
    referralId: String?,
    onBackClick: () -> Unit,
    openScreen: (String) -> Unit,
    viewModel: ReferralViewModel = hiltViewModel()
){

}