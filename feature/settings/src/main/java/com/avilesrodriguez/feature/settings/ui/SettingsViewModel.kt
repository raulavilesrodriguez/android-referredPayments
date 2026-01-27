package com.avilesrodriguez.feature.settings.ui

import com.avilesrodriguez.domain.ext.normalizeName
import com.avilesrodriguez.domain.model.industries.IndustriesType
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.domain.usecases.CurrentUserId
import com.avilesrodriguez.domain.usecases.DownloadUrlPhoto
import com.avilesrodriguez.domain.usecases.GetUser
import com.avilesrodriguez.domain.usecases.HasUser
import com.avilesrodriguez.domain.usecases.SaveUser
import com.avilesrodriguez.domain.usecases.SecureDeleteAccount
import com.avilesrodriguez.domain.usecases.UploadPhoto
import com.avilesrodriguez.presentation.ext.MAX_LENGTH_COUNT_NUMBER_BANK
import com.avilesrodriguez.presentation.ext.MAX_LENGTH_IDENTITY_CARD
import com.avilesrodriguez.presentation.ext.MAX_LENGTH_NAME
import com.avilesrodriguez.presentation.ext.MAX_LENGTH_RUC
import com.avilesrodriguez.presentation.industries.getById
import com.avilesrodriguez.presentation.navigation.NavRoutes
import com.avilesrodriguez.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val currentUserIdUseCase: CurrentUserId,
    private val hasUser: HasUser,
    private val getUser: GetUser,
    private val uploadPhoto: UploadPhoto,
    private val downloadPhoto: DownloadUrlPhoto,
    private val saveUser: SaveUser,
    private val secureDeleteAccount: SecureDeleteAccount
) : BaseViewModel() {
    private val _uiState = MutableStateFlow<UserData?>(null)
    val uiState: StateFlow<UserData?> = _uiState
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()
    private val _isEntryValid = MutableStateFlow(true)
    val isEntryValid: StateFlow<Boolean> = _isEntryValid.asStateFlow()

    private var localPhotoUri: String? = null

    val currentUserId
        get() = currentUserIdUseCase()

    init {
        launchCatching {
            if (hasUser()){
                _uiState.value = getUser(currentUserId)
                _isEntryValid.value = validateInput(_uiState.value)
            }
        }
    }

    private fun validateInput(uiState: UserData?): Boolean {
        return with(uiState) {
            when (this) {
                is UserData.Client -> {
                    name?.isNotBlank() == true
                }
                is UserData.Provider -> {
                    name?.isNotBlank() == true
                }
                else -> false
            }
        }
    }

    fun reloadUserData() {
        launchCatching {
            if (hasUser()) {
                _uiState.value = getUser(currentUserId)
            }
        }
    }

    fun secureDeleteAccount(restartApp: (String) -> Unit){
        launchCatching {
            secureDeleteAccount(currentUserId)
            restartApp(NavRoutes.Splash)
        }
    }

    fun updateName(newName: String){
        // Solo deja pasar letras y espacios, eliminando lo demás al instante
        val allowedSymbols = setOf('.', '-', ',', '/')

        val filteredName = newName
            .filter { it.isLetter() || it.isDigit() || it.isWhitespace() || allowedSymbols.contains(it) }
            .take(MAX_LENGTH_NAME)
        val currentState = _uiState.value
        if(currentState != null){
            _uiState.value = when(currentState){
                is UserData.Client -> currentState.copy(name = filteredName, nameLowercase = filteredName.normalizeName())
                is UserData.Provider -> currentState.copy(name = filteredName, nameLowercase = filteredName.normalizeName())
            }
        }
        _isEntryValid.value = validateInput(_uiState.value)
    }

    fun updatePhoto(newPhotoUri: String){
        localPhotoUri = newPhotoUri
        val currentState = _uiState.value
        if(currentState != null){
            _uiState.value = when(currentState){
                is UserData.Client -> currentState.copy(photoUrl = newPhotoUri)
                is UserData.Provider -> currentState.copy(photoUrl = newPhotoUri)
            }
        }
    }

    fun updateIndustry(industry: Int){
        val filteredNameIndustry = IndustriesType.getById(industry)
        val currentState = _uiState.value
        if(currentState is UserData.Provider){
            _uiState.value = currentState.copy(industry = filteredNameIndustry)
        }
    }

    fun updateIdentityCard(identityCard: String){
        val currentState = _uiState.value
        if(currentState != null){
            _uiState.value = when(currentState){
                is UserData.Client -> {
                    val filteredIdentityCard = identityCard.filter { it.isDigit() }.take(MAX_LENGTH_IDENTITY_CARD)
                    currentState.copy(identityCard = filteredIdentityCard)
                }
                is UserData.Provider -> {
                    val filteredIdentityCardRuc = identityCard.filter { it.isDigit() }.take(MAX_LENGTH_RUC)
                    currentState.copy(ciOrRuc = filteredIdentityCardRuc)
                }
            }
        }
    }

    fun updateCountNumberBank(countNumberBank: String){
        val currentState = _uiState.value
        val filteredCountNumberBank = countNumberBank.filter { it.isDigit() }.take(MAX_LENGTH_COUNT_NUMBER_BANK)
        if(currentState != null){
            _uiState.value = when(currentState){
                is UserData.Client -> currentState.copy(countNumberPay = filteredCountNumberBank)
                is UserData.Provider -> currentState.copy(countNumber = filteredCountNumberBank)
            }
        }
    }

    fun onSaveClick(popUp: () -> Unit) {
        val currentState = _uiState.value ?: return
        if(!validateInput(currentState)){
            _isEntryValid.value = false
            return
        }
        launchCatching {
            _isSaving.value = true
            var finalPhotoUrl = currentState.photoUrl
            if(localPhotoUri != null) {
                val remotePath = "profile_images/${currentUserId}.jpg"
                val newPhotoUri = _uiState.value?.photoUrl ?: ""

                // Upload photo to Firebase Storage
                uploadPhoto(newPhotoUri, remotePath)

                // Download URL of the uploaded photo
                finalPhotoUrl = downloadPhoto(remotePath)
                localPhotoUri = null
            }
            _uiState.value = when (currentState) {
                is UserData.Client -> currentState.copy(photoUrl = finalPhotoUrl)
                is UserData.Provider -> currentState.copy(photoUrl = finalPhotoUrl)
            }
            _uiState.value?.let {
                saveUser(it)
            }
            // navigate to settings screen
            popUp()
        }.invokeOnCompletion { _isSaving.value = false }
    }

    fun cancelEditUser(popUp: () -> Unit){
        popUp()
    }

}