package com.avilesrodriguez.feature.settings.ui

import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.domain.usecases.CurrentUserId
import com.avilesrodriguez.domain.usecases.DownloadUrlPhoto
import com.avilesrodriguez.domain.usecases.GetUser
import com.avilesrodriguez.domain.usecases.HasUser
import com.avilesrodriguez.domain.usecases.SaveUser
import com.avilesrodriguez.domain.usecases.SecureDeleteAccount
import com.avilesrodriguez.domain.usecases.UploadPhoto
import com.avilesrodriguez.presentation.avatar.DEFAULT_AVATAR_USER
import com.avilesrodriguez.presentation.ext.MAX_LENGTH_COUNT_NUMBER_BANK
import com.avilesrodriguez.presentation.ext.MAX_LENGTH_IDENTITY_CARD
import com.avilesrodriguez.presentation.ext.MAX_LENGTH_INDUSTRY
import com.avilesrodriguez.presentation.ext.MAX_LENGTH_NAME
import com.avilesrodriguez.presentation.ext.MAX_LENGTH_RUC
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

    val currentUserId
        get() = currentUserIdUseCase()

    init {
        launchCatching {
            if (hasUser()){
                _uiState.value = getUser(currentUserId)
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
                is UserData.Client -> currentState.copy(name = filteredName)
                is UserData.Provider -> currentState.copy(name = filteredName)
            }
        }
    }

    fun updatePhoto(newPhotoUri: String){
        val currentState = _uiState.value
        if(currentState != null){
            _uiState.value = when(currentState){
                is UserData.Client -> currentState.copy(photoUrl = newPhotoUri)
                is UserData.Provider -> currentState.copy(photoUrl = newPhotoUri)
            }
        }
    }

    fun updateIndustry(industry: String){
        val allowedSymbols = setOf('.', '-', ',', '/')
        val filteredNameIndustry = industry
            .filter { it.isLetter() || it.isDigit() || it.isWhitespace() || allowedSymbols.contains(it) }
            .take(MAX_LENGTH_INDUSTRY)
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
        launchCatching {
            _isSaving.value = true
            val remotePath = "profile_images/${currentUserId}.jpg"

            val newPhotoUri = _uiState.value?.photoUrl ?: DEFAULT_AVATAR_USER

            // Upload photo to Firebase Storage
            uploadPhoto(newPhotoUri, remotePath)

            // Download URL of the uploaded photo
            val finalPhotoUrl = downloadPhoto(remotePath)
            val currentState = _uiState.value
            if (currentState != null) {
                _uiState.value = when (currentState) {
                    is UserData.Client -> currentState.copy(photoUrl = finalPhotoUrl)
                    is UserData.Provider -> currentState.copy(photoUrl = finalPhotoUrl)
                }
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