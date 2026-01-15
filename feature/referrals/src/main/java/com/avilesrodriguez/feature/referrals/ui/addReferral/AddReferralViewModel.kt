package com.avilesrodriguez.feature.referrals.ui.addReferral

import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.domain.usecases.CurrentUserId
import com.avilesrodriguez.domain.usecases.GetUser
import com.avilesrodriguez.domain.usecases.HasUser
import com.avilesrodriguez.domain.usecases.SaveReferral
import com.avilesrodriguez.feature.referrals.ui.addReferral.model.AddReferralUiState
import com.avilesrodriguez.feature.referrals.ui.addReferral.model.toReferral
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.ext.MAX_LENGTH_NAME
import com.avilesrodriguez.presentation.ext.MIN_PASS_LENGTH_PHONE_ECUADOR
import com.avilesrodriguez.presentation.ext.isValidEmail
import com.avilesrodriguez.presentation.ext.isValidNumber
import com.avilesrodriguez.presentation.snackbar.SnackbarManager
import com.avilesrodriguez.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AddReferralViewModel @Inject constructor(
    private val currentUserIdUseCase: CurrentUserId,
    private val hasUser: HasUser,
    private val getUser: GetUser,
    private val saveReferral: SaveReferral
) : BaseViewModel() {
    private val _uiState = MutableStateFlow(AddReferralUiState())
    val uiState: StateFlow<AddReferralUiState> = _uiState.asStateFlow()

    private val _userDataStore = MutableStateFlow<UserData?>(null)
    val userDataStore: StateFlow<UserData?> = _userDataStore

    val currentUserId
        get() = currentUserIdUseCase()

    init {
        launchCatching {
            if(hasUser()){
                _userDataStore.value = getUser(currentUserId)
            }

        }
    }

    private val phoneNumber
        get() = _uiState.value.numberPhone

    private val email
        get() = _uiState.value.email

    private fun validateInput(uiState: AddReferralUiState): Boolean{
        return with(uiState){
            name.isNotBlank() && email.isNotBlank() && numberPhone.isNotBlank()
        }
    }

    fun onNameChange(newName: String){
        // Solo deja pasar letras y espacios, eliminando lo demás al instante
        val allowedSymbols = setOf('.', '-', ',', '/')

        val filteredName = newName
            .filter { it.isLetter() || it.isDigit() || it.isWhitespace() || allowedSymbols.contains(it) }
            .take(MAX_LENGTH_NAME)

        _uiState.value = _uiState.value.copy(name = filteredName)
        val currentState = _uiState.value
        _uiState.value = _uiState.value.copy(isEntryValid = validateInput(currentState))
    }

    fun onEmailChange(newEmail: String){
        _uiState.value = _uiState.value.copy(email = newEmail)
        val currentState = _uiState.value
        _uiState.value = _uiState.value.copy(isEntryValid = validateInput(currentState))
    }

    fun onNumberPhoneChange(newNumberPhone: String){
        val filtered = newNumberPhone.filter { it.isDigit() }.take(MIN_PASS_LENGTH_PHONE_ECUADOR)
        _uiState.value = _uiState.value.copy(numberPhone = filtered)
        val currentState = _uiState
        _uiState.value = _uiState.value.copy(isEntryValid = validateInput(currentState.value))
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
            _uiState.value = _uiState.value.copy(isSaving = true)
            // save referral
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val formattedTimestampForUI = sdf.format(java.util.Date(System.currentTimeMillis()))
            val currentState = _uiState.value
            val referral = currentState.toReferral(
                clientId = currentUserId,
                providerId = providerId,
                createdAt = System.currentTimeMillis(),
                null
            )
            saveReferral(referral)

            // navigate to referrals screen
            popUp()
        }.invokeOnCompletion { _uiState.value = _uiState.value.copy(isSaving = false) }
    }
}