package com.avilesrodriguez.feature.messages.ui.newMessage

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.avilesrodriguez.domain.model.message.Message
import com.avilesrodriguez.domain.model.referral.Referral
import com.avilesrodriguez.domain.model.referral.ReferralStatus
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.attachment.AttachmentPreviews
import com.avilesrodriguez.presentation.composables.ToolBarWithIcon
import com.avilesrodriguez.presentation.ext.MAX_LENGTH_CONTENT
import com.avilesrodriguez.presentation.ext.MAX_LENGTH_SUBJECT
import com.avilesrodriguez.presentation.ext.toDisplayName
import com.avilesrodriguez.presentation.ext.toDisplayIcon
import com.avilesrodriguez.presentation.ext.toColor
import com.avilesrodriguez.presentation.fakeData.message1
import com.avilesrodriguez.presentation.fakeData.referral
import com.avilesrodriguez.presentation.fakeData.userClient
import com.avilesrodriguez.presentation.fakeData.userProvider

@Composable
fun NewMessage(
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


    NewMessageContent(
        onBackClick = onBackClick,
        newMessageState = newMessageState,
        user = user,
        referral = referral,
        loading = loading,
        localFiles = localFiles,
        onSubjectChange = viewModel::onSubjectChange,
        onContentChange = viewModel::onContentChange,
        onAttachFiles = viewModel::onAttachFiles,
        onRemoveFile = viewModel::onRemoveFile,
        onStatusChange = viewModel::onStatusChange,
        onSaveMessage = { viewModel.onSaveMessage(onBackClick) },
        clientWhoReferred = clientWhoReferred,
        providerThatReceived = providerThatReceived
    )

}

@Composable
fun NewMessageContent(
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
    onStatusChange: (ReferralStatus) -> Unit,
    onSaveMessage: () -> Unit,
    clientWhoReferred: UserData?,
    providerThatReceived: UserData?
){
    Scaffold(
        topBar = {
            ToolBarWithIcon(
                iconBack = R.drawable.arrow_back,
                title = R.string.process_referral,
                backClick = { onBackClick() }
            )
        },
        content = { paddingValues ->
            NewEmail(
                newMessageState = newMessageState,
                user = user,
                referral = referral,
                loading = loading,
                localFiles = localFiles,
                onSubjectChange = onSubjectChange,
                onContentChange = onContentChange,
                onAttachFiles = onAttachFiles,
                onRemoveFile = onRemoveFile,
                onStatusChange = onStatusChange,
                onSaveMessage = onSaveMessage,
                clientWhoReferred = clientWhoReferred,
                providerThatReceived = providerThatReceived,
                modifier = Modifier.padding(paddingValues),
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewEmail(
    newMessageState: Message,
    user: UserData?,
    referral: Referral,
    loading: Boolean,
    localFiles: List<String>,
    onSubjectChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onAttachFiles: (List<String>) -> Unit,
    onRemoveFile: (String) -> Unit,
    onStatusChange: (ReferralStatus) -> Unit,
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
        verticalArrangement = Arrangement.spacedBy(16.dp)
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top=8.dp),
            horizontalArrangement = Arrangement.Start
        ){
            Text(
                text = stringResource(R.string.from),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = from,
                style = MaterialTheme.typography.titleSmall
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ){
            Text(
                text = stringResource(R.string.to),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = to,
                style = MaterialTheme.typography.titleSmall
            )
        }

        Column {
            OutlinedTextField(
                value = newMessageState.subject,
                onValueChange = onSubjectChange,
                label = { Text(stringResource(R.string.subject)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Text(
                text = "${newMessageState.subject.length}/$MAX_LENGTH_SUBJECT",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // --- 1. SELECTOR DE ESTADO (Solo para Providers) ---
        if (user is UserData.Provider) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.status_update),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.referred, referral.name),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val statusOptions = listOf(
                    ReferralStatus.PROCESSING,
                    ReferralStatus.REJECTED,
                    ReferralStatus.PAID
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(statusOptions) { status ->
                        val isSelected = referral.status == status
                        FilterChip(
                            selected = isSelected,
                            onClick = { onStatusChange(status) },
                            label = { Text(stringResource(status.toDisplayName())) },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(status.toDisplayIcon()),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = if (isSelected) Color.Unspecified else status.toColor()
                                )
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
            if(referral.status == ReferralStatus.PAID){
                (clientWhoReferred as? UserData.Client)?.let { client ->
                    BankDetailsCard(
                        client = client,
                        amountUsd = "",
                        onAmountChange = {},
                        onPayClick = {},
                        onCancelButton = {},
                        onCopyClick = {}
                    )
                }
                val subjectPaid = stringResource(R.string.proof_of_payment, referral.name)
                onSubjectChange(subjectPaid)
                LaunchedEffect(Unit) {
                    if(newMessageState.subject.isBlank()){
                        onSubjectChange(subjectPaid)
                    }
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        }

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

        // --- 4. VISTA PREVIA DE ADJUNTOS ---
        if (localFiles.isNotEmpty()) {
            Text(
                text = stringResource(R.string.attachments),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            AttachmentPreviews(uris = localFiles, onRemove = onRemoveFile)
        }

        // --- 5. BOTONES DE ACCIÓN ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { launcher.launch("*/*") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.AttachFile, null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.attach))
            }

            Button(
                onClick = onSaveMessage,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                enabled = !loading && newMessageState.subject.isNotBlank() && newMessageState.content.isNotBlank()
            ) {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Icon(Icons.AutoMirrored.Filled.Send, null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.send))
                }
            }
        }
        Text(
            text = stringResource(R.string.warning_to_send_pay),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NewMessagePreview(){
    MaterialTheme {
        NewMessageContent(
            onBackClick = {},
            newMessageState = message1,
            user = userProvider,
            referral = referral,
            loading = false,
            localFiles = emptyList(),
            onSubjectChange = {},
            onContentChange = {},
            onAttachFiles = {},
            onRemoveFile = {},
            onStatusChange = {},
            onSaveMessage = {},
            clientWhoReferred = userClient,
            providerThatReceived = userProvider
        )
    }
}