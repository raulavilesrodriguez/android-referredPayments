package com.avilesrodriguez.feature.referrals.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.avilesrodriguez.domain.model.referral.Referral
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.domain.usecases.CurrentUserId
import com.avilesrodriguez.domain.usecases.GetReferralById
import com.avilesrodriguez.domain.usecases.GetReferralsByClient
import com.avilesrodriguez.domain.usecases.GetReferralsByProvider
import com.avilesrodriguez.domain.usecases.GetUser
import com.avilesrodriguez.domain.usecases.HasUser
import com.avilesrodriguez.domain.usecases.SaveReferral
import com.avilesrodriguez.domain.usecases.SearchReferralsByClient
import com.avilesrodriguez.domain.usecases.SearchReferralsByProvider
import com.avilesrodriguez.feature.referrals.ui.model.AddReferralUiState
import com.avilesrodriguez.feature.referrals.ui.model.toAddReferralUiState
import com.avilesrodriguez.feature.referrals.ui.model.toReferral
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.ext.MAX_LENGTH_NAME
import com.avilesrodriguez.presentation.ext.MIN_PASS_LENGTH_PHONE_ECUADOR
import com.avilesrodriguez.presentation.ext.isValidEmail
import com.avilesrodriguez.presentation.ext.isValidNumber
import com.avilesrodriguez.presentation.snackbar.SnackbarManager
import com.avilesrodriguez.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import java.text.SimpleDateFormat
import java.util.Locale
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
    private val searchReferralsByProvider: SearchReferralsByProvider,
    private val getReferralById: GetReferralById,
    private val saveReferral: SaveReferral
) : BaseViewModel(){

    private val _uiState = MutableStateFlow<List<Referral>>(emptyList())
    val uiState: StateFlow<List<Referral>> = _uiState.asStateFlow()
    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _addReferralState = MutableStateFlow(AddReferralUiState())
    val addReferralState: StateFlow<AddReferralUiState> = _addReferralState.asStateFlow()

    private val _referralState = MutableStateFlow(Referral())
    val referralState: StateFlow<Referral> = _referralState.asStateFlow()

    private val _userDataStore = MutableStateFlow<UserData?>(null)
    val userDataStore: StateFlow<UserData?> = _userDataStore

    var clientWhoReferred by mutableStateOf<UserData?>(null)
    var providerThatReceived by mutableStateOf<UserData?>(null)

    private var searchJob: Job? = null

    val currentUserId
        get() = currentUserIdUseCase()

    private val phoneNumber
        get() = _addReferralState.value.numberPhone

    private val email
        get() = _addReferralState.value.email

    init {
        launchCatching {
            if(hasUser()){
                _userDataStore.value = getUser(currentUserId)
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
            val userData = _userDataStore.value
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
        searchJob?.invokeOnCompletion { if (it is CancellationException) _isLoading.value = false }
    }

    fun onReferralClick(referral: Referral, openScreen: (String) -> Unit) {
        _referralState.value = referral
        val idClientWhoReferred = referral.clientId
        val idProviderReceiveReferred = referral.providerId
        launchCatching {
            clientWhoReferred = getUser(idClientWhoReferred)
            providerThatReceived = getUser(idProviderReceiveReferred)
        }
    }

    private fun validateInput(uiState: AddReferralUiState): Boolean{
        return with(uiState){
            name.isNotBlank() && email.isNotBlank() && numberPhone.isNotBlank()
        }
    }

    fun onClickReferral(referralId: String){
        launchCatching {
            val referral = getReferralById(referralId)
            _addReferralState.value = referral?.toAddReferralUiState()?: AddReferralUiState()
        }
    }

    fun onNameChange(newName: String){
        // Solo deja pasar letras y espacios, eliminando lo demás al instante
        val allowedSymbols = setOf('.', '-', ',', '/')

        val filteredName = newName
            .filter { it.isLetter() || it.isDigit() || it.isWhitespace() || allowedSymbols.contains(it) }
            .take(MAX_LENGTH_NAME)

        _addReferralState.value = _addReferralState.value.copy(name = filteredName)
        val currentState = _addReferralState.value
        _addReferralState.value = _addReferralState.value.copy(isEntryValid = validateInput(currentState))
    }

    fun onEmailChange(newEmail: String){
        _addReferralState.value = _addReferralState.value.copy(email = newEmail)
        val currentState = _addReferralState.value
        _addReferralState.value = _addReferralState.value.copy(isEntryValid = validateInput(currentState))
    }

    fun onNumberPhoneChange(newNumberPhone: String){
        val filtered = newNumberPhone.filter { it.isDigit() }.take(MIN_PASS_LENGTH_PHONE_ECUADOR)
        _addReferralState.value = _addReferralState.value.copy(numberPhone = filtered)
        val currentState = _addReferralState
        _addReferralState.value = _addReferralState.value.copy(isEntryValid = validateInput(currentState.value))
    }

    fun onSaveClick(providerId: String, popUp: () -> Unit){
        if(!phoneNumber.isValidNumber()){
            SnackbarManager.showMessage(R.string.invalid_phone_number)
            return
        }
        if(!email.isValidEmail()){
            SnackbarManager.showMessage(R.string.email_error)
            return
        }
        launchCatching {
            _addReferralState.value = _addReferralState.value.copy(isSaving = true)
            // save referral
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val formattedTimestampForUI = sdf.format(java.util.Date(System.currentTimeMillis()))
            val currentState = _addReferralState.value
            val referral = currentState.toReferral(
                clientId = currentUserId,
                providerId = providerId,
                createdAt = System.currentTimeMillis(),
                null
            )
            saveReferral(referral)

            // navigate to referrals screen
            popUp()
        }.invokeOnCompletion { _addReferralState.value = _addReferralState.value.copy(isSaving = false) }
    }

}