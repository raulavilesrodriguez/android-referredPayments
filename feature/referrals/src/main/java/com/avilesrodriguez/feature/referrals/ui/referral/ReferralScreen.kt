package com.avilesrodriguez.feature.referrals.ui.referral

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.avilesrodriguez.domain.model.referral.Referral
import com.avilesrodriguez.domain.model.referral.ReferralStatus
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.composables.BasicButton
import com.avilesrodriguez.presentation.composables.ToolBarWithIcon
import com.avilesrodriguez.presentation.ext.basicButton
import com.avilesrodriguez.presentation.ext.toColor
import com.avilesrodriguez.presentation.ext.toDisplayIcon
import com.avilesrodriguez.presentation.ext.toDisplayName
import com.avilesrodriguez.presentation.ext.truncate
import com.avilesrodriguez.presentation.fakeData.referral
import com.avilesrodriguez.presentation.fakeData.userClient
import com.avilesrodriguez.presentation.fakeData.userProvider
import com.avilesrodriguez.presentation.profile.ItemEditProfile
import com.avilesrodriguez.presentation.profile.ItemProfile

@Composable
fun ReferralScreen(
    referralId: String?,
    onBackClick: () -> Unit,
    openScreen: (String) -> Unit,
    viewModel: ReferralViewModel = hiltViewModel()
){
    LaunchedEffect(Unit) {
        viewModel.loadReferralInformation(referralId.orEmpty())
    }
    val referral by viewModel.referralState.collectAsState()
    val user by viewModel.userDataStore.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val clientWhoReferred = viewModel.clientWhoReferred
    val providerThatReceived = viewModel.providerThatReceived

    val subjectAccept = stringResource(R.string.subject_referral_accepted)
    val contentAccept = stringResource(R.string.content_referral_accepted)


    ReferralScreenContent(
        onBackClick = onBackClick,
        referral = referral,
        user = user,
        isLoading = isLoading,
        clientWhoReferred = clientWhoReferred,
        providerThatReceived = providerThatReceived,
        onNameClick = { viewModel.onNameReferral(openScreen)},
        onEmailClick = { viewModel.onEmailReferral(openScreen)},
        onPhoneClick = { viewModel.onPhoneReferral(openScreen)},
        onAcceptReferral = { viewModel.onAcceptReferral(subjectAccept, contentAccept, openScreen)},
        onProcessClick = { viewModel.onProcessReferral(openScreen)}
    )
}

@Composable
fun ReferralScreenContent(
    onBackClick: () -> Unit,
    referral: Referral,
    user: UserData?,
    isLoading: Boolean,
    clientWhoReferred: UserData?,
    providerThatReceived: UserData?,
    onNameClick: () -> Unit,
    onEmailClick: () -> Unit,
    onPhoneClick: () -> Unit,
    onAcceptReferral: () -> Unit,
    onProcessClick: () -> Unit
){
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            ToolBarWithIcon(
                iconBack = R.drawable.arrow_back,
                title = R.string.information_referral,
                backClick = { onBackClick() }
            )
        },
        content = { paddingValues ->
            if(!isLoading){
                ProfileReferral(
                    referral = referral,
                    user = user,
                    clientWhoReferred = clientWhoReferred,
                    providerThatReceived = providerThatReceived,
                    onNameClick = onNameClick,
                    onEmailClick = onEmailClick,
                    onPhoneClick = onPhoneClick,
                    onAcceptReferral = onAcceptReferral,
                    onProcessClick = onProcessClick,
                    modifier = Modifier.padding(paddingValues)
                )
            } else{
                Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    )
}

@Composable
fun ProfileReferral(
    referral: Referral,
    user: UserData?,
    clientWhoReferred: UserData?,
    providerThatReceived: UserData?,
    onNameClick: () -> Unit,
    onEmailClick: () -> Unit,
    onPhoneClick: () -> Unit,
    onAcceptReferral: () -> Unit,
    onProcessClick: () -> Unit,
    modifier: Modifier = Modifier
){
    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ReferralStatus(status = referral.status)

        when(user){
            is UserData.Client -> {
                ItemEditProfile(R.drawable.name, title = R.string.name_referred, data = referral.name, iconEdit = R.drawable.edit_gray){onNameClick()}
                ItemEditProfile(R.drawable.mail, title = R.string.email_referred, data = referral.email, iconEdit = R.drawable.edit_gray){onEmailClick()}
                ItemEditProfile(R.drawable.phone, title = R.string.phone_number_referred, data = referral.numberPhone, iconEdit = R.drawable.edit_gray){onPhoneClick()}
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
                val nameProvider = providerThatReceived?.name?.truncate(30)?:""
                ItemProfile(R.drawable.step, title = R.string.referring, data = nameProvider)
                BasicButton(
                    text = R.string.review_payment_process,
                    modifier = Modifier.basicButton()
                ) { onProcessClick() }
            }
            is UserData.Provider -> {
                val isPending = referral.status == ReferralStatus.PENDING
                ItemProfile(icon = R.drawable.name, title = R.string.name_referred, data = referral.name)
                ItemProfile(
                    icon = R.drawable.mail,
                    title = R.string.email_referred,
                    data = if(isPending) "••••@••••.com" else referral.email
                )
                ItemProfile(
                    icon = R.drawable.phone,
                    title = R.string.phone_number_referred,
                    data = if(isPending) "09••••••••" else referral.numberPhone
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
                val nameClient = clientWhoReferred?.name?.truncate(30)?:""
                ItemProfile(R.drawable.step, title = R.string.referral, data = nameClient)
                if(isPending){
                    BasicButton(
                        text = R.string.accept_and_view_contact,
                        modifier = Modifier.basicButton()
                    ) { onAcceptReferral() }
                } else {
                    BasicButton(
                        text = R.string.process_referral,
                        modifier = Modifier.basicButton(),
                    ) { onProcessClick() }
                }
            }
            else -> {}
        }
    }
}

@Composable
fun ReferralStatus(status: ReferralStatus){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Absolute.Left,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(status.toDisplayIcon()),
            contentDescription = null,
            modifier = Modifier
                .padding(end = 8.dp),
            tint = status.toColor()
        )
        Box(
            modifier = Modifier
                .background(status.toColor(), shape = RoundedCornerShape(50))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(status.toDisplayName()),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileReferralPreview(){
    MaterialTheme {
        ReferralScreenContent(
            onBackClick = {},
            referral = referral,
            user = userProvider,
            isLoading = false,
            clientWhoReferred = userClient,
            providerThatReceived = userProvider,
            onNameClick = {},
            onEmailClick = {},
            onPhoneClick = {},
            onAcceptReferral = {},
            onProcessClick = {}
        )
    }
}