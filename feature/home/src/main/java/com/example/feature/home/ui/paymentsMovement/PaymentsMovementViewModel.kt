package com.example.feature.home.ui.paymentsMovement

import com.avilesrodriguez.domain.model.referral.ReferralWithNames
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.domain.usecases.CurrentUserId
import com.avilesrodriguez.domain.usecases.GetReferralsByClient
import com.avilesrodriguez.domain.usecases.GetReferralsByProvider
import com.avilesrodriguez.domain.usecases.GetUser
import com.avilesrodriguez.domain.usecases.GetUserFlow
import com.avilesrodriguez.domain.usecases.HasUser
import com.avilesrodriguez.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

@HiltViewModel
class PaymentsMovementViewModel @Inject constructor(
    private val currentUserIdUseCase: CurrentUserId,
    private val hasUser: HasUser,
    private val getReferralsByProvider: GetReferralsByProvider,
    private val getReferralsByClient: GetReferralsByClient,
    private val getUserFlow: GetUserFlow,
    private val getUser: GetUser
): BaseViewModel() {
    private val _userDataStore = MutableStateFlow<UserData?>(null)
    val userDataStore: StateFlow<UserData?> = _userDataStore
    private val _referralsProvider = MutableStateFlow<List<ReferralWithNames>>(emptyList())
    val referralsProvider: StateFlow<List<ReferralWithNames>> = _referralsProvider
    private val _referralsClient = MutableStateFlow<List<ReferralWithNames>>(emptyList())
    val referralsClient: StateFlow<List<ReferralWithNames>> = _referralsClient
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private var referralsJob: Job? = null

    // Caché of names
    private val nameCache = ConcurrentHashMap<String, String>()

    val currentUserId
        get() = currentUserIdUseCase()

    init {
        launchCatching{
            if(hasUser()){
                launch {
                    getUserFlow(currentUserId).collect {
                        _userDataStore.value = it
                    }
                }
            }
            val user = _userDataStore.filterNotNull().first()
            when(user){
                is UserData.Provider ->{
                    loadReferralsByProvider()
                }
                is UserData.Client -> {
                    loadReferralsByClient()
                }
            }
        }
    }

    private fun loadReferralsByProvider() {
        _isLoading.value = true
        referralsJob?.cancel()
        referralsJob = launchCatching {
            getReferralsByProvider(currentUserId)
                .collect { referrals ->
                    // 1. Identificar IDs de clientes únicos que no están en caché
                    val uniqueIds = referrals.map { it.clientId }.distinct()
                    val missingIds = uniqueIds.filter { !nameCache.containsKey(it) }

                    // 2. Cargar nombres faltantes en paralelo
                    if (missingIds.isNotEmpty()) {
                        coroutineScope {
                            missingIds.map { id ->
                                async { id to (getUser(id)?.name ?: "") }
                            }.awaitAll().forEach { (id, name) ->
                                nameCache[id] = name
                            }
                        }
                    }

                    // 3. Mapear la lista final usando el caché
                    val referralsWithNames = referrals.map { referral ->
                        ReferralWithNames(
                            referral = referral,
                            otherPartyName = nameCache[referral.clientId] ?: ""
                        )
                    }
                    _referralsProvider.value = referralsWithNames
                    _isLoading.value = false
                }
        }
    }

    private fun loadReferralsByClient(){
        _isLoading.value = true
        referralsJob?.cancel()
        referralsJob = launchCatching {
            getReferralsByClient(currentUserId)
                .collect { referrals ->
                    // 1. Identificar IDs de proveedores únicos que no están en caché
                    val uniqueIds = referrals.map { it.providerId }.distinct()
                    val missingIds = uniqueIds.filter { !nameCache.containsKey(it) }

                    // 2. Cargar nombres faltantes en paralelo
                    if (missingIds.isNotEmpty()) {
                        coroutineScope {
                            missingIds.map { id ->
                                async { id to (getUser(id)?.name ?: "") }
                            }.awaitAll().forEach { (id, name) ->
                                nameCache[id] = name
                            }
                        }
                    }

                    // 3. Mapear la lista final usando el caché
                    val referralsWithNames = referrals.map { referral ->
                        ReferralWithNames(
                            referral = referral,
                            otherPartyName = nameCache[referral.providerId] ?: ""
                        )
                    }
                    _referralsClient.value = referralsWithNames
                    _isLoading.value = false
                }
        }
    }
}
