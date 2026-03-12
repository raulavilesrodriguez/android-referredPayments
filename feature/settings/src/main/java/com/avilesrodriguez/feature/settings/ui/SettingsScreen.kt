package com.avilesrodriguez.feature.settings.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.avatar.Avatar
import com.avilesrodriguez.presentation.avatar.DEFAULT_AVATAR_USER
import com.avilesrodriguez.presentation.composables.BottomBarNavigation
import com.avilesrodriguez.presentation.composables.TopBarMain
import com.avilesrodriguez.presentation.navigation.ActionOptionsHome
import com.avilesrodriguez.presentation.navigation.generateTabs
import com.avilesrodriguez.presentation.profile.ItemEditProfile
import com.avilesrodriguez.presentation.profile.ItemProfile
import kotlinx.coroutines.launch

sealed class SettingsContent{
    data object EditSplash: SettingsContent()
    data object EditUser : SettingsContent()

    companion object{
        val Saver: Saver<SettingsContent?, Any> = Saver(
            save = { content ->
                when (content) {
                    is EditSplash -> "edit_splash"
                    is EditUser -> "edit_user"
                    null -> null
                }
            },
            restore = { value ->
                val str = value as? String ?: return@Saver null
                when {
                    str == "edit_splash" -> EditSplash
                    str == "edit_user" -> EditUser
                    else -> null
                }
            }
        )
    }
}

