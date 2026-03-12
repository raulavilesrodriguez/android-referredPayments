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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.avilesrodriguez.domain.model.message.Message
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.attachment.AttachmentPreviews
import com.avilesrodriguez.presentation.ext.MAX_LENGTH_CONTENT
import com.avilesrodriguez.presentation.ext.MAX_LENGTH_SUBJECT

@Composable
fun ProcessReferral(
    newMessageState: Message,
    from: String,
    to: String,
    loading: Boolean,
    localFiles: List<String>,
    onSubjectChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onAttachFiles: (List<String>) -> Unit,
    onRemoveFile: (String) -> Unit,
    onSaveMessage: () -> Unit
){
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        onAttachFiles(uris.map { it.toString() })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        InfoHeadMessage(label = stringResource(R.string.from), value = from)
        InfoHeadMessage(label = stringResource(R.string.to), value = to)

        Column {
            OutlinedTextField(
                value = newMessageState.subject,
                onValueChange = onSubjectChange,
                label = { Text(stringResource(R.string.subject)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
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

        // --- 3. CONTENIDO DEL MENSAJE ---
        Column {
            OutlinedTextField(
                value = newMessageState.content,
                onValueChange = onContentChange,
                label = { Text(stringResource(R.string.message_body)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 160.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
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
                Icon(Icons.Default.AttachFile, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(text=stringResource(R.string.attach), color = MaterialTheme.colorScheme.primary)
            }

            val canSend = !loading &&
                    newMessageState.subject.isNotBlank() &&
                    newMessageState.content.isNotBlank()

            Button(
                onClick = onSaveMessage,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                enabled = canSend
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