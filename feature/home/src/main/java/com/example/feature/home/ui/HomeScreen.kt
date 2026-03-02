package com.example.feature.home.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.rememberPaneExpansionState
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigationsuite.ExperimentalMaterial3AdaptiveNavigationSuiteApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.avilesrodriguez.domain.model.industries.IndustriesType
import com.avilesrodriguez.domain.model.referral.ReferralMetrics
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.domain.model.user.UserType
import com.avilesrodriguez.feature.referrals.ui.referrals.ReferralsScreen
import com.avilesrodriguez.feature.settings.ui.SettingsScreen
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.composables.DropdownContextMenu
import com.avilesrodriguez.presentation.industries.label
import com.avilesrodriguez.presentation.industries.options
import com.example.feature.home.models.StartListTab
import com.example.feature.home.models.UserAndReferralMetrics
import com.example.feature.home.ui.details.DetailScreenUser
import com.example.feature.home.ui.paymentsMovement.PaymentsMovement
import kotlinx.coroutines.launch
import java.util.Locale

sealed class HomeDetailContent {
    data object Policies : HomeDetailContent()
    data object Payments : HomeDetailContent()
    data class UserDetail(val userId: String) : HomeDetailContent()
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

    val options = ActionOptionsHome.getOptions()
    val industryOptions = IndustriesType.options(true)
    val referralsConversion = String.format(Locale.US, "%.2f", referralsConversionViewModel)

    val adaptiveInfo = currentWindowAdaptiveInfo()
    val coroutineScope = rememberCoroutineScope()

    val tabs = generateTabs()
    
