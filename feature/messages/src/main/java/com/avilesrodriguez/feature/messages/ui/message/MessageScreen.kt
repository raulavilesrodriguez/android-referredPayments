package com.avilesrodriguez.feature.messages.ui.message

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.avilesrodriguez.domain.model.message.Message
import com.avilesrodriguez.domain.model.referral.Referral
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.attachment.AttachmentDownload
import com.avilesrodriguez.presentation.composables.ButtonWithIcon
import com.avilesrodriguez.presentation.time.formatTimeBasic

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
    val user by viewModel.userDataStore.collectAsState()
    val messageState by viewModel.messageState.collectAsState()
    val referralState by viewModel.referralState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val clientWhoReferred = viewModel.clientWhoReferred
    val providerThatReceived = viewModel.providerThatReceived

    if(!isLoading){
        MessageScreenContent(
            onBackClick = onBackClick,
            user = user,
            messageState = messageState,
            referral = referralState,
            clientWhoReferred = clientWhoReferred,
            providerThatReceived = providerThatReceived,
            onReplyClick = { viewModel.replyMessage(openScreen) },
            downloadFile = {viewModel.downloadFile(it)}
        )
    }else{
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(8.dp)
            )
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MessageScreenContent(
    onBackClick: () -> Unit,
    user: UserData?,
    messageState: Message,
    referral: Referral,
    clientWhoReferred: UserData?,
    providerThatReceived: UserData?,
    onReplyClick: () -> Unit,
    downloadFile: (String) -> Unit,
){
    Scaffold(
        topBar = {
            TopAppBar(
                title = {Text(text = referral.name)},
                navigationIcon = {
                    IconButton(
                        onClick = { onBackClick() }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_back),
                            contentDescription = null
                        )
                    }
                }
            )
        },
        bottomBar = {
            ButtonWithIcon(
                text = R.string.reply,
                icon = R.drawable.reply,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp)
            ){ onReplyClick()}
        },
        content = { innerPadding ->
            Email(
                user = user,
                messageState = messageState,
                clientWhoReferred = clientWhoReferred,
                providerThatReceived = providerThatReceived,
                downloadFile = downloadFile,
                modifier = Modifier.padding(innerPadding)
            )
        }
    )
}

@Composable
private fun Email(
    user: UserData?,
    messageState: Message,
    clientWhoReferred: UserData?,
    providerThatReceived: UserData?,
    downloadFile: (String) -> Unit,
    modifier: Modifier = Modifier
){
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val formattedDate = formatTimeBasic(messageState.createdAt)

        val from = when(user){
            is UserData.Provider -> {
                if(user.uid == messageState.senderId) providerThatReceived?.name?:"" else clientWhoReferred?.name?:""
            }
            is UserData.Client -> {
                if(user.uid == messageState.senderId) clientWhoReferred?.name?:"" else providerThatReceived?.name?:""
            }
            else -> {""}
        }

        val to = when(user){
            is UserData.Provider -> {
                if(user.uid == messageState.receiverId) providerThatReceived?.name?:"" else clientWhoReferred?.name?:""
            }
            is UserData.Client -> {
                if(user.uid == messageState.receiverId) clientWhoReferred?.name?:"" else providerThatReceived?.name?:""
            }
            else -> {""}
        }
        InfoHeadEmail(title = stringResource(R.string.from), value = from, modifier = Modifier.padding(top=8.dp))
        InfoHeadEmail(title = stringResource(R.string.to), value = to)
        InfoHeadEmail(title = stringResource(R.string.date), value = formattedDate)
        InfoHeadEmail(title = stringResource(R.string.subject), value = messageState.subject)
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        Text(
            text = messageState.content,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        )
        if(messageState.attachmentsUrl.isNotEmpty()) {
            Text(
                text = stringResource(R.string.attachments),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            val localFiles = messageState.attachmentsUrl
            AttachmentDownload(localFiles, downloadFile)
        }
    }
}

@Composable
private fun InfoHeadEmail(
    title: String,
    value: String,
    modifier: Modifier = Modifier
){
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ){
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall
        )
    }
}