package com.avilesrodriguez.feature.messages.ui.newMessage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun NewMessage(
    referralId: String?,
    onBackClick: () -> Unit,
    openAndPopUp: (String, String) -> Unit,
    viewModel: NewMessageViewModel = hiltViewModel()
){
    LaunchedEffect(Unit) {
        viewModel.loadReferralInformation(referralId.orEmpty())
    }

}