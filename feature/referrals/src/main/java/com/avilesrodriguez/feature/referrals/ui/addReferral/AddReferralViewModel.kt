package com.avilesrodriguez.feature.referrals.ui.addReferral

import android.util.Log
import com.avilesrodriguez.domain.usecases.CurrentUserId
import com.avilesrodriguez.domain.usecases.SaveReferral
import com.avilesrodriguez.domain.usecases.UpdateProviderProcessingReferralsCount
import com.avilesrodriguez.feature.referrals.ui.model.AddReferralUiState
import com.avilesrodriguez.feature.referrals.ui.model.toReferral
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.ext.MAX_LENGTH_NAME
import com.avilesrodriguez.presentation.ext.MIN_PASS_LENGTH_PHONE_ECUADOR
import com.avilesrodriguez.presentation.ext.isValidEmail
import com.avilesrodriguez.presentation.ext.isValidNumber
import com.avilesrodriguez.presentation.navigation.NavRoutes
import com.avilesrodriguez.presentation.snackbar.SnackbarManager
import com.avilesrodriguez.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AddReferralViewModel @Inject constructor(
    private val currentUserIdUseCase: CurrentUserId,
    private val saveReferral: SaveReferral,
    private val updateProviderProcessingReferralsCount: UpdateProviderProcessingReferralsCount
) : BaseViewModel() {
    private val _addReferralState = MutableStateFlow(AddReferralUiState())
    val addReferralState: StateFlow<AddReferralUiState> = _addReferralState.asStateFlow()

    val currentUserId
        get() = currentUserIdUseCase()
    private val phoneNumber
        get() = _addReferralState.value.numberPhone

    private val email
        get() = _addReferralState.value.email

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
        val currentState = _addReferralState.value
        _addReferralState.value = _addReferralState.value.copy(isEntryValid = validateInput(currentState))
    }

    fun onSaveClick(providerId: String?, openAndPopUp: (String, String) -> Unit){
        if(providerId == null) return
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
            //val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            //val formattedTimestampForUI = sdf.format(java.util.Date(System.currentTimeMillis()))
            val currentState = _addReferralState.value
            val referral = currentState.toReferral(
                clientId = currentUserId,
                providerId = providerId,
                createdAt = System.currentTimeMillis()
            )
            try {
                val isSuccess = saveReferral(referral)
                if(isSuccess){
                    updateProviderProcessingReferralsCount(providerId, 1)
                    openAndPopUp(NavRoutes.REFERRALS, NavRoutes.NEW_REFERRAL)
                }else{
                    SnackbarManager.showMessage(R.string.error_referral_exists)
                }
            } catch (e: Exception){
                Log.e("AddReferralViewModel", "Error saving referral", e)
                SnackbarManager.showMessage(R.string.generic_error)
            }
        }.invokeOnCompletion { _addReferralState.value = _addReferralState.value.copy(isSaving = false) }
    }
}