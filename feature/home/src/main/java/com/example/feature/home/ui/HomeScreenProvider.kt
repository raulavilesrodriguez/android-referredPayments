package com.example.feature.home.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.BuildCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Payment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.avilesrodriguez.domain.model.banks.AccountType
import com.avilesrodriguez.domain.model.referral.ReferralMetrics
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.domain.model.user.UserType
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.avatar.Avatar
import com.avilesrodriguez.presentation.composables.SearchFieldBasic
import com.avilesrodriguez.presentation.composables.StatItem
import com.avilesrodriguez.presentation.details.DetailMetricItem
import com.avilesrodriguez.presentation.fakeData.userProvider
import com.example.feature.home.models.UserAndReferralMetrics

@Composable
fun HomeScreenProvider(
    user: UserData,
    isLoading: Boolean,
    searchText: String,
    updateSearchText: (String) -> Unit,
    referralsMetrics: ReferralMetrics,
    onUserClick: (String) -> Unit,
    usersAndMetrics: List<UserAndReferralMetrics>,
    referralsConversion: String
){
    val provider = user as UserData.Provider

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

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            BalanceCardProvider(
                paidReferrals = referralsMetrics.paidReferrals.toString(),
                iconPaidReferrals = Icons.Default.People,
                moneyPaid = provider.moneyPaid.toString(),
                iconMoneyPaid = Icons.Default.AccountBalanceWallet
            )
        }
        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .nestedScroll(noPagerScrollConnection),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item{
                    StatItem(
                        modifier = Modifier,
                        title = stringResource(R.string.total_payout),
                        value = "${provider.totalPayouts}",
                        icon = Icons.Outlined.Payment,
                        color = MaterialTheme.colorScheme.primaryContainer
                    )
                }
                item{
                    StatItem(
                        modifier = Modifier,
                        title = stringResource(R.string.payment_rating),
                        value = "${provider.paymentRating}",
                        icon = Icons.Default.Star,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    )
                }
                item{
                    StatItem(
                        modifier = Modifier,
                        title = stringResource(R.string.referrals_conversion),
                        value = referralsConversion,
                        icon = Icons.Default.AutoAwesome,
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    )
                }
                item{
                    StatItem(
                        modifier = Modifier,
                        title = stringResource(R.string.total_referrals),
                        value = "${referralsMetrics.totalReferrals}",
                        icon = Icons.Default.People,
                        color = MaterialTheme.colorScheme.inversePrimary
                    )
                }
                item{
                    StatItem(
                        modifier = Modifier,
                        title = stringResource(R.string.pending),
                        value = "${referralsMetrics.pendingReferrals}",
                        icon = Icons.Default.Alarm,
                        color = MaterialTheme.colorScheme.surfaceDim
                    )
                }
                item {
                    StatItem(
                        modifier = Modifier,
                        title = stringResource(R.string.processing),
                        value = "${referralsMetrics.processingReferrals}",
                        icon = Icons.Default.BuildCircle,
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    )
                }
                item {
                    StatItem(
                        modifier = Modifier,
                        title = stringResource(R.string.rejected),
                        value = "${referralsMetrics.rejectedReferrals}",
                        icon = Icons.Default.Block,
                        color = MaterialTheme.colorScheme.errorContainer
                    )
                }
            }
        }
        item{
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
            item {
                Box(Modifier
                    .fillMaxWidth()
                    .height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        } else {
            items(usersAndMetrics){ client ->
                ClientRow(clientMetrics = client, onClientClick = onUserClick)
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun BalanceCardProvider(paidReferrals: String, iconPaidReferrals: ImageVector, moneyPaid: String, iconMoneyPaid: ImageVector) {
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
            Row(
                modifier = Modifier.fillMaxWidth().padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ){
                Icon(imageVector = iconPaidReferrals, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ){
                    Text(
                        text = stringResource(R.string.paid_referreds),
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1
                    )
                    Text(
                        text = paidReferrals,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineLarge,
                        maxLines = 1,
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ){
                Icon(imageVector = iconMoneyPaid, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ){
                    Text(
                        text = stringResource(R.string.money_paid),
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1
                    )
                    Text(
                        text = "$${moneyPaid}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineMedium,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun ClientRow(clientMetrics: UserAndReferralMetrics, onClientClick: (String) -> Unit){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable{onClientClick(clientMetrics.user.uid)}
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ){
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding( 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ){
                Avatar(
                    photoUri = clientMetrics.user.photoUrl,
                    size = 40.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = clientMetrics.user.name ?:"",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                DetailMetricItem(
                    icon = Icons.Default.People,
                    value = clientMetrics.referralMetrics.totalReferrals.toString(),
                    label = stringResource(R.string.total_referrals)
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding( 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ){
                DetailMetricItem(
                    icon = Icons.Default.Alarm,
                    value = clientMetrics.referralMetrics.pendingReferrals.toString(),
                    label = stringResource(R.string.pending)
                )
                VerticalDivider(modifier = Modifier.height(40.dp))
                DetailMetricItem(
                    icon = Icons.Default.CheckCircle,
                    value = clientMetrics.referralMetrics.paidReferrals.toString(),
                    label = stringResource(R.string.paid)
                )
                VerticalDivider(modifier = Modifier.height(40.dp))
                DetailMetricItem(
                    icon = Icons.Default.BuildCircle,
                    value = clientMetrics.referralMetrics.processingReferrals.toString(),
                    label = stringResource(R.string.processing)
                )
                VerticalDivider(modifier = Modifier.height(40.dp))
                DetailMetricItem(
                    icon = Icons.Default.Block,
                    value = clientMetrics.referralMetrics.rejectedReferrals.toString(),
                    label = stringResource(R.string.rejected)
                )
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun HomeScreenProviderPreview(){
    MaterialTheme {
        HomeScreenProvider(
            user = userProvider,
            isLoading = false,
            searchText = "",
            updateSearchText = {},
            referralsMetrics = ReferralMetrics(
                totalReferrals = 20,
                pendingReferrals = 5,
                processingReferrals = 3,
                rejectedReferrals = 2,
                paidReferrals = 10
            ),
            onUserClick = {},
            usersAndMetrics = generateFakeUserAndReferralMetrics(),
            referralsConversion = "10.00"
        )
    }
}

private fun generateFakeUserAndReferralMetrics(): List<UserAndReferralMetrics> = listOf(
    UserAndReferralMetrics(
        user = UserData.Client(
            uid = "1",
            isActive = true,
            name = "Britny Muelas",
            email = "britny@gmail.com",
            photoUrl = "https://i.pravatar.cc/150?u=12",
            type = UserType.CLIENT,
            nameLowercase = "britny muelas",
            identityCard = "1098765432",
            countNumberPay = "12223440455",
            bankName = "Produbanco",
            accountType = AccountType.SAVINGS,
            moneyEarned = 1000.0
        ),
        referralMetrics = ReferralMetrics(
            totalReferrals = 5,
            pendingReferrals = 1,
            processingReferrals = 1,
            rejectedReferrals = 2,
            paidReferrals = 1
        )
    ),
    UserAndReferralMetrics(
        user = UserData.Client(
            uid = "1",
            isActive = true,
            name = "Liz Aura",
            email = "lizaura@gmail.com",
            photoUrl = "https://i.pravatar.cc/150?u=20",
            type = UserType.CLIENT,
            nameLowercase = "liz aura",
            identityCard = "1249765432",
            countNumberPay = "12223440466",
            bankName = "Pichincha",
            accountType = AccountType.SAVINGS,
            moneyEarned = 1500.0
        ),
        referralMetrics = ReferralMetrics(
            totalReferrals = 5,
            pendingReferrals = 2,
            processingReferrals = 0,
            rejectedReferrals = 1,
            paidReferrals = 2
        )
    ),
    UserAndReferralMetrics(
        user = UserData.Client(
            uid = "1",
            isActive = true,
            name = "Silvana Murillo",
            email = "silvi@gmail.com",
            photoUrl = "https://i.pravatar.cc/150?u=22",
            type = UserType.CLIENT,
            nameLowercase = "silvana murillo",
            identityCard = "0015765439",
            countNumberPay = "12223440498",
            bankName = "Banco de Guayaquil",
            accountType = AccountType.CHECKING,
            moneyEarned = 4000.0
        ),
        referralMetrics = ReferralMetrics(
            totalReferrals = 5,
            pendingReferrals = 1,
            processingReferrals = 1,
            rejectedReferrals = 0,
            paidReferrals = 3
        )
    ),
    UserAndReferralMetrics(
        user = UserData.Client(
            uid = "1",
            isActive = true,
            name = "Julio Cuvero",
            email = "julio.cuvero@gmail.com",
            photoUrl = "https://i.pravatar.cc/150?u=23",
            type = UserType.CLIENT,
            nameLowercase = "julio cuvero",
            identityCard = "1495765430",
            countNumberPay = "10273440456",
            bankName = "Austro",
            accountType = AccountType.CHECKING,
            moneyEarned = 2000.0
        ),
        referralMetrics = ReferralMetrics(
            totalReferrals = 5,
            pendingReferrals = 2,
            processingReferrals = 0,
            rejectedReferrals = 2,
            paidReferrals = 1
        )
    ),
)