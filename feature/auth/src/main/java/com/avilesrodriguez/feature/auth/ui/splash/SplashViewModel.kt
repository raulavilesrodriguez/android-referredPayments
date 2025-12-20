package com.avilesrodriguez.feature.auth.ui.splash

import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.domain.usecases.CurrentUserId
import com.avilesrodriguez.domain.usecases.GetUser
import com.avilesrodriguez.domain.usecases.HasUser
import com.avilesrodriguez.presentation.navigation.NavRoutes
import com.avilesrodriguez.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val currentUserIdUseCase: CurrentUserId,
    private val hasUser: HasUser,
    private val getUser: GetUser
) : BaseViewModel() {

    private val _userDataStore = MutableStateFlow<UserData?>(null)
    val userDataStore: StateFlow<UserData?> = _userDataStore

    val currentUserId
        get() = currentUserIdUseCase()

    fun getUserData(){
        launchCatching {
            if(hasUser()){
                _userDataStore.value = getUser(currentUserId)
            }
        }
    }

    fun alreadyLoggedIn(openAndPopUp: (String, String) -> Unit){
        launchCatching {
            val user = getUser(currentUserId)
            if (user?.name !=null){
                openAndPopUp(NavRoutes.Home, NavRoutes.Splash)
            } else {
                openAndPopUp(NavRoutes.Login, NavRoutes.Splash)
            }
        }
    }

}