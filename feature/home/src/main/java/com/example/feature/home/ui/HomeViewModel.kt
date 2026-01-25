package com.example.feature.home.ui

import com.avilesrodriguez.domain.model.industries.IndustriesType
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.domain.usecases.CurrentUserId
import com.avilesrodriguez.domain.usecases.GetUser
import com.avilesrodriguez.domain.usecases.HasUser
import com.avilesrodriguez.domain.usecases.SearchUsersClient
import com.avilesrodriguez.domain.usecases.SearchUsersProvider
import com.avilesrodriguez.domain.usecases.SignOut
import com.avilesrodriguez.presentation.ext.normalizeName
import com.avilesrodriguez.presentation.industries.getById
import com.avilesrodriguez.presentation.navigation.NavRoutes
import com.avilesrodriguez.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(FlowPreview::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val currentUserIdUseCase: CurrentUserId,
    private val hasUser: HasUser,
    private val getUser: GetUser,
    private val signOut: SignOut,
    private val searchUsersProvider: SearchUsersProvider,
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

    private val _selectedIndustry = MutableStateFlow<IndustriesType?>(null)
    val selectedIndustry: StateFlow<IndustriesType?> = _selectedIndustry.asStateFlow()

    private var searchJob: Job? = null

    val currentUserId
        get() = currentUserIdUseCase()

    init{
        launchCatching {
            if(hasUser()){
                _userDataStore.value = getUser(currentUserId)
            }
            combine(_searchText, _selectedIndustry){ text, industry ->
                Pair(text, industry)
            }
                .debounce(300)
                .distinctUntilChanged()
                .collect { (query, industry) ->
                    searchJob?.cancel() // cancel previous search
                    loadData(query.normalizeName(), industry)
                }
        }
    }

    fun updateSearchText(newText: String) {
        _searchText.value = newText
    }

    fun onIndustryChange(industry: Int){
        val filteredNameIndustry = IndustriesType.getById(industry)
        _selectedIndustry.value = filteredNameIndustry
    }

    private fun loadData(query: String, industry: IndustriesType?){
        _isLoading.value = true
        searchJob = launchCatching {
            val user = _userDataStore.value
            when(user){
                is UserData.Client -> {
                    searchUsersProvider(query, industry?.name)
                        .collect { users ->
                            _users.value = users
                            _isLoading.value = false
                        }
                }
                is UserData.Provider -> {
                    searchUsersClient(query, currentUserId)
                        .collect { users ->
                            _users.value = users
                            _isLoading.value = false
                        }
                }
                else -> {emptyList<UserData>()}
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