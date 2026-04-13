package com.example.feature.home.ui

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsDraggedAsState
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.BuildCircle
import androidx.compose.material.icons.filled.DoubleArrow
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import com.avilesrodriguez.domain.model.productsProvider.ProductProvider
import com.avilesrodriguez.domain.model.referral.ReferralMetrics
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.avatar.Avatar
import com.avilesrodriguez.presentation.composables.FullSearch
import com.avilesrodriguez.presentation.composables.RatingBar
import com.avilesrodriguez.presentation.composables.StatItem
import com.avilesrodriguez.presentation.ext.truncate
import com.avilesrodriguez.presentation.fakeData.productsFake
import com.avilesrodriguez.presentation.fakeData.productsRealTimeFake
import com.avilesrodriguez.presentation.fakeData.userClient
import com.avilesrodriguez.presentation.industries.icons
import com.avilesrodriguez.presentation.industries.label
import com.avilesrodriguez.presentation.industries.options
import com.avilesrodriguez.presentation.time.formatTimestamp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun HomeScreenClient(
    user: UserData,
    isLoading: Boolean,
    searchText: String,
    updateSearchText: (String) -> Unit,
    selectedIndustry: Int?,
    onIndustryChange: (Int) -> Unit,
    industryOptions: List<Int>,
    referralsMetrics: ReferralMetrics,
    onPaymentView: () -> Unit,
    onGraphMetricsView: () -> Unit,
    canReferUserClient: Boolean,
    onViewMoreProducts: () -> Unit,
    loadMoreProducts: () -> Unit,
    isPaginationActive: Boolean,
    showButton: Boolean,
    productsRealTime: List<ProductProvider>,
    products: List<ProductProvider>,
    onProductClick: (String) -> Unit,
    onViewRealProducts: () -> Unit
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

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // boton scroll to top
    val showScrollToTopButton by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 2
        }
    }

    // Detectamos si el usuario tiene el dedo en la pantalla moviendo la lista
    val isDragged by listState.interactionSource.collectIsDraggedAsState()

    LaunchedEffect(listState) {
        snapshotFlow {
            val atBottom = !listState.canScrollForward  //ya no hay más contenido abajo → estoy en el final
            isDragged && atBottom
        }
            .distinctUntilChanged()
            .collect { shouldLoad ->
                if(shouldLoad){
                    Log.d("HomeScreenClient", "Loading more products OJOO...")
                    loadMoreProducts()
                }
            }
    }

    Box(modifier = Modifier.fillMaxSize()){
        Column(modifier = Modifier.fillMaxSize()) {
            BalanceCard(
                balance = client?.moneyEarned.toString(),
                onPaymentView = onPaymentView
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                state = listState
            ) {
                //Statics
                item {
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
                item{
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
                            text = stringResource(R.string.search_products),
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

                if(!isPaginationActive){
                    if(productsRealTime.isNotEmpty()){
                        items(
                            productsRealTime,
                            key = { product -> product.id }
                        ){ product ->
                            ProductRow(product = product, onProductClick = onProductClick, isRealProduct = true)
                        }
                        if(showButton){
                            item{
                                TextButton(
                                    onClick = {onViewMoreProducts()}
                                ) {
                                    Text(text = stringResource(R.string.view_more_products))
                                    Icon(imageVector = Icons.Default.DoubleArrow, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    } else if(!isLoading){
                        item{
                            Box(
                                modifier = Modifier.fillMaxWidth().height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.no_have_products),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    if(isLoading && productsRealTime.isNotEmpty()){
                        item {
                            Box(Modifier
                                .fillMaxWidth()
                                .height(200.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                        .wrapContentWidth(Alignment.CenterHorizontally)
                                )
                            }
                        }
                    }
                }else{
                    if(products.isNotEmpty()){
                        item{
                            TextButton(
                                onClick = {onViewRealProducts()}
                            ) {
                                Text(text = stringResource(R.string.view_recent_products))
                                Icon(imageVector = Icons.Default.DoubleArrow, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        items(
                            products,
                            key = { product -> product.id }
                        ){
                            ProductRow(product = it, onProductClick = onProductClick, isRealProduct = false)
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    } else if(!isLoading){
                        item{
                            Box(
                                modifier = Modifier.fillMaxWidth().height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.no_have_products),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    if(isLoading && products.isEmpty()){
                        item{
                            Box(Modifier
                                .fillMaxWidth()
                                .height(200.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                        .wrapContentWidth(Alignment.CenterHorizontally)
                                )
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showScrollToTopButton,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        listState.animateScrollToItem(0)
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Go Up",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
private fun BalanceCard(balance: String, onPaymentView: () -> Unit) {
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
            .padding(16.dp)
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
private fun ProductRow(product: ProductProvider, onProductClick: (String) -> Unit, isRealProduct: Boolean){
    val createdAt = formatTimestamp(product.createdAt)
    val updatedAt = formatTimestamp(product.updatedAt)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable{onProductClick(product.id)},
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Absolute.Left
        ) {
            //Avatar(photoUri = product.providerPhotoUrl, size = 42.dp)
            //Spacer(modifier = Modifier.width(4.dp))
            Column(
                modifier = Modifier.weight(1f).padding(4.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = product.name.truncate(30),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = product.providerName.truncate(30),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RatingBar(rating = product.providerRating, isEditable = false)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = String.format(Locale.US, "%.1f", product.providerRating),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            Column(
                modifier = Modifier.padding(4.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "$${product.payByReferral.truncate(4)}",
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = stringResource(R.string.pay_by_referral),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = if(isRealProduct) updatedAt else createdAt,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}



@Composable
private fun ProviderCard(provider: UserData.Provider, onUserClick: (String) -> Unit) {
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
                    RatingBar(rating = provider.paymentRating, isEditable = false)
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
            isLoading = false,
            searchText = "",
            updateSearchText = {},
            selectedIndustry = null,
            onIndustryChange = {},
            industryOptions = IndustriesType.options(true),
            referralsMetrics = ReferralMetrics(
                totalReferrals = 20,
                pendingReferrals = 5,
                processingReferrals = 3,
                rejectedReferrals = 2,
                paidReferrals = 10
            ),
            onPaymentView = {},
            onGraphMetricsView = {},
            canReferUserClient = false,
            onViewMoreProducts = {},
            loadMoreProducts = {},
            isPaginationActive = false,
            showButton = true,
            productsRealTime = productsRealTimeFake,
            products = productsFake,
            onProductClick = {},
            onViewRealProducts = {}
        )
    }
}
