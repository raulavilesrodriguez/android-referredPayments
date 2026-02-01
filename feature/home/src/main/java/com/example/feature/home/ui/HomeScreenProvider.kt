package com.example.feature.home.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.avilesrodriguez.domain.model.referral.ReferralMetrics
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.avatar.Avatar
import com.avilesrodriguez.presentation.composables.SearchFieldBasic
import com.avilesrodriguez.presentation.composables.StatItem
import com.example.feature.home.models.UserAndReferralMetrics

@Composable
fun HomeScreenProvider(
    user: UserData,
    isLoading: Boolean,
    searchText: String,
    updateSearchText: (String) -> Unit,
    referralMetricsProvider: ReferralMetrics,
    onUserClick: (String) -> Unit,
    usersAndMetrics: List<UserAndReferralMetrics>
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
            items(usersAndMetrics){ client ->
                ClientCard(clientMetrics = client, onClientClick = onUserClick)
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
fun ClientCard(clientMetrics: UserAndReferralMetrics, onClientClick: (String) -> Unit){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable{onClientClick(clientMetrics.user.uid)},
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ){
                Avatar(
                    photoUri = clientMetrics.user.photoUrl,
                    size = 40.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = clientMetrics.user.name ?:""
                )
            }
            InfoMetric(
                icon= R.drawable.people,
                title = stringResource(R.string.total_referrals),
                value = clientMetrics.referralMetrics.totalReferrals.toString(),
                color = MaterialTheme.colorScheme.primary
            )
            InfoMetric(
                icon = R.drawable.sentiment_pending,
                title = stringResource(R.string.pending),
                value = clientMetrics.referralMetrics.pendingReferrals.toString(),
                color = Color(0xFFF5AD18)
            )
            InfoMetric(
                icon = R.drawable.sentiment_processing,
                title = stringResource(R.string.processing),
                value = clientMetrics.referralMetrics.processingReferrals.toString(),
                color = Color(0xFF6594B1)
            )
            InfoMetric(
                icon = R.drawable.sentiment_rejected,
                title = stringResource(R.string.rejected),
                value = clientMetrics.referralMetrics.rejectedReferrals.toString(),
                color = Color(0XFFDC0E0E)
            )
            InfoMetric(
                icon = R.drawable.sentiment_paid,
                title = stringResource(R.string.paid),
                value = clientMetrics.referralMetrics.paidReferrals.toString(),
                color = Color(0xFF08CB00)
            )
        }
    }
}

@Composable
private fun InfoMetric(@DrawableRes icon: Int, title: String, value: String, color: Color){
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)){
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            tint = color
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column{
            Text(text = title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = color)
        }
    }
}