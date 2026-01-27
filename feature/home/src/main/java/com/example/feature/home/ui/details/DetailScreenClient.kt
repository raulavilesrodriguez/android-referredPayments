package com.example.feature.home.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.avilesrodriguez.domain.model.referral.Referral
import com.avilesrodriguez.domain.model.referral.ReferralMetrics
import com.avilesrodriguez.domain.model.referral.ReferralStatus
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.avatar.Avatar
import com.avilesrodriguez.presentation.composables.ToolBarDetails

@Composable
fun DetailScreenClient(
    client: UserData.Client,
    referrals: List<Referral>,
    referralsMetrics: ReferralMetrics,
    selectedStatus: ReferralStatus?,
    filterReferralsByStatus: (Int) -> Unit,
    statusOptions: List<Int>,
    onBackClick: () -> Unit,
    onReferClick: (String) -> Unit
){
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            ToolBarDetails(
                title = R.string.information_client,
                backClick = { onBackClick() },
                modifier = Modifier.background(
                    MaterialTheme.colorScheme.secondary
                )
            )
        },
        content = { innerPadding ->
            ProfileClient(
                client = client,
                referrals = referrals,
                onReferClick = onReferClick,
                referralsMetrics = referralsMetrics,
                selectedStatus = selectedStatus,
                filterReferralsByStatus = filterReferralsByStatus,
                statusOptions = statusOptions,
                modifier = Modifier.padding(innerPadding)
            )
        }
    )
}

@Composable
private fun ProfileClient(
    client: UserData.Client,
    referrals: List<Referral>,
    onReferClick: (String) -> Unit,
    referralsMetrics: ReferralMetrics,
    selectedStatus: ReferralStatus?,
    filterReferralsByStatus: (Int) -> Unit,
    statusOptions: List<Int>,
    modifier: Modifier = Modifier
){
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ){
        Box(modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(
                MaterialTheme.colorScheme.secondary,
            )
        ){
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = client.name ?: stringResource(R.string.unnamed),
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Avatar(
                    photoUri = client.photoUrl,
                    size = 80.dp
                )
            }
        }
        Column(modifier = Modifier.padding(16.dp)){
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ){

            }
        }
    }
}