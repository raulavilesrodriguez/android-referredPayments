package com.avilesrodriguez.feature.messages.ui.newMessage

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.avilesrodriguez.domain.model.message.Message
import com.avilesrodriguez.domain.model.referral.Referral
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.attachment.AttachmentPreviews
import com.avilesrodriguez.presentation.composables.MenuDropdownBoxStrings

@Composable
fun RejectReferral(
    newMessageState: Message,
    from: String,
    to: String,
    referral: Referral,
    loading: Boolean,
    localFiles: List<String>,
    onContentChange: (String) -> Unit,
    onAttachFiles: (List<String>) -> Unit,
    onRemoveFile: (String) -> Unit,
    onReasonToReject: (String) -> Unit,
    onRejectMessage: () -> Unit
){
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        onAttachFiles(uris.map { it.toString() })
    }

    val rejectionOptions = listOf(
        stringResource(R.string.rejection_wrong_number),
        stringResource(R.string.rejection_not_interested),
        stringResource(R.string.rejection_unknown_referrer),
        stringResource(R.string.rejection_already_client),
        stringResource(R.string.rejection_duplicated),
        stringResource(R.string.rejection_out_of_area),
        stringResource(R.string.rejection_unreachable),
        stringResource(R.string.rejection_not_qualified)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val subjectReject = stringResource(R.string.rejected_referral, referral.name)

        InfoHeadMessage(label = stringResource(R.string.from), value = from)
        InfoHeadMessage(label = stringResource(R.string.to), value = to)
        InfoHeadMessage(label = stringResource(R.string.subject), value = subjectReject)
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // --- 3. CONTENIDO DEL MENSAJE ---
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MenuDropdownBoxStrings(
                options = rejectionOptions,
                selectedOption = newMessageState.content,
                onClick = onReasonToReject,
                title = stringResource(R.string.select_rejection_reason)
            )
            Spacer(Modifier.height(8.dp))
            if(newMessageState.content.isNotBlank()){
                Text(
                    text = stringResource(R.string.message_body),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = newMessageState.content,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Justify
                )
            }
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
                Icon(Icons.Default.AttachFile, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(text=stringResource(R.string.attach), color = MaterialTheme.colorScheme.primary)
            }

            val canSend = !loading && newMessageState.content.isNotBlank()

            Button(
                onClick = onRejectMessage,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                enabled = canSend
            ) {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Icon(Icons.AutoMirrored.Filled.Send, null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.reject))
                }
            }
        }
    }
}