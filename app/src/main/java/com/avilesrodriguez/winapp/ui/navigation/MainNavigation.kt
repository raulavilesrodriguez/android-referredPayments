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
import androidx.compose.runtime.LaunchedEffect
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
import com.avilesrodriguez.feature.messages.ui.messages.MessagesScreen
import com.avilesrodriguez.feature.messages.ui.newMessage.NewMessage
import com.avilesrodriguez.feature.messages.ui.newMessage.NewMessageViewModel
import com.avilesrodriguez.feature.messages.ui.newMessage.PayReferral
import com.avilesrodriguez.feature.referrals.ui.addReferral.AddReferralScreen
import com.avilesrodriguez.feature.referrals.ui.referral.EditEmailReferral
import com.avilesrodriguez.feature.referrals.ui.referral.EditNameReferral
import com.avilesrodriguez.feature.referrals.ui.referral.EditPhoneReferral
import com.avilesrodriguez.feature.referrals.ui.referral.ReferralScreen
import com.avilesrodriguez.feature.referrals.ui.referral.ReferralViewModel
import com.avilesrodriguez.feature.settings.ui.EditScreen
import com.avilesrodriguez.presentation.navigation.AppState
import com.avilesrodriguez.presentation.navigation.DeepLinks
import com.avilesrodriguez.presentation.navigation.NavRoutes
import com.avilesrodriguez.presentation.snackbar.SnackbarManager
import com.avilesrodriguez.winapp.ui.theme.WinAppTheme
import com.example.feature.home.ui.HomeScreen
import com.example.feature.home.ui.PoliciesScreen
import com.example.feature.home.ui.details.DetailScreenUser
import kotlinx.coroutines.CoroutineScope


@Composable
fun MainNavigation(sharedFileUri: String? = null){
    WinAppTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            val appState = rememberAppState()

            // React to share file
            LaunchedEffect(sharedFileUri) {
                sharedFileUri?.let { uri ->
                    // CODIFICAR la URI para que sea segura en la ruta
                    val encodedUri = java.net.URLEncoder.encode(uri, "UTF-8")
                    appState.navigate("${NavRoutes.PAY_REFERRAL}?sharedUri=$encodedUri")
                }
            }

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
                    referralGraph(appState)
                    addDetailUser(appState)
                    addNewReferral(appState)
                    addMessages(appState)
                    newMessageGraph(appState)
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
            deepLinks = listOf(navDeepLink { uriPattern = DeepLinks.REFERRAL_URL })
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
            ReferralScreen(
                referralId = referralId,
                onBackClick = { appState.popUp() },
                openScreen = { route -> appState.navigate(route) },
                viewModel = viewModel
            )
        }
        composable(
            route = NavRoutes.EDIT_NAME_REFERRAL
        ){ backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                appState.navController.getBackStackEntry(NavRoutes.REFERRAL_GRAPH)
            }
            val viewModel: ReferralViewModel = hiltViewModel(parentEntry)
            EditNameReferral(
                onBackClick = { appState.popUp() },
                viewModel = viewModel
            )
        }
        composable(
            route = NavRoutes.EDIT_EMAIL_REFERRAL
        ){ backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                appState.navController.getBackStackEntry(NavRoutes.REFERRAL_GRAPH)
            }
            val viewModel: ReferralViewModel = hiltViewModel(parentEntry)
            EditEmailReferral(
                onBackClick = { appState.popUp() },
                viewModel = viewModel
            )
        }
        composable(
            route = NavRoutes.EDIT_PHONE_REFERRAL
        ){ backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                appState.navController.getBackStackEntry(NavRoutes.REFERRAL_GRAPH)
            }
            val viewModel: ReferralViewModel = hiltViewModel(parentEntry)
            EditPhoneReferral(
                onBackClick = { appState.popUp() },
                viewModel = viewModel
            )
        }
    }
}

private fun NavGraphBuilder.addDetailUser(appState: AppState){
    composable(
        route = NavRoutes.USER_DETAIL,
        arguments = listOf(navArgument(NavRoutes.UserArgs.ID){type = NavType.StringType})
    ){ backStackEntry ->
        val userId = backStackEntry.arguments?.getString(NavRoutes.UserArgs.ID)
        DetailScreenUser(
            uId = userId,
            popUp = { appState.popUp() },
            openScreen = { route -> appState.navigate(route) }
        )
    }
}

private fun NavGraphBuilder.addNewReferral(appState: AppState){
    composable(
        route = NavRoutes.NEW_REFERRAL,
        arguments = listOf(navArgument(NavRoutes.UserArgs.ID){type = NavType.StringType})
    ) { backStackEntry ->
        val userId = backStackEntry.arguments?.getString(NavRoutes.UserArgs.ID)
        AddReferralScreen(
            providerId = userId,
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

private fun NavGraphBuilder.newMessageGraph(appState: AppState){
    navigation(
        startDestination = NavRoutes.NEW_MESSAGE,
        route = NavRoutes.NEW_MESSAGE_GRAPH
    ){
        composable(
            route = NavRoutes.NEW_MESSAGE,
            arguments = listOf(navArgument(NavRoutes.ReferralArgs.ID) { type = NavType.StringType }),
        ){ backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                appState.navController.getBackStackEntry(NavRoutes.NEW_MESSAGE_GRAPH)
            }
            val viewModel: NewMessageViewModel = hiltViewModel(parentEntry)
            val referralId = backStackEntry.arguments?.getString(NavRoutes.ReferralArgs.ID)
            NewMessage(
                referralId = referralId,
                onBackClick = { appState.popUp() },
                openScreen = { route -> appState.navigate(route) },
                viewModel = viewModel
            )
        }
        composable(
            route = NavRoutes.PAY_REFERRAL,
            arguments = listOf(
                navArgument("sharedUri") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ){ backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                appState.navController.getBackStackEntry(NavRoutes.NEW_MESSAGE_GRAPH)
            }
            val viewModel: NewMessageViewModel = hiltViewModel(parentEntry)
            // Extraer la URI de los argumentos
            val sharedUri = backStackEntry.arguments?.getString("sharedUri")
            PayReferral(
                sharedUri = sharedUri,
                onBackClick = { appState.popUp() },
                openAndPopUp = {route, popUp -> appState.navigateAndPopUp(route, popUp)},
                viewModel = viewModel
            )
        }
    }
}