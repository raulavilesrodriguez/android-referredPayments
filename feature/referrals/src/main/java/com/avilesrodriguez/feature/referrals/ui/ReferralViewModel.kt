package com.avilesrodriguez.feature.referrals.ui

import com.avilesrodriguez.domain.model.referral.Referral
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.domain.usecases.CurrentUserId
import com.avilesrodriguez.domain.usecases.GetReferralsByClient
import com.avilesrodriguez.domain.usecases.GetReferralsByProvider
import com.avilesrodriguez.domain.usecases.GetUser
import com.avilesrodriguez.domain.usecases.HasUser
import com.avilesrodriguez.domain.usecases.SearchReferralsByClient
import com.avilesrodriguez.domain.usecases.SearchReferralsByProvider
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
class ReferralViewModel @Inject constructor(
    private val currentUserIdUseCase: CurrentUserId,
    private val hasUser: HasUser,
    private val getUser: GetUser,
    private val getReferralsByClient: GetReferralsByClient,
    private val getReferralsByProvider: GetReferralsByProvider,
    private val searchReferralsByClient: SearchReferralsByClient,
    private val searchReferralsByProvider: SearchReferralsByProvider
) : BaseViewModel(){

    private val _uiState = MutableStateFlow<List<Referral>>(emptyList())
    val uiState: StateFlow<List<Referral>> = _uiState.asStateFlow()
    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    var userData: UserData? = null
    private var searchJob: Job? = null

    val currentUserId
        get() = currentUserIdUseCase()

    init {
        launchCatching {
            if(hasUser()){
                userData = getUser(currentUserId)
            }
            _searchText
                .debounce(300)
                .distinctUntilChanged()
                .collect { query ->
                    searchJob?.cancel() // cancel previous search
                    if (query.isEmpty()) {
                        fetchAllReferrals()
                    } else {
                        performSearch(query)
                    }
                }
        }
    }

    fun updateSearchText(newText: String) {
        _searchText.value = newText
    }

    private fun fetchAllReferrals(){
        _isLoading.value = true
        searchJob = launchCatching {
            when(userData){
                is UserData.Client -> {
                    getReferralsByClient(currentUserId)
                        .collect { referrals ->
                            _uiState.value = referrals
                            _isLoading.value = false
                        }
                }
                is UserData.Provider -> {
                    getReferralsByProvider(currentUserId)
                        .collect { referrals ->
                            _uiState.value = referrals
                            _isLoading.value = false
                        }
                }
                else -> { emptyList<Referral>() }
            }
        }
        searchJob?.invokeOnCompletion { if (it is CancellationException) _isLoading.value = false }
    }

    private fun performSearch(query: String){
        _isLoading.value = true
        searchJob = launchCatching {
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
        searchJob?.invokeOnCompletion { if (it is CancellationException) _isLoading.value = false }
    }

}