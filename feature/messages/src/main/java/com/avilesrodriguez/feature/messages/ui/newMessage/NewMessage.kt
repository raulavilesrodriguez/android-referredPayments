package com.avilesrodriguez.feature.messages.ui.newMessage

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.avilesrodriguez.domain.model.message.Message
import com.avilesrodriguez.domain.model.referral.Referral
import com.avilesrodriguez.domain.model.referral.ReferralStatus
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.banksPays.copyClientData
import com.avilesrodriguez.presentation.banksPays.openBankApp
import com.avilesrodriguez.presentation.composables.BasicToolbar
import com.avilesrodriguez.presentation.composables.ToolBarWithIcon
import com.avilesrodriguez.presentation.ext.nameSelect
import com.avilesrodriguez.presentation.fakeData.message1
import com.avilesrodriguez.presentation.fakeData.referral
import com.avilesrodriguez.presentation.fakeData.userClient
import com.avilesrodriguez.presentation.fakeData.userProvider
import com.avilesrodriguez.presentation.viewmodel.SharedAttachmentViewModel

@Composable
fun NewMessage(
    referralId: String?,
    onBackClick: () -> Unit,
    openScreen: (String) -> Unit,
    showTopBar: Boolean = true,
    viewModel: NewMessageViewModel = hiltViewModel(),
    sharedAttachmentViewModel: SharedAttachmentViewModel = hiltViewModel(LocalActivity.current as ComponentActivity)
){
    LaunchedEffect(referralId) {
        viewModel.loadReferralInformation(referralId.orEmpty())
    }
    // Observamos el archivo del ViewModel compartido
    val sharedFileFromActivity = sharedAttachmentViewModel.currentFileUri
    LaunchedEffect(sharedFileFromActivity) {
        if(sharedFileFromActivity != null){
            viewModel.onAttachFiles(listOf(sharedFileFromActivity))
            sharedAttachmentViewModel.consumeFile()
        }
    }
    val newMessageState by viewModel.newMessageState.collectAsState()
    val user by viewModel.userDataStore.collectAsState()
    val referral by viewModel.referralState.collectAsState()
    val loading by viewModel.isLoading.collectAsState()
    val localFiles by viewModel.localFiles.collectAsState()
    val clientWhoReferred = viewModel.clientWhoReferred
    val providerThatReceived = viewModel.providerThatReceived
    val amountUsdState by viewModel.amountUsdState.collectAsState()
    val selectedOption by viewModel.selectedOption.collectAsState()

    val context = LocalContext.current

    val subjectPaid = stringResource(R.string.proof_of_payment, referral.name)
    val contentPaid = stringResource(R.string.content_payment, amountUsdState, referral.name)
    val selectedBankPackage = selectedOption?.packageName ?:""
    val subjectReject = stringResource(R.string.rejected_referral, referral.name)

    NewMessageContent(
        onBackClick = {
            viewModel.resetValues()
            onBackClick()
                      },
        newMessageState = newMessageState,
        user = user,
        referral = referral,
        loading = loading,
        localFiles = localFiles,
        onSubjectChange = viewModel::onSubjectChange,
        onContentChange = viewModel::onContentChange,
        onAttachFiles = viewModel::onAttachFiles,
        onRemoveFile = viewModel::onRemoveFile,
        onSaveMessage = { viewModel.onSaveMessage(onBackClick) },
        onRejectMessage = {viewModel.onRejectReferral(subjectReject, onBackClick)},
        clientWhoReferred = clientWhoReferred,
        providerThatReceived = providerThatReceived,
        amountUsd = amountUsdState,
        onAmountChange = viewModel::onAmountChange,
        onPayClick = {
            if(selectedBankPackage.isNotBlank()) openBankApp(selectedBankPackage, context)
        },
        onCancelPay = {
            viewModel.resetValues()
            onBackClick()
                      },
        onCopyClick = {infoUser -> copyClientData(context, infoUser)},
        selectedOption = selectedOption?.label,
        onBankChange = viewModel::onBankChange,
        onSendPay = {viewModel.onSendPay(subjectPaid, contentPaid, onBackClick)},
        resetValues = viewModel::resetValues,
        showTopBar = showTopBar,
        onReasonToReject = viewModel::onReasonToReject
    )
}

