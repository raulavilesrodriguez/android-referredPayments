package com.example.feature.home.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness2
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.avilesrodriguez.domain.model.referral.Referral
import com.avilesrodriguez.domain.model.referral.ReferralMetrics
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.avatar.Avatar
import com.avilesrodriguez.presentation.composables.MenuDropdownBox
import com.avilesrodriguez.presentation.composables.ToolBarDetails
import com.avilesrodriguez.presentation.details.DetailMetricItem
import com.avilesrodriguez.presentation.fakeData.generateFakeReferrals
import com.avilesrodriguez.presentation.fakeData.userClient

@Composable
fun DetailScreenClient(
    client: UserData.Client,
    referrals: List<Referral>,
    referralsMetrics: ReferralMetrics,
    selectedStatus: Int?,
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
    selectedStatus: Int?,
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
                    style = MaterialTheme.typography.headlineMedium,
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
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ){
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ){
                DetailMetricItem(
                    icon = Icons.Default.People,
                    value = referralsMetrics.totalReferrals.toString(),
                    label = stringResource(R.string.all_referrals),
                    tint = Color(0xFFFFC107)
                )
                VerticalDivider(modifier = Modifier.height(40.dp))
                DetailMetricItem(
                    icon = Icons.Default.People,
                    value = referralsMetrics.pendingReferrals.toString(),
                    label = stringResource(R.string.pending),
                    tint = Color(0xFFC40C0C)
                )
                VerticalDivider(modifier = Modifier.height(40.dp))
                if(client.isActive){
                    DetailMetricItem(
                        icon = Icons.Default.BrightnessHigh,
                        value = "",
                        label = stringResource(R.string.active_user),
                        tint = Color(0xFFFFC107)
                    )
                }else{
                    DetailMetricItem(
                        icon = Icons.Default.Brightness2,
                        value = "",
                        label = stringResource(R.string.inactive_user),
                        tint = Color(0xFFC40C0C)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
            Text(
                text = stringResource(R.string.referreds_by, client.name?:""),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(
                    text = stringResource(R.string.filter_by_status),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.weight(1f))
                MenuDropdownBox(
                    options = statusOptions,
                    selectedOption = selectedStatus?:R.string.all_status,
                    onClick = filterReferralsByStatus,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetailScreenClientPreview(){
    MaterialTheme {
        DetailScreenClient(
            client = userClient,
            referrals = generateFakeReferrals(),
            referralsMetrics = ReferralMetrics(
                totalReferrals = 10,
                pendingReferrals = 3,
                processingReferrals = 2,
                rejectedReferrals = 1,
                paidReferrals = 4
            ),
            selectedStatus = null,
            filterReferralsByStatus = {},
            statusOptions = emptyList(),
            onBackClick = {},
            onReferClick = {}
        )
    }
}