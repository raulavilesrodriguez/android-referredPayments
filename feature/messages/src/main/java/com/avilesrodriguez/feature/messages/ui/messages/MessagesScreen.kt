package com.avilesrodriguez.feature.messages.ui.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.avilesrodriguez.domain.model.message.Message
import com.avilesrodriguez.domain.model.referral.Referral
import com.avilesrodriguez.domain.model.user.UserData

@Composable
fun MessagesScreen(
    referralId: String?,
    onBackClick: () -> Unit,
    openScreen: (String) -> Unit,
    viewModel: MessagesViewModel = hiltViewModel()
){
    LaunchedEffect(Unit) {
        viewModel.loadReferralInformation(referralId.orEmpty())
    }
    val uiState by viewModel.uiState.collectAsState()
    val searchText by viewModel.searchText.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val user by viewModel.userDataStore.collectAsState()
    val referralState by viewModel.referralState.collectAsState()

    MessagesScreenContent(
        onBackClick = onBackClick,
        searchText = searchText,
        onValueChange = viewModel::updateSearchText,
        onMessageClick = { message ->
            viewModel.onMessageClick(message, openScreen) },
        messages = uiState,
        user = user,
        referral = referralState,
        isLoading = isLoading,
        onDeletedMessage = { message -> viewModel.onDeleteMessage(message) }
    )
}

@Composable
fun MessagesScreenContent(
    onBackClick: () -> Unit,
    searchText: String,
    onValueChange: (String) -> Unit,
    onMessageClick: (Message) -> Unit,
    messages: List<Message>,
    user: UserData?,
    referral: Referral,
    isLoading: Boolean,
    onDeletedMessage: (Message) -> Unit
){

}