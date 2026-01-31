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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.avilesrodriguez.domain.model.industries.IndustriesType
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.composables.MenuDropdownBox
import com.avilesrodriguez.presentation.composables.RatingBar
import com.avilesrodriguez.presentation.composables.SearchFieldBasic
import com.avilesrodriguez.presentation.composables.StatItem
import com.avilesrodriguez.presentation.fakeData.userClient
import com.avilesrodriguez.presentation.fakeData.usersProviders
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
    onUserClick: (String) -> Unit
) {
    val client = user as? UserData.Client

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            BalanceCard(
                balance = client?.moneyEarned ?: "0.00",
                received = client?.moneyReceived ?: "0.00"
            )
        }
        //Statics
        item(span = { GridItemSpan(maxLineSpan) }) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatItem(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.referrals),
                    value = "${client?.totalReferrals ?: 0}",
                    icon = Icons.Default.People,
                    color = MaterialTheme.colorScheme.primaryContainer
                )
                StatItem(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.pending_payments),
                    value = "${client?.pendingPayments ?: 0}",
                    icon = Icons.Default.AccountBalanceWallet,
                    color = MaterialTheme.colorScheme.secondaryContainer
                )
            }
        }
        item(span = { GridItemSpan(maxLineSpan)}){
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = stringResource(R.string.search_companies),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SearchFieldBasic(
                        value = searchText,
                        onValueChange = updateSearchText,
                        placeholder = R.string.search,
                        trailingIcon = R.drawable.search,
                        modifier = Modifier.weight(1f)
                    )
                    MenuDropdownBox(
                        options = industryOptions,
                        selectedOption = selectedIndustry?:R.string.all_industries,
                        onClick = onIndustryChange,
                        modifier = Modifier.widthIn(max = 164.dp)
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
fun BalanceCard(balance: String, received: String) {
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
            Text(text = stringResource(R.string.total_profits),
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = "$$balance",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text=stringResource(R.string.charged), color = MaterialTheme.colorScheme.onPrimary)
            Text("$$received", fontWeight = FontWeight.Bold, color= MaterialTheme.colorScheme.onPrimary)
        }
    }
}


@Composable
fun ProviderCard(provider: UserData.Provider, onUserClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable{onUserClick(provider.uid)},
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            AsyncImage(
                model = provider.photoUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = provider.name ?: stringResource(R.string.unnamed),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = stringResource(provider.industry.label()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
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
    MaterialTheme {
        HomeScreenClient(
            user = userClient,
            users = usersProviders,
            isLoading = false,
            searchText = "",
            updateSearchText = {},
            selectedIndustry = null,
            onIndustryChange = {},
            industryOptions = IndustriesType.options(true),
            onUserClick = {}
        )
    }
}
