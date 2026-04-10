package com.example.feature.home.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.rememberPaneExpansionState
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.avilesrodriguez.domain.model.businessRules.BusinessRules
import com.avilesrodriguez.domain.model.industries.IndustriesType
import com.avilesrodriguez.domain.model.productsProvider.ProductProvider
import com.avilesrodriguez.domain.model.referral.ReferralMetrics
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.domain.model.user.UserType
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.composables.BottomBarNavigation
import com.avilesrodriguez.presentation.composables.TopBarMain
import com.avilesrodriguez.presentation.industries.label
import com.avilesrodriguez.presentation.industries.options
import com.avilesrodriguez.presentation.navigation.ActionOptionsHome
import com.avilesrodriguez.presentation.navigation.generateTabs
import com.avilesrodriguez.presentation.permissions.NotificationPermissionHandler
import com.example.feature.home.models.UserAndReferralMetrics
import com.example.feature.home.ui.details.DetailScreenUser
import com.example.feature.home.ui.graphicsMetrics.GraphMetrics
import com.example.feature.home.ui.paymentsMovement.PaymentsMovement
import com.example.feature.home.ui.products.addProduct.AddProductScreen
import com.example.feature.home.ui.products.detailProduct.DetailProductScreen
import kotlinx.coroutines.launch
import java.util.Locale

sealed class HomeDetailContent {
    data object Payments : HomeDetailContent()
    data object GraphMetrics : HomeDetailContent()
    data object AddProduct: HomeDetailContent()
    data class ProductDetail(val productId: String) : HomeDetailContent()
    data class UserDetail(val userId: String) : HomeDetailContent()

