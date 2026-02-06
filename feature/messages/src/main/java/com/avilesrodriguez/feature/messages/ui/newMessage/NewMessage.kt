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
        onSaveMessage = { viewModel.onSaveMessage(onBackClick) }
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
    onSaveMessage: () -> Unit
){
    Scaffold(
        topBar = {
            ToolBarWithIcon(
                iconBack = R.drawable.arrow_back,
                title = R.string.new_email,
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
        // --- 1. SELECTOR DE ESTADO (Solo para Providers) ---
        if (user is UserData.Provider) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.status_update),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                val statusOptions = listOf(
                    ReferralStatus.PROCESSING,
                    ReferralStatus.REJECTED,
                    ReferralStatus.PAID
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        }

        // --- 2. ASUNTO ---
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

        // --- 3. CONTENIDO DEL MENSAJE ---
        Column {
            OutlinedTextField(
                value = newMessageState.content,
                onValueChange = onContentChange,
                label = { Text(stringResource(R.string.message_body)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 150.dp),
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
    }
}
