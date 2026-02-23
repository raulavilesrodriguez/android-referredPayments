package com.avilesrodriguez.feature.messages.ui.newMessage

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.avilesrodriguez.domain.model.message.Message
import com.avilesrodriguez.domain.model.referral.Referral
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.composables.ToolBarWithIcon
import com.avilesrodriguez.presentation.ext.MAX_LENGTH_CONTENT

@Composable
fun RejectReferral(
    referralId: String?,
    onBackClick: () -> Unit,
    viewModel: NewMessageViewModel = hiltViewModel()
){
    LaunchedEffect(Unit) {
        viewModel.loadReferralInformation(referralId.orEmpty())
    }
    val newMessageState by viewModel.newMessageState.collectAsState()
    val user by viewModel.userDataStore.collectAsState()
    val referral by viewModel.referralState.collectAsState()
    val loading by viewModel.isLoading.collectAsState()
    val localFiles by viewModel.localFiles.collectAsState()
    val clientWhoReferred = viewModel.clientWhoReferred
    val providerThatReceived = viewModel.providerThatReceived

    val subjectReject = stringResource(R.string.rejected_referral, referral.name)


}

@Composable
private fun RejectReferralContent(
    onBackClick: () -> Unit,
    newMessageState: Message,
    user: UserData?,
    referral: Referral,
    loading: Boolean,
    localFiles: List<String>,
    onSubjectChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onAttachFiles: (List<String>) -> Unit,
    onRemoveFile: (String) -> Unit,
    onSaveMessage: () -> Unit,
    clientWhoReferred: UserData?,
    providerThatReceived: UserData?
){
    Scaffold(
        topBar = {
            ToolBarWithIcon(
                iconBack = R.drawable.arrow_back,
                title = stringResource(R.string.referred, referral.name),
                backClick = { onBackClick() }
            )
        },
        content = { paddingValues ->
            MessageReject(
                newMessageState = newMessageState,
                user = user,
                referral = referral,
                loading = loading,
                localFiles = localFiles,
                onSubjectChange = onSubjectChange,
                onContentChange = onContentChange,
                onAttachFiles = onAttachFiles,
                onRemoveFile = onRemoveFile,
                onSaveMessage = onSaveMessage,
                clientWhoReferred = clientWhoReferred,
                providerThatReceived = providerThatReceived,
                modifier = Modifier.padding(paddingValues)
            )
        }
    )
}

@Composable
private fun MessageReject(
    newMessageState: Message,
    user: UserData?,
    referral: Referral,
    loading: Boolean,
    localFiles: List<String>,
    onSubjectChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onAttachFiles: (List<String>) -> Unit,
    onRemoveFile: (String) -> Unit,
    onSaveMessage: () -> Unit,
    clientWhoReferred: UserData?,
    providerThatReceived: UserData?,
    modifier: Modifier = Modifier
){
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        onAttachFiles(uris.map { it.toString() })
    }


    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val from = when(user){
            is UserData.Provider -> {providerThatReceived?.name?:""}
            is UserData.Client -> {clientWhoReferred?.name?:""}
            else -> {""}
        }

        val to = when(user){
            is UserData.Provider -> {clientWhoReferred?.name?:""}
            is UserData.Client -> {providerThatReceived?.name?:""}
            else -> {""}
        }
        Text(
            text = stringResource(R.string.reject),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
        InfoHeadMessage(label = stringResource(R.string.from), value = from)
        InfoHeadMessage(label = stringResource(R.string.to), value = to)
        InfoHeadMessage(label = stringResource(R.string.rejected_referral), value = referral.name)
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // --- 3. CONTENIDO DEL MENSAJE ---
        Column {
            OutlinedTextField(
                value = newMessageState.content,
                onValueChange = onContentChange,
                label = { Text(stringResource(R.string.message_body)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 160.dp),
                shape = RoundedCornerShape(12.dp)
            )
            Text(
                text = "${newMessageState.content.length}/$MAX_LENGTH_CONTENT",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}