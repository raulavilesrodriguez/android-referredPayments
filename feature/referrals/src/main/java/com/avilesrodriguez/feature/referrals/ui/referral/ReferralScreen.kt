package com.avilesrodriguez.feature.referrals.ui.referral

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.avilesrodriguez.domain.model.referral.Referral
import com.avilesrodriguez.domain.model.referral.ReferralStatus
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.composables.ProfileToolBar
import com.avilesrodriguez.presentation.ext.toColor
import com.avilesrodriguez.presentation.ext.toDisplayName
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
        providerThatReceived = providerThatReceived
    )
}

@Composable
fun ReferralScreenContent(
    onBackClick: () -> Unit,
    referral: Referral,
    user: UserData?,
    clientWhoReferred: UserData?,
    providerThatReceived: UserData?
){
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            ProfileToolBar(
                iconBack = R.drawable.arrow_back,
                title = R.string.referred,
                backClick = { onBackClick() }
            )
        },
        content = { paddingValues ->
            ProfileReferral(
                referral = referral,
                user = user,
                clientWhoReferred = clientWhoReferred,
                providerThatReceived = providerThatReceived,
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
        ReferralStatus(status = referral.status, icon = R.drawable.circle)
        ItemProfile(R.drawable.name, title = R.string.name_referred, data = referral.name)
        ItemProfile(R.drawable.mail, title = R.string.email_referred, data = referral.email)
        ItemProfile(R.drawable.phone, title = R.string.phone_number_referred, data = referral.numberPhone)
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    }
}

@Composable
fun ReferralStatus(status: ReferralStatus, icon: Int){
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Absolute.Left,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = status.toColor(),
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = stringResource(status.toDisplayName()),
            fontWeight = FontWeight.Bold,
            color = status.toColor(),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}