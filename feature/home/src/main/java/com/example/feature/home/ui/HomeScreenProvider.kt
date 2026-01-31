package com.example.feature.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.DoneOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.avilesrodriguez.domain.model.referral.ReferralMetrics
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.composables.SearchFieldBasic
import com.avilesrodriguez.presentation.composables.StatItem

@Composable
fun HomeScreenProvider(
    user: UserData,
    users: List<UserData>,
    isLoading: Boolean,
    searchText: String,
    updateSearchText: (String) -> Unit,
    referralMetricsProvider: ReferralMetrics,
    onUserClick: (String) -> Unit
){
    val provider = user as UserData.Provider

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            BalanceCardProvider(
                totalReferrals = referralMetricsProvider.totalReferrals.toString(),
                referralsConversion = provider.referralsConversion?:"0.00"
            )
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatItem(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.money_paid),
                    value = "$${provider.moneyPaid ?: "0.00"}",
                    icon = Icons.Default.AccountBalanceWallet,
                    color = MaterialTheme.colorScheme.primaryContainer
                )
                StatItem(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.money_to_pay),
                    value = "$${provider.moneyToPay ?: "0.00"}",
                    icon = Icons.Default.AccountBalanceWallet,
                    color = MaterialTheme.colorScheme.secondaryContainer
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatItem(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.total_payout),
                    value = "${provider.totalPayouts}",
                    icon = Icons.Outlined.DoneOutline,
                    color = MaterialTheme.colorScheme.primaryContainer
                )
                StatItem(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.payment_rating),
                    value = "${provider.paymentRating}",
                    icon = Icons.Default.Star,
                    color = MaterialTheme.colorScheme.secondaryContainer
                )
            }
        }
        item(span = { GridItemSpan(maxLineSpan)}){
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = stringResource(R.string.search_referrers),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                SearchFieldBasic(
                    value = searchText,
                    onValueChange = updateSearchText,
                    placeholder = R.string.search,
                    trailingIcon = R.drawable.search
                )
            }
        }
        if (isLoading) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(Modifier
                    .fillMaxWidth()
                    .height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        } else {
            items(users){ client ->
                ClientCard(client = client as UserData.Client, onClientClick = onUserClick)
            }
        }
    }
}

@Composable
private fun BalanceCardProvider(totalReferrals: String, referralsConversion: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.total_referrals),
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = totalReferrals,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text=stringResource(R.string.referrals_conversion),
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = referralsConversion,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color= MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun ClientCard(client:UserData.Client, onClientClick: (String) -> Unit){

}