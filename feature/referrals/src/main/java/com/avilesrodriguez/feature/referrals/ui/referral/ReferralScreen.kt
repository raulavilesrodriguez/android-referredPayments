package com.avilesrodriguez.feature.referrals.ui.referral

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.avilesrodriguez.presentation.composables.ProfileToolBar
import com.avilesrodriguez.presentation.ext.basicButton
import com.avilesrodriguez.presentation.ext.toColor
import com.avilesrodriguez.presentation.ext.toDisplayIcon
import com.avilesrodriguez.presentation.ext.toDisplayName
import com.avilesrodriguez.presentation.ext.truncate
import com.avilesrodriguez.presentation.fakeData.referral
import com.avilesrodriguez.presentation.fakeData.userClient
import com.avilesrodriguez.presentation.fakeData.userProvider
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
    val clientWhoReferred = viewModel.clientWhoReferred
    val providerThatReceived = viewModel.providerThatReceived

    ReferralScreenContent(
        onBackClick = onBackClick,
        referral = referral,
        user = user,
        clientWhoReferred = clientWhoReferred,
        providerThatReceived = providerThatReceived,
        onPayClick = {}
    )
}

@Composable
fun ReferralScreenContent(
    onBackClick: () -> Unit,
    referral: Referral,
    user: UserData?,
    clientWhoReferred: UserData?,
    providerThatReceived: UserData?,
    onPayClick: () -> Unit
){
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            ProfileToolBar(
                iconBack = R.drawable.arrow_back,
                title = R.string.information_referral,
                backClick = { onBackClick() }
            )
        },
        content = { paddingValues ->
            ProfileReferral(
                referral = referral,
                user = user,
                clientWhoReferred = clientWhoReferred,
                providerThatReceived = providerThatReceived,
                onPayClick = onPayClick,
                modifier = Modifier.padding(paddingValues)
            )
        }
    )
}

@Composable
fun ProfileReferral(
    referral: Referral,
    user: UserData?,
    clientWhoReferred: UserData?,
    providerThatReceived: UserData?,
    onPayClick: () -> Unit,
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
        ItemProfile(R.drawable.name, title = R.string.name_referred, data = referral.name)
        ItemProfile(R.drawable.mail, title = R.string.email_referred, data = referral.email)
        ItemProfile(R.drawable.phone, title = R.string.phone_number_referred, data = referral.numberPhone)
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
        when(user){
            is UserData.Client -> {
                val nameProvider = providerThatReceived?.name?.truncate(30)?:""
                ItemProfile(R.drawable.step, title = R.string.referring, data = nameProvider)
            }
            is UserData.Provider -> {
                val nameClient = clientWhoReferred?.name?.truncate(30)?:""
                ItemProfile(R.drawable.step, title = R.string.referral, data = nameClient)
                BasicButton(
                    R.string.paid_to,
                    Modifier.basicButton(),
                    referral.name.truncate(25)
                ) { onPayClick() }
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
                .padding(end = 8.dp)
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
            user = userClient,
            clientWhoReferred = userClient,
            providerThatReceived = userProvider,
            onPayClick = {}
        )
    }
}