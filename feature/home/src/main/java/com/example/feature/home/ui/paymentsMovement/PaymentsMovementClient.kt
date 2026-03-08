package com.example.feature.home.ui.paymentsMovement

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.avilesrodriguez.domain.model.referral.ReferralStatus
import com.avilesrodriguez.domain.model.referral.ReferralWithNames
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.composables.ToolBarWithIcon
import com.avilesrodriguez.presentation.ext.truncate
import com.avilesrodriguez.presentation.fakeData.referral
import com.avilesrodriguez.presentation.fakeData.userProvider
import com.avilesrodriguez.presentation.time.formatTimestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsScreenClient(
    client: UserData.Client,
    onBackClick: () -> Unit,
    referrals: List<ReferralWithNames>,
    isLoading: Boolean,
    showTopBar: Boolean = true // Añadido para soporte adaptativo
){
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            if (showTopBar) {
                ToolBarWithIcon(
                    iconBack = R.drawable.arrow_back,
                    title = stringResource(R.string.payments),
                    backClick = { onBackClick() }
                )
            }
        },
        content = { innerPadding ->
            if(isLoading){
                Box(
                    modifier = Modifier
                        .fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            } else {
                PaymentsClient(
                    client = client,
                    referrals = referrals,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    )
}

@Composable
private fun PaymentsClient(
    client: UserData.Client,
    referrals: List<ReferralWithNames>,
    modifier: Modifier = Modifier
){
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ReferralsList(client = client, referrals = referrals)
    }
}

@Composable
private fun ReferralsList(
    client: UserData.Client,
    referrals: List<ReferralWithNames>
){
    val referralsPaid = referrals.filter { it.referral.status == ReferralStatus.PAID }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item{
            client.name?.let {
                Text(
                    text = it,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        items(referralsPaid){ item ->
            ReferralPaidItem(referral = item)
        }
    }
}

@Composable
private fun ReferralPaidItem(
    referral: ReferralWithNames
){
    val paidAt = formatTimestamp(referral.referral.paidAt)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ){
            Column {
                Text(
                    text = referral.referral.name.truncate(30),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.payments_for, referral.otherPartyName),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = "USD ${referral.referral.amountPaid}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = paidAt,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReferralPaidItemPreview(){
    MaterialTheme {
        ReferralPaidItem(
            referral = ReferralWithNames(
                referral = referral,
                otherPartyName = userProvider.name?:""
            )
        )
    }
}
