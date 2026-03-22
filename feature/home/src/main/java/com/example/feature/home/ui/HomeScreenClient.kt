package com.example.feature.home.ui

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.BuildCircle
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.avilesrodriguez.domain.model.industries.IndustriesType
import com.avilesrodriguez.domain.model.referral.ReferralMetrics
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.composables.FullSearch
import com.avilesrodriguez.presentation.composables.RatingBar
import com.avilesrodriguez.presentation.composables.StatItem
import com.avilesrodriguez.presentation.fakeData.userClient
import com.avilesrodriguez.presentation.fakeData.usersProviders
import com.avilesrodriguez.presentation.industries.icons
import com.avilesrodriguez.presentation.industries.label
import com.avilesrodriguez.presentation.industries.options
import java.util.Locale

@Composable
fun HomeScreenClient(
    user: UserData,
    users: List<UserData>,
    isLoading: Boolean,
    searchText: String,
    updateSearchText: (String) -> Unit,
    selectedIndustry: Int?,
    onIndustryChange: (Int) -> Unit,
    industryOptions: List<Int>,
    onUserClick: (String) -> Unit,
    referralsMetrics: ReferralMetrics,
    onPaymentView: () -> Unit,
    onGraphMetricsView: () -> Unit,
    canReferUserClient: Boolean,
) {
    val client = user as? UserData.Client

    // Bloqueador de scroll para el Pager
    val noPagerScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                // Consumimos cualquier scroll horizontal restante para que no llegue al Pager
                return if (source == NestedScrollSource.UserInput) {
                    Offset(x = available.x, y = 0f)
                } else {
                    Offset.Zero
                }
            }
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(1),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            BalanceCard(
                balance = client?.moneyEarned.toString(),
                onPaymentView = onPaymentView
            )
        }
        //Statics
        item(span = { GridItemSpan(maxLineSpan) }) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .nestedScroll(noPagerScrollConnection) // Aplicamos el bloqueador aquí
                    .clickable{onGraphMetricsView()},
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            ) {
                item {
                    StatItem(
                        modifier = Modifier.padding(start=4.dp),
                        title = stringResource(R.string.referrals),
                        value = "${referralsMetrics.totalReferrals}",
                        icon = Icons.Default.People,
                        color = MaterialTheme.colorScheme.surface
                    )
                }
                item {
                    StatItem(
                        modifier = Modifier,
                        title = stringResource(R.string.pending),
                        value = "${referralsMetrics.pendingReferrals}",
                        icon = Icons.Default.Alarm,
                        color = MaterialTheme.colorScheme.surface
                    )
                }
                item{
                    StatItem(
                        modifier = Modifier,
                        title = stringResource(R.string.processing),
                        value = "${referralsMetrics.processingReferrals}",
                        icon = Icons.Default.BuildCircle,
                        color = MaterialTheme.colorScheme.surface
                    )
                }
                item {
                    StatItem(
                        modifier = Modifier,
                        title = stringResource(R.string.rejected),
                        value = "${referralsMetrics.rejectedReferrals}",
                        icon = Icons.Default.Block,
                        color = MaterialTheme.colorScheme.surface
                    )
                }
            }
        }
        item(span = { GridItemSpan(maxLineSpan)}){
            Column(
                modifier = Modifier.padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if(!canReferUserClient){
                    StatItem(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        title = stringResource(R.string.refer_client_restriction),
                        value = stringResource(R.string.warning),
                        icon = Icons.Default.WarningAmber,
                        color = MaterialTheme.colorScheme.errorContainer
                    )
                }
                Text(
                    text = stringResource(R.string.search_companies),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp),
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    FullSearch(
                        options = industryOptions,
                        selectedOption = selectedIndustry?:R.string.all_industries,
                        onClick = onIndustryChange,
                        value = searchText,
                        onValueChange = updateSearchText,
                        placeholder = R.string.search,
                        trailingIcon = R.drawable.search,
                        modifier = Modifier
                    )
                }
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
            items(users) { provider ->
                ProviderCard(provider = provider as UserData.Provider, onUserClick = onUserClick)
            }
        }
    }
}

@Composable
fun BalanceCard(balance: String, onPaymentView: () -> Unit) {
    var isVisible by remember { mutableStateOf(true) }
    val icon =
        if (isVisible) painterResource(R.drawable.visibility)
        else painterResource(R.drawable.visibility_off)

    val displayedBalance =
        if (isVisible) "$$balance"
        else "$" + "*".repeat(balance.length)

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPaymentView() },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.total_profits),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ){
                Text(
                    text = displayedBalance,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(12.dp))
                IconButton(
                    onClick = { isVisible = !isVisible },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(painter = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}


@Composable
fun ProviderCard(provider: UserData.Provider, onUserClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable{onUserClick(provider.uid)},
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Absolute.Left
        ) {
            Icon(
                painter = painterResource(provider.industry.icons()),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(24.dp))
            Column(
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = provider.name ?: stringResource(R.string.unnamed),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = stringResource(provider.industry.label()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RatingBar(rating = provider.paymentRating)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = String.format(Locale.US, "%.1f", provider.paymentRating),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenClientPreview() {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme
    ) {
        HomeScreenClient(
            user = userClient,
            users = usersProviders,
            isLoading = false,
            searchText = "",
            updateSearchText = {},
            selectedIndustry = null,
            onIndustryChange = {},
            industryOptions = IndustriesType.options(true),
            onUserClick = {},
            referralsMetrics = ReferralMetrics(
                totalReferrals = 20,
                pendingReferrals = 5,
                processingReferrals = 3,
                rejectedReferrals = 2,
                paidReferrals = 10
            ),
            onPaymentView = {},
            onGraphMetricsView = {},
            canReferUserClient = false
        )
    }
}
