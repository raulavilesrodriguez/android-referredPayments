package com.avilesrodriguez.feature.messages.ui.messages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.avilesrodriguez.domain.model.message.Message
import com.avilesrodriguez.domain.model.referral.Referral
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.composables.SearchFieldBasic
import com.avilesrodriguez.presentation.composables.ToolBarWithIcon

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
    val clientWhoReferred = viewModel.clientWhoReferred
    val providerThatReceived = viewModel.providerThatReceived

    MessagesScreenContent(
        onBackClick = onBackClick,
        searchText = searchText,
        onValueChange = viewModel::updateSearchText,
        onMessageClick = { message ->
            viewModel.onMessageClick(message, openScreen) },
        messages = uiState,
        user = user,
        isLoading = isLoading,
        clientWhoReferred = clientWhoReferred,
        providerThatReceived = providerThatReceived,
        referral = referralState,
        onNewMessageClick = { viewModel.onNewMessageClick(openScreen) }
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
    isLoading: Boolean,
    clientWhoReferred: UserData?,
    providerThatReceived: UserData?,
    referral: Referral,
    onNewMessageClick: () -> Unit
){
    Scaffold(
        topBar = {
            ToolBarWithIcon(
                iconBack = R.drawable.arrow_back,
                title = R.string.emails_referral,
                backClick = { onBackClick() },
                argument = referral.name
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                containerColor = MaterialTheme.colorScheme.primary,
                onClick = onNewMessageClick
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        content = { innerPadding ->
            InBox(
                searchText = searchText,
                onValueChange = onValueChange,
                onMessageClick = onMessageClick,
                messages = messages,
                currentUserId = user?.uid?:"",
                clientWhoReferred = clientWhoReferred,
                providerThatReceived = providerThatReceived,
                isLoading = isLoading,
                modifier = Modifier.padding(innerPadding)
            )
        }
    )
}

@Composable
fun InBox(
    searchText: String,
    onValueChange: (String) -> Unit,
    onMessageClick: (Message) -> Unit,
    messages: List<Message>,
    currentUserId: String,
    clientWhoReferred: UserData?,
    providerThatReceived: UserData?,
    isLoading: Boolean,
    modifier: Modifier = Modifier
){
    Column(modifier = modifier.fillMaxSize()){
        SearchFieldBasic(
            value = searchText,
            onValueChange = onValueChange,
            placeholder = R.string.search_in_mail,
            trailingIcon = R.drawable.search,
            modifier = Modifier
                .padding(16.dp)
        )
        Box(modifier = Modifier.weight(1f).fillMaxWidth()){
            if(!isLoading){
                if(messages.isNotEmpty()){
                    MessagesList(
                        onMessageClick = onMessageClick,
                        messages = messages,
                        currentUserId = currentUserId,
                        clientWhoReferred = clientWhoReferred,
                        providerThatReceived = providerThatReceived)
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text= stringResource(R.string.no_have_emails))
                    }
                }
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}