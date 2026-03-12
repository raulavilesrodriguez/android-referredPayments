package com.avilesrodriguez.feature.messages.ui.newMessage

import androidx.annotation.StringRes
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.avilesrodriguez.domain.model.referral.Referral
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.banksPays.BanksEcuador
import com.avilesrodriguez.presentation.banksPays.options
import com.avilesrodriguez.presentation.composables.MenuDropdownBoxLeadIcon
import com.avilesrodriguez.presentation.attachment.AttachmentPreviews
import com.avilesrodriguez.presentation.banksPays.label
import com.avilesrodriguez.presentation.fakeData.referral
import com.avilesrodriguez.presentation.fakeData.userClient
import kotlinx.coroutines.delay

@Composable
fun PayReferral(
    referral: Referral,
    from: String,
    to: String,
    clientWhoReferred: UserData?,
    amountUsd: String,
    onAmountChange: (String) -> Unit,
    onPayClick: () -> Unit,
    onCancelPay: () -> Unit,
    onCopyClick: (String) -> Unit,
    selectedOption: Int?,
    onBankChange: (Int) -> Unit,
    onSendPay: () -> Unit,
    loading: Boolean,
    localFiles: List<String>,
    onRemoveFile: (String) -> Unit
) {
    val banksOptions = BanksEcuador.options()
    val client = clientWhoReferred as UserData.Client
    val subjectPaid = stringResource(R.string.proof_of_payment, referral.name)

    Column(
        modifier = Modifier
        .fillMaxSize()
        .padding(8.dp)
        .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        InfoHeadMessage(label = stringResource(R.string.from), value = from)
        InfoHeadMessage(label = stringResource(R.string.to), value = to)
        InfoHeadMessage(label= stringResource(R.string.subject), value = subjectPaid)
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        DetailRow(label = R.string.name, value = client.name ?: "")
        DetailRow(label = R.string.bank_name, value = client.bankName ?: "")
        DetailRow(label = R.string.account_type, value = stringResource(client.accountType.label()))
        DetailRowCopy(label = R.string.count_number_pay, value = client.countNumberPay ?: ""){onCopyClick(it)}
        DetailRowCopy(label = R.string.identity_card, value = client.identityCard ?: ""){onCopyClick(it)}
        
        Text(
            text = stringResource(R.string.amount_to_pay),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        OutlinedTextField(
            placeholder = {
                Text(
                    text=stringResource(R.string.usd_0_00),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                ) },
            value = amountUsd,
            onValueChange = onAmountChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            singleLine = true,
            maxLines = 1,
            shape = RoundedCornerShape(12.dp),
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.Center
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        )
        
        MenuDropdownBoxLeadIcon(
            options = banksOptions,
            selectedOption = selectedOption?:R.string.choose_your_bank,
            onClick = onBankChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ){
            OutlinedButton(
                onClick = { onCancelPay() },
                modifier = Modifier.padding(vertical = 4.dp),
            ) {
                Icon(Icons.Default.Cancel, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(text=stringResource(R.string.cancel), color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.width(12.dp))
            val canPay = !loading && selectedOption !=null
            Button(
                onClick = { onPayClick() },
                modifier = Modifier.padding(vertical = 4.dp),
                enabled = canPay
            ) {
                Icon(Icons.Default.Apps, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.pay_commission))
            }
        }

        Text(
            text = stringResource(R.string.proof_of_payment_attach),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 100.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceContainerLowest,
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ){
                if (localFiles.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_files_attached),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    AttachmentPreviews(uris = localFiles, onRemove = onRemoveFile)
                }
            }
            
            val amountValue = amountUsd.toDoubleOrNull() ?: 0.0
            val canSend = !loading && amountUsd.isNotBlank() && amountValue > 0.0 && localFiles.isNotEmpty()
            
            Button(
                onClick = { onSendPay() },
                modifier = Modifier.height(50.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = canSend,
                contentPadding = PaddingValues(12.dp)
            ) {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.AutoMirrored.Filled.Send, null)
                        Text(stringResource(R.string.send), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
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

@Composable
private fun DetailRow(@StringRes label: Int, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(stringResource(label), style = MaterialTheme.typography.bodySmall)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DetailRowCopy(@StringRes label: Int, value: String, onCopyClick: (String) -> Unit){
    var isSelected by remember { mutableStateOf(false) }
    LaunchedEffect(isSelected) {
        if (isSelected) {
            delay(2500L)
            isSelected = false
        }
    }
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(stringResource(label), style = MaterialTheme.typography.bodySmall)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(
                onClick = {
                    onCopyClick(value)
                    isSelected = true
                },
                enabled = !isSelected
            ) {
                Icon(
                    imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
            if (isSelected) {
                Text(
                    text = stringResource(R.string.copied),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.animateContentSize()
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PayReferralPreview(){
    MaterialTheme {
        PayReferral(
            referral = referral,
            from = "Seguros Atlantida",
            to = "Brayan Muelas",
            clientWhoReferred = userClient,
            amountUsd = "10",
            onAmountChange = {},
            onPayClick = {},
            onCancelPay = {},
            onCopyClick = {},
            selectedOption = null,
            onBankChange = {},
            onSendPay = {},
            loading = false,
            localFiles = emptyList(),
            onRemoveFile = {}
        )
    }
}
