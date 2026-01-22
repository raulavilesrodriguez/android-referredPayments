package com.example.feature.home.ui

import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.domain.usecases.CurrentUserId
import com.avilesrodriguez.domain.usecases.GetUser
import com.avilesrodriguez.domain.usecases.GetUsersClient
import com.avilesrodriguez.domain.usecases.GetUsersProvider
import com.avilesrodriguez.domain.usecases.GetUsersProviderByIndustry
import com.avilesrodriguez.domain.usecases.HasUser
import com.avilesrodriguez.domain.usecases.SearchUsersClient
import com.avilesrodriguez.domain.usecases.SearchUsersProvider
import com.avilesrodriguez.domain.usecases.SignOut
import com.avilesrodriguez.presentation.navigation.NavRoutes
import com.avilesrodriguez.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val currentUserIdUseCase: CurrentUserId,
    private val hasUser: HasUser,
    private val getUser: GetUser,
    private val signOut: SignOut,
    private val getUsersProvider: GetUsersProvider,
    private val getUsersProviderByIndustry: GetUsersProviderByIndustry,
    private val searchUsersProvider: SearchUsersProvider,
    private val getUsersClient: GetUsersClient,
    private val searchUsersClient: SearchUsersClient
) : BaseViewModel() {
    private val _userDataStore = MutableStateFlow<UserData?>(null)
    val userDataStore: StateFlow<UserData?> = _userDataStore

    private val _users = MutableStateFlow<List<UserData>>(emptyList())
    val users: StateFlow<List<UserData>> = _users.asStateFlow()

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val currentUserId
        get() = currentUserIdUseCase()

    fun getUserData(){
        launchCatching {
            if(hasUser()){
                _userDataStore.value = getUser(currentUserId)
            }
        }
    }

    fun onActionClick(openScreen: (String) -> Unit, restartApp: (String) -> Unit, action: Int){
        when(ActionOptionsHome.getById(action)){
            ActionOptionsHome.POLICIES -> openScreen(NavRoutes.Policies)
            ActionOptionsHome.SIGN_OUT -> launchCatching {
                signOut()
                restartApp(NavRoutes.Splash)
            }
        }
    }

    fun editUser(openScreen: (String) -> Unit){
        openScreen(NavRoutes.EditUser)
    }
}