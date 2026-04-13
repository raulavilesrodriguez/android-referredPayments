package com.example.feature.home.ui.products.detailProduct

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness2
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.avilesrodriguez.domain.model.productsProvider.ProductProvider
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.avatar.Avatar
import com.avilesrodriguez.presentation.avatar.DEFAULT_AVATAR_USER
import com.avilesrodriguez.presentation.composables.BasicToolbar
import com.avilesrodriguez.presentation.composables.StatItem
import com.avilesrodriguez.presentation.composables.ToolBarWithIcon
import com.avilesrodriguez.presentation.details.DetailMetricItem
import com.avilesrodriguez.presentation.fakeData.productProvider
import com.avilesrodriguez.presentation.fakeData.userProvider
import com.avilesrodriguez.presentation.industries.icons
import com.avilesrodriguez.presentation.industries.label
import java.util.Locale


@Composable
fun DetailProductScreen(
    productId: String?,
    onBackClick: () -> Unit,
    openScreen: (String) -> Unit,
    deleteProduct: () -> Unit,
    showTopBar: Boolean = true,
    viewModel: DetailProductViewModel = hiltViewModel()
){
    LaunchedEffect(productId) {
        viewModel.loadProductInformation(productId)
    }

    val canReferUserClient by viewModel.canReferUserClient.collectAsState()
    val isProviderSaturated by viewModel.isProviderSaturated.collectAsState()
    val product by viewModel.productState.collectAsState()
    val providerUser by viewModel.providerUser.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    var showDialogDeleteProduct by remember { mutableStateOf(false) }

    val provider = providerUser as? UserData.Provider
    if (product.id.isEmpty() || provider == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .wrapContentWidth(Alignment.CenterHorizontally)
            )
        }
    } else {
        DetailProductScreenContent(
            onBackClick = onBackClick,
            canReferUserClient = canReferUserClient,
            isProviderSaturated = isProviderSaturated,
            product = product,
            providerUser = provider,
            onAddReferClick = { viewModel.onAddReferClick(product.providerId, product.id, openScreen) },
            currentUser = currentUser,
            onDeleteClick = { showDialogDeleteProduct = true },
            onEditClick = { viewModel.onEditProductClick(product.id, openScreen) },
            showTopBar = showTopBar
        )
    }

    if(showDialogDeleteProduct){
        AlertDialog(
            onDismissRequest = { showDialogDeleteProduct = false },
            title = { Text(text = stringResource(R.string.delete_product)) },
            text = { Text(text = stringResource(R.string.warning_delete_product)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialogDeleteProduct = false
                        deleteProduct()
                    }
                ) {
                    Text(text = stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialogDeleteProduct = false }
                ){
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun DetailProductScreenContent(
    onBackClick: () -> Unit,
    canReferUserClient: Boolean,
    isProviderSaturated: Boolean,
    product: ProductProvider,
    providerUser: UserData.Provider,
    onAddReferClick: () -> Unit,
    currentUser: UserData?,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    showTopBar: Boolean = true
){
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            if (showTopBar) {
                ToolBarWithIcon(
                    iconBack = R.drawable.arrow_back,
                    title = stringResource(R.string.product_information),
                    backClick = { onBackClick() }
                )
            } else {
                BasicToolbar(stringResource(R.string.product_information))
            }
        },
        bottomBar = {
            when (currentUser) {
                is UserData.Client -> {
                    ButtonToRefer(
                        onReferClick = onAddReferClick,
                        isSaturated = isProviderSaturated,
                        canReferUserClient = canReferUserClient
                    )
                }
                is UserData.Provider -> {
                    ButtonsToDeleteAndEdit(
                        onDeleteClick = onDeleteClick,
                        onEditClick = onEditClick
                    )
                }
                else -> {}
            }
        },
        content = { innerPadding ->
            ProfileProductProvider(
                product = product,
                provider = providerUser,
                isSaturated = isProviderSaturated,
                modifier = Modifier.padding(innerPadding)
            )
        }
    )
}

@Composable
fun ButtonToRefer(
    onReferClick: () -> Unit,
    isSaturated: Boolean,
    canReferUserClient: Boolean
){
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 12.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Button(
            onClick = { onReferClick() },
            enabled = !isSaturated && canReferUserClient,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.PersonAdd, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.refer_now),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ButtonsToDeleteAndEdit(
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(
            onClick = { onDeleteClick()}
        ) {
            Text(
                text = stringResource(R.string.delete_product),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Icon(
                painter = painterResource(R.drawable.delete),
                contentDescription = null,
                modifier = Modifier.padding(start = 4.dp).size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        TextButton(
            onClick = { onEditClick() }
        ) {
            Text(
                text = stringResource(R.string.edit_product),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Icon(
                painter = painterResource(R.drawable.edit),
                contentDescription = null,
                modifier = Modifier.padding(start = 4.dp).size(24.dp),
                tint = MaterialTheme.colorScheme.primary
                )
        }
    }
}

@Composable
private fun ProfileProductProvider(
    product: ProductProvider,
    provider: UserData.Provider,
    isSaturated: Boolean,
    modifier: Modifier = Modifier
){
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 8.dp)
    ){
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ){
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "$ ${product.payByReferral}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.pay_by_referral),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = stringResource(provider.industry.label()),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        Text(
            text = stringResource(R.string.about_product),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )
        InfoGeneralProduct(product = product)
        Text(
            text = stringResource(R.string.company_description),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )
        InfoGeneralProvider(provider = provider)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp)
        ){
            Row(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ){
                DetailMetricItem(
                    icon = Icons.Default.Star,
                    value = String.format(Locale.US, "%.1f", provider.paymentRating),
                    label = stringResource(R.string.rating),
                    tint = Color(0xFF0F2854)
                )
                VerticalDivider(modifier = Modifier.height(40.dp), thickness = 1.dp)
                DetailMetricItem(
                    icon = Icons.Default.Payments,
                    value = "${provider.totalPayouts}",
                    label = stringResource(R.string.payouts),
                    tint = Color(0xFF1C4D8D)
                )
                VerticalDivider(modifier = Modifier.height(40.dp))
                if(provider.isActive){
                    DetailMetricItem(
                        icon = Icons.Default.BrightnessHigh,
                        value ="",
                        label = stringResource(R.string.active_user),
                        tint = Color(0xFF08CB00)
                    )
                } else {
                    DetailMetricItem(
                        icon = Icons.Default.Brightness2,
                        value ="",
                        label = stringResource(R.string.inactive_user),
                        tint = Color(0xFFC40C0C)
                    )
                }
            }
        }
        InfoCard(
            icon = Icons.Default.Business,
            title = stringResource(R.string.settings_industry),
            value = stringResource(provider.industry.label())
        )
        if (!provider.website.isNullOrBlank()) {
            InfoCard(
                icon = Icons.Default.Language,
                title = stringResource(R.string.website),
                value = provider.website!!
            )
        }
        if(isSaturated){
            StatItem(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = 16.dp),
                title = stringResource(R.string.provider_saturated),
                value = stringResource(R.string.warning),
                icon = Icons.Default.WarningAmber,
                color = MaterialTheme.colorScheme.errorContainer
            )
        }
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun InfoGeneralProvider(
    provider: UserData.Provider,
){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth().padding(start = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Absolute.Left
        ) {
            val photo = provider.photoUrl.ifEmpty { DEFAULT_AVATAR_USER }
            Avatar(photoUri = photo, size = 42.dp)
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
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = provider.companyDescription ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                )
            }

        }
    }
}

@Composable
private fun InfoGeneralProduct(product: ProductProvider){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth().padding(start = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Absolute.Left
        ) {
            Icon(
                painter = painterResource(product.industry.icons()),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(42.dp).padding(start = 4.dp)
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
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 10,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                )
            }
        }
    }
}


@Composable
private fun InfoCard(icon: ImageVector, title: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(16.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetailProductScreenPreview() {
    MaterialTheme {
        DetailProductScreenContent(
            onBackClick = {},
            canReferUserClient = true,
            isProviderSaturated = false,
            product = productProvider,
            providerUser = userProvider,
            currentUser = userProvider,
            onAddReferClick = {},
            onDeleteClick = {},
            onEditClick = {},
            showTopBar = true
        )
    }
}