    // SAVER PARA QUE EL CONTENIDO SOBREVIVA A LA ROTACIÓN
    companion object {
        val Saver: Saver<HomeDetailContent?, Any> = Saver(
            save = { content ->
                when (content) {
                    is Payments -> "payments"
                    is GraphMetrics -> "graph_metrics"
                    is AddProduct -> "add_product"
                    is ProductDetail -> "product_detail:${content.productId}"
                    is UserDetail -> "user_detail:${content.userId}"
                    null -> null
                }
            },
            restore = { value ->
                val str = value as? String ?: return@Saver null
                when {
                    str == "payments" -> Payments
                    str == "graph_metrics" -> GraphMetrics
                    str == "add_product" -> AddProduct
                    str.startsWith("product_detail:") -> ProductDetail(str.removePrefix("product_detail:"))
                    str.startsWith("user_detail:") -> UserDetail(str.removePrefix("user_detail:"))
                    else -> null
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun HomeScreen(
    openScreen: (String) -> Unit,
    restartApp: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
){
    val userData by viewModel.userDataStore.collectAsState()
    val users by viewModel.users.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchText by viewModel.searchText.collectAsState()
    val selectedIndustry by viewModel.selectedIndustry.collectAsState()
    val referralsMetrics by viewModel.uiStateReferralsMetrics.collectAsState()
    val usersAndMetrics by viewModel.usersAndMetrics.collectAsState()
    val referralsConversionViewModel by viewModel.referralsConversion.collectAsState()
    val processingInfo by viewModel.processingCountReferralsProvider.collectAsState()
    val isSaturated = (processingInfo) >= BusinessRules.MAX_PROCESSING_REFERRALS
    val canReferUserClient by viewModel.canReferUserClient.collectAsState()
    val isPaginationActive by viewModel.isPaginationActive.collectAsState()
    val showButton by viewModel.showViewMoreButton.collectAsState()
    val productsRealTime by viewModel.productsStateRealTime.collectAsState()
    val products by viewModel.productsState.collectAsState()

    val options = ActionOptionsHome.getOptions()
    val industryOptions = IndustriesType.options(true)
    val referralsConversion = String.format(Locale.US, "%.2f", referralsConversionViewModel)
    var showNotificationPermission by remember { mutableStateOf(false) }

    if (showNotificationPermission) {
        NotificationPermissionHandler(onDismiss = { showNotificationPermission = false })
    }

    val adaptiveInfo = currentWindowAdaptiveInfo()
    val coroutineScope = rememberCoroutineScope()
    val tabs = generateTabs()
    
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(1) }

    val isTabletLandscape = adaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(840) &&
            adaptiveInfo.windowSizeClass.isHeightAtLeastBreakpoint(480)

    val isHomeTab = selectedTabIndex == 1

    val customDirective = calculatePaneScaffoldDirective(adaptiveInfo).copy(
        maxHorizontalPartitions = if (isTabletLandscape && isHomeTab) 2 else 1,
        horizontalPartitionSpacerSize = 20.dp
    )

    val navigator = rememberListDetailPaneScaffoldNavigator<HomeDetailContent>(
        scaffoldDirective = customDirective
    )
    
    // USAMOS REMEMBER SAVEABLE CON EL SAVER PERSONALIZADO
    var detailContent by rememberSaveable(stateSaver = HomeDetailContent.Saver) { 
        mutableStateOf(if (isTabletLandscape) HomeDetailContent.Payments else null) 
    }

    val paneExpansionState = rememberPaneExpansionState()
    val isShowingBothPanels = isTabletLandscape && isHomeTab && navigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail] == PaneAdaptedValue.Expanded

    LaunchedEffect(isShowingBothPanels) {
        if (isShowingBothPanels) {
            paneExpansionState.setFirstPaneProportion(0.5f)
        }
    }

    LaunchedEffect(selectedTabIndex) {
        if (selectedTabIndex == 1) {
            if (isTabletLandscape && detailContent == null) {
                detailContent = HomeDetailContent.Payments
                navigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
            }
        } else {
            if (navigator.canNavigateBack()) {
                navigator.navigateBack()
            }
            detailContent = null

            when(selectedTabIndex){
                0 -> viewModel.onReferrals { openScreen(it) }
                2 -> viewModel.onSettings { openScreen(it) }
            }
        }
    }

    BackHandler(navigator.canNavigateBack()) {
        coroutineScope.launch { navigator.navigateBack() }
    }

    Row(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainer)) {
        if (isTabletLandscape) {
            NavigationRail(
                modifier = Modifier.width(84.dp).fillMaxHeight(),
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Spacer(Modifier.weight(1f))
                tabs.forEachIndexed { index, tab ->
                    NavigationRailItem(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        icon = { Icon(tab.icon, null) },
                        label = { Text(stringResource(tab.title)) },
                        alwaysShowLabel = false
                    )
                }
                Spacer(Modifier.weight(1f))
            }
        }

        ListDetailPaneScaffold(
            modifier = Modifier.weight(1f),
            directive = navigator.scaffoldDirective,
            value = navigator.scaffoldValue,
            paneExpansionState = paneExpansionState,
            listPane = {
                AnimatedPane {
                    Scaffold(
                        contentWindowInsets = WindowInsets.safeDrawing,
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        topBar = {
                            TopBarMain(
                                title = stringResource(R.string.app_name_presentation),
                                options=options,
                                iconNotification = R.drawable.notifications_twotone,
                                onNotificationClick = { showNotificationPermission = true },
                                onActionClick = { action ->
                                viewModel.onActionClick(openScreen, restartApp, action)
                                }
                            )
                        },
                        bottomBar = {
                            if (!isTabletLandscape) {
                                BottomBarNavigation(
                                    currentTab = selectedTabIndex,
                                    tabs = tabs,
                                    onClick = { index -> selectedTabIndex = index }
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(modifier = Modifier.fillMaxSize().padding(innerPadding).clipToBounds()) {
                            when(selectedTabIndex){
                                1 -> HomeMainContent(
                                    user = userData,
                                    users = users,
                                    isLoading = isLoading,
                                    searchText = searchText,
                                    updateSearchText = viewModel::updateSearchText,
                                    selectedIndustry = selectedIndustry?.label(),
                                    onIndustryChange = viewModel::onIndustryChange,
                                    industryOptions = industryOptions,
                                    onUserClick = { uId ->
                                        detailContent = HomeDetailContent.UserDetail(uId)
                                        coroutineScope.launch { navigator.navigateTo(ListDetailPaneScaffoldRole.Detail) }
                                    },
                                    referralsMetrics = referralsMetrics,
                                    usersAndMetrics = usersAndMetrics,
                                    referralsConversion = referralsConversion,
                                    onPaymentView = {
                                        detailContent = HomeDetailContent.Payments
                                        coroutineScope.launch { navigator.navigateTo(ListDetailPaneScaffoldRole.Detail) }
                                    },
                                    onGraphMetricsView = {
                                        detailContent = HomeDetailContent.GraphMetrics
                                        coroutineScope.launch { navigator.navigateTo(ListDetailPaneScaffoldRole.Detail) }
                                    },
                                    isSaturated = isSaturated,
                                    canReferUserClient = canReferUserClient,
                                    onViewMoreProducts = viewModel::onViewMoreProducts,
                                    loadMoreProducts = viewModel::loadMoreProducts,
                                    isPaginationActive = isPaginationActive,
                                    showButton = showButton,
                                    productsRealTime = productsRealTime,
                                    products = products,
                                    onAddProductClick = {
                                        detailContent = HomeDetailContent.AddProduct
                                        coroutineScope.launch { navigator.navigateTo(ListDetailPaneScaffoldRole.Detail) }
                                    },
                                    onProductClick = {productId ->
                                        detailContent = HomeDetailContent.ProductDetail(productId)
                                        coroutineScope.launch { navigator.navigateTo(ListDetailPaneScaffoldRole.Detail) }
                                    },
                                    onViewRealProducts = viewModel::onViewRealProducts
                                )
                                else -> {
                                    Box(Modifier.fillMaxSize())
                                }
                            }
                        }
                    }
                }
            },
            detailPane = {
                AnimatedPane {
                    if (isHomeTab) {
                        when (val content = detailContent) {
                            is HomeDetailContent.Payments -> {
                                PaymentsMovement(
                                    popUp = { coroutineScope.launch { navigator.navigateBack() } },
                                    showTopBar = !isShowingBothPanels
                                )
                            }
                            is HomeDetailContent.GraphMetrics ->{
                                GraphMetrics(
                                    popUp = { coroutineScope.launch { navigator.navigateBack() } },
                                    showTopBar = !isShowingBothPanels
                                )
                            }
                            is HomeDetailContent.AddProduct -> {
                                AddProductScreen(
                                    onBackClick = { coroutineScope.launch { navigator.navigateBack() } },
                                    showTopBar = !isShowingBothPanels
                                )
                            }
                            is HomeDetailContent.ProductDetail -> {
                                DetailProductScreen(
                                    productId = content.productId,
                                    onBackClick = { coroutineScope.launch { navigator.navigateBack() } },
                                    openScreen = openScreen,
                                    deleteProduct = {
                                        detailContent = null
                                        viewModel.hideDelete(content.productId)
                                        coroutineScope.launch { navigator.navigateBack() } },
                                    showTopBar = !isShowingBothPanels
                                )
                            }
                            is HomeDetailContent.UserDetail -> {
                                DetailScreenUser(
                                    uId = content.userId,
                                    popUp = { coroutineScope.launch { navigator.navigateBack() } },
                                    openScreen = openScreen,
                                    showTopBar = !isShowingBothPanels
                                )
                            }
                            null -> {}
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun HomeMainContent(
    user: UserData?,
    users: List<UserData>,
    isLoading: Boolean,
    searchText: String,
    updateSearchText: (String) -> Unit,
    selectedIndustry: Int?,
    onIndustryChange: (Int) -> Unit,
    industryOptions: List<Int>,
    onUserClick: (String) -> Unit,
    referralsMetrics: ReferralMetrics,
    usersAndMetrics: List<UserAndReferralMetrics>,
    referralsConversion: String,
    onPaymentView: () -> Unit,
    onGraphMetricsView: () -> Unit,
    isSaturated: Boolean,
    canReferUserClient: Boolean,
    onViewMoreProducts: () -> Unit,
    loadMoreProducts: () -> Unit,
    isPaginationActive: Boolean,
    showButton: Boolean,
    productsRealTime: List<ProductProvider>,
    products: List<ProductProvider>,
    onAddProductClick: () -> Unit,
    onProductClick: (String) -> Unit,
    onViewRealProducts: () -> Unit
) {
    if (user != null) {
        when (user.type) {
            UserType.CLIENT -> HomeScreenClient(
                user = user,
                users = users,
                isLoading = isLoading,
                searchText = searchText,
                updateSearchText = updateSearchText,
                selectedIndustry = selectedIndustry,
                onIndustryChange = onIndustryChange,
                industryOptions = industryOptions,
                onUserClick = onUserClick,
                referralsMetrics = referralsMetrics,
                onPaymentView = onPaymentView,
                onGraphMetricsView = onGraphMetricsView,
                canReferUserClient = canReferUserClient,
                onViewMoreProducts = onViewMoreProducts,
                loadMoreProducts = loadMoreProducts,
                isPaginationActive = isPaginationActive,
                showButton = showButton,
                productsRealTime = productsRealTime,
                products = products,
                onProductClick = onProductClick,
                onViewRealProducts = onViewRealProducts
            )
            UserType.PROVIDER -> HomeScreenProvider(
                user = user,
                isLoading = isLoading,
                searchText = searchText,
                updateSearchText = updateSearchText,
                referralsMetrics = referralsMetrics,
                onUserClick = onUserClick,
                usersAndMetrics = usersAndMetrics,
                referralsConversion = referralsConversion,
                onPaymentView = onPaymentView,
                onGraphMetricsView = onGraphMetricsView,
                isSaturated = isSaturated,
                onViewMoreProducts = onViewMoreProducts,
                loadMoreProducts = loadMoreProducts,
                isPaginationActive = isPaginationActive,
                showButton = showButton,
                productsRealTime = productsRealTime,
                products = products,
                onAddProductClick = onAddProductClick,
                onProductClick = onProductClick,
                onViewRealProducts = onViewRealProducts
            )
        }
    } else {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    }
}
