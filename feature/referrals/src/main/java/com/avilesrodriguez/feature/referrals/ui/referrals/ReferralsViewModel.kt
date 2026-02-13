package com.avilesrodriguez.feature.referrals.ui.referrals

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.avilesrodriguez.domain.ext.normalizeName
import com.avilesrodriguez.domain.model.referral.Referral
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.domain.usecases.CurrentUserId
import com.avilesrodriguez.domain.usecases.GetReferralsByClient
import com.avilesrodriguez.domain.usecases.GetReferralsByProvider
import com.avilesrodriguez.domain.usecases.GetUser
import com.avilesrodriguez.domain.usecases.HasUser
import com.avilesrodriguez.presentation.navigation.NavRoutes
import com.avilesrodriguez.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@OptIn(FlowPreview::class)
@HiltViewModel
class ReferralsViewModel @Inject constructor(
    private val currentUserIdUseCase: CurrentUserId,
    private val hasUser: HasUser,
    private val getUser: GetUser,
    private val getReferralsByClient: GetReferralsByClient,
    private val getReferralsByProvider: GetReferralsByProvider
) : BaseViewModel(){
    private val _allReferrals = MutableStateFlow<List<Referral>>(emptyList())
    private val _uiState = MutableStateFlow<List<Referral>>(emptyList())
    val uiState: StateFlow<List<Referral>> = _uiState.asStateFlow()
    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _userDataStore = MutableStateFlow<UserData?>(null)
    val userDataStore: StateFlow<UserData?> = _userDataStore
    var clientWhoReferred by mutableStateOf<UserData?>(null)
    var providerThatReceived by mutableStateOf<UserData?>(null)

    private var referralsJob: Job? = null

    val currentUserId
        get() = currentUserIdUseCase()

    init {
        launchCatching {
            if(hasUser()){
                _userDataStore.value = getUser(currentUserId)
                fetchAllReferrals()
            }
            _searchText
                .debounce(300)
                .distinctUntilChanged()
                .collect { query ->
                    filterReferralsLocally(query)
                }
        }
    }

    fun updateSearchText(newText: String) {
        _searchText.value = newText
    }

    private fun fetchAllReferrals(){
        _isLoading.value = true
        referralsJob?.cancel()
        referralsJob =launchCatching {
            val userData = _userDataStore.value
            val flow = when(userData) {
                is UserData.Client -> getReferralsByClient(currentUserId)
                is UserData.Provider -> getReferralsByProvider(currentUserId)
                else -> null
            }

            flow?.collect { referrals ->
                _allReferrals.value = referrals
                //si habia algo escrito
                filterReferralsLocally(_searchText.value)
                _isLoading.value = false
            }
        }
        referralsJob?.invokeOnCompletion { if (it is CancellationException) _isLoading.value = false }
    }

    private fun filterReferralsLocally(query: String) {
        val queryNormalized = query.normalizeName()
        if (queryNormalized.isEmpty()) {
            _uiState.value = _allReferrals.value
        } else {
            // Busca en cualquier parte del nombre (contains)
            _uiState.value = _allReferrals.value.filter { referral ->
                referral.nameLowercase.contains(queryNormalized)
            }
        }
    }

    /**
    private fun performSearch(query: String){
        _isLoading.value = true
        referralsJob = launchCatching {
            val userData = _userDataStore.value
            when(userData){
                is UserData.Client -> {
                    searchReferralsByClient(query, currentUserId)
                        .collect { referrals ->
                            _uiState.value = referrals
                            _isLoading.value = false
                        }
                }
                is UserData.Provider -> {
                    searchReferralsByProvider(query, currentUserId)
                        .collect { referrals ->
                            _uiState.value = referrals
                            _isLoading.value = false
                        }
                }
                else -> { emptyList<Referral>() }
            }
        }
        referralsJob?.invokeOnCompletion { if (it is CancellationException) _isLoading.value = false }
    } */

    fun onReferralClick(referral: Referral, openScreen: (String) -> Unit) {
        launchCatching {
            clientWhoReferred = getUser(referral.clientId)
            providerThatReceived = getUser(referral.providerId)
        }
        val route = NavRoutes.REFERRAL_DETAIL.replace("{${NavRoutes.ReferralArgs.ID}}", referral.id)
        openScreen(route)
    }

}