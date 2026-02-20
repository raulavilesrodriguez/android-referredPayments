package com.avilesrodriguez.feature.referrals.ui.referrals

import com.avilesrodriguez.domain.ext.normalizeName
import com.avilesrodriguez.domain.model.referral.Referral
import com.avilesrodriguez.domain.model.referral.ReferralWithNames
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
    private val _allReferrals = MutableStateFlow<List<ReferralWithNames>>(emptyList())
    private val _uiState = MutableStateFlow<List<ReferralWithNames>>(emptyList())
    val uiState: StateFlow<List<ReferralWithNames>> = _uiState.asStateFlow()
    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _userDataStore = MutableStateFlow<UserData?>(null)
    val userDataStore: StateFlow<UserData?> = _userDataStore

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
                val referralsWithNames = referrals.map { referral ->
                    val otherId = if (userData is UserData.Client) referral.providerId else referral.clientId
                    val otherUser = getUser(otherId) // Obtenemos el usuario (puedes usar cache en el Repository)
                    ReferralWithNames(
                        referral = referral,
                        otherPartyName = otherUser?.name ?: ""
                    )
                }
                _allReferrals.value = referralsWithNames
                //si habia algo escrito
                filterReferralsLocally(_searchText.value)
                _isLoading.value = false
            }
        }
        referralsJob?.invokeOnCompletion { if (it is CancellationException) _isLoading.value = false }
    }

    private fun filterReferralsLocally(query: String) {
        val queryNormalized = query.normalizeName()
        val allEnriched = _allReferrals.value
        if (queryNormalized.isEmpty()) {
            _uiState.value = allEnriched
        } else {
            // Busca en cualquier parte del nombre (contains)
            _uiState.value = _allReferrals.value.filter { item ->
                item.referral.nameLowercase.contains(queryNormalized)
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
        val route = NavRoutes.REFERRAL_DETAIL.replace("{${NavRoutes.ReferralArgs.ID}}", referral.id)
        openScreen(route)
    }

}