@OptIn( ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun SettingsScreen(
    openScreen: (String) -> Unit,
    restartApp: (String) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
){
    val userData by viewModel.uiState.collectAsState()
    var showDialogDeleteAccount by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.reloadUserData()
    }

    val options = ActionOptionsHome.getOptions()
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val coroutineScope = rememberCoroutineScope()
    val tabs = generateTabs()

    // 1. FUENTE DE VERDAD: Estado de la pestaña actual
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(2) }

    val isTabletLandscape = adaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(840) &&
            adaptiveInfo.windowSizeClass.isHeightAtLeastBreakpoint(480)

    val isSettingsTab = selectedTabIndex == 2

    val customDirective = calculatePaneScaffoldDirective(adaptiveInfo).copy(
        maxHorizontalPartitions = if (isTabletLandscape && isSettingsTab) 2 else 1,
        horizontalPartitionSpacerSize = 20.dp
    )

    val navigator = rememberListDetailPaneScaffoldNavigator<SettingsContent>(
        scaffoldDirective = customDirective
    )

    var detailContent by rememberSaveable(stateSaver = SettingsContent.Saver) {
        mutableStateOf(if (isTabletLandscape) SettingsContent.EditSplash else null)
    }

    val paneExpansionState = rememberPaneExpansionState()
    val isShowingBothPanels = isTabletLandscape && isSettingsTab && navigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail] == PaneAdaptedValue.Expanded

    LaunchedEffect(isShowingBothPanels) {
        if (isShowingBothPanels) {
            paneExpansionState.setFirstPaneProportion(0.5f)
        }
    }
    // NAVEGACIÓN Y LIMPIEZA: Maneja el inicio, el regreso a la pestaña de Inicio y el cambio a otras pestañas
    LaunchedEffect(selectedTabIndex) {
        if(selectedTabIndex == 2){
            if(isTabletLandscape && detailContent == null){
                detailContent = SettingsContent.EditSplash
                navigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
            }
        } else {
            if (navigator.canNavigateBack()) {
                navigator.navigateBack()
            }
            detailContent = null

            when(selectedTabIndex){
                0 -> viewModel.onReferrals { openScreen(it) }
                1 -> viewModel.onHome { openScreen(it) }
            }
        }
    }

    BackHandler(navigator.canNavigateBack()) {
        coroutineScope.launch { navigator.navigateBack() }
    }

    Row(modifier=Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainer)){
        if(isTabletLandscape){
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
                        modifier = Modifier.weight(1f),
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        contentWindowInsets = WindowInsets.safeDrawing,
                        topBar = {
                            TopBarMain(
                                title = stringResource(R.string.app_name_presentation),
                                options = options,
                                onActionClick = { action ->
                                    viewModel.onActionClick(openScreen, restartApp, action)
                                }
                            )
                        },
                        bottomBar = {
                            if(!isTabletLandscape){
                                BottomBarNavigation(
                                    currentTab = selectedTabIndex,
                                    tabs = tabs,
                                    onClick = {index -> selectedTabIndex = index}
                                )
                            }
                        },
                        floatingActionButton = {
                            FloatingActionButton(
                                containerColor = MaterialTheme.colorScheme.primary,
                                onClick = {
                                    detailContent = SettingsContent.EditUser
                                    coroutineScope.launch { navigator.navigateTo(ListDetailPaneScaffoldRole.Detail) }
                                }
                            ) {
                                Icon(painterResource(R.drawable.edit), stringResource(R.string.edit))
                            }
                        }
                    ) { innerPadding ->
                        Box(modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)){
                            when(selectedTabIndex){
                                2 -> Profile(
                                    userData = userData,
                                    onDeleteAccountClick = { showDialogDeleteAccount = true }
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
                    if(isSettingsTab){
                        when(detailContent){
                            is SettingsContent.EditSplash -> EditSplash()
                            is SettingsContent.EditUser -> {
                                EditScreen(
                                    popUp = { coroutineScope.launch { navigator.navigateBack() } },
                                    Cancel = { detailContent = null },
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

    if(showDialogDeleteAccount){
        AlertDialog(
            onDismissRequest = { showDialogDeleteAccount = false },
            title = {Text(stringResource(R.string.delete_account))},
            text = {Text(stringResource(R.string.warning_delete_account))},
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialogDeleteAccount = false
                        viewModel.secureDeleteAccount(restartApp)
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialogDeleteAccount = false }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun Profile(
    userData: UserData?,
    onDeleteAccountClick: () -> Unit
){
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter){
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .clickable {},
                contentAlignment = Alignment.Center // Center the content
            ) {
                Avatar(
                    photoUri = if(userData?.photoUrl.isNullOrBlank()) DEFAULT_AVATAR_USER else userData.photoUrl,
                    size = 100.dp
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.fillMaxWidth()){
                ItemProfile(icon = R.drawable.name, title = R.string.settings_name, data = userData?.name?:stringResource(R.string.no_information))
                ItemProfile(icon = R.drawable.mail, title = R.string.email, data = userData?.email?:stringResource(R.string.no_information))
                when(userData){
                    is UserData.Client -> {
                        ItemProfile(
                            icon = R.drawable.id_card,
                            title = R.string.settings_identity_card_client,
                            data = userData.identityCard?:stringResource(R.string.no_information))
                        ItemProfile(
                            icon = R.drawable.bank,
                            title = R.string.settings_bank_name_client,
                            data = userData.bankName?:stringResource(R.string.no_information)
                        )
                        ItemProfile(
                            icon = R.drawable.account,
                            title = R.string.settings_count_number_bank_client,
                            data = userData.countNumberPay?:stringResource(R.string.no_information)
                        )
                        ItemProfile(
                            icon = R.drawable.account_type,
                            title = R.string.settings_account_type,
                            data = userData.accountType.name
                        )
                    }
                    is UserData.Provider -> {
                        ItemProfile(
                            icon = R.drawable.id_card,
                            title = R.string.settings_identity_card_provider,
                            data = userData.ciOrRuc?: stringResource(R.string.no_information)
                        )
                        ItemProfile(
                            icon = R.drawable.industry,
                            title = R.string.settings_industry,
                            data = userData.industry.name
                        )
                        ItemProfile(
                            icon = R.drawable.description,
                            title = R.string.company_description,
                            data = userData.companyDescription?: stringResource(R.string.no_information)
                        )
                        ItemProfile(
                            icon = R.drawable.website,
                            title = R.string.website,
                            data = userData.website?: stringResource(R.string.no_information)
                        )
                    }
                    else -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                ItemEditProfile(icon = R.drawable.delete_user, title = R.string.delete_account, data = "", iconEdit = R.drawable.delete) { onDeleteAccountClick() }
                /**
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )*/
            }
        }
    }
}