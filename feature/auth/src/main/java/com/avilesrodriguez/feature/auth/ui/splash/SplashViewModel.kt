package com.avilesrodriguez.feature.auth.ui.splash

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

    // Delays diferenciados para una mejor UX
    private val quickSplashDelay = 10L  // Para re-inicios rápidos (permisos, rotación)
    private val normalSplashDelay = 2500L // Para la primera carga de marca
    private var isCheckStarted = false

    val currentUserId get() = currentUserIdUseCase()

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
                // FCM
                launchCatching(snackbar = false) {
                    getAndStoreFCMToken(userId)
                }
                
                if (user?.name != null) {
                    _userDataStore.value = user
                }
                
                // Si el usuario ya está autenticado, el splash debe ser muy rápido
                waitDelay(startTime, quickSplashDelay)
                
                if (user?.name != null) {
                    _navigationEvent.send(NavRoutes.HOME to NavRoutes.SPLASH)
                } else {
                    _navigationEvent.send(NavRoutes.LOGIN to NavRoutes.SPLASH)
                }
            } else {
                val firstTime = isFirstTime()
                // Solo si es la primera vez mostramos la marca por más tiempo
                val delayToUse = if (firstTime) normalSplashDelay else quickSplashDelay
                waitDelay(startTime, delayToUse)
                
                if (firstTime) {
                    _navigationEvent.send(NavRoutes.SIGN_UP to NavRoutes.SPLASH)
                } else {
                    _navigationEvent.send(NavRoutes.LOGIN to NavRoutes.SPLASH)
                }
            }
        }
    }

    private suspend fun waitDelay(startTime: Long, duration: Long) {
        val remainingTime = duration - (System.currentTimeMillis() - startTime)
        if (remainingTime > 0) delay(remainingTime)
    }
}
