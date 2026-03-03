package com.avilesrodriguez.feature.referrals.ui.referrals

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.avilesrodriguez.domain.model.referral.Referral
import com.avilesrodriguez.domain.model.referral.ReferralWithNames
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.feature.referrals.ui.referral.ReferralScreen
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.composables.BottomBarNavigation
import com.avilesrodriguez.presentation.composables.TopBarMain
import com.avilesrodriguez.presentation.fakeData.userClient
import com.avilesrodriguez.presentation.navigation.ActionOptionsHome
import com.avilesrodriguez.presentation.navigation.generateTabs
import kotlinx.coroutines.launch

sealed class ReferralsDetailContent{
    data object ReferralStart : ReferralsDetailContent()
    data class ReferralDetail(val referralId: String): ReferralsDetailContent()
    companion object{
        val Saver: Saver<ReferralsDetailContent?, Any> = Saver(
            save = { content ->
                when(content){
                    is ReferralStart -> "referral_start"
                    is ReferralDetail -> "referral_detail:${content.referralId}"
                    null -> null
                }
            },
            restore = { value ->
                val str = value as? String ?: return@Saver null
                when {
                    str == "referral_start" -> ReferralStart
                    str.startsWith("referral_detail:") -> ReferralDetail(str.removePrefix("referral_detail:"))
                    else -> null
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ReferralsScreen(
    openScreen: (String) -> Unit,
    restartApp: (String) -> Unit,
    viewModel: ReferralsViewModel = hiltViewModel()
){
    val uiState by viewModel.uiState.collectAsState()
    val searchText by viewModel.searchText.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val user by viewModel.userDataStore.collectAsState()

    val options = ActionOptionsHome.getOptions()
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val coroutineScope = rememberCoroutineScope()
    val tabs = generateTabs()

    // 1. FUENTE DE VERDAD: Estado de la pestaña actual
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    val isTabletLandscape = adaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(840) &&
            adaptiveInfo.windowSizeClass.isHeightAtLeastBreakpoint(480)
    val isReferralTab = selectedTabIndex == 0

    val customDirective = calculatePaneScaffoldDirective(adaptiveInfo).copy(
        maxHorizontalPartitions = if (isTabletLandscape && isReferralTab) 2 else 1,
        horizontalPartitionSpacerSize = 20.dp
    )

    val navigator = rememberListDetailPaneScaffoldNavigator<ReferralsDetailContent>(
        scaffoldDirective = customDirective
    )

    var detailContent by rememberSaveable(stateSaver = ReferralsDetailContent.Saver) {
        mutableStateOf(if(isTabletLandscape) ReferralsDetailContent.ReferralStart else null)
    }

    val paneExpansionState = rememberPaneExpansionState()
    val isShowingBothPanels = isTabletLandscape && isReferralTab && navigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail] == PaneAdaptedValue.Expanded

    LaunchedEffect(isShowingBothPanels) {
        if (isShowingBothPanels) {
            paneExpansionState.setFirstPaneProportion(0.6f)
        }
    }

    // NAVEGACIÓN Y LIMPIEZA: Maneja el inicio, el regreso a la pestaña de Inicio y el cambio a otras pestañas
    LaunchedEffect(selectedTabIndex) {
        if (selectedTabIndex == 0) {
            if (isTabletLandscape && detailContent == null) {
                detailContent = ReferralsDetailContent.ReferralStart
                navigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
            }
        } else {
            // Si salimos de Inicio, cerramos detalle y limpiamos estado para evitar navegación fantasma
            if (navigator.canNavigateBack()) {
                navigator.navigateBack()
            }
            detailContent = null

            when(selectedTabIndex){
                1 -> viewModel.onHome { openScreen(it) }
                2 -> viewModel.onSettings { openScreen(it) }
            }
        }
    }

    BackHandler(navigator.canNavigateBack()) {
        coroutineScope.launch { navigator.navigateBack() }
    }

    Row(Modifier.fillMaxSize()){
        if(isTabletLandscape){
            NavigationRail(
                modifier = Modifier
                    .width(84.dp)
                    .fillMaxHeight(),
                containerColor = MaterialTheme.colorScheme.surface
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
                        topBar = {
                            TopBarMain(
                                title = stringResource(R.string.referrals),
                                options = options
                            ) { action ->
                                viewModel.onActionClick(openScreen, restartApp, action)
                            }
                        },
                        bottomBar = {
                            if(!isTabletLandscape){
                                BottomBarNavigation(
                                    currentTab = selectedTabIndex,
                                    tabs = tabs,
                                    onClick = { selectedTabIndex = it }
                                )
                            }
                        },
                    ){innerPadding ->
                        Box(modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .clipToBounds()){
                            when(selectedTabIndex){
                                0 -> ReferralsScreenContent(
                                    searchText = searchText,
                                    onValueChange = viewModel::updateSearchText,
                                    onReferralClick = { referral ->
                                        detailContent = ReferralsDetailContent.ReferralDetail(referral.id)
                                        coroutineScope.launch { navigator.navigateTo(ListDetailPaneScaffoldRole.Detail) }
                                                      },
                                    referrals = uiState,
                                    user = user,
                                    isLoading = isLoading
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
                    if(isReferralTab){
                        when(val content = detailContent){
                            is ReferralsDetailContent.ReferralStart -> ReferralStar()
                            is ReferralsDetailContent.ReferralDetail -> {
                                ReferralScreen(
                                    referralId = content.referralId,
                                    onBackClick = {coroutineScope.launch { navigator.navigateBack() }},
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
private fun ReferralsScreenContent(
    searchText: String,
    onValueChange: (String) -> Unit,
    onReferralClick: (Referral) -> Unit,
    referrals: List<ReferralWithNames>,
    user: UserData?,
    isLoading: Boolean
){
    val focusManager = LocalFocusManager.current

    Column(modifier = Modifier.fillMaxSize()){
        if(!isLoading){
            if(referrals.isNotEmpty()){
                ReferralsList(
                    searchText = searchText,
                    onValueChange = onValueChange,
                    onReferralClick = onReferralClick,
                    referrals = referrals,
                    user = user,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = stringResource(R.string.no_have_referreds))
                }
            }
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }

    // close keyboard when focus changes
    DisposableEffect(Unit) {
        onDispose {
            focusManager.clearFocus()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReferralsScreenPreview(){
    MaterialTheme {
        ReferralsScreenContent(
            searchText = "",
            onValueChange = {},
            onReferralClick = {},
            referrals = listOf(),
            user = userClient,
            isLoading = false
        )
    }
}