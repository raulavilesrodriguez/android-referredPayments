package com.avilesrodriguez.feature.referrals.ui.referral

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.avilesrodriguez.domain.ext.normalizeName
import com.avilesrodriguez.domain.model.message.Message
import com.avilesrodriguez.domain.model.referral.Referral
import com.avilesrodriguez.domain.model.referral.ReferralStatus
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.domain.model.validationRules.ValidationRules
import com.avilesrodriguez.domain.usecases.account.CurrentUserId
import com.avilesrodriguez.domain.usecases.message.GetMessagesByReferral
import com.avilesrodriguez.domain.usecases.referral.GetReferralByIdFlow
import com.avilesrodriguez.domain.usecases.user.GetUser
import com.avilesrodriguez.domain.usecases.account.HasUser
import com.avilesrodriguez.domain.usecases.message.SaveMessage
import com.avilesrodriguez.domain.usecases.referral.SaveRatingWithTransaction
import com.avilesrodriguez.domain.usecases.referral.UpdateReferralFields
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.ext.isValidEmail
import com.avilesrodriguez.presentation.ext.isValidNumber
import com.avilesrodriguez.presentation.navigation.NavRoutes
import com.avilesrodriguez.presentation.snackbar.SnackbarManager
import com.avilesrodriguez.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class ReferralViewModel @Inject constructor(
    private val currentUserIdUseCase: CurrentUserId,
    private val hasUser: HasUser,
    private val getUser: GetUser,
    private val updateReferralFields: UpdateReferralFields,
    private val saveMessage: SaveMessage,
    private val getMessagesByReferral: GetMessagesByReferral,
    private val saveRatingWithTransaction: SaveRatingWithTransaction,
    private val getReferralByIdFlow: GetReferralByIdFlow
) : BaseViewModel() {
    private val _referralState = MutableStateFlow<Referral?>(null)
    val referralState: StateFlow<Referral?> = _referralState.asStateFlow()
    private val _referralRating = MutableStateFlow(0.0)
    val referralRating: StateFlow<Double> = _referralRating.asStateFlow()
    private val _userDataStore = MutableStateFlow<UserData?>(null)
    val userDataStore: StateFlow<UserData?> = _userDataStore

    var clientWhoReferred by mutableStateOf<UserData?>(null)
    var providerThatReceived by mutableStateOf<UserData?>(null)
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _isLoadingRating = MutableStateFlow(false)
    val isLoadingRating: StateFlow<Boolean> = _isLoadingRating.asStateFlow()
    private var loadJob: Job? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    val unReadMessages: StateFlow<Int> = _referralState
        .filterNotNull()
        .flatMapLatest { referral ->
            getMessagesByReferral(referral.id).map { messages ->
                messages.count { it.receiverId == currentUserId && !it.isRead }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val currentUserId
        get() = currentUserIdUseCase()

    private val nameReferral
        get() = _referralState.value?.name?:""

    init {
        launchCatching {
            if(hasUser()){
                _userDataStore.value = getUser(currentUserId)
            }
        }
    }

    fun loadReferralInformation(referralId: String){
        if (_referralState.value?.id == referralId) return
        loadJob?.cancel()
        _isLoading.value = true
        loadJob = launchCatching {
            try {
                getReferralByIdFlow(referralId).collect { referral ->
                    if (referral != null) {
                        _referralState.value = referral
                        _referralRating.value = referral.rating
                        if(clientWhoReferred == null || providerThatReceived == null){
                            updateRelatedUsers(referral)
                        }
                    }
                    _isLoading.value = false
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun updateRelatedUsers(referral: Referral) {
        coroutineScope {
            val clientDef = async { getUser(referral.clientId) }
            val providerDef = async { getUser(referral.providerId) }
            clientWhoReferred = clientDef.await()
            providerThatReceived = providerDef.await()
        }
    }

    fun onNameReferral(openScreen: (String) -> Unit){
        val id = _referralState.value?.id ?: return
        val route = NavRoutes.EDIT_NAME_REFERRAL.replace("{${NavRoutes.ReferralArgs.ID}}", id)
        openScreen(route)
    }

    fun onEmailReferral(openScreen: (String) -> Unit){
        val id = _referralState.value?.id ?: return
        val route = NavRoutes.EDIT_EMAIL_REFERRAL.replace("{${NavRoutes.ReferralArgs.ID}}", id)
        openScreen(route)
    }

    fun onPhoneReferral(openScreen: (String) -> Unit){
        val id = _referralState.value?.id ?: return
        val route = NavRoutes.EDIT_PHONE_REFERRAL.replace("{${NavRoutes.ReferralArgs.ID}}", id)
        openScreen(route)
    }

    fun onAcceptReferral(subject:String, content:String, openScreen: (String) -> Unit){
        val referralId = _referralState.value?.id?:return
        val clientId = _referralState.value?.clientId?:return
        launchCatching {
            _isLoading.value = true
            val updates = mapOf(
                "status" to ReferralStatus.PROCESSING.name
            )

            updateReferralFields(referralId, updates)

            val systemMessage = Message(
                referralId = referralId,
                senderId = currentUserId,
                receiverId = clientId,
                subject = "$subject $nameReferral",
                content = content,
                createdAt = System.currentTimeMillis()
            )
            saveMessage(systemMessage)
            _isLoading.value = false
        }
    }

    fun onProcessReferral(openScreen: (String) -> Unit){
        val referralId = _referralState.value?.id?:""
        val route = NavRoutes.MESSAGES_SCREEN.replace("{${NavRoutes.ReferralArgs.ID}}", referralId)
        openScreen(route)
    }

    fun updateName(newName: String){
        val allowedSymbols = setOf('.', '-', ',', '/')

        val filteredName = newName
            .filter { it.isLetter() || it.isDigit() || it.isWhitespace() || allowedSymbols.contains(it) }
            .take(ValidationRules.MAX_LENGTH_NAME)

        _referralState.value = _referralState.value?.copy(name = filteredName)
    }

    fun onSaveName(popUp: () -> Unit) {
        val currentName = _referralState.value?.name ?: ""
        if(currentName.isBlank()){
            return
        }
        launchCatching {
            val updates = mapOf(
                "name" to currentName,
                "nameLowercase" to currentName.normalizeName()
            )
            val referralId = _referralState.value?.id?:""
            updateReferralFields(referralId, updates)
            popUp()
        }
    }

    fun updateEmail(newEmail: String){
        _referralState.value = _referralState.value?.copy(email = newEmail)
    }

    fun onSaveEmail(popUp: () -> Unit) {
        val currentEmail = _referralState.value?.email ?: ""
        if(!currentEmail.isValidEmail()){
            SnackbarManager.showMessage(R.string.email_error)
            return
        }
        launchCatching {
            val updates = mapOf(
                "email" to currentEmail
            )
            val referralId = _referralState.value?.id?:""
            updateReferralFields(referralId, updates)
            popUp()
        }
    }

    fun updateNumberPhone(newNumberPhone: String){
        val filtered = newNumberPhone.filter { it.isDigit() }.take(ValidationRules.MIN_PASS_LENGTH_PHONE_ECUADOR)
        _referralState.value = _referralState.value?.copy(numberPhone = filtered)
    }

    fun onSaveNumberPhone(popUp: () -> Unit) {
        val currentPhone = _referralState.value?.numberPhone ?: ""
        if(!currentPhone.isValidNumber()){
            SnackbarManager.showMessage(R.string.invalid_phone_number)
            return
        }
        launchCatching {
            val updates = mapOf(
                "numberPhone" to currentPhone
            )
            val referralId = _referralState.value?.id?:""
            updateReferralFields(referralId, updates)
            popUp()
        }
    }

    fun onRatingChanged(rating: Double){
        val referralRating = _referralRating.value
        if(referralRating>0.0) return
        _referralState.value = _referralState.value?.copy(rating = rating)
    }

    fun onFeedbackReasonChanged(reason:String){
        _referralState.value = _referralState.value?.copy(feedbackReason = reason)
    }

    fun saveRatings(){
        if (_isLoadingRating.value) return
        val currentRatingInDb = _referralRating.value
        val currentReferral = _referralState.value?:return
        val currentProvider = providerThatReceived as? UserData.Provider ?: return
        if(currentRatingInDb>0.0) return
        launchCatching {
            try {
                _isLoadingRating.value = true
                val referralUpdates = mapOf(
                    "rating" to currentReferral.rating,
                    "feedbackReason" to (currentReferral.feedbackReason ?: "")
                )

                saveRatingWithTransaction(
                    referralId = currentReferral.id,
                    referralUpdates = referralUpdates,
                    providerId = currentProvider.uid,
                    ratingReferral = currentReferral.rating
                )
            } finally {
                _isLoadingRating.value = false
            }
        }
    }
}
