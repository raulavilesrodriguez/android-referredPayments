package com.avilesrodriguez.winapp.ui.navigation

import android.content.res.Resources
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import com.avilesrodriguez.feature.auth.ui.login.LoginScreen
import com.avilesrodriguez.feature.auth.ui.sign_up.SignUpScreen
import com.avilesrodriguez.feature.auth.ui.splash.SplashScreen
import com.avilesrodriguez.feature.referrals.ui.ReferralDetailScreen
import com.avilesrodriguez.feature.referrals.ui.ReferralViewModel
import com.avilesrodriguez.feature.settings.ui.EditScreen
import com.avilesrodriguez.presentation.navigation.AppState
import com.avilesrodriguez.presentation.navigation.DeepLinks
import com.avilesrodriguez.presentation.navigation.NavRoutes
import com.avilesrodriguez.presentation.snackbar.SnackbarManager
import com.avilesrodriguez.winapp.ui.theme.WinAppTheme
import com.example.feature.home.ui.HomeScreen
import com.example.feature.home.ui.PoliciesScreen
import kotlinx.coroutines.CoroutineScope


@Composable
fun MainNavigation(){
    WinAppTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            val appState = rememberAppState()

            Scaffold(
                snackbarHost = {
                    SnackbarHost(
                        hostState = appState.snackbarHostState,
                        modifier = Modifier.padding(32.dp),
                        snackbar = { snackbarData ->
                            Snackbar(
                                snackbarData,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            )
                        }
                    )
                }
            ) { innerPadding ->
                NavHost(
                    navController = appState.navController,
                    startDestination = NavRoutes.Splash,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    addSplash(appState)
                    addSignUp(appState)
                    addLogin(appState)
                    addHome(appState)
                    addPolicies(appState)
                    addEditUser(appState)
                }
            }
        }
    }
}

@Composable
fun rememberAppState(
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    navController: NavHostController = rememberNavController(),
    snackbarManager: SnackbarManager = SnackbarManager,
    resources: Resources = resources(),
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) =
    remember(snackbarHostState, navController, snackbarManager, resources,coroutineScope){
        AppState(snackbarHostState, navController, snackbarManager, resources, coroutineScope)
    }

@Composable
@ReadOnlyComposable
fun resources(): Resources {
    LocalConfiguration.current
    return LocalResources.current
}

private fun NavGraphBuilder.addSplash(appState: AppState) {
    composable(NavRoutes.Splash) {
        SplashScreen(
            openAndPopUp = {route, popUp -> appState.navigateAndPopUp(route, popUp)}
        )
    }
}

private fun NavGraphBuilder.addSignUp(appState: AppState){
    composable(NavRoutes.SignUp) {
        SignUpScreen(
            openAndPopUp = {route, popUp -> appState.navigateAndPopUp(route, popUp)}
        )
    }
}

private fun NavGraphBuilder.addLogin(appState: AppState){
    composable(NavRoutes.Login) {
        LoginScreen(
            openAndPopUp = {route, popUp -> appState.navigateAndPopUp(route, popUp)}
        )
    }
}

private fun NavGraphBuilder.addHome(appState: AppState){
    composable(NavRoutes.Home) {
        HomeScreen(
            openScreen = {route -> appState.navigate(route)},
            restartApp = {route -> appState.clearAndNavigate(route)}
        )
    }
}

private fun NavGraphBuilder.addPolicies(appState: AppState){
    composable(NavRoutes.Policies) {
        PoliciesScreen(
            popUp = {appState.popUp()}
        )
    }
}

private fun NavGraphBuilder.addEditUser(appState: AppState) {
    composable(NavRoutes.EditUser) {
        EditScreen(
            popUp = { appState.popUp() }
        )
    }
}

private fun NavGraphBuilder.referralGraph(appState: AppState){
    navigation(
        startDestination = NavRoutes.REFERRAL_DETAIL,
        route = NavRoutes.REFERRAL_GRAPH
    ){
        composable(
            route = NavRoutes.REFERRAL_DETAIL,
            arguments = listOf(navArgument(NavRoutes.ReferralArgs.ID) { type = NavType.StringType }),
            deepLinks = listOf(navDeepLink { uriPattern = DeepLinks.REFERRAL_ROUTE })
        ) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                //Le pedimos explícitamente al NavController que nos dé el NavBackStackEntry que
                // corresponde a la ruta de nuestro grafo anidado
                appState.navController.getBackStackEntry(NavRoutes.REFERRAL_GRAPH)
            }
            //Le pasamos esa entrada del grafo padre a hiltViewModel. Esto le dice a Hilt:
            // "El ciclo de vida de este ViewModel no está atado a la pantalla actual,
            // sino al grafo de navegación padre"
            val viewModel: ReferralViewModel = hiltViewModel(parentEntry)
            val referralId = backStackEntry.arguments?.getString(NavRoutes.ReferralArgs.ID)
            ReferralDetailScreen(
                referralId = referralId,
                onBackClick = { appState.popUp() },
                openScreen = { route -> appState.navigate(route) },
                viewModel = viewModel
            )

        }
    }
}