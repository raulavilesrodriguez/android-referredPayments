package com.avilesrodriguez.feature.auth.ui.splash

import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.domain.usecases.CurrentUserId
import com.avilesrodriguez.domain.usecases.GetUser
import com.avilesrodriguez.domain.usecases.HasUser
import com.avilesrodriguez.domain.usecases.IsFirstTime
import com.avilesrodriguez.presentation.navigation.NavRoutes
import com.avilesrodriguez.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val currentUserIdUseCase: CurrentUserId,
    private val hasUser: HasUser,
    private val getUser: GetUser,
    private val isFirstTime: IsFirstTime
) : BaseViewModel() {

    private val _userDataStore = MutableStateFlow<UserData?>(null)
    val userDataStore: StateFlow<UserData?> = _userDataStore

    private val delay = 3000L // 2 seconds

    val currentUserId
        get() = currentUserIdUseCase()

    fun alreadyLoggedIn(
        openAndPopUp: (String, String) -> Unit
    ){
        launchCatching {
            val startTime = System.currentTimeMillis()
            if(hasUser()){
                val userId = currentUserId
                val user = if (userId.isNotEmpty()) getUser(userId) else null
                if (user?.name != null) {
                    _userDataStore.value = user
                }
                waitDelay(startTime)
                if (user?.name !=null){
                    openAndPopUp(NavRoutes.HOME, NavRoutes.SPLASH)
                } else {
                    openAndPopUp(NavRoutes.LOGIN, NavRoutes.SPLASH)
                }
            } else {
                val firstTime = isFirstTime()
                waitDelay(startTime)
                if(firstTime){
                    openAndPopUp(NavRoutes.SIGN_UP, NavRoutes.SPLASH)
                } else {
                    openAndPopUp(NavRoutes.LOGIN, NavRoutes.SPLASH)
                }
            }
        }
    }

    private suspend fun waitDelay(startTime: Long) {
        val remainingTime = delay - (System.currentTimeMillis() - startTime)
        if (remainingTime > 0) delay(remainingTime)
    }

}