    // 1. FUENTE DE VERDAD: Estado de la pestaña actual
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(1) }
    val pagerState = rememberPagerState(initialPage = 1) { tabs.size }

    // Sincronización Pager -> Estado (Gesto swipe en móvil)
    LaunchedEffect(pagerState.currentPage) {
        selectedTabIndex = pagerState.currentPage
    }

    // Sincronización Estado -> Pager (Clic in Rail/BottomBar)
    LaunchedEffect(selectedTabIndex) {
        if (pagerState.currentPage != selectedTabIndex) {
            try { pagerState.scrollToPage(selectedTabIndex) } catch (e: Exception) {}
        }
    }

    val isTabletLandscape = adaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(840) &&
            adaptiveInfo.windowSizeClass.isHeightAtLeastBreakpoint(480)

    val isHomeTab = selectedTabIndex == 1

    Row(Modifier.fillMaxSize()) {
        if (isTabletLandscape) {
            NavigationRail(
                modifier = Modifier.width(84.dp).fillMaxHeight(),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Spacer(Modifier.weight(1f))
                tabs.forEachIndexed { index, tab ->
                    val icon = when(index) {
                        0 -> Icons.Default.Star
                        1 -> Icons.Default.Home
                        else -> Icons.Default.Person
                    }
                    NavigationRailItem(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        icon = { Icon(icon, null) },
                        label = { Text(stringResource(tab.title)) },
                        alwaysShowLabel = false
                    )
                }
                Spacer(Modifier.weight(1f))
            }
        }

        val customDirective = calculatePaneScaffoldDirective(adaptiveInfo).copy(
            maxHorizontalPartitions = if (isTabletLandscape && isHomeTab) 2 else 1,
            horizontalPartitionSpacerSize = 24.dp
        )

        val navigator = rememberListDetailPaneScaffoldNavigator<HomeDetailContent>(
            scaffoldDirective = customDirective
        )
        
        var detailContent by remember { mutableStateOf<HomeDetailContent?>(null) }
        val paneExpansionState = rememberPaneExpansionState()
        
        val isShowingBothPanels = isTabletLandscape && isHomeTab && navigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail] == PaneAdaptedValue.Expanded

        LaunchedEffect(isShowingBothPanels) {
            if (isShowingBothPanels) {
                paneExpansionState.setFirstPaneProportion(0.6f)
            }
        }

        LaunchedEffect(selectedTabIndex) {
            if (selectedTabIndex != 1) {
                coroutineScope.launch { 
                    if (navigator.canNavigateBack()) navigator.navigateBack()
                    detailContent = null
                }
            }
        }

        BackHandler(navigator.canNavigateBack()) {
            coroutineScope.launch { navigator.navigateBack() }
        }

        ListDetailPaneScaffold(
            modifier = Modifier.weight(1f),
            directive = navigator.scaffoldDirective,
            value = navigator.scaffoldValue,
            paneExpansionState = paneExpansionState,
            listPane = {
                AnimatedPane {
                    Scaffold(
                        topBar = {
                            HomeScreenTopBar(selectedTabIndex, options, onActionClick = { action ->
                                if (ActionOptionsHome.getById(action) == ActionOptionsHome.POLICIES) {
                                    detailContent = HomeDetailContent.Policies
                                    coroutineScope.launch { navigator.navigateTo(ListDetailPaneScaffoldRole.Detail) }
                                } else {
                                    viewModel.onActionClick(openScreen, restartApp, action)
                                }
                            })
                        },
                        bottomBar = {
                            if (!isTabletLandscape) {
                                HomeBottomBar(selectedTabIndex, tabs, onClick = { index ->
                                    selectedTabIndex = index
                                })
                            }
                        },
                        floatingActionButton = {
                            if (selectedTabIndex == 2) {
                                FloatingActionButton(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    onClick = { viewModel.editUser(openScreen) }
                                ) {
                                    Icon(painterResource(R.drawable.edit), stringResource(R.string.edit))
                                }
                            }
                        }
                    ) { innerPadding ->
                        if (isTabletLandscape) {
                            Box(modifier = Modifier.fillMaxSize().padding(innerPadding).clipToBounds()) {
                                when(selectedTabIndex){
                                    0 -> ReferralsScreen(openScreen = openScreen)
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
                                        }
                                    )
                                    2 -> SettingsScreen(openScreen = openScreen, restartApp = restartApp)
                                }
                            }
                        } else {
                            HorizontalPager(
                                modifier = Modifier.fillMaxSize().padding(innerPadding).clipToBounds(),
                                state = pagerState,
                                userScrollEnabled = true
                            ) { index ->
                                when(index){
                                    0 -> ReferralsScreen(openScreen = openScreen)
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
                                        }
                                    )
                                    2 -> SettingsScreen(openScreen = openScreen, restartApp = restartApp)
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
                            is HomeDetailContent.Policies -> {
                                PoliciesScreen(
                                    popUp = { coroutineScope.launch { navigator.navigateBack() } },
                                    showTopBar = !isShowingBothPanels
                                )
                            }
                            is HomeDetailContent.Payments -> {
                                PaymentsMovement(
                                    popUp = { coroutineScope.launch { navigator.navigateBack() } },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenTopBar(currentTab: Int, options: List<Int>, onActionClick: (Int) -> Unit) {
    TopAppBar(
        title = {
            when (currentTab) {
                0 -> { Text(stringResource(R.string.referrals)) }
                1 -> { Text(stringResource(R.string.app_name_presentation)) }
                2 -> { Text(stringResource(R.string.profile)) }
            }
        },
        actions = {
            DropdownContextMenu(options = options) { action -> onActionClick(action) }
        }
    )
}

@Composable
private fun HomeBottomBar(currentTab: Int, tabs: List<StartListTab>, onClick: (Int) -> Unit) {
    BottomAppBar(
        modifier = Modifier.height(56.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        contentPadding = PaddingValues(0.dp),
        windowInsets = WindowInsets(0, 0, 0, 0),
        tonalElevation = NavigationBarDefaults.Elevation
    ) {
        tabs.forEachIndexed { index, tab ->
            val isSelected = currentTab == index
            val icon = when(index) {
                0 -> Icons.Default.Star
                1 -> Icons.Default.Home
                else -> Icons.Default.Person
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onClick(index) },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else androidx.compose.ui.graphics.Color.Transparent,
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(stringResource(tab.title), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
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
    onPaymentView: () -> Unit
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
                onPaymentView = onPaymentView
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
                onPaymentView = onPaymentView
            )
        }
    } else {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    }
}

fun generateTabs(): List<StartListTab> {
    return listOf(
        StartListTab(R.string.referrals),
        StartListTab(R.string.start),
        StartListTab(R.string.settings)
    )
}
