package com.avilesrodriguez.feature.messages.ui.newMessage

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.banksPays.BanksEcuador
import com.avilesrodriguez.presentation.banksPays.options
import com.avilesrodriguez.presentation.composables.MenuDropdownBoxLeadIcon

@Composable
fun PayReferral(
    onBackClick: () -> Unit,
    openScreen: (String) -> Unit,
    viewModel: NewMessageViewModel = hiltViewModel()
){
    val newMessageState by viewModel.newMessageState.collectAsState()
    val user by viewModel.userDataStore.collectAsState()
    val referral by viewModel.referralState.collectAsState()
    val loading by viewModel.isLoading.collectAsState()
    val localFiles by viewModel.localFiles.collectAsState()
    val clientWhoReferred = viewModel.clientWhoReferred
    val amountUsdState by viewModel.amountUsdState.collectAsState()
    val selectedOption by viewModel.selectedOption.collectAsState()

    val subjectPaid = stringResource(R.string.proof_of_payment, referral.name)
    if(clientWhoReferred != null){
        val client = clientWhoReferred as UserData.Client
        BankDetailsCard(
            client = client,
            amountUsd = amountUsdState,
            onAmountChange = viewModel::onAmountChange,
            onPayClick = {  },
            onCancelButton = onBackClick,
            onCopyClick = {},
            selectedOption = selectedOption?.label,
            onBankChange = viewModel::onBankChange,
            onSendPay = {}
        )
    }
}
@Composable
fun BankDetailsCard(
    client: UserData.Client,
    amountUsd: String,
    onAmountChange: (String) -> Unit,
    onPayClick: () -> Unit,
    onCancelButton: () -> Unit,
    onCopyClick: () -> Unit,
    selectedOption: Int?,
    onBankChange: (Int) -> Unit,
    onSendPay: () -> Unit
) {
    val banksOptions = BanksEcuador.options()
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
            DetailRowCopy(label = R.string.count_number_pay, value = client.countNumberPay ?: ""){onCopyClick()}
            DetailRowCopy(label = R.string.identity_card, value = client.identityCard ?: ""){onCopyClick()}
            OutlinedTextField(
                value = amountUsd,
                onValueChange = onAmountChange,
                label = { Text(stringResource(R.string.amount_usd)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                singleLine = true,
                maxLines = 1,
                shape = RoundedCornerShape(12.dp)
            )
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
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
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
                Button(
                    onClick = { onPayClick() },
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth()
                ) {
                    Icon(Icons.Default.Apps, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.pay_commission))
                }
            }
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
private fun DetailRowCopy(@StringRes label: Int, value: String, onCopyClick: () -> Unit){
    var isSelected by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(stringResource(label), style = MaterialTheme.typography.bodySmall)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        if(isSelected){
            IconButton(
                onClick = { isSelected = false },
            ) {
                Icon(Icons.Default.Bookmark, null, modifier = Modifier.size(18.dp))
            }
        }else{
            IconButton(
                onClick = {
                    onCopyClick()
                    isSelected = !isSelected
                          },
            ) {
                Icon(Icons.Default.BookmarkBorder, null, modifier = Modifier.size(18.dp))
            }
        }
    }
}