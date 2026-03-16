package com.avilesrodriguez.feature.auth.ui.splash

import android.util.Log
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.domain.usecases.CurrentUserId
import com.avilesrodriguez.domain.usecases.GetAndStoreFCMToken
import com.avilesrodriguez.domain.usecases.GetUser
import com.avilesrodriguez.domain.usecases.HasUser
import com.avilesrodriguez.domain.usecases.IsFirstTime
import com.avilesrodriguez.presentation.navigation.NavRoutes
import com.avilesrodriguez.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val currentUserIdUseCase: CurrentUserId,
    private val hasUser: HasUser,
    private val getUser: GetUser,
    private val isFirstTime: IsFirstTime,
    private val getAndStoreFCMToken: GetAndStoreFCMToken
) : BaseViewModel() {

    private val _userDataStore = MutableStateFlow<UserData?>(null)
    val userDataStore: StateFlow<UserData?> = _userDataStore
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Canal para eventos de navegación (solo se consumen una vez)
    private val _navigationEvent = Channel<Pair<String, String>>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    private val splashDelay = 3000L
    private var isCheckStarted = false

    val currentUserId
        get() = currentUserIdUseCase()

    fun alreadyLoggedIn() {
        // Evita que se ejecute de nuevo al rotar
        if (isCheckStarted) return
        isCheckStarted = true

        launchCatching {
            _isLoading.value = true
            val startTime = System.currentTimeMillis()
            
            if (hasUser()) {
                val userId = currentUserId
                val user = if (userId.isNotEmpty()) getUser(userId) else null

                // FCM: Intentamos sincronizar el token una vez.
                launchCatching(snackbar = false) {
                    val wasUpdated = getAndStoreFCMToken(userId)
                    if (wasUpdated) {
                        Log.d("FCM_TOKEN_UPDATE", "FCM Token sincronizado con éxito: $userId")
                    } else {
                        Log.d("FCM_TOKEN_UPDATE", "No fue necesario actualizar el token (ya estaba al día)")
                    }
                }
                
                if (user?.name != null) {
                    _userDataStore.value = user
                }
                
                waitDelay(startTime)
                
                if (user?.name != null) {
                    _navigationEvent.send(NavRoutes.HOME to NavRoutes.SPLASH)
                } else {
                    _navigationEvent.send(NavRoutes.LOGIN to NavRoutes.SPLASH)
                }
            } else {
                val firstTime = isFirstTime()
                waitDelay(startTime)
                
                if (firstTime) {
                    _navigationEvent.send(NavRoutes.SIGN_UP to NavRoutes.SPLASH)
                } else {
                    _navigationEvent.send(NavRoutes.LOGIN to NavRoutes.SPLASH)
                }
            }
        }
    }

    private suspend fun waitDelay(startTime: Long) {
        val remainingTime = splashDelay - (System.currentTimeMillis() - startTime)
        if (remainingTime > 0) delay(remainingTime)
    }
}
