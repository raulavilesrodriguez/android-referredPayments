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
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.avilesrodriguez.feature.auth.ui.login.LoginScreen
import com.avilesrodriguez.feature.auth.ui.sign_up.SignUpScreen
import com.avilesrodriguez.feature.auth.ui.splash.SplashScreen
import com.avilesrodriguez.feature.messages.ui.messages.MessagesScreen
import com.avilesrodriguez.feature.referrals.ui.addReferral.AddReferralScreen
import com.avilesrodriguez.feature.referrals.ui.referral.EditEmailReferral
import com.avilesrodriguez.feature.referrals.ui.referral.EditNameReferral
import com.avilesrodriguez.feature.referrals.ui.referral.EditPhoneReferral
import com.avilesrodriguez.feature.referrals.ui.referrals.ReferralsScreen
import com.avilesrodriguez.feature.settings.ui.SettingsScreen
import com.avilesrodriguez.presentation.navigation.AppState
import com.avilesrodriguez.presentation.navigation.DeepLinks
import com.avilesrodriguez.presentation.navigation.NavRoutes
import com.avilesrodriguez.presentation.snackbar.SnackbarManager
import com.avilesrodriguez.winapp.ui.theme.WinAppTheme
import com.example.feature.home.ui.HomeScreen
import com.example.feature.home.ui.PoliciesScreen
import com.example.feature.home.ui.products.editProduct.EditProductScreen
import kotlinx.coroutines.CoroutineScope


@Composable
fun MainNavigation(){
    WinAppTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            val appState = rememberAppState()

            Scaffold(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
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
                    startDestination = NavRoutes.SPLASH,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    addSplash(appState)
                    addSignUp(appState)
                    addLogin(appState)
                    addHome(appState)
                    referralsNavigation(appState)
                    settingsNavigation(appState)
                    addPolicies(appState)
                    editNameReferralNavigation(appState)
                    editEmailReferralNavigation(appState)
                    editPhoneReferralNavigation(appState)
                    addNewReferral(appState)
                    addMessages(appState)
                    editProductNavigation(appState)
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
    composable(NavRoutes.SPLASH) {
        SplashScreen(
            openAndPopUp = {route, popUp -> appState.navigateAndPopUp(route, popUp)}
        )
    }
}

private fun NavGraphBuilder.addSignUp(appState: AppState){
    composable(NavRoutes.SIGN_UP) {
        SignUpScreen(
            openAndPopUp = {route, popUp -> appState.navigateAndPopUp(route, popUp)}
        )
    }
}

private fun NavGraphBuilder.addLogin(appState: AppState){
    composable(NavRoutes.LOGIN) {
        LoginScreen(
            openAndPopUp = {route, popUp -> appState.navigateAndPopUp(route, popUp)}
        )
    }
}

private fun NavGraphBuilder.addHome(appState: AppState){
    composable(NavRoutes.HOME) {
        HomeScreen(
            openScreen = {route -> appState.navigate(route)},
            restartApp = {route -> appState.clearAndNavigate(route)}
        )
    }
}

private fun NavGraphBuilder.referralsNavigation(appState: AppState){
    composable(
        route = NavRoutes.REFERRALS_ROUTE,
        arguments = listOf(
            navArgument(NavRoutes.ReferralsArgs.ID) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        ),
        deepLinks = listOf(navDeepLink { uriPattern = DeepLinks.REFERRAL_URL })
    ) { backStackEntry ->
        val referralId = backStackEntry.arguments?.getString(NavRoutes.ReferralsArgs.ID)
        ReferralsScreen(
            referralId = referralId,
            openScreen = { route -> appState.navigate(route) },
            restartApp = {route -> appState.clearAndNavigate(route)}
        )
    }
}

private fun NavGraphBuilder.settingsNavigation(appState: AppState){
    composable(NavRoutes.SETTINGS) {
        SettingsScreen(
            openScreen = { route -> appState.navigate(route) },
            restartApp = {route -> appState.clearAndNavigate(route)}
        )
    }
}

private fun NavGraphBuilder.addPolicies(appState: AppState){
    composable(NavRoutes.POLICIES) {
        PoliciesScreen(
            popUp = {appState.popUp()}
        )
    }
}

private fun NavGraphBuilder.editNameReferralNavigation(appState: AppState){
    composable(
        route = NavRoutes.EDIT_NAME_REFERRAL,
        arguments = listOf(navArgument(NavRoutes.ReferralArgs.ID){type = NavType.StringType})
    ){ backStackEntry ->
        val referralId = backStackEntry.arguments?.getString(NavRoutes.ReferralArgs.ID)
        EditNameReferral(
            referralId = referralId,
            onBackClick = { appState.popUp() }
        )
    }
}

private fun NavGraphBuilder.editEmailReferralNavigation(appState: AppState){
    composable(
        route = NavRoutes.EDIT_EMAIL_REFERRAL,
        arguments = listOf(navArgument(NavRoutes.ReferralArgs.ID){type = NavType.StringType})
    ){ backStackEntry ->
        val referralId = backStackEntry.arguments?.getString(NavRoutes.ReferralArgs.ID)
        EditEmailReferral(
            referralId = referralId,
            onBackClick = { appState.popUp() }
        )

    }
}

private fun NavGraphBuilder.editPhoneReferralNavigation(appState: AppState){
    composable(
        route = NavRoutes.EDIT_PHONE_REFERRAL,
        arguments = listOf(navArgument(NavRoutes.ReferralArgs.ID){type = NavType.StringType})
    ){ backStackEntry ->
        val referralId = backStackEntry.arguments?.getString(NavRoutes.ReferralArgs.ID)
        EditPhoneReferral(
            referralId = referralId,
            onBackClick = { appState.popUp() }
        )
    }
}

private fun NavGraphBuilder.addNewReferral(appState: AppState){
    composable(
        route = NavRoutes.NEW_REFERRAL,
        arguments = listOf(
            navArgument(NavRoutes.UserArgs.ID){type = NavType.StringType},
            navArgument(NavRoutes.ProductArgs.ID){
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) { backStackEntry ->
        val providerId = backStackEntry.arguments?.getString(NavRoutes.UserArgs.ID)
        val productId = backStackEntry.arguments?.getString(NavRoutes.ProductArgs.ID)
        AddReferralScreen(
            providerId = providerId,
            productId = productId,
            openAndPopUp = {route, popUp -> appState.navigateAndPopUp(route, popUp)},
            onBackClick = { appState.popUp() }
        )
    }
}

private fun NavGraphBuilder.addMessages(appState: AppState){
    composable(
        route = NavRoutes.MESSAGES_SCREEN,
        arguments = listOf(navArgument(NavRoutes.ReferralArgs.ID) { type = NavType.StringType }),
    ){ backStackEntry ->
        val referralId = backStackEntry.arguments?.getString(NavRoutes.ReferralArgs.ID)
        MessagesScreen(
            referralId = referralId,
            onBackClick = { appState.popUp() },
            openScreen = { route -> appState.navigate(route) }
        )
    }
}

private fun NavGraphBuilder.editProductNavigation(appState: AppState){
    composable(
        route = NavRoutes.EDIT_PRODUCT,
        arguments = listOf(navArgument(NavRoutes.ProductArgs.ID){type = NavType.StringType})
    ){ backStackEntry ->
        val productId = backStackEntry.arguments?.getString(NavRoutes.ProductArgs.ID)
        EditProductScreen(
            productId = productId,
            onBackClick = { appState.popUp() }
        )
    }
}