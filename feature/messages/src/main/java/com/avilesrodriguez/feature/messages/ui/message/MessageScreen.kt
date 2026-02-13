package com.avilesrodriguez.feature.messages.ui.message

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.avilesrodriguez.domain.model.message.Message
import com.avilesrodriguez.domain.model.referral.Referral
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.composables.ToolBarWithIcon

@Composable
fun MessageScreen(
    messageId: String?,
    onBackClick: () -> Unit,
    openScreen: (String) -> Unit,
    viewModel: MessageViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.loadMessage(messageId.orEmpty())
    }
    val messageState by viewModel.messageState.collectAsState()
    val referralState by viewModel.referralState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val clientWhoReferred = viewModel.clientWhoReferred
    val providerThatReceived = viewModel.providerThatReceived

    MessageScreenContent(
        onBackClick = onBackClick,
        messageState = messageState,
        referral = referralState,
        isLoading = isLoading,
        clientWhoReferred = clientWhoReferred,
        providerThatReceived = providerThatReceived,
        onReplyClick = { viewModel.replyMessage(openScreen) }
    )
}

@Composable
private fun MessageScreenContent(
    onBackClick: () -> Unit,
    messageState: Message,
    referral: Referral,
    isLoading: Boolean,
    clientWhoReferred: UserData?,
    providerThatReceived: UserData?,
    onReplyClick: () -> Unit
){
    Scaffold(
        topBar = {
            ToolBarWithIcon(
                iconBack = R.drawable.arrow_back,
                title = R.string.emails_referral,
                backClick = { onBackClick() }
            )
        },
        content = { innerPadding ->
            Email(
                messageState = messageState,
                referral = referral,
                isLoading = isLoading,
                clientWhoReferred = clientWhoReferred,
                providerThatReceived = providerThatReceived,
                onReplyClick = onReplyClick,
                modifier = Modifier.padding(innerPadding)
            )
        }
    )
}

@Composable
private fun Email(
    messageState: Message,
    referral: Referral,
    isLoading: Boolean,
    clientWhoReferred: UserData?,
    providerThatReceived: UserData?,
    onReplyClick: () -> Unit,
    modifier: Modifier = Modifier
){

}