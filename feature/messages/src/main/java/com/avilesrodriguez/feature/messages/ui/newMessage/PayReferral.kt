package com.avilesrodriguez.feature.messages.ui.newMessage

import androidx.annotation.StringRes
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.banksPays.BanksEcuador
import com.avilesrodriguez.presentation.banksPays.options
import com.avilesrodriguez.presentation.composables.MenuDropdownBoxLeadIcon
import com.avilesrodriguez.presentation.attachment.AttachmentPreviews
import com.avilesrodriguez.presentation.banksPays.copyClientData
import com.avilesrodriguez.presentation.banksPays.openBankApp
import kotlinx.coroutines.delay

@Composable
fun PayReferral(
    sharedUri: String?,
    onBackClick: () -> Unit,
    openAndPopUp: (String, String) -> Unit,
    viewModel: NewMessageViewModel = hiltViewModel()
){
    LaunchedEffect(sharedUri) {
        sharedUri?.let { uri ->
            viewModel.onAttachFiles(listOf(uri))
        }
    }
    val referral by viewModel.referralState.collectAsState()
    val loading by viewModel.isLoading.collectAsState()
    val localFiles by viewModel.localFiles.collectAsState()
    val clientWhoReferred = viewModel.clientWhoReferred
    val amountUsdState by viewModel.amountUsdState.collectAsState()
    val selectedOption by viewModel.selectedOption.collectAsState()

    val context = LocalContext.current

    val subjectPaid = stringResource(R.string.proof_of_payment, referral.name)
    val contentPaid = stringResource(R.string.content_payment, amountUsdState, referral.name)
    val selectedBankPackage = selectedOption?.packageName ?:""


    if(clientWhoReferred != null){
        val client = clientWhoReferred as UserData.Client
        BankDetailsCard(
            client = client,
            amountUsd = amountUsdState,
            onAmountChange = viewModel::onAmountChange,
            onPayClick = {
                if(selectedBankPackage.isNotBlank()) openBankApp(selectedBankPackage, context)
                },  //open apps of banks
            onCancelButton = onBackClick,
            onCopyClick = {infoUser -> copyClientData(context, infoUser)}, //copy data referral
            selectedOption = selectedOption?.label,
            onBankChange = viewModel::onBankChange,
            onSendPay = {viewModel.onSendPay(subjectPaid, contentPaid, openAndPopUp)}, //update info referral and user.Client
            loading = loading,
            localFiles = localFiles,
            onRemoveFile = viewModel::onRemoveFile
        )
    } else {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}
@Composable
fun BankDetailsCard(
    client: UserData.Client,
    amountUsd: String,
    onAmountChange: (String) -> Unit,
    onPayClick: () -> Unit,
    onCancelButton: () -> Unit,
    onCopyClick: (String) -> Unit,
    selectedOption: Int?,
    onBankChange: (Int) -> Unit,
    onSendPay: () -> Unit,
    loading: Boolean,
    localFiles: List<String>,
    onRemoveFile: (String) -> Unit,
) {
    val banksOptions = BanksEcuador.options()
    //var isClickPay by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.client_bank_details),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(Modifier.height(8.dp))
            DetailRow(label = R.string.bank_name, value = client.bankName ?: "")
            DetailRow(label = R.string.account_type, value = client.accountType ?: "")
            DetailRowCopy(label = R.string.count_number_pay, value = client.countNumberPay ?: ""){onCopyClick(it)}
            DetailRowCopy(label = R.string.identity_card, value = client.identityCard ?: ""){onCopyClick(it)}
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = stringResource(R.string.amount_usd),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(
                    value = amountUsd,
                    onValueChange = onAmountChange,
                    label = { Text(stringResource(R.string.zero)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    singleLine = true,
                    maxLines = 1,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            MenuDropdownBoxLeadIcon(
                options = banksOptions,
                selectedOption = selectedOption?:R.string.choose_your_bank,
                onClick = onBankChange,
                modifier = Modifier.widthIn(max = 164.dp)
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ){
                OutlinedButton(
                    onClick = { onCancelButton() },
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                ) {
                    Icon(Icons.Default.Cancel, null, modifier = Modifier.size(18.dp))
                    Text(stringResource(R.string.cancel))
                }
                val canPay = !loading && selectedOption !=null
                Button(
                    onClick = { onPayClick() },
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                    enabled = canPay
                ) {
                    Icon(Icons.Default.Apps, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.pay_commission))
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
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
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
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
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
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
                    tint = if (isSelected) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface
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