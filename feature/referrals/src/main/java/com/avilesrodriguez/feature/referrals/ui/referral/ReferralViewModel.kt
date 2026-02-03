package com.avilesrodriguez.feature.referrals.ui.referral

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.avilesrodriguez.domain.ext.normalizeName
import com.avilesrodriguez.domain.model.referral.Referral
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.domain.usecases.CurrentUserId
import com.avilesrodriguez.domain.usecases.GetReferralById
import com.avilesrodriguez.domain.usecases.GetUser
import com.avilesrodriguez.domain.usecases.HasUser
import com.avilesrodriguez.domain.usecases.UpdateReferralFields
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.ext.MAX_LENGTH_NAME
import com.avilesrodriguez.presentation.ext.MIN_PASS_LENGTH_PHONE_ECUADOR
import com.avilesrodriguez.presentation.ext.isValidEmail
import com.avilesrodriguez.presentation.ext.isValidNumber
import com.avilesrodriguez.presentation.navigation.NavRoutes
import com.avilesrodriguez.presentation.snackbar.SnackbarManager
import com.avilesrodriguez.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class ReferralViewModel @Inject constructor(
    private val currentUserIdUseCase: CurrentUserId,
    private val hasUser: HasUser,
    private val getUser: GetUser,
    private val getReferralById: GetReferralById,
    private val updateReferralFields: UpdateReferralFields
) : BaseViewModel() {
    private val _referralState = MutableStateFlow(Referral())
    val referralState: StateFlow<Referral> = _referralState.asStateFlow()
    private val _userDataStore = MutableStateFlow<UserData?>(null)
    val userDataStore: StateFlow<UserData?> = _userDataStore

    var clientWhoReferred by mutableStateOf<UserData?>(null)
    var providerThatReceived by mutableStateOf<UserData?>(null)
    private var loadJob: Job? = null

    val currentUserId
        get() = currentUserIdUseCase()

    private val nameReferral
        get() = _referralState.value.name
    private val emailReferral
        get() = _referralState.value.email
    private val numberPhoneReferral
        get() = _referralState.value.numberPhone

    init {
        launchCatching {
            if(hasUser()){
                _userDataStore.value = getUser(currentUserId)
            }
        }
    }

    fun loadReferralInformation(referralId: String){
        loadJob?.cancel()
        loadJob = launchCatching {
            val referral = getReferralById(referralId)
            if(referral != null){
                _referralState.value = referral
                val clientDeferred = async { getUser(referral.clientId) }
                val providerDeferred = async { getUser(referral.providerId) }
                clientWhoReferred = clientDeferred.await()
                providerThatReceived = providerDeferred.await()
            } else {
                _referralState.value = Referral()
            }
        }
    }

    fun onNameReferral(openScreen: (String) -> Unit){
        openScreen(NavRoutes.EDIT_NAME_REFERRAL)
    }

    fun onEmailReferral(openScreen: (String) -> Unit){
        openScreen(NavRoutes.EDIT_EMAIL_REFERRAL)
    }

    fun onPhoneReferral(openScreen: (String) -> Unit){
        openScreen(NavRoutes.EDIT_PHONE_REFERRAL)
    }

    fun onPayReferral(openScreen: (String) -> Unit){
        openScreen(NavRoutes.PAY_REFERRAL)
    }

    fun updateName(newName: String){
        // Solo deja pasar letras y espacios, eliminando lo demás al instante
        val allowedSymbols = setOf('.', '-', ',', '/')

        val filteredName = newName
            .filter { it.isLetter() || it.isDigit() || it.isWhitespace() || allowedSymbols.contains(it) }
            .take(MAX_LENGTH_NAME)

        _referralState.value = _referralState.value.copy(name = filteredName)
    }

    fun onSaveName(popUp: () -> Unit) {
        if(nameReferral.isBlank()){
            return
        }
        launchCatching {
            val updates = mapOf(
                "name" to nameReferral,
                "nameLowercase" to nameReferral.normalizeName()
            )
            val referralId = _referralState.value.id
            updateReferralFields(referralId, updates)
            popUp()
        }
    }

    fun updateEmail(newEmail: String){
        _referralState.value = _referralState.value.copy(email = newEmail)
    }

    fun onSaveEmail(popUp: () -> Unit) {
        if(!emailReferral.isValidEmail()){
            SnackbarManager.showMessage(R.string.email_error)
            return
        }
        launchCatching {
            val updates = mapOf(
                "email" to emailReferral
            )
            val referralId = _referralState.value.id
            updateReferralFields(referralId, updates)
            popUp()
        }
    }

    fun updateNumberPhone(newNumberPhone: String){
        val filtered = newNumberPhone.filter { it.isDigit() }.take(MIN_PASS_LENGTH_PHONE_ECUADOR)
        _referralState.value = _referralState.value.copy(numberPhone = filtered)
    }

    fun onSaveNumberPhone(popUp: () -> Unit) {
        if(!numberPhoneReferral.isValidNumber()){
            SnackbarManager.showMessage(R.string.invalid_phone_number)
            return
        }
        launchCatching {
            val updates = mapOf(
                "numberPhone" to numberPhoneReferral
            )
            val referralId = _referralState.value.id
            updateReferralFields(referralId, updates)
            popUp()
        }
    }

}