@Composable
private fun NewMessageContent(
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
    onRejectMessage: () -> Unit,
    clientWhoReferred: UserData?,
    providerThatReceived: UserData?,
    amountUsd: String,
    onAmountChange: (String) -> Unit,
    onPayClick: () -> Unit,
    onCancelPay: () -> Unit,
    onCopyClick: (String) -> Unit,
    selectedOption: Int?,
    onBankChange: (Int) -> Unit,
    onSendPay: () -> Unit,
    resetValues: () -> Unit,
    showTopBar: Boolean,
    onReasonToReject: (String) -> Unit,
){
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            if(showTopBar){
                ToolBarWithIcon(
                    iconBack = R.drawable.arrow_back,
                    title = stringResource(R.string.referred, referral.name),
                    backClick = { onBackClick() }
                )
            }else{
                BasicToolbar(title = stringResource(R.string.referred, referral.name))
            }
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
                onSaveMessage = onSaveMessage,
                onRejectMessage = onRejectMessage,
                clientWhoReferred = clientWhoReferred,
                providerThatReceived = providerThatReceived,
                amountUsd = amountUsd,
                onAmountChange = onAmountChange,
                onPayClick = onPayClick,
                onCancelPay = onCancelPay,
                onCopyClick = onCopyClick,
                selectedOption = selectedOption,
                onBankChange = onBankChange,
                onSendPay = onSendPay,
                resetValues = resetValues,
                onReasonToReject = onReasonToReject,
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
    onSaveMessage: () -> Unit,
    onRejectMessage: () -> Unit,
    clientWhoReferred: UserData?,
    providerThatReceived: UserData?,
    amountUsd: String,
    onAmountChange: (String) -> Unit,
    onPayClick: () -> Unit,
    onCancelPay: () -> Unit,
    onCopyClick: (String) -> Unit,
    selectedOption: Int?,
    onBankChange: (Int) -> Unit,
    onSendPay: () -> Unit,
    resetValues: () -> Unit,
    onReasonToReject: (String) -> Unit,
    modifier: Modifier = Modifier
){
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

    var showProcessReferral by rememberSaveable { mutableStateOf(true) }
    var showPayReferral by rememberSaveable { mutableStateOf(false) }
    var showRejectReferral by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if(user is UserData.Provider && referral.status == ReferralStatus.PROCESSING){
            StatusProcess { status ->
                when(status){
                    ReferralStatus.REJECTED -> {
                        showProcessReferral = false
                        showPayReferral = false
                        showRejectReferral = true
                        resetValues()
                    }
                    ReferralStatus.PAID -> {
                        showProcessReferral = false
                        showPayReferral = true
                        showRejectReferral = false
                        resetValues()
                    }
                    ReferralStatus.PROCESSING -> {
                        showProcessReferral = true
                        showPayReferral = false
                        showRejectReferral = false
                        resetValues()
                    }
                    else -> {}
                }
            }
        }
        Spacer(Modifier.height(8.dp))

        if(showProcessReferral){
            ProcessReferral(
                newMessageState = newMessageState,
                from = from,
                to = to,
                loading = loading,
                localFiles = localFiles,
                onSubjectChange = onSubjectChange,
                onContentChange = onContentChange,
                onAttachFiles = onAttachFiles,
                onRemoveFile = onRemoveFile,
                onSaveMessage = onSaveMessage
            )
        }
        if(showRejectReferral){
            RejectReferral(
                newMessageState = newMessageState,
                from = from,
                to = to,
                referral = referral,
                loading = loading,
                localFiles = localFiles,
                onContentChange = onContentChange,
                onAttachFiles = onAttachFiles,
                onRemoveFile = onRemoveFile,
                onReasonToReject = onReasonToReject,
                onRejectMessage = onRejectMessage
            )
        }

        if(showPayReferral){
            PayReferral(
                referral = referral,
                from = from,
                to = to,
                clientWhoReferred = clientWhoReferred,
                amountUsd = amountUsd,
                onAmountChange = onAmountChange,
                onPayClick = onPayClick,
                onCancelPay = onCancelPay,
                onCopyClick = onCopyClick,
                selectedOption = selectedOption,
                onBankChange = onBankChange,
                onSendPay = onSendPay,
                loading = loading,
                localFiles = localFiles,
                onRemoveFile = onRemoveFile
            )
        }

    }
}

@Composable
fun StatusProcess(
    onStatusChange: (ReferralStatus) -> Unit
) {
    val statusOptions = listOf(
        ReferralStatus.PROCESSING,
        ReferralStatus.REJECTED,
        ReferralStatus.PAID
    )
    var colorStatus by rememberSaveable { mutableStateOf(ReferralStatus.PROCESSING) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(statusOptions) { status ->
                OutlinedButton(
                    onClick = {
                        onStatusChange(status)
                        colorStatus = status
                              },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if(colorStatus == status) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Text(
                        text = stringResource(status.nameSelect()),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (colorStatus == status)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
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
            onSaveMessage = {},
            onRejectMessage = {},
            clientWhoReferred = userClient,
            providerThatReceived = userProvider,
            amountUsd = "",
            onAmountChange = {},
            onPayClick = {},
            onCancelPay = {},
            onCopyClick = {},
            selectedOption = null,
            onBankChange = {},
            onSendPay = {},
            resetValues = {},
            showTopBar = true,
            onReasonToReject = {}
        )
    }
}