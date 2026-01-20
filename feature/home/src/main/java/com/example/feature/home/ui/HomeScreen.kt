package com.example.feature.home.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.domain.model.user.UserType
import com.avilesrodriguez.feature.referrals.ui.referrals.ReferralsScreen
import com.avilesrodriguez.feature.settings.ui.SettingsScreen
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.composables.DropdownContextMenu
import com.example.feature.home.models.StartListTab
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    openScreen: (String) -> Unit,
    restartApp: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
){
    LaunchedEffect(Unit) {
        viewModel.getUserData()
    }
    val userData by viewModel.userDataStore.collectAsState()
    val options = ActionOptionsHome.getOptions()

    HomeScreenContent(
        user = userData,
        options = options,
        onActionClick = { action ->
            viewModel.onActionClick(openScreen, restartApp, action)
        },
        openScreen = openScreen,
        restartApp = restartApp,
        onEditClick = { viewModel.editUser(openScreen) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    user: UserData?,
    options: List<Int>,
    onActionClick: (Int) -> Unit,
    openScreen: (String) -> Unit,
    restartApp: (String) -> Unit,
    onEditClick: () -> Unit
){
    val tabs = generateTabs()
    val pagerState = rememberPagerState(1){tabs.size}

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing, // para que no se ponga encima de la parte superior del movil
        topBar = {
            TopAppBar(
                title = {
                    when (pagerState.currentPage) {
                        0 -> {
                            Text(stringResource(R.string.referrals))
                        }
                        1 -> {
                            Text(stringResource(R.string.app_name_presentation))
                        }
                        2 -> {
                            Text(stringResource(R.string.profile))
                        }
                    }
                },
                actions = {
                    DropdownContextMenu(
                        options = options,
                        modifier = Modifier
                    ) { action -> onActionClick(action) }
                }
            )
        },
        bottomBar = {
            val coroutineScope = rememberCoroutineScope()

            PrimaryTabRow(
                selectedTabIndex = pagerState.currentPage,
                modifier = Modifier.navigationBarsPadding()  //para que no se ponga encima de la parte inferior del movil
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        text = {Text(stringResource(tab.title))},
                        selected = index == pagerState.currentPage,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            if (pagerState.currentPage == 2) {
                FloatingActionButton(
                    containerColor = MaterialTheme.colorScheme.primary,
                    onClick = onEditClick
                ) {
                    Icon(
                        painter = painterResource(R.drawable.edit),
                        contentDescription = stringResource(R.string.edit)
                    )
                }
            }
        },
        content = { innerPadding ->
            HorizontalPager(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                state = pagerState,
                userScrollEnabled = true
            ) { index ->
                when(index){
                    0 -> {
                        ReferralsScreen(
                            openScreen = openScreen
                        )
                    }
                    1 -> {
                        HomeMainContent(user)
                    }
                    2 -> {
                        SettingsScreen(
                            openScreen = openScreen,
                            restartApp = restartApp
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun HomeMainContent(user: UserData?) {
    if (user != null) {
        when (user.type) {
            UserType.CLIENT -> HomeScreenClient(user = user)
            UserType.PROVIDER -> HomeScreenProvider(user = user)
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