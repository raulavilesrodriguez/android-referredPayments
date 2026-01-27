package com.example.feature.home.ui.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.avilesrodriguez.domain.model.referral.ReferralStatus
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.presentation.ext.options

@Composable
fun DetailScreenUser(
    uId: String?,
    popUp: () -> Unit,
    openScreen: (String) -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
){
    LaunchedEffect(Unit) {
        viewModel.loadUserInformation(uId)
    }
    val userData by viewModel.userDataStore.collectAsState()
    val referrals by viewModel.uiStateReferrals.collectAsState()
    val referralsMetrics by viewModel.uiStateReferralsMetrics.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()

    val statusOptions = ReferralStatus.options(true)

    when(userData){
        is UserData.Provider -> {
            DetailScreenProvider(
                provider = userData as UserData.Provider,
                onBackClick = popUp,
                onAddReferClick = {uid -> viewModel.onAddReferClick(uid, openScreen)}
            )
        }
        is UserData.Client -> {
            DetailScreenClient(
                client = userData as UserData.Client,
                referrals = referrals,
                referralsMetrics = referralsMetrics,
                selectedStatus = selectedStatus,
                filterReferralsByStatus = viewModel::filterReferralsByStatus,
                statusOptions = statusOptions,
                onBackClick = popUp,
                onReferClick = {id -> viewModel.onReferClick(id, openScreen)}
            )
        }
        else -> {
            popUp()
        }
    }
}

