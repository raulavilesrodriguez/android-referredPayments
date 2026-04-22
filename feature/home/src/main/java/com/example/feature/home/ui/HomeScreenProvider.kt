package com.example.feature.home.ui

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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.BuildCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.DoubleArrow
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.outlined.Payment
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.avilesrodriguez.domain.model.productsProvider.ProductProvider
import com.avilesrodriguez.domain.model.referral.ReferralMetrics
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.avatar.Avatar
import com.avilesrodriguez.presentation.composables.SearchFieldBasic
import com.avilesrodriguez.presentation.composables.StatItem
import com.avilesrodriguez.presentation.details.DetailMetricItem
import com.avilesrodriguez.presentation.ext.fieldModifier
import com.avilesrodriguez.presentation.ext.truncate
import com.avilesrodriguez.presentation.fakeData.productsFake
import com.avilesrodriguez.presentation.fakeData.productsRealTimeFake
import com.avilesrodriguez.presentation.fakeData.userProvider
import com.avilesrodriguez.presentation.profile.ItemEdit
import com.avilesrodriguez.presentation.time.formatTimestamp
import com.example.feature.home.models.UserAndReferralMetrics
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@Composable
fun HomeScreenProvider(
    user: UserData,
    isLoading: Boolean,
    searchText: String,
    updateSearchText: (String) -> Unit,
    referralsMetrics: ReferralMetrics,
    referralsConversion: String,
    onPaymentView: () -> Unit,
    onGraphMetricsView: () -> Unit,
    isSaturated: Boolean,
    onViewMoreProducts: () -> Unit,
    loadMoreProducts: () -> Unit,
    isPaginationActive: Boolean,
    showButton: Boolean,
    productsRealTime: List<ProductProvider>,
    products: List<ProductProvider>,
    onAddProductClick: () -> Unit,
    onProductClick: (String) -> Unit,
    onViewRealProducts: () -> Unit,
    onPayClickByProvider: () -> Unit
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

    val focusManager = LocalFocusManager.current

    LaunchedEffect(isDragged) {
        if (isDragged) {
            focusManager.clearFocus()
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow {
            val atBottom = !listState.canScrollForward  //ya no hay más contenido abajo → estoy en el final
            isDragged && atBottom
        }
            .distinctUntilChanged()
            .collect { shouldLoad ->
                if(shouldLoad){
                    loadMoreProducts()
                }
            }
    }

    Box(modifier = Modifier.fillMaxSize()){
        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                state = listState
            ) {
                item {
                    BalanceCardProvider(
                        paidReferrals = referralsMetrics.paidReferrals.toString(),
                        iconPaidReferrals = Icons.Default.People,
                        moneyPaid = provider.moneyPaid.toString(),
                        iconMoneyPaid = Icons.Default.AccountBalanceWallet,
                        onPaymentView = onPaymentView
                    )
                }
                item {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .nestedScroll(noPagerScrollConnection)
                            .clickable{onGraphMetricsView()},
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val valueLimit = provider.referralLimit - provider.totalPayouts
                        item{
                            StatItem(
                                modifier = Modifier.padding(start=4.dp),
                                title = stringResource(R.string.referral_limit),
                                value = if(valueLimit > 0) valueLimit.toString() else "0",
                                icon = Icons.Default.Diamond,
                                color = MaterialTheme.colorScheme.surface
                            )
                        }
                        item{
                            StatItem(
                                modifier = Modifier.padding(start=4.dp),
                                title = stringResource(R.string.total_payout),
                                value = "${provider.totalPayouts}",
                                icon = Icons.Outlined.Payment,
                                color = MaterialTheme.colorScheme.surface
                            )
                        }
                        item{
                            StatItem(
                                modifier = Modifier,
                                title = stringResource(R.string.payment_rating),
                                value = "${provider.paymentRating}",
                                icon = Icons.Default.Star,
                                color = MaterialTheme.colorScheme.surface
                            )
                        }
                        item{
                            StatItem(
                                modifier = Modifier,
                                title = stringResource(R.string.referrals_conversion),
                                value = referralsConversion,
                                icon = Icons.Default.AutoAwesome,
                                color = MaterialTheme.colorScheme.surface
                            )
                        }
                        item{
                            StatItem(
                                modifier = Modifier,
                                title = stringResource(R.string.total_referrals),
                                value = "${referralsMetrics.totalReferrals}",
                                icon = Icons.Default.People,
                                color = MaterialTheme.colorScheme.surface
                            )
                        }
                        item{
                            StatItem(
                                modifier = Modifier,
                                title = stringResource(R.string.pending),
                                value = "${referralsMetrics.pendingReferrals}",
                                icon = Icons.Default.Alarm,
                                color = MaterialTheme.colorScheme.surface
                            )
                        }
                        item {
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
                if(provider.totalPayouts>= provider.referralLimit){
                    item {
                        ButtonToPay(onPayClickByProvider)
                    }
                }
                if(isSaturated && provider.totalPayouts < provider.referralLimit){
                    item{
                        StatItem(
                            modifier = Modifier.fillMaxWidth(),
                            title = stringResource(R.string.message_to_provider_saturated),
                            value = stringResource(R.string.warning),
                            icon = Icons.Default.WarningAmber,
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            tintIcon = Color(0xFFF8DE22)
                        )
                    }
                }
                item{
                    ItemEdit(
                        onClick = onAddProductClick,
                        modifier = Modifier.fieldModifier(),
                        data = stringResource(R.string.add_new_product),
                        icon = R.drawable.sell_twotone,
                        iconEdit = R.drawable.add
                    )
                }
                item{
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.search_products),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        SearchFieldBasic(
                            value = searchText,
                            onValueChange = updateSearchText,
                            placeholder = R.string.search,
                            trailingIcon = R.drawable.search
                        )
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
                            item {
                                TextButton(
                                    onClick = {onViewMoreProducts()}
                                ) {
                                    Text(text = stringResource(R.string.view_more_products))
                                    Icon(imageVector = Icons.Default.DoubleArrow, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    } else if(!isLoading) {
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
                    if (products.isNotEmpty()) {
                        item {
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
                        ){ product ->
                            ProductRow(product = product, onProductClick = onProductClick, isRealProduct = false)
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
                    if(isLoading && products.isNotEmpty()){
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
private fun BalanceCardProvider(
    paidReferrals: String,
    iconPaidReferrals: ImageVector,
    moneyPaid: String,
    iconMoneyPaid: ImageVector,
    onPaymentView: () -> Unit
    ) {
    var isVisible by remember { mutableStateOf(true) }
    val icon =
        if (isVisible) painterResource(R.drawable.visibility)
        else painterResource(R.drawable.visibility_off)
    val displayMoneyPaid = if (isVisible) "$$moneyPaid" else "$" + "*".repeat(moneyPaid.length)

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onPaymentView() },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ){
                Text(
                    text = stringResource(R.string.paid_referreds),
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ){
                    Icon(
                        imageVector = iconPaidReferrals,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = paidReferrals,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineLarge,
                        maxLines = 1,
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(4.dp).fillMaxWidth()
            ){
                Text(
                    text = stringResource(R.string.money_paid),
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ){
                    Icon(
                        imageVector = iconMoneyPaid,
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = displayMoneyPaid,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineMedium,
                        maxLines = 1
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
}

@Composable
private fun ButtonToPay(onPayClickByProvider: () -> Unit){
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                Icon(imageVector = Icons.Default.WarningAmber, contentDescription = null, tint = Color(0xFFF8DE22))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.comment_process_pay),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Justify,
                    modifier = Modifier
                        .padding(4.dp)
                        .weight(1f)
                )
            }
            Button(
                onClick = onPayClickByProvider,
                modifier = Modifier.padding(4.dp)
            ){
                Text(
                    text = stringResource(R.string.pay),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.bodyMedium
                )
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
private fun ClientRow(clientMetrics: UserAndReferralMetrics, onClientClick: (String) -> Unit){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable{onClientClick(clientMetrics.user.uid)},
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
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
                        icon = Icons.Default.Diamond,
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
            referralsConversion = "10.00",
            onPaymentView = {},
            onGraphMetricsView = {},
            isSaturated = true,
            onViewMoreProducts = {},
            loadMoreProducts = {},
            isPaginationActive = true,
            showButton = false,
            productsRealTime = productsRealTimeFake,
            products = productsFake,
            onAddProductClick = {},
            onProductClick = {},
            onViewRealProducts = {},
            onPayClickByProvider = {}
        )
    }
}

/**
fun generateFakeUserAndReferralMetrics(): List<UserAndReferralMetrics> = listOf(
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
) */